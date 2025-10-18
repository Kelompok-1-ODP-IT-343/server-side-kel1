package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.KprApplicationRequest;
import com.kelompoksatu.griya.dto.KprApplicationResponse;
import com.kelompoksatu.griya.entity.*;
import com.kelompoksatu.griya.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

/**
 * Service for KPR Application business logic
 */
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

    /**
     * Submit a new KPR application
     */
    @Transactional
    public KprApplicationResponse submitApplication(Integer userId, KprApplicationRequest request) {
        log.info("Processing KPR application for user: {} and property: {}", userId, request.getPropertyId());

        // 1. Authentication & Authorization
        User user = validateUser(userId);
        
        // 2. Data Validation (Properties)
        Property property = validateProperty(request.getPropertyId(), request.getDownPayment(), request.getLoanTermYears());
        
        // 3. Check for existing pending applications
        if (kprApplicationRepository.existsPendingApplicationByUserAndProperty(userId, request.getPropertyId())) {
            throw new IllegalStateException("You already have a pending application for this property");
        }

        // 4. Rate Selection & Calculation
        BigDecimal loanAmount = property.getPrice().subtract(request.getDownPayment());
        KprRate selectedRate = selectBestRate(property, loanAmount, request.getLoanTermYears(), userId);
        
        // 5. Monthly installment calculation
        BigDecimal monthlyInstallment = calculateMonthlyInstallment(loanAmount, selectedRate.getEffectiveRate(), request.getLoanTermYears());
        
        // 6. Approval Preparation
        Integer currentApprovalLevel = determineInitialApprovalLevel(loanAmount);
        
        // 7. Database Transaction (Atomic)
        String applicationNumber = generateApplicationNumber();
        KprApplication application = createKprApplication(userId, request, property, selectedRate, 
                                                        loanAmount, monthlyInstallment, applicationNumber, currentApprovalLevel);
        
        KprApplication savedApplication = kprApplicationRepository.save(application);
        
        // 8. Create initial approval workflow
        createInitialApprovalWorkflow(savedApplication.getId(), currentApprovalLevel);
        
        log.info("KPR application created successfully with ID: {} and number: {}", 
                savedApplication.getId(), savedApplication.getApplicationNumber());
        
        return new KprApplicationResponse(
                savedApplication.getId(),
                savedApplication.getApplicationNumber(),
                savedApplication.getStatus(),
                monthlyInstallment,
                selectedRate.getEffectiveRate()
        );
    }

    /**
     * Validate user authentication and status
     */
    private User validateUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new IllegalStateException("User account is suspended");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("User account is not eligible for KPR application");
        }
        
        return user;
    }

    /**
     * Validate property and loan parameters
     */
    private Property validateProperty(Integer propertyId, BigDecimal downPayment, Integer loanTermYears) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        
        if (property.getStatus() != Property.PropertyStatus.AVAILABLE) {
            throw new IllegalStateException("Property is not available for purchase");
        }
        
        if (!property.getIsKprEligible()) {
            throw new IllegalStateException("Property is not eligible for KPR financing");
        }
        
        // Validate down payment
        BigDecimal minDownPayment = property.getPrice()
                .multiply(property.getMinDownPaymentPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        if (downPayment.compareTo(minDownPayment) < 0) {
            throw new IllegalArgumentException(
                    String.format("Down payment must be at least %.2f%% of property price (Rp %.2f)", 
                            property.getMinDownPaymentPercent(), minDownPayment));
        }
        
        // Validate loan term
        if (loanTermYears > property.getMaxLoanTermYears()) {
            throw new IllegalArgumentException(
                    String.format("Loan term cannot exceed %d years for this property", 
                            property.getMaxLoanTermYears()));
        }
        
        return property;
    }

    /**
     * Select the best KPR rate based on criteria
     */
    private KprRate selectBestRate(Property property, BigDecimal loanAmount, Integer loanTermYears, Integer userId) {
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
        Optional<KprRate> bestRateOpt = kprRateRepository.findBestEligibleRate(
                propertyTypeFilter, customerSegment, loanAmount, loanTermYears, monthlyIncome, LocalDate.now());
        
        if (bestRateOpt.isEmpty()) {
            // Fallback to basic rate selection without customer segment
            bestRateOpt = kprRateRepository.findEligibleRates(propertyTypeFilter, loanAmount, loanTermYears, LocalDate.now())
                    .stream().findFirst();
        }
        
        return bestRateOpt.orElseThrow(() -> 
                new IllegalStateException("No eligible KPR rate found for the specified criteria"));
    }

    /**
     * Calculate monthly installment using standard loan formula
     */
    public BigDecimal calculateMonthlyInstallment(BigDecimal principal, BigDecimal annualRate, Integer years) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0 || annualRate.compareTo(BigDecimal.ZERO) <= 0 || years <= 0) {
            throw new IllegalArgumentException("Invalid loan parameters");
        }
        
        // Convert annual rate to monthly rate
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
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

    /**
     * Convert property type to rate filter
     */
    private KprRate.PropertyTypeFilter convertPropertyType(Property.PropertyType propertyType) {
        return switch (propertyType) {
            case RUMAH -> KprRate.PropertyTypeFilter.RUMAH;
            case APARTEMEN -> KprRate.PropertyTypeFilter.APARTEMEN;
            case RUKO -> KprRate.PropertyTypeFilter.RUKO;
            default -> KprRate.PropertyTypeFilter.ALL;
        };
    }

    /**
     * Determine customer segment based on occupation
     */
    private KprRate.CustomerSegment determineCustomerSegment(String occupation) {
        if (occupation == null) return KprRate.CustomerSegment.ALL;
        
        String occ = occupation.toLowerCase();
        if (occ.contains("pegawai") || occ.contains("karyawan") || occ.contains("employee")) {
            return KprRate.CustomerSegment.EMPLOYEE;
        } else if (occ.contains("dokter") || occ.contains("lawyer") || occ.contains("professional")) {
            return KprRate.CustomerSegment.PROFESSIONAL;
        } else if (occ.contains("wiraswasta") || occ.contains("entrepreneur") || occ.contains("bisnis")) {
            return KprRate.CustomerSegment.ENTREPRENEUR;
        } else if (occ.contains("pensioner") || occ.contains("pensiun")) {
            return KprRate.CustomerSegment.PENSIONER;
        }
        
        return KprRate.CustomerSegment.ALL;
    }

    /**
     * Determine initial approval level based on loan amount
     */
    private Integer determineInitialApprovalLevel(BigDecimal loanAmount) {
        return approvalLevelRepository.findApplicableLevels(loanAmount)
                .stream()
                .findFirst()
                .map(ApprovalLevel::getId)
                .orElse(null);
    }

    /**
     * Generate unique application number
     */
    private String generateApplicationNumber() {
        int currentYear = LocalDate.now().getYear();
        String yearPrefix = "KPR-" + currentYear + "-%";
        Integer nextSequence = kprApplicationRepository.getNextSequenceNumber(yearPrefix);
        
        return String.format("KPR-%d-%06d", currentYear, nextSequence);
    }

    /**
     * Create KPR application entity
     */
    private KprApplication createKprApplication(Integer userId, KprApplicationRequest request, Property property, 
                                              KprRate selectedRate, BigDecimal loanAmount, BigDecimal monthlyInstallment,
                                              String applicationNumber, Integer currentApprovalLevel) {
        
        // Compose property address
        String propertyAddress = String.format("%s, %s, %s %s", 
                property.getAddress(), property.getCity(), property.getProvince(), property.getPostalCode());
        
        // Get developer name if available
        String developerName = null;
        if (property.getDeveloperId() != null) {
            developerName = developerRepository.findById(property.getDeveloperId())
                    .map(Developer::getCompanyName)
                    .orElse(null);
        }
        
        KprApplication application = new KprApplication();
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
        application.setPurpose(request.getPurpose());
        application.setStatus(KprApplication.ApplicationStatus.SUBMITTED);
        application.setCurrentApprovalLevel(currentApprovalLevel);
        application.setSubmittedAt(LocalDateTime.now());
        
        return application;
    }

    /**
     * Create initial approval workflow
     */
    private void createInitialApprovalWorkflow(Integer applicationId, Integer approvalLevelId) {
        if (approvalLevelId == null) return;
        
        Optional<ApprovalLevel> levelOpt = approvalLevelRepository.findById(approvalLevelId);
        if (levelOpt.isEmpty()) return;
        
        ApprovalLevel level = levelOpt.get();
        
        ApprovalWorkflow workflow = new ApprovalWorkflow();
        workflow.setApplicationId(applicationId);
        workflow.setApprovalLevelId(approvalLevelId);
        workflow.setStage(ApprovalWorkflow.WorkflowStage.DOCUMENT_VERIFICATION);
        workflow.setStatus(ApprovalWorkflow.WorkflowStatus.PENDING);
        workflow.setPriority(ApprovalWorkflow.WorkflowPriority.NORMAL);
        
        // Set due date based on timeout hours
        if (level.getTimeoutHours() != null) {
            workflow.setDueDate(LocalDateTime.now().plusHours(level.getTimeoutHours()));
        }
        
        approvalWorkflowRepository.save(workflow);
    }
}