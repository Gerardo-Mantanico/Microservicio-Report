package com.example.reports.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String organization;
    private String photoUrl;
    private String identificationNumber;
    private String role;
    private Long roleId;
    private Double walletBalance;
}
