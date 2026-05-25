package com.example.reports.config;

import com.example.reports.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable) // Deshabilitar CORS en el microservicio, el Gateway se encarga
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/reports/earnings").hasRole("ADMIN_SISTEMA")
                        .requestMatchers("/api/v1/reports/congress-by-institution").hasRole("ADMIN_SISTEMA")
                        .requestMatchers("/api/v1/reports/participants").hasRole("ADMIN_CONGRESO")
                        .requestMatchers("/api/v1/reports/attendance-by-activity").hasRole("ADMIN_CONGRESO")
                        .requestMatchers("/api/v1/reports/workshop-reservations").hasRole("ADMIN_CONGRESO")
                        .requestMatchers("/api/v1/reports/earnings-by-congress").hasRole("ADMIN_CONGRESO")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
