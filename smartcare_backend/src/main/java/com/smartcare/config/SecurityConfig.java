package com.smartcare.config;

import com.smartcare.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints — fully public
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/register/doctor", "/api/v1/auth/login").permitAll()
                // FCM token registration — any authenticated user
                .requestMatchers("/api/v1/auth/fcm-token").authenticated()
                // Admin registration — admin only
                .requestMatchers("/api/v1/auth/register/admin").hasRole("ADMIN")

                // Hospitals/Branches/Departments — GETs are public, mutations are admin-only
                .requestMatchers(HttpMethod.GET, "/api/v1/hospitals", "/api/v1/hospitals/**", "/api/v1/branches", "/api/v1/branches/**", "/api/v1/departments", "/api/v1/departments/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/hospitals", "/api/v1/hospitals/**", "/api/v1/branches", "/api/v1/branches/**", "/api/v1/departments", "/api/v1/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/hospitals", "/api/v1/hospitals/**", "/api/v1/branches", "/api/v1/branches/**", "/api/v1/departments", "/api/v1/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/hospitals", "/api/v1/hospitals/**", "/api/v1/branches", "/api/v1/branches/**", "/api/v1/departments", "/api/v1/departments/**").hasRole("ADMIN")

                // Doctors — GETs need only authentication, mutations are admin/staff
                .requestMatchers(HttpMethod.GET, "/api/v1/doctors", "/api/v1/doctors/**", "/api/v1/schedules", "/api/v1/schedules/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/doctors", "/api/v1/doctors/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/v1/doctors", "/api/v1/doctors/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/doctors", "/api/v1/doctors/**").hasAnyRole("ADMIN", "STAFF")
                // Schedule updates — doctors can update their own, admins can update any
                .requestMatchers(HttpMethod.POST, "/api/v1/schedules", "/api/v1/schedules/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/schedules", "/api/v1/schedules/**").hasAnyRole("DOCTOR", "ADMIN")

                // Queues — GETs are authenticated, mutations are doctor/staff/admin
                .requestMatchers(HttpMethod.GET, "/api/v1/queues", "/api/v1/queues/**").authenticated()
                .requestMatchers("/api/v1/queues/*/call-next").hasAnyRole("DOCTOR", "STAFF", "ADMIN")
                .requestMatchers("/api/v1/queues/*/status").hasAnyRole("DOCTOR", "STAFF", "ADMIN")
                .requestMatchers("/api/v1/queues/*/priority").hasAnyRole("DOCTOR", "STAFF", "ADMIN")

                // Appointments — GETs need authentication, POST booking = patient only
                .requestMatchers(HttpMethod.GET, "/api/v1/appointments", "/api/v1/appointments/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/appointments", "/api/v1/appointments/**").hasAnyRole("PATIENT", "ADMIN")
                // Cancel/reschedule also allowed by admin
                .requestMatchers(HttpMethod.PUT, "/api/v1/appointments", "/api/v1/appointments/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/appointments", "/api/v1/appointments/**").hasAnyRole("PATIENT", "ADMIN")

                // Patient profile — patient or admin
                .requestMatchers(HttpMethod.GET, "/api/v1/patients", "/api/v1/patients/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/patients", "/api/v1/patients/**").hasAnyRole("PATIENT", "ADMIN")

                // AI — predict is authenticated; retrain is admin only
                .requestMatchers(HttpMethod.POST, "/api/v1/ai/predict-wait-time").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/ai/retrain").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/ai/metrics").hasAnyRole("ADMIN", "DOCTOR")

                // Analytics and audit logs — admin only
                .requestMatchers("/api/v1/analytics/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/audit-logs/**").hasRole("ADMIN")

                // Notifications — own patient, any admin
                .requestMatchers("/api/v1/notifications/**").authenticated()

                // Catch-all: require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
