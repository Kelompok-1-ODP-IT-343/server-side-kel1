package com.kelompoksatu.griya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for approval confirmation Contains information about the approval status, user ID, and
 * application ID
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalConfirmation {
  private Boolean isApproved;
  private String reason;
}
