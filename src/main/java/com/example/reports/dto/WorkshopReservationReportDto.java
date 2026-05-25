package com.example.reports.dto;

import com.example.reports.client.dto.AsistenciaDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopReservationReportDto {
    private Long activityId;
    private long totalReservations;
    private Map<String, Long> reservationsByParticipationType;
    private List<AsistenciaDto> reservations;
}
