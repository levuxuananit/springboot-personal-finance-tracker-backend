package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {
    private Long useId;
    private String fullName;
    private String email;
    private String avatar;
}
