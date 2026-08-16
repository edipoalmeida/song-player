package com.songplayer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security configuration for the music streaming API.
 *
 * <p>All {@code /api/v1/**} endpoints require authentication.
 * Observability ({@code /actuator/health}) and API documentation
 * ({@code /swagger-ui/**}, {@code /api-docs/**}) remain public so
 * liveness probes and developer tooling work without credentials.
 *
 * <p>The application ships without a user store — extend this class
 * to wire an {@code UserDetailsService} or OAuth2 resource server
 * when real users are introduced.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @SuppressWarnings("java:S4502") // CSRF disabled intentionally: stateless REST API using HTTP Basic/JWT — no session cookies
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // infrastructure & docs — always public
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // read-only catalog can be public
                        .requestMatchers(HttpMethod.GET, "/api/v1/songs", "/api/v1/songs/**").permitAll()
                        // everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Use HTTP Basic for now — replace with OAuth2 / JWT when identity provider is added
                .httpBasic(basic -> {})
                .build();
    }
}
