package com.example.reports.dto;

import com.example.reports.client.dto.ConferenceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongressByInstitutionDto {
    private Long institutionId;
    private String institutionName;
    private long totalConferences;
    private long activeConferences;
    private List<ConferenceDto> conferences;
}
