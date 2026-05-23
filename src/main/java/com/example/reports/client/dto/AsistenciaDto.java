package com.example.reports.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaDto {
    private Long idAsistencia;
    private Long idActividad;
    private Long idUsuario;
    private Integer idTipoParticipacion;
    private String nombreTipoParticipacion;
    private Long registradoPor;
    private LocalDateTime registradoEn;
}
