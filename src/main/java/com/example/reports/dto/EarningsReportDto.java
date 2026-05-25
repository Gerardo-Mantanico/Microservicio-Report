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
    private BigDecimal totalEarnings; // This is the total system commission
    private BigDecimal totalGrossVolume; // Total amount paid across all congresses
    private BigDecimal averageEarningsPerCongress;
    
    private long totalConferences;
    private long totalRegistrations;
    
    private List<CongressEarningsSummaryDto> congressEarnings;
    private List<InstitutionEarningsSummaryDto> institutionEarnings;

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
        private BigDecimal netEarnings; // System commission for this congress
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstitutionEarningsSummaryDto {
        private Long institutionId;
        private String institutionName;
        private long totalCongresses;
        private long totalRegistrations;
        private BigDecimal totalGrossEarnings;
        private BigDecimal totalSystemCommission;
    }
}
