package com.example.reports.controller;

import com.example.reports.dto.*;
import com.example.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reportes del sistema de congresos")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/earnings")
    @Operation(summary = "Ganancias totales del sistema", description = "Total de ganancias de todos los congresos. Requiere rol ADMIN_SISTEMA.")
    public ResponseEntity<EarningsReportDto> getEarnings() {
        return ResponseEntity.ok(reportService.getEarningsReport());
    }

    @GetMapping("/congress-by-institution")
    @Operation(summary = "Congresos agrupados por institución", description = "Lista congresos por institución. Requiere rol ADMIN_SISTEMA.")
    public ResponseEntity<List<CongressByInstitutionDto>> getCongressByInstitution() {
        return ResponseEntity.ok(reportService.getCongressByInstitution());
    }

    @GetMapping("/participants")
    @Operation(summary = "Participantes de un congreso", description = "Lista de participantes registrados. Requiere rol ADMIN_CONGRESO.")
    public ResponseEntity<ParticipantReportDto> getParticipants(@RequestParam Long conferenceId) {
        return ResponseEntity.ok(reportService.getParticipants(conferenceId));
    }

    @GetMapping("/attendance-by-activity")
    @Operation(summary = "Asistencias por actividad", description = "Asistencias registradas para una actividad. Requiere rol ADMIN_CONGRESO.")
    public ResponseEntity<AttendanceByActivityDto> getAttendanceByActivity(@RequestParam Long activityId) {
        return ResponseEntity.ok(reportService.getAttendanceByActivity(activityId));
    }

    @GetMapping("/workshop-reservations")
    @Operation(summary = "Reservas de taller", description = "Reservas registradas para una actividad tipo taller. Requiere rol ADMIN_CONGRESO.")
    public ResponseEntity<WorkshopReservationReportDto> getWorkshopReservations(@RequestParam Long activityId) {
        return ResponseEntity.ok(reportService.getWorkshopReservations(activityId));
    }

    @GetMapping("/earnings-by-congress")
    @Operation(summary = "Ganancias por congreso", description = "Total de ganancias de un congreso específico. Requiere rol ADMIN_CONGRESO.")
    public ResponseEntity<EarningsByCongressDto> getEarningsByCongress(@RequestParam Long conferenceId) {
        return ResponseEntity.ok(reportService.getEarningsByCongress(conferenceId));
    }
}
