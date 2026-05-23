package com.example.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopReservationReportDto {
    private Long activityId;
    private long totalReservations;
    private Map<String, Long> reservationsByParticipationType;
}
