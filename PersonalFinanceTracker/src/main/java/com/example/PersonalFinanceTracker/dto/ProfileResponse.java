package com.example.PersonalFinanceTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String avatar;

}