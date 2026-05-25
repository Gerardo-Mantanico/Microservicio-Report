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
                RegistrationDto.builder().id(1L).conferenceId(10L).amountPaid(new BigDecimal("100.00")).build(),
                RegistrationDto.builder().id(3L).conferenceId(10L).amountPaid(new BigDecimal("50.00")).build()
        ));
        when(congresoClient.getAllConferences()).thenReturn(List.of(
                ConferenceDto.builder().id(10L).institutionId(1L).name("Conf 10").build()
        ));
        when(congresoClient.getAllInstitutions()).thenReturn(List.of(
                InstitutionDto.builder().id(1L).name("Inst 1").build()
        ));
        when(authClient.getConfiguration(anyString())).thenReturn(
                SystemConfigurationDto.builder().configurationValue("10").build()
        );

        EarningsReportDto result = reportService.getEarningsReport();

        assertNotNull(result);
        // Total sum = 150.00. Commission = 15.00
        assertEquals(new BigDecimal("15.00"), result.getTotalEarnings());
        assertEquals(new BigDecimal("150.00"), result.getTotalGrossVolume());
        assertEquals(new BigDecimal("15.00"), result.getAverageEarningsPerCongress());
        assertEquals(1, result.getCongressEarnings().size());
        assertEquals(1, result.getInstitutionEarnings().size());
    }

    @Test
    void getCongressByInstitutionShouldGroupAndCountActiveConferences() {
        when(congresoClient.getAllInstitutions()).thenReturn(List.of(
                InstitutionDto.builder().id(1L).name("Inst A").build()
        ));
        when(congresoClient.getAllConferences()).thenReturn(List.of(
                ConferenceDto.builder().id(100L).institutionId(1L).name("Conf 1").active(true).build()
        ));

        List<CongressByInstitutionDto> result = reportService.getCongressByInstitution();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getTotalConferences());
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
                AsistenciaDto.builder().idUsuario(1L).nombreTipoParticipacion("Ponente").build()
        ));
        when(authClient.getUserById(1L)).thenReturn(UserResponse.builder().fullName("U1").build());

        WorkshopReservationReportDto result = reportService.getWorkshopReservations(activityId);

        assertEquals(activityId, result.getActivityId());
        assertEquals(1, result.getTotalReservations());
        assertEquals("U1", result.getReservations().get(0).getUserFullName());
    }

    @Test
    void getEarningsByCongressShouldIncludeKPIsAndTrend() {
        Long conferenceId = 999L;
        LocalDateTime now = LocalDateTime.now();
        when(congresoClient.getAllConferences()).thenReturn(List.of(
                ConferenceDto.builder().id(conferenceId).name("Test Conf").price(new BigDecimal("100")).build()
        ));
        when(congresoClient.getAllRegistrations()).thenReturn(List.of(
                RegistrationDto.builder().conferenceId(conferenceId).amountPaid(new BigDecimal("100")).registeredAt(now).build()
        ));
        when(authClient.getConfiguration(anyString())).thenReturn(
                SystemConfigurationDto.builder().configurationValue("10").build()
        );

        EarningsByCongressDto result = reportService.getEarningsByCongress(conferenceId);

        assertEquals("Test Conf", result.getConferenceName());
        assertEquals(new BigDecimal("10.00"), result.getTotalCommission());
        assertEquals(new BigDecimal("90.00"), result.getTotalNetEarnings());
        assertEquals(new BigDecimal("90.00"), result.getAverageNetEarningsPerRegistration());
        assertEquals(1, result.getRegistrationsByDate().size());
    }
}
