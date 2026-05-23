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
public class ParticipantReportDto {
    private Long conferenceId;
    private String conferenceName;
    private long totalParticipants;
    private BigDecimal totalEarnings;
    private List<ParticipantEntryDto> participants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantEntryDto {
        private Long userId;
        private BigDecimal amountPaid;
        private LocalDateTime registeredAt;
    }
}
