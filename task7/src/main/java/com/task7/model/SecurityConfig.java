package com.task7.model;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/users/create", "/users/list",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )

                // CSRF Configuration - ENABLED by default
                .csrf(csrf -> csrf
                                // Use cookie-based CSRF tokens for better compatibility
                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // You can add URLs to ignore CSRF if needed (not recommended for forms)
                        // .ignoringRequestMatchers("/api/**")
                )

                // Form login configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/users/list", true)
                        .failureUrl("/login?error=true")
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Session management
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired=true")
                );

        return http.build();
    }
}