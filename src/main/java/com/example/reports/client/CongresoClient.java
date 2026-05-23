package com.example.reports.client;

import com.example.reports.client.dto.ConferenceDto;
import com.example.reports.client.dto.InstitutionDto;
import com.example.reports.client.dto.RegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CongresoClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.congreso-url}")
    private String congresoUrl;

    public List<ConferenceDto> getAllConferences() {
        try {
            ConferenceDto[] result = restTemplate.getForObject(
                    congresoUrl + "/api/v1/conferences", ConferenceDto[].class);
            return result != null ? Arrays.asList(result) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<InstitutionDto> getAllInstitutions() {
        try {
            InstitutionDto[] result = restTemplate.getForObject(
                    congresoUrl + "/api/v1/institutions", InstitutionDto[].class);
            return result != null ? Arrays.asList(result) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<RegistrationDto> getAllRegistrations() {
        try {
            RegistrationDto[] result = restTemplate.getForObject(
                    congresoUrl + "/api/v1/registrations", RegistrationDto[].class);
            return result != null ? Arrays.asList(result) : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
