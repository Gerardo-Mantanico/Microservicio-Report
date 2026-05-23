package com.example.reports.client;

import com.example.reports.client.dto.AsistenciaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsistenciasClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.asistencias-url}")
    private String asistenciasUrl;

    public List<AsistenciaDto> getAttendanceByActivity(Long activityId) {
        try {
            AsistenciaDto[] result = restTemplate.getForObject(
                    asistenciasUrl + "/api/v1/asistencias/actividad/" + activityId,
                    AsistenciaDto[].class);
            return result != null ? Arrays.asList(result) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
