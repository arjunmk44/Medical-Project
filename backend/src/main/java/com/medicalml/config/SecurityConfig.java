package com.medicalml.config;

import com.medicalml.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the Medical ML Platform.
 *
 * Configures:
 * - Stateless JWT-based authentication (no server-side sessions).
 * - CORS policy allowing the React frontend to communicate with the API.
 * - URL-based and role-based access control rules.
 * - BCrypt password hashing with strength factor 12.
 *
 * @Configuration — Declares this class as a Spring configuration source.
 * @EnableWebSecurity — Activates Spring Security's HTTP security filters.
 * @EnableMethodSecurity — Enables @PreAuthorize annotations on controller
 *                       methods.
 * @RequiredArgsConstructor — Lombok: generates a constructor injecting all
 *                          final fields.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT authentication filter — validates tokens on every request. */
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Defines the main security filter chain for HTTP requests.
     *
     * Access control rules:
     * - /api/auth/** — Public (login, register, refresh token).
     * - /swagger-ui/**, /v3/api-docs/** — Public (API documentation).
     * - DELETE /api/patients/** — ADMIN role only (patient deletion).
     * - POST /api/ml/retrain — ADMIN role only (model retraining).
     * - /api/admin/** — ADMIN role only.
     * - /api/** — Requires authentication (any logged-in user).
     * - All other requests — Permitted (e.g., static assets, health checks).
     *
     * The JwtAuthFilter runs *before* the default
     * UsernamePasswordAuthenticationFilter
     * so that JWT tokens are validated before Spring's built-in auth mechanism.
     *
     * @param http HttpSecurity builder provided by Spring
     * @return Configured SecurityFilterChain bean
     * @throws Exception If configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT-based APIs
                .csrf(csrf -> csrf.disable())
                // Enable CORS with our custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Use stateless sessions — no session cookies, every request must carry a JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define URL-based authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/swagger-ui/**", "/api/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasRole("ADMIN")
                        .requestMatchers("/api/ml/retrain").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                // Insert JWT filter before the default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder bean using BCrypt with strength factor 12.
     * BCrypt automatically handles salt generation and is resistant to brute-force
     * attacks.
     * Strength 12 means 2^12 = 4096 hashing iterations.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes the AuthenticationManager as a bean so it can be injected
     * into services (e.g., AuthService) for programmatic authentication.
     *
     * @param config Spring's AuthenticationConfiguration
     * @return The default AuthenticationManager
     * @throws Exception If retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS (Cross-Origin Resource Sharing) configuration.
     *
     * Allows:
     * - Origins: localhost:3000 (Vite dev server), localhost:80, localhost (Nginx).
     * - Methods: GET, POST, PUT, DELETE, OPTIONS.
     * - Headers: All headers are allowed (e.g., Authorization, Content-Type).
     * - Credentials: Cookies and Authorization headers are included in cross-origin
     * requests.
     *
     * @return URL-based CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:80", "http://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
