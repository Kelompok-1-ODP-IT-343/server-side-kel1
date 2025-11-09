package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.DeveloperStatsResponse;
import com.kelompoksatu.griya.entity.KprApplication;
import com.kelompoksatu.griya.repository.StatDeveloperRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatDeveloperService {

  private final StatDeveloperRepository statDeveloperRepository;

  public DeveloperStatsResponse getDashboard(Integer developerId, String range) {
    int months = parseMonths(range);
    LocalDate endDate = LocalDate.now().withDayOfMonth(1).plusMonths(1); // first day next month
    LocalDate startDate = endDate.minusMonths(months);

    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.atStartOfDay();

    // KPI current vs previous month
    LocalDateTime currStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
    LocalDateTime currEnd = currStart.plusMonths(1);
    LocalDateTime prevStart = currStart.minusMonths(1);
    LocalDateTime prevEnd = currStart;

    long approvedCurr =
        statDeveloperRepository.countApprovedByDeveloperAndCreatedBetween(
            developerId, currStart, currEnd);
    long approvedPrev =
        statDeveloperRepository.countApprovedByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);

    long rejectedCurr =
        statDeveloperRepository.countRejectedByDeveloperAndCreatedBetween(
            developerId, currStart, currEnd);
    long rejectedPrev =
        statDeveloperRepository.countRejectedByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);

    long pendingCurr =
        statDeveloperRepository.countPendingByDeveloperAndCreatedBetween(
            developerId, currStart, currEnd);
    long pendingPrev =
        statDeveloperRepository.countPendingByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);

    long customersCurr =
        statDeveloperRepository.countDistinctUsersByDeveloperAndCreatedBetween(
            developerId, currStart, currEnd);
    long customersPrev =
        statDeveloperRepository.countDistinctUsersByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);

    DeveloperStatsResponse.Kpi kpi =
        new DeveloperStatsResponse.Kpi(
            new DeveloperStatsResponse.Kpi.KpiItem(
                approvedCurr, percentageChange(approvedPrev, approvedCurr)),
            new DeveloperStatsResponse.Kpi.KpiItem(
                rejectedCurr, percentageChange(rejectedPrev, rejectedCurr)),
            new DeveloperStatsResponse.Kpi.KpiItem(
                pendingCurr, percentageChange(pendingPrev, pendingCurr)),
            new DeveloperStatsResponse.Kpi.KpiItem(
                customersCurr, percentageChange(customersPrev, customersCurr)));

    // Load applications for the range and aggregate
    List<KprApplication> apps =
        statDeveloperRepository.findApplicationsByDeveloperAndCreatedBetween(
            developerId, start, end);

    Map<String, List<KprApplication>> byMonth =
        apps.stream()
            .collect(
                Collectors.groupingBy(
                    a ->
                        a.getCreatedAt()
                            .toLocalDate()
                            .getMonth()
                            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)));

    List<DeveloperStatsResponse.GrowthAndDemandEntry> growth = new ArrayList<>();
    List<DeveloperStatsResponse.OutstandingLoanEntry> outstanding = new ArrayList<>();
    List<DeveloperStatsResponse.UserRegisteredEntry> registered = new ArrayList<>();

    // ensure chronological last N months
    List<String> monthOrder = buildMonthOrder(months);

    for (String mon : monthOrder) {
      List<KprApplication> monthApps = byMonth.getOrDefault(mon, List.of());

      long totalRequests = monthApps.size();
      long totalApproved =
          monthApps.stream().filter(a -> "APPROVED".equals(a.getStatus().name())).count();

      BigDecimal approvedAmount =
          monthApps.stream()
              .filter(a -> "APPROVED".equals(a.getStatus().name()))
              .map(a -> a.getLoanAmount() == null ? BigDecimal.ZERO : a.getLoanAmount())
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      long distinctUsers =
          monthApps.stream()
              .map(a -> a.getUser() == null ? null : a.getUser().getId())
              .filter(id -> id != null)
              .collect(Collectors.toSet())
              .size();

      growth.add(
          new DeveloperStatsResponse.GrowthAndDemandEntry(mon, totalRequests, totalApproved));
      outstanding.add(
          new DeveloperStatsResponse.OutstandingLoanEntry(
              mon, approvedAmount.divide(BigDecimal.valueOf(1_000_000_000L)).doubleValue()));

      // Full month name for user_registered per sample
      String fullMon = monthFullName(mon);
      registered.add(new DeveloperStatsResponse.UserRegisteredEntry(fullMon, distinctUsers));
    }

    // Processing funnel within the overall range
    Map<String, Long> funnelMap = buildFunnel(apps);
    List<DeveloperStatsResponse.FunnelEntry> funnel =
        List.of(
            new DeveloperStatsResponse.FunnelEntry("Draft", funnelMap.getOrDefault("Draft", 0L)),
            new DeveloperStatsResponse.FunnelEntry("Review", funnelMap.getOrDefault("Review", 0L)),
            new DeveloperStatsResponse.FunnelEntry(
                "Approval", funnelMap.getOrDefault("Approval", 0L)),
            new DeveloperStatsResponse.FunnelEntry("Reject", funnelMap.getOrDefault("Reject", 0L)));

    return DeveloperStatsResponse.ofNow(kpi, growth, outstanding, funnel, registered);
  }

  private int parseMonths(String range) {
    if (range == null || range.isBlank()) return 7;
    switch (range.toLowerCase()) {
      case "last_12_months":
      case "12m":
        return 12;
      case "last_7_months":
      case "7m":
        return 7;
      case "last_6_months":
      case "6m":
        return 6;
      default:
        return 7;
    }
  }

  private double percentageChange(long previous, long current) {
    if (previous == 0) {
      return current == 0 ? 0.0 : 100.0;
    }
    return ((double) (current - previous) / (double) previous) * 100.0;
  }

  private List<String> buildMonthOrder(int months) {
    List<String> order = new ArrayList<>();
    LocalDate cursor = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);
    for (int i = 0; i < months; i++) {
      order.add(cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
      cursor = cursor.plusMonths(1);
    }
    return order;
  }

  private String monthFullName(String shortName) {
    if (shortName == null || shortName.isBlank()) return "";
    String key = shortName.substring(0, 1).toUpperCase() + shortName.substring(1).toLowerCase();
    switch (key) {
      case "Jan":
        return "January";
      case "Feb":
        return "February";
      case "Mar":
        return "March";
      case "Apr":
        return "April";
      case "May":
        return "May";
      case "Jun":
        return "June";
      case "Jul":
        return "July";
      case "Aug":
        return "August";
      case "Sep":
        return "September";
      case "Oct":
        return "October";
      case "Nov":
        return "November";
      case "Dec":
        return "December";
      default:
        return key;
    }
  }

  private Map<String, Long> buildFunnel(List<KprApplication> apps) {
    Map<String, Long> m = new HashMap<>();
    Set<String> reviewStatuses = Set.of("DOCUMENT_VERIFICATION", "PROPERTY_APPRAISAL");
    Set<String> approvalStatuses = Set.of("CREDIT_ANALYSIS", "APPROVAL_PENDING");

    long draft = apps.stream().filter(a -> "SUBMITTED".equals(a.getStatus().name())).count();
    long review = apps.stream().filter(a -> reviewStatuses.contains(a.getStatus().name())).count();
    long approval =
        apps.stream().filter(a -> approvalStatuses.contains(a.getStatus().name())).count();
    long reject = apps.stream().filter(a -> "REJECTED".equals(a.getStatus().name())).count();

    m.put("Draft", draft);
    m.put("Review", review);
    m.put("Approval", approval);
    m.put("Reject", reject);
    return m;
  }
}
