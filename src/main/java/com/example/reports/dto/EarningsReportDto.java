package com.example.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarningsReportDto {
    private BigDecimal totalEarnings;
    private long totalConferences;
    private long totalRegistrations;
    private List<CongressEarningsSummaryDto> congressEarnings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CongressEarningsSummaryDto {
        private Long conferenceId;
        private String conferenceName;
        private String institutionName;
        private long registrationCount;
        private BigDecimal grossEarnings;
        private BigDecimal netEarnings;
    }
}
