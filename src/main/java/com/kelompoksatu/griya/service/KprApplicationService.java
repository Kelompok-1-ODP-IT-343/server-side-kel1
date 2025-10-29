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
import lombok.*;
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

  public List<KprHistoryListResponse> getApprovalDeveloper(Integer userID) {
    logger.info("Validate user: {}", userID);
    User user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    logger.info("Validate developer: {}", user.getDeveloper().getId());
    Developer developer =
        developerRepository
            .findById(user.getDeveloper().getId())
            .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    List<KprApplication> applications =
        kprApplicationRepository.findKprApplicationsByDeveloperIDHistory(developer.getId());

    return applications.stream()
        .map(
            application ->
                new KprHistoryListResponse(
                    application.getId(),
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

  public List<KprInProgress> getKprApplicationsOnProgressByDeveloper(Integer userID) {
    logger.info("Validate user: {}", userID);
    User user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    logger.info("Validate developer: {}", user.getDeveloper().getId());
    Developer developer =
        developerRepository
            .findById(user.getDeveloper().getId())
            .orElseThrow(() -> new IllegalArgumentException("Developer not found"));
    logger.info("KPR In Progress search started by {}", developer.getId());

    List<KprApplication> applications =
        kprApplicationRepository.findKprApplicationsOnProgressByDeveloper(userID);

    logger.info("KPR In Progress search completed by {}", developer.getId());
    return applications.stream()
        .map(
            application ->
                new KprInProgress(
                    application.getId(),
                    application.getUser().getUsername(),
                    application.getUser().getEmail(),
                    application.getUser().getPhone(),
                    application.getApplicationNumber(),
                    application.getProperty().getTitle(),
                    application.getProperty().getAddress(),
                    application.getLoanAmount(),
                    application.getCreatedAt().toString(),
                    application.getKprRate().getRateName(),
                    application.getStatus().toString()))
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

    // Add retry mechanism for handling concurrent requests
    int maxRetries = 5;
    for (int attempt = 0; attempt < maxRetries; attempt++) {
      try {
        // Get base sequence number
        long timestamp = System.currentTimeMillis() % 10000; // 4 digit
        int randomComponent = (int) (Math.random() * 100); // 2 digit

        long finalSequence = (timestamp * 100) + randomComponent; // total 6 digit

        // Format: "KPR-" (4) + datePrefix (6) + "-" (1) + finalSequence (6) = 17 chars max
        // Jadi kita tambahkan padding supaya total 20 char pas
        String applicationNumber = String.format("KPR-%s-%06d", datePrefix, finalSequence);

        // Kalau kamu mau pastikan fix 20 chars, bisa tambahkan 3 digit random tambahan
        if (applicationNumber.length() < 20) {
          int extra = (int) (Math.random() * Math.pow(10, 20 - applicationNumber.length()));
          applicationNumber += String.format("%0" + (20 - applicationNumber.length()) + "d", extra);
        }

        // Check if this number already exists (additional safety check)
        if (!kprApplicationRepository.findByApplicationNumber(applicationNumber).isPresent()) {
          return applicationNumber;
        }

        // If exists, wait a bit and retry
        Thread.sleep(10 + (attempt * 5)); // Progressive backoff

      } catch (Exception e) {
        logger.warn(
            "Attempt {} failed to generate unique application number: {}",
            attempt + 1,
            e.getMessage());
        if (attempt == maxRetries - 1) {
          // Fallback: use UUID-based approach
          String uuid =
              java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
          return String.format("KPR-%s-%s", datePrefix, uuid);
        }
      }
    }

    // This should never be reached, but just in case
    throw new RuntimeException(
        "Failed to generate unique application number after " + maxRetries + " attempts");
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
  @Transactional
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
                    application.getId(),
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

  public List<KprHistoryListResponse> getAssignedApproverHistory(Integer userId) {
    // Validate user role
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!user.getRole().toString().equalsIgnoreCase("APPROVER")) {
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
    if (!user.getRole().toString().equalsIgnoreCase("APPROVER")) {
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

  // Buat get all List KPR untuk admin walaupun history atau in progress
  public List<KprInProgress> getAllKprApplicationsAll(Integer userID) {
    var user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!user.getRole().toString().equalsIgnoreCase("ADMIN")) {
      throw new IllegalArgumentException("You are not authorized to view this application");
    }
    // Get history from repository
    List<KprInProgress> history = kprApplicationRepository.findKprApplicationsAll();

    if (history.isEmpty()) {
      logger.info("No KPR application history found for user ID: {}", userID);
    } else {
      logger.info(
          "Retrieved {} KPR application history records for user ID: {}", history.size(), userID);
    }
    return history;
  }

  /**
   * Get detailed KPR application information with all related entities
   *
   * @param applicationId Application ID
   * @param currentUserId Current user ID for authorization
   * @return Comprehensive application details
   */
  @Transactional(readOnly = true)
  public KprApplicationDetailResponse getApplicationDetail(
      Integer applicationId, Integer currentUserId) {
    log.info(
        "Fetching comprehensive application detail for ID: {} by user: {}",
        applicationId,
        currentUserId);

    // 1. Fetch application with all relationships eagerly loaded
    KprApplication application =
        kprApplicationRepository
            .findByIdWithAllRelations(applicationId)
            .orElseThrow(
                () -> new RuntimeException("Application not found with ID: " + applicationId));

    // 2. Authorization check - user can only view their own applications or admin/staff can view
    // all
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("Current user not found"));

    boolean isOwner = application.getUserId().equals(currentUserId);
    boolean isStaff =
        currentUser.getRole() != null
            && (currentUser.getRole().getName().contains("ADMIN")
                || currentUser.getRole().getName().contains("STAFF")
                || currentUser.getRole().getName().contains("MANAGER"));

    boolean isDeveloper =
        application.getProperty().getDeveloper().getUser().getId() == currentUserId;

    if (!isOwner && !isStaff && !isDeveloper) {
      throw new RuntimeException("Unauthorized to view this application");
    }

    // 3. Fetch user profile for comprehensive user data
    UserProfile userProfile =
        userProfileRepository.findByUserId(application.getUserId()).orElse(null);

    // 4. Fetch all approval workflows for this application
    List<ApprovalWorkflow> approvalWorkflows =
        approvalWorkflowRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);

    // 5. Fetch all application documents
    List<ApplicationDocument> documents =
        applicationDocumentRepository.findByApplicationIdOrderByUploadedAtDesc(applicationId);

    // 6. Calculate LTV ratio
    double ltvRatio = 0.0;
    if (application.getPropertyValue() != null
        && application.getPropertyValue().compareTo(BigDecimal.ZERO) > 0) {
      ltvRatio =
          application
              .getLoanAmount()
              .divide(application.getPropertyValue(), 4, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100))
              .doubleValue();
    }

    // 7. Build comprehensive response
    return KprApplicationDetailResponse.builder()
        // Basic application info
        .applicationId(application.getId())
        .applicationNumber(application.getApplicationNumber())
        .status(application.getStatus())

        // User information with profile
        .userInfo(buildUserInfo(application.getUser(), userProfile))

        // Property information
        .propertyInfo(buildPropertyInfo(application.getProperty()))

        // Developer information
        .developerInfo(
            buildDeveloperInfo(
                application.getProperty() != null
                    ? application.getProperty().getDeveloper()
                    : null))

        // KPR Rate information
        .kprRateInfo(buildKprRateInfo(application.getKprRate()))

        // Loan details
        .propertyType(application.getPropertyType())
        .propertyValue(application.getPropertyValue())
        .loanAmount(application.getLoanAmount())
        .loanTermYears(application.getLoanTermYears())
        .interestRate(application.getInterestRate())
        .monthlyInstallment(application.getMonthlyInstallment())
        .downPayment(application.getDownPayment())
        .ltvRatio(BigDecimal.valueOf(ltvRatio))

        // Property details
        .propertyAddress(application.getPropertyAddress())
        .propertyCertificateType(application.getPropertyCertificateType())
        .developerName(application.getDeveloperName())
        .purpose(application.getPurpose())

        // Application status and timestamps
        .submittedAt(application.getSubmittedAt())
        .approvedAt(application.getApprovedAt())
        .rejectedAt(application.getRejectedAt())
        .rejectionReason(application.getRejectionReason())
        .notes(application.getNotes())
        .createdAt(application.getCreatedAt())
        .updatedAt(application.getUpdatedAt())

        // Approval workflows
        .approvalWorkflows(buildApprovalWorkflowInfoList(approvalWorkflows))

        // Documents
        .documents(buildDocumentInfoList(documents))
        .build();
  }

  /** Build comprehensive user information including profile data */
  private KprApplicationDetailResponse.UserInfo buildUserInfo(User user, UserProfile profile) {
    if (user == null) return null;

    return KprApplicationDetailResponse.UserInfo.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .phone(user.getPhone())
        .fullName(profile != null ? profile.getFullName() : null)
        .nik(profile != null ? profile.getNik() : null)
        .npwp(profile != null ? profile.getNpwp() : null)
        .birthPlace(profile != null ? profile.getBirthPlace() : null)
        .gender(profile != null && profile.getGender() != null ? profile.getGender().name() : null)
        .maritalStatus(
            profile != null && profile.getMaritalStatus() != null
                ? profile.getMaritalStatus().name()
                : null)
        .address(profile != null ? profile.getAddress() : null)
        .city(profile != null ? profile.getCity() : null)
        .province(profile != null ? profile.getProvince() : null)
        .postalCode(profile != null ? profile.getPostalCode() : null)
        .occupation(profile != null ? profile.getOccupation() : null)
        .companyName(profile != null ? profile.getCompanyName() : null)
        .monthlyIncome(profile != null ? profile.getMonthlyIncome() : null)
        .build();
  }

  /** Build comprehensive property information */
  private KprApplicationDetailResponse.PropertyInfo buildPropertyInfo(Property property) {
    if (property == null) return null;

    return KprApplicationDetailResponse.PropertyInfo.builder()
        .propertyId(property.getId())
        .propertyCode(property.getPropertyCode())
        .title(property.getTitle())
        .description(property.getDescription())
        .address(property.getAddress())
        .city(property.getCity())
        .province(property.getProvince())
        .postalCode(property.getPostalCode())
        .district(property.getDistrict())
        .village(property.getVillage())
        .landArea(property.getLandArea())
        .buildingArea(property.getBuildingArea())
        .bedrooms(property.getBedrooms())
        .bathrooms(property.getBathrooms())
        .floors(property.getFloors())
        .garage(property.getGarage())
        .yearBuilt(property.getYearBuilt())
        .price(property.getPrice())
        .pricePerSqm(property.getPricePerSqm())
        .certificateType(property.getCertificateType())
        .certificateNumber(property.getCertificateNumber())
        .pbbValue(property.getPbbValue())
        .status(property.getStatus())
        .minDownPaymentPercent(property.getMinDownPaymentPercent())
        .maxLoanTermYears(property.getMaxLoanTermYears())
        .build();
  }

  /** Build comprehensive developer information */
  private KprApplicationDetailResponse.DeveloperInfo buildDeveloperInfo(Developer developer) {
    if (developer == null) return null;

    return KprApplicationDetailResponse.DeveloperInfo.builder()
        .developerId(developer.getId())
        .companyName(developer.getCompanyName())
        .companyCode(developer.getCompanyCode())
        .businessLicense(developer.getBusinessLicense())
        .developerLicense(developer.getDeveloperLicense())
        .contactPerson(developer.getContactPerson())
        .phone(developer.getPhone())
        .email(developer.getEmail())
        .website(developer.getWebsite())
        .address(developer.getAddress())
        .city(developer.getCity())
        .province(developer.getProvince())
        .postalCode(developer.getPostalCode())
        .establishedYear(developer.getEstablishedYear())
        .description(developer.getDescription())
        .specialization(
            developer.getSpecialization() != null ? developer.getSpecialization().name() : null)
        .isPartner(developer.getIsPartner())
        .partnershipLevel(
            developer.getPartnershipLevel() != null ? developer.getPartnershipLevel().name() : null)
        .commissionRate(developer.getCommissionRate())
        .status(developer.getStatus().name())
        .verifiedAt(developer.getVerifiedAt())
        .build();
  }

  /** Build comprehensive KPR rate information */
  private KprApplicationDetailResponse.KprRateInfo buildKprRateInfo(KprRate kprRate) {
    if (kprRate == null) return null;

    return KprApplicationDetailResponse.KprRateInfo.builder()
        .rateName(kprRate.getRateName())
        .rateType(kprRate.getRateType().name())
        .propertyType(kprRate.getPropertyType().name())
        .customerSegment(kprRate.getCustomerSegment().name())
        .baseRate(kprRate.getBaseRate())
        .margin(kprRate.getMargin())
        .effectiveRate(kprRate.getEffectiveRate())
        .minLoanAmount(kprRate.getMinLoanAmount())
        .maxLoanAmount(kprRate.getMaxLoanAmount())
        .minTermYears(kprRate.getMinTermYears())
        .maxTermYears(kprRate.getMaxTermYears())
        .maxLtvRatio(kprRate.getMaxLtvRatio())
        .minIncome(kprRate.getMinIncome())
        .maxAge(kprRate.getMaxAge())
        .minDownPaymentPercent(kprRate.getMinDownPaymentPercent())
        .adminFee(kprRate.getAdminFee())
        .adminFeePercent(kprRate.getAdminFeePercent())
        .appraisalFee(kprRate.getAppraisalFee())
        .insuranceRate(kprRate.getInsuranceRate())
        .notaryFeePercent(kprRate.getNotaryFeePercent())
        .isPromotional(kprRate.getIsPromotional())
        .promoDescription(kprRate.getPromoDescription())
        .build();
  }

  /** Build comprehensive approval workflow information list */
  private List<KprApplicationDetailResponse.ApprovalWorkflowInfo> buildApprovalWorkflowInfoList(
      List<ApprovalWorkflow> workflows) {
    if (workflows == null || workflows.isEmpty()) {
      return new ArrayList<>();
    }

    return workflows.stream().map(this::buildApprovalWorkflowInfo).collect(Collectors.toList());
  }

  /** Build comprehensive approval workflow information */
  private KprApplicationDetailResponse.ApprovalWorkflowInfo buildApprovalWorkflowInfo(
      ApprovalWorkflow workflow) {
    if (workflow == null) return null;

    // Fetch assigned user details
    User assignedUser = null;
    if (workflow.getAssignedTo() != null) {
      assignedUser = userRepository.findById(workflow.getAssignedTo()).orElse(null);
    }

    // Fetch escalated user details
    User escalatedUser = null;
    if (workflow.getEscalatedTo() != null) {
      escalatedUser = userRepository.findById(workflow.getEscalatedTo()).orElse(null);
    }

    return KprApplicationDetailResponse.ApprovalWorkflowInfo.builder()
        .workflowId(workflow.getId())
        .applicationId(workflow.getApplicationId())
        .stage(workflow.getStage())
        .status(workflow.getStatus())
        .priority(workflow.getPriority())
        .assignedTo(workflow.getAssignedTo())
        .escalatedTo(workflow.getEscalatedTo())
        .dueDate(workflow.getDueDate())
        .startedAt(workflow.getStartedAt())
        .completedAt(workflow.getCompletedAt())
        .approvalNotes(workflow.getApprovalNotes())
        .rejectionReason(workflow.getRejectionReason())
        .approvalNotes(workflow.getApprovalNotes())
        .createdAt(workflow.getCreatedAt())
        .updatedAt(workflow.getUpdatedAt())
        .build();
  }

  /** Build comprehensive document information list */
  private List<KprApplicationDetailResponse.DocumentInfo> buildDocumentInfoList(
      List<ApplicationDocument> documents) {
    if (documents == null || documents.isEmpty()) {
      return new ArrayList<>();
    }

    return documents.stream().map(this::buildDocumentInfo).collect(Collectors.toList());
  }

  /** Build comprehensive document information */
  private KprApplicationDetailResponse.DocumentInfo buildDocumentInfo(
      ApplicationDocument document) {
    if (document == null) return null;

    // Fetch verifier details
    User verifier = null;
    if (document.getVerifiedBy() != null) {
      verifier = userRepository.findById(document.getVerifiedBy()).orElse(null);
    }

    return KprApplicationDetailResponse.DocumentInfo.builder()
        .documentId(document.getId())
        .documentType(document.getDocumentType())
        .documentName(document.getDocumentName())
        .filePath(document.getFilePath())
        .fileSize(document.getFileSize())
        .mimeType(document.getMimeType())
        .isVerified(document.getIsVerified())
        .verifiedBy(document.getVerifiedBy())
        .verifiedAt(document.getVerifiedAt())
        .verificationNotes(document.getVerificationNotes())
        .uploadedAt(document.getUploadedAt())
        .build();
  }
}
