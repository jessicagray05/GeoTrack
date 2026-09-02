package com.jessicagray.geotrack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/",
                    "/login.html",
                    "/css/**",
                    "/js/**"
                ).permitAll()

                // Only ADMIN can delete projects
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/projects/**"
                ).hasRole("ADMIN")

                // Only ADMIN can delete investigations
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/investigations/**"
                ).hasRole("ADMIN")

                // ADMIN and STAFF can use the rest of the API
                .requestMatchers(
                    "/api/**"
                ).hasAnyRole("ADMIN", "STAFF")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form

                .loginPage("/login.html")

                .loginProcessingUrl("/login")

                .defaultSuccessUrl(
                    "/index.html",
                    true
                )

                .failureUrl(
                    "/login.html?error=true"
                )

                .permitAll()
            )

            .logout(logout -> logout

                .logoutSuccessUrl(
                    "/login.html"
                )

                .permitAll()
            );

        return http.build();
    }
}