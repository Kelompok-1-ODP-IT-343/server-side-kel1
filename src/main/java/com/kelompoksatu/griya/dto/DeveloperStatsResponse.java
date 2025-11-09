package com.kelompoksatu.griya.dto;

import java.time.Instant;
import java.util.List;

public class DeveloperStatsResponse {

  private String timestamp;
  private Kpi kpi;
  private List<GrowthAndDemandEntry> growthAndDemand;
  private List<OutstandingLoanEntry> outstandingLoan;
  private List<FunnelEntry> processingFunnel;
  private List<UserRegisteredEntry> userRegistered;

  public static DeveloperStatsResponse ofNow(
      Kpi kpi,
      List<GrowthAndDemandEntry> growthAndDemand,
      List<OutstandingLoanEntry> outstandingLoan,
      List<FunnelEntry> processingFunnel,
      List<UserRegisteredEntry> userRegistered) {
    DeveloperStatsResponse res = new DeveloperStatsResponse();
    res.timestamp = Instant.now().toString();
    res.kpi = kpi;
    res.growthAndDemand = growthAndDemand;
    res.outstandingLoan = outstandingLoan;
    res.processingFunnel = processingFunnel;
    res.userRegistered = userRegistered;
    return res;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public Kpi getKpi() {
    return kpi;
  }

  public List<GrowthAndDemandEntry> getGrowthAndDemand() {
    return growthAndDemand;
  }

  public List<OutstandingLoanEntry> getOutstandingLoan() {
    return outstandingLoan;
  }

  public List<FunnelEntry> getProcessingFunnel() {
    return processingFunnel;
  }

  public List<UserRegisteredEntry> getUserRegistered() {
    return userRegistered;
  }

  // Nested DTOs
  public static class Kpi {
    private KpiItem approved;
    private KpiItem rejected;
    private KpiItem pending;
    private KpiItem customers;

    public Kpi(KpiItem approved, KpiItem rejected, KpiItem pending, KpiItem customers) {
      this.approved = approved;
      this.rejected = rejected;
      this.pending = pending;
      this.customers = customers;
    }

    public KpiItem getApproved() {
      return approved;
    }

    public KpiItem getRejected() {
      return rejected;
    }

    public KpiItem getPending() {
      return pending;
    }

    public KpiItem getCustomers() {
      return customers;
    }

    public static class KpiItem {
      private long value;
      private double percentage_change;

      public KpiItem(long value, double percentage_change) {
        this.value = value;
        this.percentage_change = percentage_change;
      }

      public long getValue() {
        return value;
      }

      public double getPercentage_change() {
        return percentage_change;
      }
    }
  }

  public static class GrowthAndDemandEntry {
    private String month;
    private long total_requests;
    private long total_approved;

    public GrowthAndDemandEntry(String month, long total_requests, long total_approved) {
      this.month = month;
      this.total_requests = total_requests;
      this.total_approved = total_approved;
    }

    public String getMonth() {
      return month;
    }

    public long getTotal_requests() {
      return total_requests;
    }

    public long getTotal_approved() {
      return total_approved;
    }
  }

  public static class OutstandingLoanEntry {
    private String month;
    private double amount_miliar;

    public OutstandingLoanEntry(String month, double amount_miliar) {
      this.month = month;
      this.amount_miliar = amount_miliar;
    }

    public String getMonth() {
      return month;
    }

    public double getAmount_miliar() {
      return amount_miliar;
    }
  }

  public static class FunnelEntry {
    private String stage;
    private long count;

    public FunnelEntry(String stage, long count) {
      this.stage = stage;
      this.count = count;
    }

    public String getStage() {
      return stage;
    }

    public long getCount() {
      return count;
    }
  }

  public static class UserRegisteredEntry {
    private String month;
    private long count;

    public UserRegisteredEntry(String month, long count) {
      this.month = month;
      this.count = count;
    }

    public String getMonth() {
      return month;
    }

    public long getCount() {
      return count;
    }
  }
}
