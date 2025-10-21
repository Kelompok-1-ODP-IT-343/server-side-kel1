package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.EmploymentData;
import com.kelompoksatu.griya.dto.KprApplicationDetailResponse;
import com.kelompoksatu.griya.dto.KprApplicationFormRequest;
import com.kelompoksatu.griya.dto.KprApplicationRequest;
import com.kelompoksatu.griya.dto.KprApplicationResponse;
import com.kelompoksatu.griya.dto.PersonalData;
import com.kelompoksatu.griya.dto.SimulationData;
import com.kelompoksatu.griya.entity.*;
import com.kelompoksatu.griya.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
      log.info(
          "Stored {} documents for application: {}", documents.size(), savedApplication.getId());

      // 13. Create initial approval workflow
      createInitialApprovalWorkflow(savedApplication.getId(), developer.getUser().getId());

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
      log.error("Error processing KPR application with documents: {}", e.getMessage(), e);
      throw e;
    }
  }

  /** Submit a new KPR application */
  @Transactional
  public KprApplicationResponse submitApplication(Integer userId, KprApplicationRequest request) {
    log.info(
        "Processing KPR application for user: {} and property: {}",
        userId,
        request.getPropertyId());

    // 1. Authentication & Authorization
    User user = validateUser(userId);

    // 2. Data Validation (Properties)
    Property property =
        validateProperty(
            request.getPropertyId(), request.getDownPayment(), request.getLoanTermYears());

    // 3. Check for existing pending applications
    if (kprApplicationRepository.existsPendingApplicationByUserAndProperty(
        userId, request.getPropertyId())) {
      throw new IllegalStateException("You already have a pending application for this property");
    }

    Developer developer =
        developerRepository
            .findById(property.getDeveloperId())
            .orElseThrow(() -> new IllegalArgumentException("Developer not found"));

    // 4. Rate Selection & Calculation
    BigDecimal loanAmount = property.getPrice().subtract(request.getDownPayment());
    KprRate selectedRate = selectBestRate(property, loanAmount, request.getLoanTermYears(), userId);

    // 5. Monthly installment calculation
    BigDecimal monthlyInstallment =
        calculateMonthlyInstallment(
            loanAmount, selectedRate.getEffectiveRate(), request.getLoanTermYears());

    // 6. Approval Preparation
    Integer currentApprovalLevel = determineInitialApprovalLevel(loanAmount);

    // 7. Database Transaction (Atomic)
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

    KprApplication savedApplication = kprApplicationRepository.save(application);

    // 8. Create initial approval workflow
    createInitialApprovalWorkflow(savedApplication.getId(), developer.getUser().getId());

    log.info(
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
    return approvalLevelRepository.findApplicableLevels(loanAmount).stream()
        .findFirst()
        .map(ApprovalLevel::getId)
        .orElse(null);
  }

  /** Generate unique application number */
  private String generateApplicationNumber() {
    int currentYear = LocalDate.now().getYear();
    String yearPrefix = "KPR-" + currentYear + "-%";
    Integer nextSequence = kprApplicationRepository.getNextSequenceNumber(yearPrefix);

    return String.format("KPR-%d-%06d", currentYear, nextSequence);
  }

  /** Create KPR application entity */
  private KprApplication createKprApplication(
      Integer userId,
      KprApplicationRequest request,
      Property property,
      KprRate selectedRate,
      BigDecimal loanAmount,
      BigDecimal monthlyInstallment,
      String applicationNumber,
      Integer currentApprovalLevel) {

    // Compose property address
    String propertyAddress =
        String.format(
            "%s, %s, %s %s",
            property.getAddress(),
            property.getCity(),
            property.getProvince(),
            property.getPostalCode());

    // Get developer name if available
    String developerName = null;
    if (property.getDeveloperId() != null) {
      developerName =
          developerRepository
              .findById(property.getDeveloperId())
              .map(Developer::getCompanyName)
              .orElse(null);
    }

    KprApplication application = new KprApplication();
    logger.info("Creating KPR application with number: {}", applicationNumber);
    logger.info(
        "Selected KPR rate: {} with effective rate: {}%",
        selectedRate.getCustomerSegment(), selectedRate.getEffectiveRate());
    application.setApplicationNumber(applicationNumber);
    application.setUserId(userId);
    application.setPropertyId(request.getPropertyId());
    application.setKprRateId(selectedRate.getId());
    application.setPropertyType(property.getPropertyType());
    application.setPropertyValue(property.getPrice());
    application.setLoanAmount(loanAmount);
    application.setLoanTermYears(request.getLoanTermYears());
    application.setInterestRate(selectedRate.getEffectiveRate());
    application.setMonthlyInstallment(monthlyInstallment);
    application.setDownPayment(request.getDownPayment());
    application.setPropertyAddress(propertyAddress);
    application.setPropertyCertificateType(property.getCertificateType());
    application.setDeveloperName(developerName);

    // Set purpose with fallback to default if null
    application.setPurpose(KprApplication.ApplicationPurpose.PRIMARY_RESIDENCE);

    application.setStatus(KprApplication.ApplicationStatus.SUBMITTED);
    application.setCurrentApprovalLevel(currentApprovalLevel);
    application.setSubmittedAt(LocalDateTime.now());

    return application;
  }

  /** Create initial approval workflow */
  private void createInitialApprovalWorkflow(Integer applicationId, Integer approvalLevelId) {
    if (approvalLevelId == null) return;

    Optional<ApprovalLevel> levelOpt = approvalLevelRepository.findById(approvalLevelId);
    if (levelOpt.isEmpty()) return;

    ApprovalLevel level = levelOpt.get();

    ApprovalWorkflow workflow = new ApprovalWorkflow();
    workflow.setApplicationId(applicationId);
    workflow.setStage(ApprovalWorkflow.WorkflowStage.DOCUMENT_VERIFICATION);
    workflow.setStatus(ApprovalWorkflow.WorkflowStatus.PENDING);
    workflow.setPriority(ApprovalWorkflow.PriorityLevel.NORMAL);

    // Set due date based on timeout hours
    if (level.getTimeoutHours() != null) {
      workflow.setDueDate(LocalDateTime.now().plusHours(level.getTimeoutHours()));
    }

    approvalWorkflowRepository.save(workflow);
  }

  /** Validate property for form request */
  private Property validatePropertyForForm(KprApplicationFormRequest formRequest) {
    Optional<Property> propertyOpt = propertyRepository.findById(formRequest.getPropertyId());
    if (propertyOpt.isEmpty()) {
      throw new IllegalArgumentException("Properti tidak ditemukan");
    }

    Property property = propertyOpt.get();
    if (property.getStatus() != Property.PropertyStatus.AVAILABLE) {
      throw new IllegalArgumentException("Properti tidak tersedia untuk KPR");
    }

    // Validate loan amount against property price
    BigDecimal maxLoanAmount =
        property.getPrice().multiply(BigDecimal.valueOf(0.80)); // Max 80% LTV
    if (formRequest.getSimulationData().getLoanAmount().compareTo(maxLoanAmount) > 0) {
      throw new IllegalArgumentException("Jumlah pinjaman melebihi 80% dari harga properti");
    }

    // Validate down payment
    BigDecimal minDownPayment =
        property.getPrice().multiply(BigDecimal.valueOf(0.20)); // Min 20% DP
    if (formRequest.getSimulationData().getDownPayment().compareTo(minDownPayment) < 0) {
      throw new IllegalArgumentException("Uang muka minimal 20% dari harga properti");
    }

    return property;
  }

  /** Update user profile with form data */
  private void updateUserProfileFromForm(
      Integer userId, PersonalData personalData, EmploymentData employmentData) {
    Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userId);
    UserProfile profile;

    if (profileOpt.isPresent()) {
      profile = profileOpt.get();
    } else {
      profile = new UserProfile();
      profile.setUserId(userId);
      profile.setCreatedAt(LocalDateTime.now());
    }

    // Update personal data
    profile.setFullName(personalData.getFullName());
    profile.setNik(personalData.getNik());
    profile.setNpwp(personalData.getNpwp());
    profile.setBirthDate(
        LocalDate.parse(personalData.getBirthDate(), DateTimeFormatter.ISO_LOCAL_DATE));
    profile.setBirthPlace(personalData.getBirthPlace());
    profile.setGender(personalData.getGenderEnum());
    profile.setMaritalStatus(personalData.getMaritalStatusEnum());
    profile.setAddress(personalData.getAddress());
    profile.setCity(personalData.getCity());
    profile.setProvince(personalData.getProvince());
    profile.setPostalCode(personalData.getPostalCode());

    // Update employment data
    profile.setOccupation(employmentData.getOccupation());
    profile.setMonthlyIncome(employmentData.getMonthlyIncome());
    profile.setCompanyName(employmentData.getCompanyName());
    // profile.setCompanyAddress(employmentData.getCompanyAddress());
    // profile.setCompanyCity(employmentData.getCompanyCity());
    // profile.setCompanyProvince(employmentData.getCompanyProvince());
    // profile.setCompanyPostalCode(employmentData.getCompanyPostalCode());

    profile.setUpdatedAt(LocalDateTime.now());
    userProfileRepository.save(profile);

    log.info("Updated user profile for user: {}", userId);
  }

  /** Validate and get KPR rate */
  private KprRate validateAndGetKprRate(Integer kprRateId, SimulationData simulationData) {
    Optional<KprRate> rateOpt = kprRateRepository.findById(kprRateId);
    if (rateOpt.isEmpty()) {
      throw new IllegalArgumentException("KPR rate tidak ditemukan");
    }

    KprRate rate = rateOpt.get();
    if (!rate.getIsActive()) {
      throw new IllegalArgumentException("KPR rate tidak aktif");
    }

    // Validate loan amount against rate limits
    if (simulationData.getLoanAmount().compareTo(rate.getMinLoanAmount()) < 0
        || simulationData.getLoanAmount().compareTo(rate.getMaxLoanAmount()) > 0) {
      throw new IllegalArgumentException(
          "Jumlah pinjaman tidak sesuai dengan ketentuan rate yang dipilih");
    }

    // Validate loan term
    if (simulationData.getLoanTermYears() < rate.getMinTermYears()
        || simulationData.getLoanTermYears() > rate.getMaxTermYears()) {
      throw new IllegalArgumentException(
          "Jangka waktu pinjaman tidak sesuai dengan ketentuan rate yang dipilih");
    }

    return rate;
  }

  /** Create KPR application from form data */
  private KprApplication createKprApplicationFromForm(
      Integer userId,
      KprApplicationFormRequest formRequest,
      Property property,
      KprRate selectedRate,
      BigDecimal monthlyInstallment,
      String applicationNumber,
      Integer currentApprovalLevel) {

    KprApplication application = new KprApplication();

    // Basic info
    application.setUserId(userId);
    application.setPropertyId(formRequest.getPropertyId());
    application.setApplicationNumber(applicationNumber);
    application.setStatus(KprApplication.ApplicationStatus.SUBMITTED);

    // Loan details
    application.setLoanAmount(formRequest.getSimulationData().getLoanAmount());
    application.setDownPayment(formRequest.getSimulationData().getDownPayment());
    application.setLoanTermYears(formRequest.getSimulationData().getLoanTermYears());
    application.setInterestRate(selectedRate.getEffectiveRate());
    application.setMonthlyInstallment(monthlyInstallment);
    application.setKprRateId(selectedRate.getId());

    // Property details
    application.setPropertyValue(formRequest.getSimulationData().getPropertyValue());
    application.setPropertyType(property.getPropertyType());
    application.setPropertyAddress(
        String.format(
            "%s, %s, %s %s",
            property.getAddress(),
            property.getCity(),
            property.getProvince(),
            property.getPostalCode()));
    application.setPropertyCertificateType(property.getCertificateType());

    // Developer name if available
    if (property.getDeveloperId() != null) {
      String developerName =
          developerRepository
              .findById(property.getDeveloperId())
              .map(Developer::getCompanyName)
              .orElse(null);
      application.setDeveloperName(developerName);
    }

    // Purpose with null check
    KprApplication.ApplicationPurpose purpose = formRequest.getPurpose();
    if (purpose == null) {
      purpose = KprApplication.ApplicationPurpose.PRIMARY_RESIDENCE;
    }
    application.setPurpose(purpose);

    // LTV ratio calculation
    application.setLtvRatio(calculateLtvRatio(formRequest));

    // Approval details
    application.setCurrentApprovalLevel(currentApprovalLevel);

    // Timestamps
    application.setSubmittedAt(LocalDateTime.now());
    application.setCreatedAt(LocalDateTime.now());
    application.setUpdatedAt(LocalDateTime.now());

    return application;
  }

  /** Store application documents */
  private List<ApplicationDocument> storeApplicationDocuments(
      Integer applicationId, KprApplicationFormRequest formRequest) {
    List<ApplicationDocument> documents = new ArrayList<>();

    try {
      // Store KTP document (required)
      if (formRequest.getKtpDocument() != null && !formRequest.getKtpDocument().isEmpty()) {
        ApplicationDocument ktpDoc =
            fileStorageService.storeFile(
                formRequest.getKtpDocument(), ApplicationDocument.DocumentType.KTP, applicationId);
        documents.add(applicationDocumentRepository.save(ktpDoc));
        log.info("Stored KTP document for application: {}", applicationId);
      }

      // Store NPWP document (optional)
      if (formRequest.getNpwpDocument() != null && !formRequest.getNpwpDocument().isEmpty()) {
        ApplicationDocument npwpDoc =
            fileStorageService.storeFile(
                formRequest.getNpwpDocument(),
                ApplicationDocument.DocumentType.NPWP,
                applicationId);
        documents.add(applicationDocumentRepository.save(npwpDoc));
        log.info("Stored NPWP document for application: {}", applicationId);
      }

      // Store salary slip document (required)
      if (formRequest.getSalarySlipDocument() != null
          && !formRequest.getSalarySlipDocument().isEmpty()) {
        ApplicationDocument salaryDoc =
            fileStorageService.storeFile(
                formRequest.getSalarySlipDocument(),
                ApplicationDocument.DocumentType.SLIP_GAJI,
                applicationId);
        documents.add(applicationDocumentRepository.save(salaryDoc));
        log.info("Stored salary slip document for application: {}", applicationId);
      }

      // Store other document (optional)
      if (formRequest.getOtherDocument() != null && !formRequest.getOtherDocument().isEmpty()) {
        ApplicationDocument otherDoc =
            fileStorageService.storeFile(
                formRequest.getOtherDocument(),
                ApplicationDocument.DocumentType.OTHER,
                applicationId);
        documents.add(applicationDocumentRepository.save(otherDoc));
        log.info("Stored other document for application: {}", applicationId);
      }

    } catch (Exception e) {
      log.error("Error storing documents for application: {}", applicationId, e);
      // Clean up any stored files if there's an error
      documents.forEach(
          doc -> {
            try {
              fileStorageService.deleteFile(doc.getFilePath());
              applicationDocumentRepository.delete(doc);
            } catch (Exception cleanupError) {
              log.error("Error cleaning up document: {}", doc.getFilePath(), cleanupError);
            }
          });
      throw new RuntimeException("Gagal menyimpan dokumen: " + e.getMessage());
    }

    return documents;
  }

  /** Calculate LTV (Loan to Value) ratio */
  private BigDecimal calculateLtvRatio(KprApplicationFormRequest formRequest) {
    BigDecimal propertyValue = formRequest.getSimulationData().getPropertyValue();
    BigDecimal loanAmount = formRequest.getSimulationData().getLoanAmount();
    return loanAmount.divide(propertyValue, 4, RoundingMode.HALF_UP);
  }

  /** Generate unique loan number with format: LOAN-YYYY-XXXXXX */
  private String generateLoanNumber() {
    String year = String.valueOf(LocalDate.now().getYear());
    String timestamp = String.valueOf(System.currentTimeMillis()).substring(7); // Last 6 digits
    return String.format("LOAN-%s-%s", year, timestamp);
  }

  /**
   * Get KPR application detail with documents
   *
   * @param applicationId Application ID
   * @param userId User ID for authorization
   * @return KprApplicationDetailResponse with application and document details
   */
  @Transactional(readOnly = true)
  public KprApplicationDetailResponse getApplicationDetail(Integer applicationId, Integer userId) {
    log.info("Retrieving KPR application detail for ID: {} by user: {}", applicationId, userId);

    // Find application
    KprApplication application =
        kprApplicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Aplikasi KPR tidak ditemukan"));

    // Get associated documents
    List<ApplicationDocument> documents =
        applicationDocumentRepository.findByApplicationIdOrderByUploadedAtDesc(applicationId);

    // Convert documents to DTO
    List<KprApplicationDetailResponse.DocumentInfo> documentInfos =
        documents.stream()
            .map(
                doc ->
                    KprApplicationDetailResponse.DocumentInfo.builder()
                        .documentId(doc.getId())
                        .documentType(doc.getDocumentType())
                        .documentName(doc.getDocumentName())
                        .filePath(doc.getFilePath())
                        .fileSize(doc.getFileSize())
                        .mimeType(doc.getMimeType())
                        .isVerified(doc.getIsVerified())
                        .verifiedBy(doc.getVerifiedBy())
                        .verifiedAt(doc.getVerifiedAt())
                        .verificationNotes(doc.getVerificationNotes())
                        .uploadedAt(doc.getUploadedAt())
                        .build())
            .toList();

    return KprApplicationDetailResponse.builder()
        .applicationId(application.getId())
        .applicationNumber(application.getApplicationNumber())
        .userId(application.getUserId())
        .propertyId(application.getPropertyId())
        .kprRateId(application.getKprRateId())
        .propertyType(application.getPropertyType())
        .propertyValue(application.getPropertyValue())
        .propertyAddress(application.getPropertyAddress())
        .propertyCertificateType(application.getPropertyCertificateType())
        .developerName(application.getDeveloperName())
        .loanAmount(application.getLoanAmount())
        .loanTermYears(application.getLoanTermYears())
        .interestRate(application.getInterestRate())
        .monthlyInstallment(application.getMonthlyInstallment())
        .downPayment(application.getDownPayment())
        .ltvRatio(application.getLtvRatio())
        .purpose(application.getPurpose())
        .status(application.getStatus())
        .currentApprovalLevel(application.getCurrentApprovalLevel())
        .submittedAt(application.getSubmittedAt())
        .approvedAt(application.getApprovedAt())
        .rejectedAt(application.getRejectedAt())
        .rejectionReason(application.getRejectionReason())
        .notes(application.getNotes())
        .createdAt(application.getCreatedAt())
        .updatedAt(application.getUpdatedAt())
        .documents(documentInfos)
        .build();
  }
}
