package com.example.tracklytics.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                        .disable()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login**", "/error**", "/oauth2/**",
                                "/css/**", "/js/**", "/images/**", "/auth-success**", "/test-jwt**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/auth-success", true)
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String spotifyId = oauth2User.getAttribute("id");
                            String displayName = oauth2User.getAttribute("display_name");

                            String jwtToken = jwtService.generateToken(spotifyId, displayName != null ? displayName : "Spotify User");

                            response.sendRedirect("/auth-success?token=" + jwtToken);
                        })
                        .failureHandler((request, response, exception) -> {
                            System.err.println("OAuth2 AUTHENTICATION FAILURE");
                            System.err.println("Exception: " + exception.getClass().getSimpleName());
                            System.err.println("Message: " + exception.getMessage());
                            response.sendRedirect("/login?error=oauth2_failed");
                        })
                );

        return http.build();
    }
}