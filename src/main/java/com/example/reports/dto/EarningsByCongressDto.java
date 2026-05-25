package com.example.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarningsByCongressDto {
    private Long conferenceId;
    private String conferenceName;
    private BigDecimal conferencePrice;
    
    private BigDecimal totalGrossEarnings;
    private BigDecimal totalCommission;
    private BigDecimal totalNetEarnings;
    
    private long totalRegistrations;
    private List<RegistrationDetailDto> registrationDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistrationDetailDto {
        private Long userId;
        private BigDecimal amountPaid;
        private BigDecimal commissionDeducted;
        private BigDecimal netEarnings;
        private LocalDateTime registeredAt;
    }
}
