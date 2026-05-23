package com.example.reports.dto;

import com.example.reports.client.dto.AsistenciaDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceByActivityDto {
    private Long activityId;
    private long totalAttendances;
    private List<AsistenciaDto> attendances;
}
