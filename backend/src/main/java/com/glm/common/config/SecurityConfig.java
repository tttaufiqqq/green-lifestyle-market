package com.glm.common.config;

import com.glm.common.security.GlmUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, GlmUserDetailsService uds) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfHandler)
                .ignoringRequestMatchers("/api/v1/payments/toyyibpay/callback",
                                         "/api/v1/payments/toyyibpay/return"))
            .userDetailsService(uds)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/health", "/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/auth/register", "/api/v1/auth/login",
                    "/api/v1/auth/verify-email", "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/api/v1/payments/toyyibpay/callback",
                                 "/api/v1/payments/toyyibpay/return").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/categories/**", "/api/v1/products/**",
                    "/api/v1/articles/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());
        return http.build();
    }
}
