package com.example.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private BigDecimal averageNetEarningsPerRegistration;
    
    private long totalRegistrations;
    private Map<String, Long> registrationsByDate;
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
