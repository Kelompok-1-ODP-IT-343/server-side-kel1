package com.kelompoksatu.griya.dto;

import lombok.*;

/**
 * DTO for authentication response
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class AuthResponse {

    @NonNull
    private String token;

    private String type = "Bearer";

    @NonNull
    private String message;
}