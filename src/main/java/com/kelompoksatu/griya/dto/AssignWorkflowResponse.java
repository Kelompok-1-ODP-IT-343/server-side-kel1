package com.kelompoksatu.griya.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@RequiredArgsConstructor
@Data
public class AssignWorkflowResponse {
  @NonNull private Integer id;
  @NonNull private Integer applicationID;
  @NonNull private Integer firstApprovalId;
  @NonNull private Integer secondApprovalId;
}
