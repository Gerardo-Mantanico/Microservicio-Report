package com.example.reports.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstitutionDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private Boolean active;
}
