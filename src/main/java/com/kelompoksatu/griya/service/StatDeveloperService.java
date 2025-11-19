package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.DeveloperStatsResponse;
import com.kelompoksatu.griya.entity.KprApplication;
import com.kelompoksatu.griya.repository.StatDeveloperRepository;
import java.math.BigDecimal;
import java.time.Duration;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatDeveloperService {

  private final StatDeveloperRepository statDeveloperRepository;

  public DeveloperStatsResponse getDashboard(Integer developerId, String range) {
    int months = parseMonths(range);
    LocalDate endDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    LocalDate startDate = endDate.minusMonths(months);

    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.atStartOfDay();

    long approved =
        statDeveloperRepository.countApprovedByDeveloperAndCreatedBetween(developerId, start, end);
    long rejected =
        statDeveloperRepository.countRejectedByDeveloperAndCreatedBetween(developerId, start, end);
    long pending =
        statDeveloperRepository.countPendingByDeveloperAndCreatedBetween(developerId, start, end);
    long customers =
        statDeveloperRepository.countDistinctUsersByDeveloperAndCreatedBetween(
            developerId, start, end);

    LocalDateTime prevStart = start.minusMonths(months);
    LocalDateTime prevEnd = start;
    long prevApproved =
        statDeveloperRepository.countApprovedByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);
    long prevRejected =
        statDeveloperRepository.countRejectedByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);
    long prevPending =
        statDeveloperRepository.countPendingByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);
    long prevCustomers =
        statDeveloperRepository.countDistinctUsersByDeveloperAndCreatedBetween(
            developerId, prevStart, prevEnd);

    DeveloperStatsResponse.Summary summary = new DeveloperStatsResponse.Summary();
    summary.setApprovedCount((int) approved);
    summary.setRejectedCount((int) rejected);
    summary.setPendingCount((int) pending);
    summary.setActiveCustomers((int) customers);
    DeveloperStatsResponse.Summary.Growth growth = new DeveloperStatsResponse.Summary.Growth();
    growth.setApproved(percentChange(approved, prevApproved));
    growth.setRejected(percentChange(rejected, prevRejected));
    growth.setPending(percentChange(pending, prevPending));
    growth.setCustomers(percentChange(customers, prevCustomers));
    summary.setGrowth(growth);

    List<KprApplication> apps =
        statDeveloperRepository.findApplicationsByDeveloperAndCreatedBetween(
            developerId, start, end);

    List<DeveloperStatsResponse.SubmissionVsApprovedItem> subVsAppr =
        buildSubmissionVsApproved(apps, start.toLocalDate(), end.toLocalDate());
    List<DeveloperStatsResponse.ValueVsIncomeItem> valVsInc =
        buildValueVsIncome(apps, start.toLocalDate(), end.toLocalDate());
    List<DeveloperStatsResponse.FunnelItem> funnel = buildFunnelItems(apps);
    List<DeveloperStatsResponse.SLABucketItem> sla = buildSlaBucketsFromApps(apps);

    DeveloperStatsResponse resp = new DeveloperStatsResponse();
    resp.setSummary(summary);
    resp.setFunnelStatus(funnel);
    resp.setSlaBucket(sla);
    resp.setSubmissionVsApproved(subVsAppr);
    resp.setValueVsIncome(valVsInc);
    return resp;
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

  private double percentChange(long current, long previous) {
    if (previous == 0) return current > 0 ? 100.0 : 0.0;
    return ((double) (current - previous) / (double) previous) * 100.0;
  }

  private List<DeveloperStatsResponse.SubmissionVsApprovedItem> buildSubmissionVsApproved(
      List<KprApplication> apps, LocalDate start, LocalDate end) {
    Map<YearMonth, List<KprApplication>> byMonth =
        apps.stream()
            .filter(a -> a.getCreatedAt() != null)
            .collect(Collectors.groupingBy(a -> YearMonth.from(a.getCreatedAt())));

    List<DeveloperStatsResponse.SubmissionVsApprovedItem> out = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL yy", new Locale("id", "ID"));
    YearMonth cur = YearMonth.from(start);
    YearMonth last = YearMonth.from(end.minusDays(1));
    while (!cur.isAfter(last)) {
      List<KprApplication> monthApps = byMonth.getOrDefault(cur, List.of());
      int submitted = monthApps.size();
      int approved =
          (int)
              monthApps.stream()
                  .filter(a -> a.getStatus() == KprApplication.ApplicationStatus.APPROVED)
                  .count();
      DeveloperStatsResponse.SubmissionVsApprovedItem item =
          new DeveloperStatsResponse.SubmissionVsApprovedItem();
      item.setMonth(capitalize(cur.format(fmt)));
      item.setSubmitted(submitted);
      item.setApproved(approved);
      out.add(item);
      cur = cur.plusMonths(1);
    }
    return out;
  }

  private List<DeveloperStatsResponse.ValueVsIncomeItem> buildValueVsIncome(
      List<KprApplication> apps, LocalDate start, LocalDate end) {
    Map<YearMonth, List<KprApplication>> byMonth =
        apps.stream()
            .filter(a -> a.getCreatedAt() != null)
            .collect(Collectors.groupingBy(a -> YearMonth.from(a.getCreatedAt())));

    List<DeveloperStatsResponse.ValueVsIncomeItem> out = new ArrayList<>();
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
      DeveloperStatsResponse.ValueVsIncomeItem item =
          new DeveloperStatsResponse.ValueVsIncomeItem();
      item.setMonth(capitalize(cur.format(fmt)));
      item.setSubmissionValue(submissionValue);
      item.setIncome(income);
      out.add(item);
      cur = cur.plusMonths(1);
    }
    return out;
  }

  private List<DeveloperStatsResponse.FunnelItem> buildFunnelItems(List<KprApplication> apps) {
    int appraisal =
        (int)
            apps.stream()
                .filter(a -> a.getStatus() == KprApplication.ApplicationStatus.PROPERTY_APPRAISAL)
                .count();
    int analysis =
        (int)
            apps.stream()
                .filter(a -> a.getStatus() == KprApplication.ApplicationStatus.CREDIT_ANALYSIS)
                .count();
    int finalApproval =
        (int)
            apps.stream()
                .filter(a -> a.getStatus() == KprApplication.ApplicationStatus.APPROVAL_PENDING)
                .count();
    int approvedCount =
        (int)
            apps.stream()
                .filter(a -> a.getStatus() == KprApplication.ApplicationStatus.APPROVED)
                .count();

    List<DeveloperStatsResponse.FunnelItem> out = new ArrayList<>();
    out.add(new DeveloperStatsResponse.FunnelItem("Property Appraisal", appraisal));
    out.add(new DeveloperStatsResponse.FunnelItem("Credit Analysis", analysis));
    out.add(new DeveloperStatsResponse.FunnelItem("Final Approval", finalApproval));
    out.add(new DeveloperStatsResponse.FunnelItem("Approved", approvedCount));
    return out;
  }

  private List<DeveloperStatsResponse.SLABucketItem> buildSlaBucketsFromApps(
      List<KprApplication> apps) {
    int bucket0to2 = 0, bucket3to5 = 0, bucketGt5 = 0;
    for (KprApplication a : apps) {
      if (a.getSubmittedAt() == null || a.getApprovedAt() == null) continue;
      long days = Duration.between(a.getSubmittedAt(), a.getApprovedAt()).toDays();
      if (days <= 2) bucket0to2++;
      else if (days <= 5) bucket3to5++;
      else bucketGt5++;
    }
    List<DeveloperStatsResponse.SLABucketItem> out = new ArrayList<>();
    out.add(new DeveloperStatsResponse.SLABucketItem("0-2 hari", bucket0to2));
    out.add(new DeveloperStatsResponse.SLABucketItem("3-5 hari", bucket3to5));
    out.add(new DeveloperStatsResponse.SLABucketItem(">5 hari", bucketGt5));
    return out;
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }
}
