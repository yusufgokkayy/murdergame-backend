package com.murdergame.common.config;

import com.murdergame.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS ENABLE (KRİTİK)
                .cors(cors -> {})

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(authz -> authz

                        // Preflight request (KRİTİK)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/team/all",
                                "/api/quiz/room/**",    // <-- BURAYI DEĞİŞTİR (tek * yerine **)
                                "/api/quiz/question/**",
                                "/api/leaderboard/**",// <-- BUNU DA EKLE (belirli bir soruyu getirme de public olsun)
                                "/api/game-room/**",
                                "/test.html",
                                "/quiz.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/ws/**", "/ws", "/ws/info", "/ws/websocket"
                        ).permitAll()

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // USER
                        .requestMatchers("/api/user/**").hasRole("USER")

                        // WEBSOCKET
                        .requestMatchers("/ws-quiz/**").permitAll()

                        // DİĞERİ
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {

        org.springframework.web.cors.CorsConfiguration config =
                new org.springframework.web.cors.CorsConfiguration();

        // DEV için açık
        config.setAllowedOriginPatterns(java.util.List.of("*"));

        config.setAllowedMethods(java.util.List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(java.util.List.of("*"));

        config.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
