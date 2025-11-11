package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.AdminStatsResponse;
import com.kelompoksatu.griya.dto.AdminStatsResponse.GrowthAndDemandItem;
import com.kelompoksatu.griya.dto.AdminStatsResponse.OutstandingLoan;
import com.kelompoksatu.griya.dto.AdminStatsResponse.OutstandingLoanItem;
import com.kelompoksatu.griya.dto.AdminStatsResponse.ProcessingFunnelItem;
import com.kelompoksatu.griya.dto.AdminStatsResponse.Summary;
import com.kelompoksatu.griya.dto.AdminStatsResponse.UserRegistered;
import com.kelompoksatu.griya.dto.AdminStatsResponse.UserRegisteredItem;
import com.kelompoksatu.griya.entity.KprApplication;
import com.kelompoksatu.griya.entity.KprApplication.ApplicationStatus;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.repository.StatAdminRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class StatAdminService {

  private final StatAdminRepository repo;

  public AdminStatsResponse getDashboard(String rangeStr) {
    Range range = parseRange(rangeStr);
    LocalDateTime end = LocalDate.now().atStartOfDay().plusDays(1);
    LocalDateTime start = computeStart(range, end);

    long approved = repo.countApprovedBetween(start, end);
    long rejected = repo.countRejectedBetween(start, end);
    long pending = repo.countPendingBetween(start, end);
    long customers = repo.countDistinctCustomersBetween(start, end);

    LocalDateTime prevEnd = start;
    LocalDateTime prevStart = computePreviousStart(range, start);
    long prevApproved = repo.countApprovedBetween(prevStart, prevEnd);
    long prevRejected = repo.countRejectedBetween(prevStart, prevEnd);
    long prevPending = repo.countPendingBetween(prevStart, prevEnd);
    long prevCustomers = repo.countDistinctCustomersBetween(prevStart, prevEnd);

    Summary summary = new Summary();
    Summary.SummaryItem approvedItem = new Summary.SummaryItem();
    approvedItem.setTotal((int) approved);
    approvedItem.setChangePercentage(percentChange(approved, prevApproved));
    Summary.SummaryItem rejectedItem = new Summary.SummaryItem();
    rejectedItem.setTotal((int) rejected);
    rejectedItem.setChangePercentage(percentChange(rejected, prevRejected));
    Summary.SummaryItem pendingItem = new Summary.SummaryItem();
    pendingItem.setTotal((int) pending);
    pendingItem.setChangePercentage(percentChange(pending, prevPending));
    Summary.CustomersItem customersItem = new Summary.CustomersItem();
    customersItem.setTotal((int) customers);
    customersItem.setUnit("nasabah");
    customersItem.setChangePercentage(percentChange(customers, prevCustomers));
    summary.setApproved(approvedItem);
    summary.setRejected(rejectedItem);
    summary.setPending(pendingItem);
    summary.setCustomers(customersItem);

    // Load entities for aggregation
    List<KprApplication> apps = repo.findApplicationsBetween(start, end);
    List<User> users = repo.findUsersCreatedBetween(start, end);

    // Monthly labels
    LocalDate startDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();

    List<GrowthAndDemandItem> growth = buildGrowthAndDemand(apps, startDate, endDate);
    OutstandingLoan ol = buildOutstandingLoan(apps, startDate, endDate);
    List<ProcessingFunnelItem> funnel = buildProcessingFunnel(apps);
    UserRegistered ur = buildUserRegistered(users, startDate, endDate);

    AdminStatsResponse resp = new AdminStatsResponse();
    resp.setRange(rangeStr);
    resp.setSummary(summary);
    resp.setGrowthAndDemand(growth);
    resp.setOutstandingLoan(ol);
    resp.setProcessingFunnel(funnel);
    resp.setUserRegistered(ur);
    return resp;
  }

  // =========================
  // Helpers
  // =========================

  public enum Range {
    SEVEN_DAYS,
    THIRTY_DAYS,
    NINETY_DAYS,
    YTD
  }

  private Range parseRange(String s) {
    if (s == null) return Range.THIRTY_DAYS;
    String t = s.trim().toLowerCase(Locale.ROOT);
    switch (t) {
      case "7d":
      case "7":
        return Range.SEVEN_DAYS;
      case "30d":
      case "30":
        return Range.THIRTY_DAYS;
      case "90d":
      case "90":
        return Range.NINETY_DAYS;
      case "ytd":
        return Range.YTD;
      default:
        return Range.THIRTY_DAYS;
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
        return end.minusDays(30);
    }
  }

  private LocalDateTime computePreviousStart(Range range, LocalDateTime start) {
    switch (range) {
      case SEVEN_DAYS:
        return start.minusDays(7);
      case THIRTY_DAYS:
        return start.minusDays(30);
      case NINETY_DAYS:
        return start.minusDays(90);
      case YTD:
        return start.minusYears(1);
      default:
        return start.minusDays(30);
    }
  }

  private double percentChange(long current, long previous) {
    if (previous == 0) return current > 0 ? 100.0 : 0.0;
    return ((double) (current - previous) / (double) previous) * 100.0;
  }

  private List<GrowthAndDemandItem> buildGrowthAndDemand(
      List<KprApplication> apps, LocalDate start, LocalDate end) {
    Map<YearMonth, List<KprApplication>> byMonth =
        apps.stream().collect(Collectors.groupingBy(a -> YearMonth.from(a.getCreatedAt())));

    List<GrowthAndDemandItem> out = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL", new Locale("id", "ID"));
    YearMonth cur = YearMonth.from(start);
    YearMonth last = YearMonth.from(end.minusDays(1));
    while (!cur.isAfter(last)) {
      final YearMonth curMonth = cur;
      List<KprApplication> monthApps = byMonth.getOrDefault(curMonth, List.of());
      int approvals =
          (int)
              monthApps.stream()
                  .filter(
                      a -> {
                        if (a.getApprovedAt() != null) {
                          return YearMonth.from(a.getApprovedAt()).equals(curMonth);
                        }
                        return a.getStatus() == ApplicationStatus.APPROVED;
                      })
                  .count();
      int rejects =
          (int)
              monthApps.stream()
                  .filter(
                      a -> {
                        if (a.getRejectedAt() != null) {
                          return YearMonth.from(a.getRejectedAt()).equals(curMonth);
                        }
                        return a.getStatus() == ApplicationStatus.REJECTED;
                      })
                  .count();
      GrowthAndDemandItem item = new GrowthAndDemandItem();
      item.setMonth(capitalize(curMonth.format(fmt)));
      item.setApproval(approvals);
      item.setReject(rejects);
      out.add(item);
      cur = cur.plusMonths(1);
    }
    return out;
  }

  private OutstandingLoan buildOutstandingLoan(
      List<KprApplication> apps, LocalDate start, LocalDate end) {
    Map<YearMonth, List<KprApplication>> byMonth =
        apps.stream().collect(Collectors.groupingBy(a -> YearMonth.from(a.getCreatedAt())));

    List<OutstandingLoanItem> data = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL", new Locale("id", "ID"));
    YearMonth cur = YearMonth.from(start);
    YearMonth last = YearMonth.from(end.minusDays(1));
    BigDecimal cumulative = BigDecimal.ZERO;
    while (!cur.isAfter(last)) {
      final YearMonth curMonth = cur;
      List<KprApplication> monthApps = byMonth.getOrDefault(curMonth, List.of());
      BigDecimal approvedValue =
          monthApps.stream()
              .filter(
                  a -> {
                    if (a.getApprovedAt() != null) {
                      return YearMonth.from(a.getApprovedAt()).equals(curMonth);
                    }
                    return a.getStatus() == ApplicationStatus.APPROVED;
                  })
              .map(KprApplication::getLoanAmount)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      cumulative = cumulative.add(approvedValue);
      OutstandingLoanItem item = new OutstandingLoanItem();
      item.setMonth(capitalize(curMonth.format(fmt)));
      // convert to billions (miliar rupiah) with 2 decimals
      item.setValue(cumulative.divide(new BigDecimal(1_000_000_000L), 2, RoundingMode.HALF_UP));
      data.add(item);
      cur = cur.plusMonths(1);
    }

    OutstandingLoan ol = new OutstandingLoan();
    ol.setUnit("miliar_rupiah");
    ol.setData(data);
    return ol;
  }

  private List<ProcessingFunnelItem> buildProcessingFunnel(List<KprApplication> apps) {
    int draft =
        (int) apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED).count();
    int review =
        (int)
            apps.stream()
                .filter(
                    a ->
                        a.getStatus() == ApplicationStatus.DOCUMENT_VERIFICATION
                            || a.getStatus() == ApplicationStatus.PROPERTY_APPRAISAL)
                .count();
    int approval =
        (int)
            apps.stream()
                .filter(
                    a ->
                        a.getStatus() == ApplicationStatus.CREDIT_ANALYSIS
                            || a.getStatus() == ApplicationStatus.APPROVAL_PENDING
                            || a.getStatus() == ApplicationStatus.APPROVED)
                .count();
    int reject =
        (int) apps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

    List<ProcessingFunnelItem> out = new ArrayList<>();
    ProcessingFunnelItem i1 = new ProcessingFunnelItem();
    i1.setStage("Draft");
    i1.setCount(draft);
    out.add(i1);
    ProcessingFunnelItem i2 = new ProcessingFunnelItem();
    i2.setStage("Review");
    i2.setCount(review);
    out.add(i2);
    ProcessingFunnelItem i3 = new ProcessingFunnelItem();
    i3.setStage("Approval");
    i3.setCount(approval);
    out.add(i3);
    ProcessingFunnelItem i4 = new ProcessingFunnelItem();
    i4.setStage("Reject");
    i4.setCount(reject);
    out.add(i4);
    return out;
  }

  private UserRegistered buildUserRegistered(List<User> users, LocalDate start, LocalDate end) {
    Map<YearMonth, List<User>> byMonth =
        users.stream()
            .filter(u -> u.getCreatedAt() != null)
            .collect(Collectors.groupingBy(u -> YearMonth.from(u.getCreatedAt())));

    List<UserRegisteredItem> data = new ArrayList<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("LLL", new Locale("id", "ID"));
    YearMonth cur = YearMonth.from(start);
    YearMonth last = YearMonth.from(end.minusDays(1));
    while (!cur.isAfter(last)) {
      int count = byMonth.getOrDefault(cur, List.of()).size();
      UserRegisteredItem item = new UserRegisteredItem();
      item.setMonth(capitalize(cur.format(fmt)));
      item.setCount(count);
      data.add(item);
      cur = cur.plusMonths(1);
    }
    UserRegistered ur = new UserRegistered();
    ur.setUnit("users");
    ur.setData(data);
    return ur;
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
  }
}
