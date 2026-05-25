package com.example.reports.service;

import com.example.reports.client.AsistenciasClient;
import com.example.reports.client.AuthClient;
import com.example.reports.client.CongresoClient;
import com.example.reports.client.dto.AsistenciaDto;
import com.example.reports.client.dto.ConferenceDto;
import com.example.reports.client.dto.InstitutionDto;
import com.example.reports.client.dto.RegistrationDto;
import com.example.reports.client.dto.SystemConfigurationDto;
import com.example.reports.client.dto.UserResponse;
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
    void getEarningsReportShouldSumOnlyNonNullAmountsAndApplyCommissionAndBreakdown() {
        when(congresoClient.getAllRegistrations()).thenReturn(List.of(
                RegistrationDto.builder().id(1L).conferenceId(10L).amountPaid(new BigDecimal("100.50")).build(),
                RegistrationDto.builder().id(2L).conferenceId(11L).amountPaid(null).build(),
                RegistrationDto.builder().id(3L).conferenceId(10L).amountPaid(new BigDecimal("49.50")).build()
        ));
        when(congresoClient.getAllConferences()).thenReturn(List.of(
                ConferenceDto.builder().id(10L).institutionId(1L).name("Conf 10").build(),
                ConferenceDto.builder().id(11L).institutionId(1L).name("Conf 11").build()
        ));
        when(congresoClient.getAllInstitutions()).thenReturn(List.of(
                InstitutionDto.builder().id(1L).name("Inst 1").build()
        ));
        when(authClient.getConfiguration(anyString())).thenReturn(
                SystemConfigurationDto.builder().configurationValue("10").build()
        );

        EarningsReportDto result = reportService.getEarningsReport();

        assertNotNull(result);
        assertEquals(new BigDecimal("15.00"), result.getTotalEarnings());
        assertEquals(2, result.getTotalConferences());
        assertEquals(3, result.getTotalRegistrations());
        assertEquals(2, result.getCongressEarnings().size());
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
    void getParticipantsShouldReturnFallbackNameWhenConferenceNotFoundAndIncludeNames() {
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
        when(authClient.getUserById(9L)).thenReturn(
                UserResponse.builder().id(9L).fullName("Test User").email("test@mail.com").build()
        );
        when(authClient.getConfiguration(anyString())).thenReturn(
                SystemConfigurationDto.builder().configurationValue("10").build()
        );

        ParticipantReportDto result = reportService.getParticipants(conferenceId);

        assertEquals("Congreso no encontrado", result.getConferenceName());
        assertEquals(1, result.getTotalParticipants());
        assertEquals(new BigDecimal("22.50"), result.getTotalEarnings());
        assertEquals("Test User", result.getParticipants().get(0).getFullName());
    }

    @Test
    void getWorkshopReservationsShouldGroupByParticipationTypeAndUseFallback() {
        Long activityId = 5L;
        when(asistenciasClient.getAttendanceByActivity(activityId)).thenReturn(List.of(
                AsistenciaDto.builder().idUsuario(1L).nombreTipoParticipacion("Ponente").build(),
                AsistenciaDto.builder().idUsuario(2L).nombreTipoParticipacion("Ponente").build(),
                AsistenciaDto.builder().idUsuario(3L).nombreTipoParticipacion(null).build()
        ));
        when(authClient.getUserById(1L)).thenReturn(UserResponse.builder().fullName("U1").build());
        when(authClient.getUserById(2L)).thenReturn(UserResponse.builder().fullName("U2").build());
        when(authClient.getUserById(3L)).thenReturn(UserResponse.builder().fullName("U3").build());

        WorkshopReservationReportDto result = reportService.getWorkshopReservations(activityId);

        assertEquals(activityId, result.getActivityId());
        assertEquals(3, result.getTotalReservations());
        assertEquals(2L, result.getReservationsByParticipationType().get("Ponente"));
        assertEquals(1L, result.getReservationsByParticipationType().get("Sin tipo"));
        assertEquals(3, result.getReservations().size());
        assertEquals("U1", result.getReservations().get(0).getUserFullName());
    }

    @Test
    void getEarningsByCongressShouldReturnZeroPriceWhenConferenceNotFound() {
        Long conferenceId = 999L;
        when(congresoClient.getAllConferences()).thenReturn(List.of());
        when(congresoClient.getAllRegistrations()).thenReturn(List.of(
                RegistrationDto.builder().conferenceId(conferenceId).amountPaid(new BigDecimal("10.00")).build(),
                RegistrationDto.builder().conferenceId(conferenceId).amountPaid(new BigDecimal("15.00")).build()
        ));
        when(authClient.getConfiguration(anyString())).thenReturn(
                SystemConfigurationDto.builder().configurationValue("10").build()
        );

        EarningsByCongressDto result = reportService.getEarningsByCongress(conferenceId);

        assertEquals("Congreso no encontrado", result.getConferenceName());
        assertEquals(BigDecimal.ZERO, result.getConferencePrice());
        assertEquals(new BigDecimal("25.00"), result.getTotalGrossEarnings());
        assertEquals(new BigDecimal("2.50"), result.getTotalCommission());
        assertEquals(new BigDecimal("22.50"), result.getTotalNetEarnings());
        assertEquals(2, result.getTotalRegistrations());
        assertEquals(2, result.getRegistrationDetails().size());
    }
}
