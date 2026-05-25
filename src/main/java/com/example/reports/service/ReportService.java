package com.example.reports.service;

import com.example.reports.client.AsistenciasClient;
import com.example.reports.client.AuthClient;
import com.example.reports.client.CongresoClient;
import com.example.reports.client.dto.AsistenciaDto;
import com.example.reports.client.dto.ConferenceDto;
import com.example.reports.client.dto.InstitutionDto;
import com.example.reports.client.dto.RegistrationDto;
import com.example.reports.client.dto.UserResponse;
import com.example.reports.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final CongresoClient congresoClient;
    private final AsistenciasClient asistenciasClient;
    private final AuthClient authClient;

    private BigDecimal getCommissionPercentage() {
        try {
            var config = authClient.getConfiguration("PORCENTAJE_COMISION");
            if (config != null && config.getConfigurationValue() != null) {
                return new BigDecimal(config.getConfigurationValue()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            // ignore
        }
        return new BigDecimal("0.10"); // fallback to 10%
    }

    public EarningsReportDto getEarningsReport() {
        List<RegistrationDto> registrations = congresoClient.getAllRegistrations();
        List<ConferenceDto> conferences = congresoClient.getAllConferences();
        List<InstitutionDto> institutions = congresoClient.getAllInstitutions();

        BigDecimal totalAmountPaid = registrations.stream()
                .map(RegistrationDto::getAmountPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionRate = getCommissionPercentage();
        BigDecimal systemEarnings = totalAmountPaid.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

        Map<Long, InstitutionDto> instMap = institutions.stream()
                .collect(Collectors.toMap(InstitutionDto::getId, i -> i, (a, b) -> a));

        List<EarningsReportDto.CongressEarningsSummaryDto> breakdown = conferences.stream()
                .map(c -> {
                    List<RegistrationDto> confRegs = registrations.stream()
                            .filter(r -> c.getId().equals(r.getConferenceId()))
                            .toList();
                    BigDecimal gross = confRegs.stream()
                            .map(RegistrationDto::getAmountPaid)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal net = gross.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
                    InstitutionDto inst = instMap.get(c.getInstitutionId());
                    return EarningsReportDto.CongressEarningsSummaryDto.builder()
                            .conferenceId(c.getId())
                            .conferenceName(c.getName())
                            .institutionName(inst != null ? inst.getName() : "Desconocida")
                            .registrationCount(confRegs.size())
                            .grossEarnings(gross)
                            .netEarnings(net)
                            .build();
                })
                .sorted(Comparator.comparing(EarningsReportDto.CongressEarningsSummaryDto::getNetEarnings).reversed())
                .collect(Collectors.toList());

        return EarningsReportDto.builder()
                .totalEarnings(systemEarnings)
                .totalConferences(conferences.size())
                .totalRegistrations(registrations.size())
                .congressEarnings(breakdown)
                .build();
    }

    public List<CongressByInstitutionDto> getCongressByInstitution() {
        List<InstitutionDto> institutions = congresoClient.getAllInstitutions();
        List<ConferenceDto> conferences = congresoClient.getAllConferences();

        Map<Long, List<ConferenceDto>> byInstitution = conferences.stream()
                .filter(c -> c.getInstitutionId() != null)
                .collect(Collectors.groupingBy(ConferenceDto::getInstitutionId));

        return institutions.stream()
                .map(inst -> {
                    List<ConferenceDto> instConfs = byInstitution.getOrDefault(inst.getId(), List.of());
                    long activeCount = instConfs.stream()
                            .filter(c -> Boolean.TRUE.equals(c.getActive()))
                            .count();
                    return CongressByInstitutionDto.builder()
                            .institutionId(inst.getId())
                            .institutionName(inst.getName())
                            .totalConferences(instConfs.size())
                            .activeConferences(activeCount)
                            .conferences(instConfs)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public ParticipantReportDto getParticipants(Long conferenceId) {
        List<RegistrationDto> registrations = congresoClient.getAllRegistrations();
        List<ConferenceDto> conferences = congresoClient.getAllConferences();

        ConferenceDto conference = conferences.stream()
                .filter(c -> conferenceId.equals(c.getId()))
                .findFirst()
                .orElse(null);

        List<RegistrationDto> conferenceRegs = registrations.stream()
                .filter(r -> conferenceId.equals(r.getConferenceId()))
                .collect(Collectors.toList());

        BigDecimal totalAmountPaid = conferenceRegs.stream()
                .map(RegistrationDto::getAmountPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commission = getCommissionPercentage();
        BigDecimal congressEarnings = totalAmountPaid.subtract(totalAmountPaid.multiply(commission)).setScale(2, RoundingMode.HALF_UP);

        Map<Long, UserResponse> userCache = new HashMap<>();

        List<ParticipantReportDto.ParticipantEntryDto> participants = conferenceRegs.stream()
                .map(r -> {
                    UserResponse user = userCache.computeIfAbsent(r.getUserId(), authClient::getUserById);
                    return ParticipantReportDto.ParticipantEntryDto.builder()
                            .userId(r.getUserId())
                            .fullName(user != null ? user.getFullName() : "Usuario #" + r.getUserId())
                            .email(user != null ? user.getEmail() : "N/A")
                            .amountPaid(r.getAmountPaid())
                            .registeredAt(r.getRegisteredAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ParticipantReportDto.builder()
                .conferenceId(conferenceId)
                .conferenceName(conference != null ? conference.getName() : "Congreso no encontrado")
                .totalParticipants(participants.size())
                .totalEarnings(congressEarnings)
                .participants(participants)
                .build();
    }

    public AttendanceByActivityDto getAttendanceByActivity(Long activityId) {
        List<AsistenciaDto> attendances = asistenciasClient.getAttendanceByActivity(activityId);
        Map<Long, UserResponse> userCache = new HashMap<>();

        attendances.forEach(a -> {
            UserResponse user = userCache.computeIfAbsent(a.getIdUsuario(), authClient::getUserById);
            if (user != null) a.setUserFullName(user.getFullName());
        });

        return AttendanceByActivityDto.builder()
                .activityId(activityId)
                .totalAttendances(attendances.size())
                .attendances(attendances)
                .build();
    }

    public WorkshopReservationReportDto getWorkshopReservations(Long activityId) {
        List<AsistenciaDto> attendances = asistenciasClient.getAttendanceByActivity(activityId);
        Map<Long, UserResponse> userCache = new HashMap<>();

        attendances.forEach(a -> {
            UserResponse user = userCache.computeIfAbsent(a.getIdUsuario(), authClient::getUserById);
            if (user != null) a.setUserFullName(user.getFullName());
        });

        Map<String, Long> byType = attendances.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getNombreTipoParticipacion() != null
                                ? a.getNombreTipoParticipacion()
                                : "Sin tipo",
                        Collectors.counting()));

        return WorkshopReservationReportDto.builder()
                .activityId(activityId)
                .totalReservations(attendances.size())
                .reservationsByParticipationType(byType)
                .reservations(attendances)
                .build();
    }

    public EarningsByCongressDto getEarningsByCongress(Long conferenceId) {
        List<RegistrationDto> registrations = congresoClient.getAllRegistrations();
        List<ConferenceDto> conferences = congresoClient.getAllConferences();

        ConferenceDto conference = conferences.stream()
                .filter(c -> conferenceId.equals(c.getId()))
                .findFirst()
                .orElse(null);

        List<RegistrationDto> conferenceRegs = registrations.stream()
                .filter(r -> conferenceId.equals(r.getConferenceId()))
                .collect(Collectors.toList());

        BigDecimal totalGross = conferenceRegs.stream()
                .map(RegistrationDto::getAmountPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionRate = getCommissionPercentage();
        BigDecimal totalCommission = totalGross.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalNet = totalGross.subtract(totalCommission).setScale(2, RoundingMode.HALF_UP);

        List<EarningsByCongressDto.RegistrationDetailDto> details = conferenceRegs.stream()
                .map(r -> {
                    BigDecimal amount = r.getAmountPaid() != null ? r.getAmountPaid() : BigDecimal.ZERO;
                    BigDecimal com = amount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal net = amount.subtract(com).setScale(2, RoundingMode.HALF_UP);
                    return EarningsByCongressDto.RegistrationDetailDto.builder()
                            .userId(r.getUserId())
                            .amountPaid(amount)
                            .commissionDeducted(com)
                            .netEarnings(net)
                            .registeredAt(r.getRegisteredAt())
                            .build();
                })
                .collect(Collectors.toList());

        return EarningsByCongressDto.builder()
                .conferenceId(conferenceId)
                .conferenceName(conference != null ? conference.getName() : "Congreso no encontrado")
                .conferencePrice(conference != null ? conference.getPrice() : BigDecimal.ZERO)
                .totalGrossEarnings(totalGross)
                .totalCommission(totalCommission)
                .totalNetEarnings(totalNet)
                .totalRegistrations(conferenceRegs.size())
                .registrationDetails(details)
                .build();
    }
}
