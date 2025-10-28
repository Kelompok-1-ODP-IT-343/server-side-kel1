package com.kelompoksatu.griya.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@RequiredArgsConstructor
@Data
public class KPRApplicant {
  // nama email phone
  @NonNull private String name;
  @NonNull private String email;
  @NonNull private String phone;
  @NonNull private String KprApplicationCode;
}
