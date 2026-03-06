package com.example.PersonalFinanceTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(regexp = "^http|https://.*$", message = "Invalid avatar URL")
    private String avatar;
}
