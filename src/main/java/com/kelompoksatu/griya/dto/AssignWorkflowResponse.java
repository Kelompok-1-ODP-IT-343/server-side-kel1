package com.kelompoksatu.griya.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class AssignWorkflowResponse {
  @NonNull private Integer applicationID;
  @NonNull private Integer firstApprovalId;
  @NonNull private Integer secondApprovalId;
}
