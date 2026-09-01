package com.example.parameterapproval.config;

import com.example.parameterapproval.security.HeaderAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, HeaderAuthenticationFilter headerFilter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> { })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**")
                            .hasAnyRole("PARAMETER_VIEWER", "PARAMETER_EDITOR", "PARAMETER_APPROVER")
                        .requestMatchers(HttpMethod.POST,
                                "/api/parameter-resources/*/search", "/api/custom-queries/*/search")
                            .hasAnyRole("PARAMETER_VIEWER", "PARAMETER_EDITOR", "PARAMETER_APPROVER")
                        .requestMatchers("/api/change-requests/*/approve", "/api/change-requests/*/reject")
                            .hasRole("PARAMETER_APPROVER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("PARAMETER_EDITOR")
                        .anyRequest().authenticated())
                .addFilterBefore(headerFilter, AnonymousAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-User-Id", "X-User-Name", "X-User-Roles"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
