package com.kelompoksatu.griya.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class AssignWorkflowRequest {
  @NonNull private Integer applicationID;
  @NonNull private Integer firstApprovalId;
  @NonNull private Integer secondApprovalId;
}
