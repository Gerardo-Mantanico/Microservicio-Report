package com.example.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarningsReportDto {
    private BigDecimal totalEarnings;
    private long totalConferences;
    private long totalRegistrations;
}
