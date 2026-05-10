package com.example.campusrunbackend.config;

import com.example.campusrunbackend.security.JwtFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter, UserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no auth required
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/services/by-category/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/providers/discovery/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/providers/{id}").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/provider/**").permitAll()
                        .requestMatchers("/api/ws/**").permitAll()
                        .requestMatchers("/api/uploads/**").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/force-approve-all").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Client/Provider mutual endpoints
                        .requestMatchers("/api/bookings/create").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/bookings/my/client").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/bookings/client/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/reviews/create").hasAnyRole("CLIENT", "PROVIDER", "ADMIN")

                        // Provider-specific endpoints
                        .requestMatchers("/api/providers/register").hasAnyRole("CLIENT", "PROVIDER", "ADMIN")
                        .requestMatchers("/api/services/create").hasAnyRole("PROVIDER", "ADMIN")
                        .requestMatchers("/api/services/**").hasAnyRole("PROVIDER", "ADMIN")
                        .requestMatchers("/api/bookings/my/provider").hasAnyRole("PROVIDER", "ADMIN")
                        .requestMatchers("/api/bookings/provider/**").hasAnyRole("PROVIDER", "ADMIN")
                        .requestMatchers("/api/bookings/update-status").hasAnyRole("PROVIDER", "ADMIN")
                        .requestMatchers("/api/bookings/*/confirm-done").hasAnyRole("CLIENT", "PROVIDER", "ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Custom JSON 401 response for missing/invalid auth token
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            Map<String, Object> body = new HashMap<>();
                            body.put("success", false);
                            body.put("message", "Authentication required. Please provide a valid Bearer token.");
                            body.put("data", null);
                            objectMapper.writeValue(response.getWriter(), body);
                        })
                        // Custom JSON 403 response for insufficient permissions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            Map<String, Object> body = new HashMap<>();
                            body.put("success", false);
                            body.put("message", "You don't have permission to access this resource");
                            body.put("data", null);
                            objectMapper.writeValue(response.getWriter(), body);
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins — required for Android emulator (10.0.2.2) and physical devices
        // In production, replace with specific domain(s)
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        // Allow credentials so Android can send cookies/auth headers
        configuration.setAllowCredentials(true);
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
