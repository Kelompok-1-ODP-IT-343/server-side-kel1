package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import java.util.List;

/** Admin dashboard response DTO matching requested JSON schema. */
public class AdminStatsResponse {

  private String range;
  private Summary summary;
  private List<GrowthAndDemandItem> growthAndDemand;
  private OutstandingLoan outstandingLoan;
  private List<ProcessingFunnelItem> processingFunnel;
  private UserRegistered userRegistered;

  public String getRange() {
    return range;
  }

  public void setRange(String range) {
    this.range = range;
  }

  public Summary getSummary() {
    return summary;
  }

  public void setSummary(Summary summary) {
    this.summary = summary;
  }

  public List<GrowthAndDemandItem> getGrowthAndDemand() {
    return growthAndDemand;
  }

  public void setGrowthAndDemand(List<GrowthAndDemandItem> growthAndDemand) {
    this.growthAndDemand = growthAndDemand;
  }

  public OutstandingLoan getOutstandingLoan() {
    return outstandingLoan;
  }

  public void setOutstandingLoan(OutstandingLoan outstandingLoan) {
    this.outstandingLoan = outstandingLoan;
  }

  public List<ProcessingFunnelItem> getProcessingFunnel() {
    return processingFunnel;
  }

  public void setProcessingFunnel(List<ProcessingFunnelItem> processingFunnel) {
    this.processingFunnel = processingFunnel;
  }

  public UserRegistered getUserRegistered() {
    return userRegistered;
  }

  public void setUserRegistered(UserRegistered userRegistered) {
    this.userRegistered = userRegistered;
  }

  // =========================
  // Nested DTOs
  // =========================

  public static class Summary {
    private SummaryItem approved;
    private SummaryItem rejected;
    private SummaryItem pending;
    private CustomersItem customers;

    public SummaryItem getApproved() {
      return approved;
    }

    public void setApproved(SummaryItem approved) {
      this.approved = approved;
    }

    public SummaryItem getRejected() {
      return rejected;
    }

    public void setRejected(SummaryItem rejected) {
      this.rejected = rejected;
    }

    public SummaryItem getPending() {
      return pending;
    }

    public void setPending(SummaryItem pending) {
      this.pending = pending;
    }

    public CustomersItem getCustomers() {
      return customers;
    }

    public void setCustomers(CustomersItem customers) {
      this.customers = customers;
    }

    public static class SummaryItem {
      private int total;
      private double changePercentage;

      public int getTotal() {
        return total;
      }

      public void setTotal(int total) {
        this.total = total;
      }

      public double getChangePercentage() {
        return changePercentage;
      }

      public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
      }
    }

    public static class CustomersItem extends SummaryItem {
      private String unit;

      public String getUnit() {
        return unit;
      }

      public void setUnit(String unit) {
        this.unit = unit;
      }
    }
  }

  public static class GrowthAndDemandItem {
    private String month;
    private int approval;
    private int reject;

    public String getMonth() {
      return month;
    }

    public void setMonth(String month) {
      this.month = month;
    }

    public int getApproval() {
      return approval;
    }

    public void setApproval(int approval) {
      this.approval = approval;
    }

    public int getReject() {
      return reject;
    }

    public void setReject(int reject) {
      this.reject = reject;
    }
  }

  public static class OutstandingLoan {
    private String unit;
    private List<OutstandingLoanItem> data;

    public String getUnit() {
      return unit;
    }

    public void setUnit(String unit) {
      this.unit = unit;
    }

    public List<OutstandingLoanItem> getData() {
      return data;
    }

    public void setData(List<OutstandingLoanItem> data) {
      this.data = data;
    }
  }

  public static class OutstandingLoanItem {
    private String month;
    private BigDecimal value;

    public String getMonth() {
      return month;
    }

    public void setMonth(String month) {
      this.month = month;
    }

    public BigDecimal getValue() {
      return value;
    }

    public void setValue(BigDecimal value) {
      this.value = value;
    }
  }

  public static class ProcessingFunnelItem {
    private String stage;
    private int count;

    public String getStage() {
      return stage;
    }

    public void setStage(String stage) {
      this.stage = stage;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }
  }

  public static class UserRegistered {
    private String unit;
    private List<UserRegisteredItem> data;

    public String getUnit() {
      return unit;
    }

    public void setUnit(String unit) {
      this.unit = unit;
    }

    public List<UserRegisteredItem> getData() {
      return data;
    }

    public void setData(List<UserRegisteredItem> data) {
      this.data = data;
    }
  }

  public static class UserRegisteredItem {
    private String month;
    private int count;

    public String getMonth() {
      return month;
    }

    public void setMonth(String month) {
      this.month = month;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }
  }
}
