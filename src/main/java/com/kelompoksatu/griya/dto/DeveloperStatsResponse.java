package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import java.util.List;

public class DeveloperStatsResponse {

  private Summary summary;
  private List<FunnelItem> funnelStatus;
  private List<SLABucketItem> slaBucket;
  private List<SubmissionVsApprovedItem> submissionVsApproved;
  private List<ValueVsIncomeItem> valueVsIncome;

  public Summary getSummary() {
    return summary;
  }

  public void setSummary(Summary summary) {
    this.summary = summary;
  }

  public List<FunnelItem> getFunnelStatus() {
    return funnelStatus;
  }

  public void setFunnelStatus(List<FunnelItem> funnelStatus) {
    this.funnelStatus = funnelStatus;
  }

  public List<SLABucketItem> getSlaBucket() {
    return slaBucket;
  }

  public void setSlaBucket(List<SLABucketItem> slaBucket) {
    this.slaBucket = slaBucket;
  }

  public List<SubmissionVsApprovedItem> getSubmissionVsApproved() {
    return submissionVsApproved;
  }

  public void setSubmissionVsApproved(List<SubmissionVsApprovedItem> submissionVsApproved) {
    this.submissionVsApproved = submissionVsApproved;
  }

  public List<ValueVsIncomeItem> getValueVsIncome() {
    return valueVsIncome;
  }

  public void setValueVsIncome(List<ValueVsIncomeItem> valueVsIncome) {
    this.valueVsIncome = valueVsIncome;
  }

  public static class Summary {
    private int approvedCount;
    private int rejectedCount;
    private int pendingCount;
    private int activeCustomers;
    private Growth growth;

    public int getApprovedCount() {
      return approvedCount;
    }

    public void setApprovedCount(int approvedCount) {
      this.approvedCount = approvedCount;
    }

    public int getRejectedCount() {
      return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
      this.rejectedCount = rejectedCount;
    }

    public int getPendingCount() {
      return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
      this.pendingCount = pendingCount;
    }

    public int getActiveCustomers() {
      return activeCustomers;
    }

    public void setActiveCustomers(int activeCustomers) {
      this.activeCustomers = activeCustomers;
    }

    public Growth getGrowth() {
      return growth;
    }

    public void setGrowth(Growth growth) {
      this.growth = growth;
    }

    public static class Growth {
      private double approved;
      private double rejected;
      private double pending;
      private double customers;

      public double getApproved() {
        return approved;
      }

      public void setApproved(double approved) {
        this.approved = approved;
      }

      public double getRejected() {
        return rejected;
      }

      public void setRejected(double rejected) {
        this.rejected = rejected;
      }

      public double getPending() {
        return pending;
      }

      public void setPending(double pending) {
        this.pending = pending;
      }

      public double getCustomers() {
        return customers;
      }

      public void setCustomers(double customers) {
        this.customers = customers;
      }
    }
  }

  public static class FunnelItem {
    private String name;
    private int count;

    public FunnelItem() {}

    public FunnelItem(String name, int count) {
      this.name = name;
      this.count = count;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }
  }

  public static class SLABucketItem {
    private String label;
    private int count;

    public SLABucketItem() {}

    public SLABucketItem(String label, int count) {
      this.label = label;
      this.count = count;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }
  }

  public static class SubmissionVsApprovedItem {
    private String month;
    private int submitted;
    private int approved;

    public String getMonth() {
      return month;
    }

    public void setMonth(String month) {
      this.month = month;
    }

    public int getSubmitted() {
      return submitted;
    }

    public void setSubmitted(int submitted) {
      this.submitted = submitted;
    }

    public int getApproved() {
      return approved;
    }

    public void setApproved(int approved) {
      this.approved = approved;
    }
  }

  public static class ValueVsIncomeItem {
    private String month;
    private BigDecimal submissionValue;
    private BigDecimal income;

    public String getMonth() {
      return month;
    }

    public void setMonth(String month) {
      this.month = month;
    }

    public BigDecimal getSubmissionValue() {
      return submissionValue;
    }

    public void setSubmissionValue(BigDecimal submissionValue) {
      this.submissionValue = submissionValue;
    }

    public BigDecimal getIncome() {
      return income;
    }

    public void setIncome(BigDecimal income) {
      this.income = income;
    }
  }
}
