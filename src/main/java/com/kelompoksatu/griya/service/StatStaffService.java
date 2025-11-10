package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.StaffStatsResponse;
import com.kelompoksatu.griya.dto.StaffStatsResponse.FunnelItem;
import com.kelompoksatu.griya.dto.StaffStatsResponse.SLABucketItem;
import com.kelompoksatu.griya.dto.StaffStatsResponse.SubmissionVsApprovedItem;
import com.kelompoksatu.griya.dto.StaffStatsResponse.Summary;
import com.kelompoksatu.griya.dto.StaffStatsResponse.ValueVsIncomeItem;
import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.KprApplication;
import com.kelompoksatu.griya.repository.StatStaffRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatStaffService {

  private final StatStaffRepository repo;

  public StatStaffService(StatStaffRepository repo) {
    this.repo = repo;
  }

  public enum Range {
    SEVEN_DAYS,
    THIRTY_DAYS,
    NINETY_DAYS,
    YTD
  }

  public StaffStatsResponse getDashboard(Integer staffUserId, String rangeStr) {
    Range range = parseRange(rangeStr);
    LocalDateTime end = LocalDate.now().atStartOfDay().plusDays(1); // inclusive end
    LocalDateTime start = computeStart(range, end);

    // Fetch current-period data scoped by assigned approver
    long approved = repo.countApprovedAssignedToStaffBetween(staffUserId, start, end);
    long rejected = repo.countRejectedAssignedToStaffBetween(staffUserId, start, end);
    long pending = repo.countPendingAssignedToStaffBetween(staffUserId, start, end);
    long customers = repo.countDistinctUsersAssignedToStaffBetween(staffUserId, start, end);

    // Previous-period for growth
    LocalDateTime prevEnd = start;
    LocalDateTime prevStart = computePreviousStart(range, start);
    long prevApproved = repo.countApprovedAssignedToStaffBetween(staffUserId, prevStart, prevEnd);
    long prevRejected = repo.countRejectedAssignedToStaffBetween(staffUserId, prevStart, prevEnd);
    long prevPending = repo.countPendingAssignedToStaffBetween(staffUserId, prevStart, prevEnd);
    long prevCustomers =
        repo.countDistinctUsersAssignedToStaffBetween(staffUserId, prevStart, prevEnd);

    Summary summary = new Summary();
    summary.setApprovedCount((int) approved);
    summary.setRejectedCount((int) rejected);
    summary.setPendingCount((int) pending);
    summary.setActiveCustomers((int) customers);
    Summary.Growth growth = new Summary.Growth();
    growth.setApproved(percentChange(approved, prevApproved));
    growth.setRejected(percentChange(rejected, prevRejected));
    growth.setPending(percentChange(pending, prevPending));
    growth.setCustomers(percentChange(customers, prevCustomers));
    summary.setGrowth(growth);

    // Applications for monthly series and funnel/SLA
    List<KprApplication> apps =
        repo.findApplicationsAssignedToStaffBetween(staffUserId, start, end);
    List<ApprovalWorkflow> flows =
        repo.findWorkflowsAssignedToStaffBetween(staffUserId, start, end);

    List<SubmissionVsApprovedItem> subVsAppr =
        buildSubmissionVsApproved(apps, start.toLocalDate(), end.toLocalDate());
    List<ValueVsIncomeItem> valVsInc =
        buildValueVsIncome(apps, start.toLocalDate(), end.toLocalDate());
    List<FunnelItem> funnel = buildFunnel(apps);
    List<SLABucketItem> sla = buildSlaBuckets(flows);

    StaffStatsResponse resp = new StaffStatsResponse();
    resp.setSummary(summary);
    resp.setFunnelStatus(funnel);
    resp.setSlaBucket(sla);
    resp.setSubmissionVsApproved(subVsAppr);
    resp.setValueVsIncome(valVsInc);
    return resp;
  }

  private Range parseRange(String r) {
    if (r == null) return Range.SEVEN_DAYS;
    switch (r.toLowerCase(Locale.ROOT)) {
      case "7d":
        return Range.SEVEN_DAYS;
      case "30d":
        return Range.THIRTY_DAYS;
      case "90d":
        return Range.NINETY_DAYS;
      case "ytd":
        return Range.YTD;
      default:
        return Range.SEVEN_DAYS;
    }
  }

  private LocalDateTime computeStart(Range range, LocalDateTime end) {
    switch (range) {
      case SEVEN_DAYS:
        return end.minusDays(7);
      case THIRTY_DAYS:
        return end.minusDays(30);
      case NINETY_DAYS:
        return end.minusDays(90);
      case YTD:
        return LocalDate.now().withDayOfYear(1).atStartOfDay();
      default:
        return end.minusDays(7);
    }
  }

  private LocalDateTime computePreviousStart(Range range, LocalDateTime currentStart) {
    switch (range) {
      case SEVEN_DAYS:
        return currentStart.minusDays(7);
      case THIRTY_DAYS:
        return currentStart.minusDays(30);
      case NINETY_DAYS:
        return currentStart.minusDays(90);
      case YTD:
        // Previous YTD equivalent: same length immediately preceding
        long days = LocalDate.now().toEpochDay() - LocalDate.now().withDayOfYear(1).toEpochDay();
        return currentStart.minusDays(days);
      default:
        return currentStart.minusDays(7);
    }
  }

  private double percentChange(long current, long previous) {
    if (previous == 0) return current > 0 ? 100.0 : 0.0;
    return ((double) (current - previous) / (double) previous) * 100.0;
  }

  private List<SubmissionVsApprovedItem> buildSubmissionVsApproved(
      List<KprApplication> apps, LocalDate start, LocalDate end) {
    // Group by YearMonth of createdAt
    Map<YearMonth, List<KprApplication>> byMonth =
        apps.stream()
            .filter(a -> a.getCreatedAt() != null)
            .collect(Collectors.groupingBy(a -> YearMonth.from(a.getCreatedAt())));

    List<SubmissionVsApprovedItem> out = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL yy", new Locale("id", "ID"));
    YearMonth cur = YearMonth.from(start);
    YearMonth last = YearMonth.from(end.minusDays(1));
    while (!cur.isAfter(last)) {
      List<KprApplication> monthApps = byMonth.getOrDefault(cur, List.of());
      int submitted = monthApps.size();
      int approved = (int) monthApps.stream().filter(a -> "APPROVED".equals(a.getStatus())).count();
      SubmissionVsApprovedItem item = new SubmissionVsApprovedItem();
      item.setMonth(capitalize(cur.format(fmt)));
      item.setSubmitted(submitted);
      item.setApproved(approved);
      out.add(item);
      cur = cur.plusMonths(1);
    }
    return out;
  }

  private List<ValueVsIncomeItem> buildValueVsIncome(
      List<KprApplication> apps, LocalDate start, LocalDate end) {
    Map<YearMonth, List<KprApplication>> byMonth =
        apps.stream()
            .filter(a -> a.getCreatedAt() != null)
            .collect(Collectors.groupingBy(a -> YearMonth.from(a.getCreatedAt())));

    List<ValueVsIncomeItem> out = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL yy", new Locale("id", "ID"));
    YearMonth cur = YearMonth.from(start);
    YearMonth last = YearMonth.from(end.minusDays(1));
    while (!cur.isAfter(last)) {
      List<KprApplication> monthApps = byMonth.getOrDefault(cur, List.of());
      BigDecimal submissionValue =
          monthApps.stream()
              .map(KprApplication::getLoanAmount)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal income =
          monthApps.stream()
              .map(KprApplication::getMonthlyInstallment)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      ValueVsIncomeItem item = new ValueVsIncomeItem();
      item.setMonth(capitalize(cur.format(fmt)));
      item.setSubmissionValue(submissionValue);
      item.setIncome(income);
      out.add(item);
      cur = cur.plusMonths(1);
    }
    return out;
  }

  private List<FunnelItem> buildFunnel(List<KprApplication> apps) {
    int appraisal =
        (int) apps.stream().filter(a -> "PROPERTY_APPRAISAL".equals(a.getStatus())).count();
    int analysis = (int) apps.stream().filter(a -> "CREDIT_ANALYSIS".equals(a.getStatus())).count();
    int finalApproval =
        (int) apps.stream().filter(a -> "APPROVAL_PENDING".equals(a.getStatus())).count();
    int approved = (int) apps.stream().filter(a -> "APPROVED".equals(a.getStatus())).count();

    List<FunnelItem> out = new ArrayList<>();
    out.add(new FunnelItem("Property Appraisal", appraisal));
    out.add(new FunnelItem("Credit Analysis", analysis));
    out.add(new FunnelItem("Final Approval", finalApproval));
    out.add(new FunnelItem("Approved", approved));
    return out;
  }

  private List<SLABucketItem> buildSlaBuckets(List<ApprovalWorkflow> flows) {
    // SLA := duration between workflow createdAt and completedAt
    int bucket0to2 = 0, bucket3to5 = 0, bucketGt5 = 0;
    for (ApprovalWorkflow aw : flows) {
      if (aw.getCompletedAt() == null || aw.getCreatedAt() == null) continue;
      long days = java.time.Duration.between(aw.getCreatedAt(), aw.getCompletedAt()).toDays();
      if (days <= 2) bucket0to2++;
      else if (days <= 5) bucket3to5++;
      else bucketGt5++;
    }
    List<SLABucketItem> out = new ArrayList<>();
    out.add(new SLABucketItem("0-2 hari", bucket0to2));
    out.add(new SLABucketItem("3-5 hari", bucket3to5));
    out.add(new SLABucketItem(">5 hari", bucketGt5));
    return out;
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }
}
