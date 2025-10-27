package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.*;
import com.kelompoksatu.griya.repository.*;
import com.kelompoksatu.griya.util.IDCloudHostS3Util;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class KprApplicationService {

  private final KprApplicationRepository kprApplicationRepository;
  private final KprRateRepository kprRateRepository;
  private final PropertyRepository propertyRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final DeveloperRepository developerRepository;
  private final ApprovalLevelRepository approvalLevelRepository;
  private final ApprovalWorkflowRepository approvalWorkflowRepository;
  private final ApplicationDocumentRepository applicationDocumentRepository;
  private final FileStorageService fileStorageService;
  private final IDCloudHostS3Util idCloudHostS3Util;

  Logger logger = LoggerFactory.getLogger(KprApplicationService.class);

  /** Submit a new KPR application with documents (form-data) */
  @Transactional
  public KprApplicationResponse submitApplicationWithDocuments(
      Integer userId, KprApplicationFormRequest formRequest) {
    logger.info(
        "Processing KPR application with documents for user: {} and property: {}",
        userId,
        formRequest.getPropertyId());

    try {
      // 1. Authentication & Authorization
      User user = validateUser(userId);

      // 2. Data Validation (Properties)
      Property property = validatePropertyForForm(formRequest);

      // 3. Check for existing pending applications
      if (kprApplicationRepository.existsPendingApplicationByUserAndProperty(
          userId, formRequest.getPropertyId())) {
        throw new IllegalStateException(
            "Anda sudah memiliki aplikasi KPR yang sedang diproses untuk properti ini");
      }

      // 4. Get Developer
      Developer developer =
          developerRepository
              .findById(property.getDeveloperId())
              .orElseThrow(() -> new IllegalArgumentException("Developer not found"));

      // 5. Update user profile with personal data
      updateUserProfileFromForm(
          userId, formRequest.getPersonalData(), formRequest.getEmploymentData());

      // 6. Get KPR rate
      KprRate selectedRate =
          validateAndGetKprRate(formRequest.getKprRateId(), formRequest.getSimulationData());

      // 7. Calculate loan details
      BigDecimal monthlyInstallment =
          calculateMonthlyInstallment(
              formRequest.getSimulationData().getLoanAmount(),
              selectedRate.getEffectiveRate(),
              formRequest.getSimulationData().getLoanTermYears());

      // 8. Determine approval level
      Integer currentApprovalLevel =
          determineInitialApprovalLevel(formRequest.getSimulationData().getLoanAmount());

      // 9. Generate application number
      String applicationNumber = generateApplicationNumber();

      // 10. Create KPR application
      KprApplication application =
          createKprApplicationFromForm(
              userId,
              formRequest,
              property,
              selectedRate,
              monthlyInstallment,
              applicationNumber,
              currentApprovalLevel);

      // 11. Save application
      KprApplication savedApplication = kprApplicationRepository.save(application);

      // 12. Store documents
      List<ApplicationDocument> documents =
          storeApplicationDocuments(savedApplication.getId(), formRequest);
      logger.info(
          "Stored {} documents for application: {}", documents.size(), savedApplication.getId());

      // 13. Create initial approval workflow
      createDeveloperApprovalWorkflow(savedApplication.getId(), developer.getUser().getId());

      // 14. Build response
      return KprApplicationResponse.builder()
          .applicationId(savedApplication.getId())
          .applicationNumber(applicationNumber)
          .status(savedApplication.getStatus())
          .monthlyInstallment(monthlyInstallment)
          .interestRate(selectedRate.getEffectiveRate())
          .message(
              "Aplikasi KPR berhasil disubmit dengan dokumen. Silakan tunggu proses verifikasi.")
          .build();

    } catch (Exception e) {
      logger.error("Error processing KPR application with documents: {}", e.getMessage(), e);
      throw e;
    }
  }

  public List<KprHistoryListResponse> getApprovalDeveloper(Integer developerId) {
    logger.info("Validate developer: {}", developerId);
    Developer developer =
        developerRepository
            .findById(developerId)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    List<KprApplication> applications =
        kprApplicationRepository.findKprApplicationsByDeveloperIDHistory(developer.getId());

    return applications.stream()
        .map(
            application ->
                new KprHistoryListResponse(
                    application.getProperty().getTitle(),
                    application.getStatus().toString(),
                    String.format(
                        "%s, %s, %s",
                        application.getProperty().getDistrict(),
                        application.getProperty().getCity(),
                        application.getProperty().getProvince()),
                    application.getApplicationNumber(),
                    application.getLoanAmount(),
                    application.getCreatedAt().toString(),
                    ""))
        .collect(Collectors.toList());
  }

  public List<KprInProgress> getKprApplicationsOnProgressByDeveloper(Integer developerId) {
    logger.info("Validate developer: {}", developerId);

    developerRepository
        .findById(developerId)
        .orElseThrow(() -> new IllegalArgumentException("Developer not found"));

    logger.info("KPR In Progress search started by {}", developerId);

    List<KprApplication> applications =
        kprApplicationRepository.findKprApplicationsOnProgressByDeveloper(developerId);

    logger.info("KPR In Progress search completed by {}", developerId);
    return applications.stream()
        .map(
            application ->
                new KprInProgress(
                    application.getId(),
                    application.getApplicationNumber(),
                    application.getProperty().getTitle(),
                    application.getProperty().getAddress(),
                    application.getLoanAmount(),
                    application.getCreatedAt().toString(),
                    application.getKprRate().getRateName()))
        .collect(Collectors.toList());
  }

  /** Validate user authentication and status */
  private User validateUser(Integer userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new IllegalStateException("User account is suspended");
    }

    if (user.getStatus() != UserStatus.ACTIVE
        && user.getStatus() != UserStatus.PENDING_VERIFICATION) {
      throw new IllegalStateException("User account is not eligible for KPR application");
    }

    return user;
  }

  /** Validate property and loan parameters */
  private Property validateProperty(
      Integer propertyId, BigDecimal downPayment, Integer loanTermYears) {
    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(() -> new IllegalArgumentException("Property not found"));

    if (property.getStatus() != Property.PropertyStatus.AVAILABLE) {
      throw new IllegalStateException("Property is not available for purchase");
    }

    if (!property.getIsKprEligible()) {
      throw new IllegalStateException("Property is not eligible for KPR financing");
    }

    // Validate down payment
    BigDecimal minDownPayment =
        property
            .getPrice()
            .multiply(property.getMinDownPaymentPercent())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

    if (downPayment.compareTo(minDownPayment) < 0) {
      throw new IllegalArgumentException(
          String.format(
              "Down payment must be at least %.2f%% of property price (Rp %.2f)",
              property.getMinDownPaymentPercent(), minDownPayment));
    }

    // Validate loan term
    if (loanTermYears > property.getMaxLoanTermYears()) {
      throw new IllegalArgumentException(
          String.format(
              "Loan term cannot exceed %d years for this property",
              property.getMaxLoanTermYears()));
    }

    return property;
  }

  /** Validate property for form submission */
  private Property validatePropertyForForm(KprApplicationFormRequest formRequest) {
    Property property =
        propertyRepository
            .findById(formRequest.getPropertyId())
            .orElseThrow(() -> new IllegalArgumentException("Property not found"));

    if (property.getStatus() != Property.PropertyStatus.AVAILABLE) {
      throw new IllegalStateException("Property is not available for purchase");
    }

    if (!property.getIsKprEligible()) {
      throw new IllegalStateException("Property is not eligible for KPR financing");
    }

    // Validate down payment
    BigDecimal minDownPayment =
        property
            .getPrice()
            .multiply(property.getMinDownPaymentPercent())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

    if (formRequest.getSimulationData().getDownPayment().compareTo(minDownPayment) < 0) {
      throw new IllegalArgumentException(
          String.format(
              "Down payment must be at least %.2f%% of property price (Rp %.2f)",
              property.getMinDownPaymentPercent(), minDownPayment));
    }

    // Validate loan term
    if (formRequest.getSimulationData().getLoanTermYears() > property.getMaxLoanTermYears()) {
      throw new IllegalArgumentException(
          String.format(
              "Loan term cannot exceed %d years for this property",
              property.getMaxLoanTermYears()));
    }

    return property;
  }

  /** Validate and get KPR rate */
  private KprRate validateAndGetKprRate(Integer kprRateId, SimulationData simulationData) {
    KprRate kprRate =
        kprRateRepository
            .findById(kprRateId)
            .orElseThrow(() -> new IllegalArgumentException("KPR rate not found"));

    if (!kprRate.getIsActive()) {
      throw new IllegalStateException("Selected KPR rate is not active");
    }

    // Validate loan amount range
    if (simulationData.getLoanAmount().compareTo(kprRate.getMinLoanAmount()) < 0
        || simulationData.getLoanAmount().compareTo(kprRate.getMaxLoanAmount()) > 0) {
      throw new IllegalArgumentException(
          String.format(
              "Loan amount must be between Rp %.2f and Rp %.2f for this rate",
              kprRate.getMinLoanAmount(), kprRate.getMaxLoanAmount()));
    }

    // Validate loan term
    if (simulationData.getLoanTermYears() < kprRate.getMinTermYears()
        || simulationData.getLoanTermYears() > kprRate.getMaxTermYears()) {
      throw new IllegalArgumentException(
          String.format(
              "Loan term must be between %d and %d years for this rate",
              kprRate.getMinTermYears(), kprRate.getMaxTermYears()));
    }

    return kprRate;
  }

  /** Validate no existing pending applications */
  private void validateNoPendingApplications(Integer userId, Integer propertyId) {
    if (kprApplicationRepository.existsPendingApplicationByUserAndProperty(userId, propertyId)) {
      throw new IllegalStateException(
          "Anda sudah memiliki aplikasi KPR yang sedang diproses untuk properti ini");
    }
  }

  /** Validate application exists */
  private KprApplication validateApplicationExists(Integer applicationId) {
    return kprApplicationRepository
        .findById(applicationId)
        .orElseThrow(() -> new IllegalArgumentException("Application not found"));
  }

  // ========================================
  // BUSINESS LOGIC - RATE SELECTION
  // ========================================

  /** Select the best KPR rate based on criteria */
  private KprRate selectBestRate(
      Property property, BigDecimal loanAmount, Integer loanTermYears, Integer userId) {
    // Convert property type to rate filter
    KprRate.PropertyTypeFilter propertyTypeFilter = convertPropertyType(property.getPropertyType());

    // Get user profile for customer segment determination
    Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
    KprRate.CustomerSegment customerSegment = KprRate.CustomerSegment.ALL;
    BigDecimal monthlyIncome = BigDecimal.ZERO;

    if (userProfileOpt.isPresent()) {
      UserProfile profile = userProfileOpt.get();
      customerSegment = determineCustomerSegment(profile.getOccupation());
      monthlyIncome = profile.getMonthlyIncome();

      // Validate age if profile exists
      int age = Period.between(profile.getBirthDate(), LocalDate.now()).getYears();
      // Note: Age validation will be done in rate selection query
    }

    // Find best eligible rate
    Optional<KprRate> bestRateOpt =
        kprRateRepository.findBestEligibleRate(
            propertyTypeFilter,
            customerSegment,
            loanAmount,
            loanTermYears,
            monthlyIncome,
            LocalDate.now());

    if (bestRateOpt.isEmpty()) {
      // Fallback to basic rate selection without customer segment
      bestRateOpt =
          kprRateRepository
              .findEligibleRates(propertyTypeFilter, loanAmount, loanTermYears, LocalDate.now())
              .stream()
              .findFirst();
    }

    return bestRateOpt.orElseThrow(
        () -> new IllegalStateException("No eligible KPR rate found for the specified criteria"));
  }

  // ========================================
  // BUSINESS LOGIC - CALCULATIONS
  // ========================================

  /** Calculate monthly installment using standard loan formula */
  public BigDecimal calculateMonthlyInstallment(
      BigDecimal principal, BigDecimal annualRate, Integer years) {
    if (principal.compareTo(BigDecimal.ZERO) <= 0
        || annualRate.compareTo(BigDecimal.ZERO) <= 0
        || years <= 0) {
      throw new IllegalArgumentException("Invalid loan parameters");
    }

    // Convert annual rate to monthly rate
    BigDecimal monthlyRate =
        annualRate
            .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
            .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

    // Number of payments
    int numberOfPayments = years * 12;

    // Calculate (1 + r)^n
    BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
    BigDecimal onePlusRatePowerN = onePlusRate.pow(numberOfPayments);

    // Calculate monthly payment: P * r * (1+r)^n / ((1+r)^n - 1)
    BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowerN);
    BigDecimal denominator = onePlusRatePowerN.subtract(BigDecimal.ONE);

    return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
  }

  /** Calculate LTV ratio */
  private BigDecimal calculateLtvRatio(KprApplicationFormRequest formRequest) {
    BigDecimal propertyValue = formRequest.getSimulationData().getPropertyValue();
    BigDecimal loanAmount = formRequest.getSimulationData().getLoanAmount();

    // Calculate LTV ratio as decimal (not percentage) to fit database precision (5,4)
    // Database expects values like 0.8000 (80%) not 80.0000
    return loanAmount.divide(propertyValue, 4, RoundingMode.HALF_UP);
  }

  // ========================================
  // BUSINESS LOGIC - UTILITY METHODS
  // ========================================

  /** Convert property type to rate filter */
  private KprRate.PropertyTypeFilter convertPropertyType(Property.PropertyType propertyType) {
    return switch (propertyType) {
      case RUMAH -> KprRate.PropertyTypeFilter.RUMAH;
      case APARTEMEN -> KprRate.PropertyTypeFilter.APARTEMEN;
      case RUKO -> KprRate.PropertyTypeFilter.RUKO;
      default -> KprRate.PropertyTypeFilter.ALL;
    };
  }

  /** Determine customer segment based on occupation */
  private KprRate.CustomerSegment determineCustomerSegment(String occupation) {
    if (occupation == null) return KprRate.CustomerSegment.ALL;

    String occ = occupation.toLowerCase();
    if (occ.contains("pegawai") || occ.contains("karyawan") || occ.contains("employee")) {
      return KprRate.CustomerSegment.EMPLOYEE;
    } else if (occ.contains("dokter") || occ.contains("lawyer") || occ.contains("professional")) {
      return KprRate.CustomerSegment.PROFESSIONAL;
    } else if (occ.contains("wiraswasta")
        || occ.contains("entrepreneur")
        || occ.contains("bisnis")) {
      return KprRate.CustomerSegment.ENTREPRENEUR;
    } else if (occ.contains("pensioner") || occ.contains("pensiun")) {
      return KprRate.CustomerSegment.PENSIONER;
    }

    return KprRate.CustomerSegment.ALL;
  }

  /** Determine initial approval level based on loan amount */
  private Integer determineInitialApprovalLevel(BigDecimal loanAmount) {
    // This is a simplified logic - in real implementation,
    // this would query approval_matrix table
    return 1; // Start with level 1
  }

  // ========================================
  // BUSINESS LOGIC - ID GENERATION
  // ========================================

  /** Generate unique application number */
  private String generateApplicationNumber() {
    String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    Long sequence = kprApplicationRepository.getNextSequenceNumber(datePrefix);
    return String.format("KPR-%s-%06d", datePrefix, sequence);
  }

  /** Generate unique loan number */
  private String generateLoanNumber() {
    String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    Long sequence = kprApplicationRepository.getNextSequenceNumber(datePrefix);
    return String.format("LOAN-%s-%06d", datePrefix, sequence);
  }

  // ========================================
  // ENTITY CREATION METHODS
  // ========================================

  /** Create KPR application entity from request */
  private KprApplication createKprApplication(
      Integer userId,
      KprApplicationRequest request,
      Property property,
      KprRate selectedRate,
      BigDecimal loanAmount,
      BigDecimal monthlyInstallment,
      String applicationNumber,
      Integer currentApprovalLevel) {

    return KprApplication.builder()
        .applicationNumber(applicationNumber)
        .userId(userId)
        .propertyId(request.getPropertyId())
        .kprRateId(selectedRate.getId())
        .propertyType(convertToApplicationPropertyType(property.getPropertyType()))
        .propertyValue(property.getPrice())
        .loanAmount(loanAmount)
        .loanTermYears(request.getLoanTermYears())
        .interestRate(selectedRate.getEffectiveRate())
        .monthlyInstallment(monthlyInstallment)
        .downPayment(request.getDownPayment())
        .propertyAddress(property.getAddress())
        .propertyCertificateType(convertToCertificateType(property.getCertificateType()))
        .developerName(property.getDeveloper().getCompanyName())
        .purpose(KprApplication.ApplicationPurpose.PRIMARY_RESIDENCE)
        .status(KprApplication.ApplicationStatus.SUBMITTED)
        .submittedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /** Create KPR application entity from form request */
  private KprApplication createKprApplicationFromForm(
      Integer userId,
      KprApplicationFormRequest formRequest,
      Property property,
      KprRate selectedRate,
      BigDecimal monthlyInstallment,
      String applicationNumber,
      Integer currentApprovalLevel) {

    // Calculate LTV ratio
    BigDecimal ltvRatio = calculateLtvRatio(formRequest);

    return KprApplication.builder()
        .applicationNumber(applicationNumber)
        .userId(userId)
        .propertyId(formRequest.getPropertyId())
        .kprRateId(selectedRate.getId())
        .propertyType(convertToApplicationPropertyType(property.getPropertyType()))
        .propertyValue(formRequest.getSimulationData().getPropertyValue())
        .loanAmount(formRequest.getSimulationData().getLoanAmount())
        .loanTermYears(formRequest.getSimulationData().getLoanTermYears())
        .interestRate(selectedRate.getEffectiveRate())
        .monthlyInstallment(monthlyInstallment)
        .downPayment(formRequest.getSimulationData().getDownPayment())
        .ltvRatio(ltvRatio)
        .propertyAddress(property.getAddress())
        .propertyCertificateType(convertToCertificateType(property.getCertificateType()))
        .developerName(property.getDeveloper().getCompanyName())
        .purpose(determinePurpose(formRequest.getPersonalData()))
        .status(KprApplication.ApplicationStatus.SUBMITTED)
        .submittedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  // ========================================
  // DATA MANAGEMENT METHODS
  // ========================================

  /** Update user profile from form data */
  private void updateUserProfileFromForm(
      Integer userId, PersonalData personalData, EmploymentData employmentData) {
    Optional<UserProfile> existingProfileOpt = userProfileRepository.findByUserId(userId);

    UserProfile profile;
    if (existingProfileOpt.isPresent()) {
      profile = existingProfileOpt.get();
    } else {
      profile = new UserProfile();
      profile.setUserId(userId);
      profile.setCreatedAt(LocalDateTime.now());
    }

    // Update personal data
    profile.setFullName(personalData.getFullName());
    profile.setNik(personalData.getNik());
    profile.setNpwp(personalData.getNpwp());
    profile.setBirthPlace(personalData.getBirthPlace());
    profile.setGender(Gender.fromString(personalData.getGender()));
    profile.setMaritalStatus(MaritalStatus.fromString(personalData.getMaritalStatus()));
    profile.setAddress(personalData.getAddress());
    profile.setCity(personalData.getCity());
    profile.setProvince(personalData.getProvince());
    profile.setPostalCode(personalData.getPostalCode());

    // Update employment data
    profile.setOccupation(employmentData.getOccupation());
    profile.setCompanyName(employmentData.getCompanyName());
    // profile.setCompanyAddress(employmentData.getCompanyAddress());
    // profile.setCompanyCity(employmentData.getCompanyCity());
    // profile.setCompanyProvince(employmentData.getCompanyProvince());
    // profile.setCompanyPostalCode(employmentData.getCompanyPostalCode());
    profile.setMonthlyIncome(employmentData.getMonthlyIncome());

    profile.setUpdatedAt(LocalDateTime.now());
    userProfileRepository.save(profile);
  }

  /** Store application documents */
  private List<ApplicationDocument> storeApplicationDocuments(
      Integer applicationId, KprApplicationFormRequest formRequest) {
    List<ApplicationDocument> documents = new ArrayList<>();

    try {
      // Store each document type
      if (formRequest.getKtpDocument() != null && !formRequest.getKtpDocument().isEmpty()) {
        ApplicationDocument doc =
            createDocumentEntity(
                applicationId, ApplicationDocument.DocumentType.KTP, formRequest.getKtpDocument());
        documents.add(applicationDocumentRepository.save(doc));
        logger.info("Successfully uploaded KTP document for application {}", applicationId);
      }

      if (formRequest.getNpwpDocument() != null && !formRequest.getNpwpDocument().isEmpty()) {
        ApplicationDocument doc =
            createDocumentEntity(
                applicationId,
                ApplicationDocument.DocumentType.NPWP,
                formRequest.getNpwpDocument());
        documents.add(applicationDocumentRepository.save(doc));
        logger.info("Successfully uploaded NPWP document for application {}", applicationId);
      }

      if (formRequest.getSalarySlipDocument() != null
          && !formRequest.getSalarySlipDocument().isEmpty()) {
        ApplicationDocument doc =
            createDocumentEntity(
                applicationId,
                ApplicationDocument.DocumentType.SLIP_GAJI,
                formRequest.getSalarySlipDocument());
        documents.add(applicationDocumentRepository.save(doc));
        logger.info("Successfully uploaded salary slip document for application {}", applicationId);
      }

      if (formRequest.getOtherDocument() != null && !formRequest.getOtherDocument().isEmpty()) {
        ApplicationDocument doc =
            createDocumentEntity(
                applicationId,
                ApplicationDocument.DocumentType.OTHER,
                formRequest.getOtherDocument());
        documents.add(applicationDocumentRepository.save(doc));
        logger.info("Successfully uploaded other document for application {}", applicationId);
      }

      logger.info(
          "Successfully stored {} documents for application {}", documents.size(), applicationId);
      return documents;
    } catch (Exception e) {
      logger.error(
          "Error storing documents for application {}: {}", applicationId, e.getMessage(), e);
      // Clean up any successfully uploaded documents if there's a failure
      documents.forEach(
          doc -> {
            try {
              // Optionally delete from S3 if needed
              applicationDocumentRepository.delete(doc);
            } catch (Exception cleanupException) {
              logger.error(
                  "Error cleaning up document {}: {}", doc.getId(), cleanupException.getMessage());
            }
          });
      throw new RuntimeException("Failed to store application documents", e);
    }
  }

  // ========================================
  // WORKFLOW MANAGEMENT METHODS
  // ========================================

  /** Create developer approval workflow */
  private void createDeveloperApprovalWorkflow(Integer applicationId, Integer developerId) {
    ApprovalWorkflow workflow =
        ApprovalWorkflow.builder()
            .applicationId(applicationId)
            .stage(ApprovalWorkflow.WorkflowStage.PROPERTY_APPRAISAL)
            .assignedTo(developerId)
            .status(ApprovalWorkflow.WorkflowStatus.PENDING)
            .priority(ApprovalWorkflow.PriorityLevel.NORMAL)
            .dueDate(LocalDateTime.now().plusDays(3))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    approvalWorkflowRepository.save(workflow);
  }

  /** Create first approval workflow */
  private void createFirstApprovalWorkflow(Integer applicationId, Integer approvalStaffId) {
    ApprovalWorkflow workflow =
        ApprovalWorkflow.builder()
            .applicationId(applicationId)
            .stage(ApprovalWorkflow.WorkflowStage.CREDIT_ANALYSIS)
            .assignedTo(approvalStaffId)
            .status(ApprovalWorkflow.WorkflowStatus.PENDING)
            .priority(ApprovalWorkflow.PriorityLevel.NORMAL)
            .dueDate(LocalDateTime.now().plusDays(5))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    approvalWorkflowRepository.save(workflow);
  }

  /** Create second approval workflow */
  private void createSecondApprovalWorkflow(Integer applicationId, Integer approvalStaffId) {
    ApprovalWorkflow workflow =
        ApprovalWorkflow.builder()
            .applicationId(applicationId)
            .stage(ApprovalWorkflow.WorkflowStage.FINAL_APPROVAL)
            .assignedTo(approvalStaffId)
            .status(ApprovalWorkflow.WorkflowStatus.PENDING)
            .priority(ApprovalWorkflow.PriorityLevel.NORMAL)
            .dueDate(LocalDateTime.now().plusDays(7))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    approvalWorkflowRepository.save(workflow);
  }

  // ========================================
  // PUBLIC SERVICE METHODS - APPLICATION SUBMISSION
  // ========================================

  /** Submit a new KPR application */
  @Transactional
  public KprApplicationResponse submitApplication(Integer userId, KprApplicationRequest request) {
    logger.info(
        "Processing KPR application for user: {} and property: {}",
        userId,
        request.getPropertyId());

    // 1. Validation Phase
    User user = validateUser(userId);
    Property property =
        validateProperty(
            request.getPropertyId(), request.getDownPayment(), request.getLoanTermYears());
    validateNoPendingApplications(userId, request.getPropertyId());

    // 2. Business Logic Phase
    Developer developer =
        developerRepository
            .findById(property.getDeveloperId())
            .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    BigDecimal loanAmount = property.getPrice().subtract(request.getDownPayment());
    KprRate selectedRate = selectBestRate(property, loanAmount, request.getLoanTermYears(), userId);
    BigDecimal monthlyInstallment =
        calculateMonthlyInstallment(
            loanAmount, selectedRate.getEffectiveRate(), request.getLoanTermYears());
    Integer currentApprovalLevel = determineInitialApprovalLevel(loanAmount);

    // 3. Entity Creation Phase
    String applicationNumber = generateApplicationNumber();
    KprApplication application =
        createKprApplication(
            userId,
            request,
            property,
            selectedRate,
            loanAmount,
            monthlyInstallment,
            applicationNumber,
            currentApprovalLevel);

    // 4. Persistence Phase
    KprApplication savedApplication = kprApplicationRepository.save(application);

    // 5. Workflow Creation Phase
    createDeveloperApprovalWorkflow(savedApplication.getId(), developer.getUser().getId());

    logger.info(
        "KPR application created successfully with ID: {} and number: {}",
        savedApplication.getId(),
        savedApplication.getApplicationNumber());

    return new KprApplicationResponse(
        savedApplication.getId(),
        savedApplication.getApplicationNumber(),
        savedApplication.getStatus(),
        monthlyInstallment,
        selectedRate.getEffectiveRate());
  }

  // ========================================
  // PUBLIC SERVICE METHODS - WORKFLOW MANAGEMENT
  // ========================================

  /** Assign approval workflow to staff members */
  @Transactional
  public AssignWorkflowResponse assignApprovalWorkflow(
      AssignWorkflowRequest request, Integer adminId) {
    // Validation Phase
    User admin = validateUser(adminId);
    if (!admin.getRole().getName().equalsIgnoreCase("ADMIN")) {
      throw new IllegalArgumentException("Only admin users can assign approval workflows");
    }

    logger.info(
        "Assigning approval workflow for application: {} to approval staff one: {}",
        request.getApplicationId(),
        request.getFirstApprovalId());

    // 1. Validation Phase
    User firstApprovalUser = validateUser(request.getFirstApprovalId());
    User secondApprovalUser = validateUser(request.getSecondApprovalId());
    KprApplication application = validateApplicationExists(request.getApplicationId());

    logger.info(
        "Approval staff one: {} has been assigned to application: {}",
        firstApprovalUser.getUsername(),
        request.getApplicationId());
    logger.info(
        "Approval staff two: {} has been assigned to application: {}",
        secondApprovalUser.getUsername(),
        request.getApplicationId());
    logger.info(
        "KPR application: {} has been assigned to approval workflow.",
        application.getApplicationNumber());

    // 2. Workflow Creation Phase
    createFirstApprovalWorkflow(application.getId(), request.getFirstApprovalId());
    createSecondApprovalWorkflow(application.getId(), request.getSecondApprovalId());

    // 3. Response Building
    return AssignWorkflowResponse.builder()
        .applicationID(application.getId())
        .firstApprovalId(request.getFirstApprovalId())
        .secondApprovalId(request.getSecondApprovalId())
        .build();
  }

  // ========================================
  // PUBLIC SERVICE METHODS - QUERY OPERATIONS
  // ========================================

  /** Get application history for user */
  public List<KprHistoryListResponse> getApplicationHistory(Integer userID) {
    logger.info("Validate user: {}", userID);

    // Validation Phase
    User user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Query Phase
    logger.info("KPR History search started by {}", userID);
    List<KprApplication> applications =
        kprApplicationRepository.findKprApplicationsByUserId(user.getId());

    // Response Building Phase
    return applications.stream()
        .map(
            application ->
                new KprHistoryListResponse(
                    application.getProperty().getTitle(),
                    application.getStatus().toString(),
                    String.format(
                        "%s, %s, %s",
                        application.getProperty().getDistrict(),
                        application.getProperty().getCity(),
                        application.getProperty().getProvince()),
                    application.getApplicationNumber(),
                    application.getLoanAmount(),
                    application.getCreatedAt().toString(),
                    ""))
        .collect(Collectors.toList());
  }

  /** Get application detail */
  @Transactional(readOnly = true)
  public KprApplicationDetailResponse getApplicationDetail(Integer applicationId, Integer userId) {
    // Validation Phase
    validateUser(userId);
    KprApplication application = validateApplicationExists(applicationId);

    // Authorization check - user can only view their own applications
    if (!application.getUserId().equals(userId)) {
      throw new IllegalArgumentException("You are not authorized to view this application");
    }

    // Response Building Phase
    return KprApplicationDetailResponse.builder()
        .applicationId(application.getId())
        .applicationNumber(application.getApplicationNumber())
        .status(application.getStatus())
        .propertyAddress(application.getPropertyAddress())
        .loanAmount(application.getLoanAmount())
        .monthlyInstallment(application.getMonthlyInstallment())
        .interestRate(application.getInterestRate())
        .loanTermYears(application.getLoanTermYears())
        .downPayment(application.getDownPayment())
        .submittedAt(application.getSubmittedAt())
        .build();
  }

  // ========================================
  // HELPER METHODS FOR TYPE CONVERSION
  // ========================================

  private Property.PropertyType convertToApplicationPropertyType(
      Property.PropertyType propertyType) {
    return switch (propertyType) {
      case RUMAH -> Property.PropertyType.RUMAH;
      case APARTEMEN -> Property.PropertyType.APARTEMEN;
      case RUKO -> Property.PropertyType.RUKO;
      case TANAH -> Property.PropertyType.TANAH;
      default -> throw new IllegalArgumentException("Unsupported property type: " + propertyType);
    };
  }

  private Property.CertificateType convertToCertificateType(
      Property.CertificateType certificateType) {
    return switch (certificateType) {
      case SHM -> Property.CertificateType.SHM;
      case HGB -> Property.CertificateType.HGB;
      case HGU -> Property.CertificateType.HGU;
      case HP -> Property.CertificateType.HP;
      case GIRIK -> Property.CertificateType.GIRIK;
      case PETOK_D -> Property.CertificateType.PETOK_D;
      default ->
          throw new IllegalArgumentException("Unsupported certificate type: " + certificateType);
    };
  }

  private KprApplication.ApplicationPurpose determinePurpose(PersonalData personalData) {
    // Simple logic - can be enhanced based on business rules
    return KprApplication.ApplicationPurpose.PRIMARY_RESIDENCE;
  }

  private ApplicationDocument createDocumentEntity(
      Integer applicationId, ApplicationDocument.DocumentType documentType, MultipartFile file) {
    try {
      // Validate file
      if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("File cannot be null or empty");
      }

      // Generate unique filename
      String originalFilename = file.getOriginalFilename();
      String fileExtension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      String fileName =
          documentType.name()
              + "_"
              + applicationId
              + "_"
              + System.currentTimeMillis()
              + fileExtension;

      // Upload file to IDCloudHost S3
      String fileUrl = idCloudHostS3Util.uploadKprDocument(file, fileName);

      return ApplicationDocument.builder()
          .applicationId(applicationId)
          .documentType(documentType)
          .documentName(fileName)
          .originalFilename(originalFilename)
          .filePath(fileUrl)
          .fileSize((int) file.getSize())
          .mimeType(file.getContentType())
          .isVerified(false)
          .uploadedAt(LocalDateTime.now())
          .build();
    } catch (Exception e) {
      logger.error(
          "Error uploading document {} for application {}: {}",
          documentType,
          applicationId,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to upload document: " + documentType.getDescription(), e);
    }
  }

  // Show all KPR for superadmin
  public List<KPRApplicant> getAllKprApplications(Integer userID) {
    var user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!user.getRole().toString().equalsIgnoreCase("ADMIN")) {
      throw new IllegalArgumentException("You are not authorized to view this application");
    }

    var applications = kprApplicationRepository.findAllKprApplications();

    List<KPRApplicant> kprApplicants = new ArrayList<>();

    for (var application : applications) {
      var kprApplicant =
          KPRApplicant.builder()
              .name(application.getUser().getUsername())
              .email(application.getUser().getEmail())
              .phone(application.getUser().getPhone())
              .KprApplicationCode(application.getApplicationNumber())
              .build();

      kprApplicants.add(kprApplicant);
    }
    return kprApplicants;
  }

  public List<KprHistoryListResponse> getAssignedVerifikatorHistory(Integer userId) {
    // Validate user role
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!user.getRole().toString().equalsIgnoreCase("VERIFIKATOR")) {
      throw new IllegalArgumentException("You are not authorized to view this history");
    }

    // Get history from repository
    List<KprHistoryListResponse> history =
        kprApplicationRepository.findKprApplicationsHistoryByUserID(userId);

    if (history.isEmpty()) {
      logger.info("No KPR application history found for user ID: {}", userId);
    } else {
      logger.info(
          "Retrieved {} KPR application history records for user ID: {}", history.size(), userId);
    }
    return history;
  }

  // Show list KprApplication on progress by userID from ApprovalWorkflow
  public List<KprInProgress> getAssignedKprApplicationsOnProgress(Integer userId) {
    // Validate user role
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!user.getRole().toString().equalsIgnoreCase("VERIFIKATOR")) {
      throw new IllegalArgumentException("You are not authorized to view this history");
    }

    // Get history from repository
    List<KprInProgress> history =
        kprApplicationRepository.findKprApplicationsOnProgressByUserID(userId);

    if (history.isEmpty()) {
      logger.info("No KPR application history found for user ID: {}", userId);
    } else {
      logger.info(
          "Retrieved {} KPR application history records for user ID: {}", history.size(), userId);
    }
    return history;
  }
}
