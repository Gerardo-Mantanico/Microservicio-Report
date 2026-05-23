package com.example.reports.controller;

import com.example.reports.dto.AttendanceByActivityDto;
import com.example.reports.dto.CongressByInstitutionDto;
import com.example.reports.dto.EarningsByCongressDto;
import com.example.reports.dto.EarningsReportDto;
import com.example.reports.dto.ParticipantReportDto;
import com.example.reports.dto.WorkshopReservationReportDto;
import com.example.reports.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void getEarningsShouldReturnOkResponse() {
        EarningsReportDto dto = EarningsReportDto.builder()
                .totalEarnings(new BigDecimal("123.00"))
                .totalConferences(2)
                .totalRegistrations(3)
                .build();
        when(reportService.getEarningsReport()).thenReturn(dto);

        ResponseEntity<EarningsReportDto> response = reportController.getEarnings();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(reportService).getEarningsReport();
    }

    @Test
    void getCongressByInstitutionShouldReturnOkResponse() {
        List<CongressByInstitutionDto> dto = List.of(
                CongressByInstitutionDto.builder().institutionId(1L).institutionName("Inst").build()
        );
        when(reportService.getCongressByInstitution()).thenReturn(dto);

        ResponseEntity<List<CongressByInstitutionDto>> response = reportController.getCongressByInstitution();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(reportService).getCongressByInstitution();
    }

    @Test
    void getParticipantsShouldReturnOkResponse() {
        Long conferenceId = 7L;
        ParticipantReportDto dto = ParticipantReportDto.builder()
                .conferenceId(conferenceId)
                .conferenceName("Conf Test")
                .build();
        when(reportService.getParticipants(conferenceId)).thenReturn(dto);

        ResponseEntity<ParticipantReportDto> response = reportController.getParticipants(conferenceId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(reportService).getParticipants(conferenceId);
    }

    @Test
    void getAttendanceByActivityShouldReturnOkResponse() {
        Long activityId = 9L;
        AttendanceByActivityDto dto = AttendanceByActivityDto.builder()
                .activityId(activityId)
                .totalAttendances(0)
                .build();
        when(reportService.getAttendanceByActivity(activityId)).thenReturn(dto);

        ResponseEntity<AttendanceByActivityDto> response = reportController.getAttendanceByActivity(activityId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(reportService).getAttendanceByActivity(activityId);
    }

    @Test
    void getWorkshopReservationsShouldReturnOkResponse() {
        Long activityId = 5L;
        WorkshopReservationReportDto dto = WorkshopReservationReportDto.builder()
                .activityId(activityId)
                .totalReservations(1)
                .reservationsByParticipationType(Map.of("Ponente", 1L))
                .build();
        when(reportService.getWorkshopReservations(activityId)).thenReturn(dto);

        ResponseEntity<WorkshopReservationReportDto> response = reportController.getWorkshopReservations(activityId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalReservations());
        verify(reportService).getWorkshopReservations(activityId);
    }

    @Test
    void getEarningsByCongressShouldReturnOkResponse() {
        Long conferenceId = 12L;
        EarningsByCongressDto dto = EarningsByCongressDto.builder()
                .conferenceId(conferenceId)
                .conferenceName("Congreso 12")
                .build();
        when(reportService.getEarningsByCongress(conferenceId)).thenReturn(dto);

        ResponseEntity<EarningsByCongressDto> response = reportController.getEarningsByCongress(conferenceId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(reportService).getEarningsByCongress(conferenceId);
    }
}
