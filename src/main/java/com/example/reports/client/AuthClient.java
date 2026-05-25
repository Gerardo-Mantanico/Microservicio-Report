package com.example.reports.client;

import com.example.reports.client.dto.SystemConfigurationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.auth-url}")
    private String authUrl;

    public SystemConfigurationDto getConfiguration(String key) {
        try {
            return restTemplate.getForObject(
                    authUrl + "/api/v1/configuration/" + key,
                    SystemConfigurationDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
