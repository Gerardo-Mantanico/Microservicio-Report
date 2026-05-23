package com.example.reports.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDto {
    private Long id;
    private Long conferenceId;
    private Long userId;
    private BigDecimal amountPaid;
    private LocalDateTime registeredAt;
}
