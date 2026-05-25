package com.example.reports.service;

import com.example.reports.client.AsistenciasClient;
import com.example.reports.client.AuthClient;
import com.example.reports.client.CongresoClient;
import com.example.reports.client.dto.AsistenciaDto;
import com.example.reports.client.dto.ConferenceDto;
import com.example.reports.client.dto.InstitutionDto;
import com.example.reports.client.dto.RegistrationDto;
import com.example.reports.client.dto.SystemConfigurationDto;
import com.example.reports.dto.CongressByInstitutionDto;
import com.example.reports.dto.EarningsByCongressDto;
import com.example.reports.dto.EarningsReportDto;
import com.example.reports.dto.ParticipantReportDto;
import com.example.reports.dto.WorkshopReservationReportDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private CongresoClient congresoClient;

    @Mock
    private AsistenciasClient asistenciasClient;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getEarningsReportShouldSumOnlyNonNullAmountsAndApplyCommission() {
        when(congresoClient.getAllRegistrations()).thenReturn(List.of(
                RegistrationDto.builder().id(1L).amountPaid(new BigDecimal("100.50")).build(),
                RegistrationDto.builder().id(2L).amountPaid(null).build(),
                RegistrationDto.builder().id(3L).amountPaid(new BigDecimal("49.50")).build()
        ));
        when(congresoClient.getAllConferences()).thenReturn(List.of(
                ConferenceDto.builder().id(10L).build(),
                ConferenceDto.builder().id(11L).build()
        ));
        // Fallback or explicit 10%
        when(authClient.getConfiguration(anyString())).thenReturn(
                SystemConfigurationDto.builder().configurationValue("10").build()
        );

        EarningsReportDto result = reportService.getEarningsReport();

        assertNotNull(result);
        // Total sum = 150.00. Commission = 10% of 150.00 = 15.00
        assertEquals(new BigDecimal("15.00"), result.getTotalEarnings());
        assertEquals(2, result.getTotalConferences());
        assertEquals(3, result.getTotalRegistrations());
    }

    @Test
    void getCongressByInstitutionShouldGroupAndCountActiveConferences() {
        when(congresoClient.getAllInstitutions()).thenReturn(List.of(
                InstitutionDto.builder().id(1L).name("Inst A").build(),
                InstitutionDto.builder().id(2L).name("Inst B").build()
        ));
        when(congresoClient.getAllConferences()).thenReturn(List.of(
                ConferenceDto.builder().id(100L).institutionId(1L).name("Conf 1").active(true).build(),
                ConferenceDto.builder().id(101L).institutionId(1L).name("Conf 2").active(false).build(),
                ConferenceDto.builder().id(102L).institutionId(2L).name("Conf 3").active(true).build()
        ));

        List<CongressByInstitutionDto> result = reportService.getCongressByInstitution();

        assertEquals(2, result.size());
        CongressByInstitutionDto instA = result.stream()
                .filter(i -> i.getInstitutionId().equals(1L))
                .findFirst()
                .orElseThrow();
        assertEquals(2, instA.getTotalConferences());
        assertEquals(1, instA.getActiveConferences());
    }

    @Test
    void getParticipantsShouldReturnFallbackNameWhenConferenceNotFound() {
        Long conferenceId = 77L;
        when(congresoClient.getAllConferences()).thenReturn(List.of());
        when(congresoClient.getAllRegistrations()).thenReturn(List.of(
                RegistrationDto.builder()
                        .conferenceId(conferenceId)
                        .userId(9L)
                        .amountPaid(new BigDecimal("25.00"))
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));

        ParticipantReportDto result = reportService.getParticipants(conferenceId);

        assertEquals("Congreso no encontrado", result.getConferenceName());
        assertEquals(1, result.getTotalParticipants());
        // 25.00 - 10% = 22.50
        assertEquals(new BigDecimal("22.50"), result.getTotalEarnings());
    }

    @Test
    void getWorkshopReservationsShouldGroupByParticipationTypeAndUseFallback() {
        Long activityId = 5L;
        when(asistenciasClient.getAttendanceByActivity(activityId)).thenReturn(List.of(
                AsistenciaDto.builder().nombreTipoParticipacion("Ponente").build(),
                AsistenciaDto.builder().nombreTipoParticipacion("Ponente").build(),
                AsistenciaDto.builder().nombreTipoParticipacion(null).build()
        ));

        WorkshopReservationReportDto result = reportService.getWorkshopReservations(activityId);

        assertEquals(activityId, result.getActivityId());
        assertEquals(3, result.getTotalReservations());
        assertEquals(2L, result.getReservationsByParticipationType().get("Ponente"));
        assertEquals(1L, result.getReservationsByParticipationType().get("Sin tipo"));
    }

    @Test
    void getEarningsByCongressShouldReturnZeroPriceWhenConferenceNotFound() {
        Long conferenceId = 999L;
        when(congresoClient.getAllConferences()).thenReturn(List.of());
        when(congresoClient.getAllRegistrations()).thenReturn(List.of(
                RegistrationDto.builder().conferenceId(conferenceId).amountPaid(new BigDecimal("10.00")).build(),
                RegistrationDto.builder().conferenceId(conferenceId).amountPaid(new BigDecimal("15.00")).build()
        ));

        EarningsByCongressDto result = reportService.getEarningsByCongress(conferenceId);

        assertEquals("Congreso no encontrado", result.getConferenceName());
        assertEquals(BigDecimal.ZERO, result.getConferencePrice());
        // Total 25.00 - 10% = 22.50
        assertEquals(new BigDecimal("22.50"), result.getTotalEarnings());
        assertEquals(2, result.getTotalRegistrations());
    }
}
