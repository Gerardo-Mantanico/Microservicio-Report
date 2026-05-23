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
public class EarningsByCongressDto {
    private Long conferenceId;
    private String conferenceName;
    private BigDecimal conferencePrice;
    private BigDecimal totalEarnings;
    private long totalRegistrations;
}
