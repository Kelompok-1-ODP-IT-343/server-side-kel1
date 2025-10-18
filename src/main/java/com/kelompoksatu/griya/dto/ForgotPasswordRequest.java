package com.kelompoksatu.griya.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotEmpty(message = "Email dibutuhkan")
    private String email;
}
