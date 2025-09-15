package com.example.tracklytics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login**", "/error**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/me", true)
                        .failureHandler((request, response, exception) -> {
                            System.err.println("=== OAuth2 AUTHENTICATION FAILURE ===");
                            System.err.println("Exception: " + exception.getClass().getSimpleName());
                            System.err.println("Message: " + exception.getMessage());
                            if (exception.getCause() != null) {
                                System.err.println("Cause: " + exception.getCause().getMessage());
                            }
                            exception.printStackTrace();
                            System.err.println("=====================================");

                            response.sendRedirect("/login?error=oauth2_failed");
                        })
                );

        return http.build();
    }
}