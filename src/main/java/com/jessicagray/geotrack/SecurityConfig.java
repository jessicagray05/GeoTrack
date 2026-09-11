package com.jessicagray.geotrack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        CookieCsrfTokenRepository csrfTokenRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        csrfTokenRepository.setCookiePath("/");

        http
            /*
             * GeoTrack uses session-cookie authentication.
             *
             * CSRF protection is enabled for authenticated application
             * actions. The XSRF-TOKEN cookie is intentionally readable
             * by GeoTrack's same-origin JavaScript so it can be returned
             * in the X-XSRF-TOKEN request header.
             *
             * The public authentication/bootstrap endpoints below are
             * temporarily excluded because login.html and
             * accept-invite.html are static pages and will be hardened
             * separately before the final public deployment.
             */
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringRequestMatchers(
                    "/login",
                    "/api/auth/register",
                    "/api/auth/invitations/validate",
                    "/api/auth/invitations/accept"
                )
            )

            .authorizeHttpRequests(auth -> auth

                /*
                 * Public pages/resources.
                 */
                .requestMatchers(
                    "/",
                    "/login.html",
                    "/accept-invite.html",
                    "/css/**",
                    "/js/**"
                ).permitAll()

                /*
                 * Public company registration.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register"
                ).permitAll()

                /*
                 * Public invitation endpoints.
                 */
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/invitations/validate",
                    "/api/auth/invitations/accept"
                ).permitAll()

                /*
                 * Team Management is organisation-ADMIN only.
                 */
                .requestMatchers(
                    "/api/team/**"
                ).hasRole("ADMIN")

                /*
                 * Only ADMIN may delete projects.
                 */
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/projects/**"
                ).hasRole("ADMIN")

                /*
                 * Only ADMIN may delete investigations.
                 */
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/investigations/**"
                ).hasRole("ADMIN")

                /*
                 * Everything else in the API requires an
                 * authenticated STAFF or ADMIN account.
                 */
                .requestMatchers(
                    "/api/**"
                ).hasAnyRole(
                    "ADMIN",
                    "STAFF"
                )

                .anyRequest()
                .authenticated()
            )

            /*
             * LOGIN
             */
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl(
                    "/index.html",
                    true
                )
                .failureUrl(
                    "/login.html?error=true"
                )
                .permitAll()
            )

            /*
             * LOGOUT
             */
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(
                    "/login.html?logout=true"
                )
                .invalidateHttpSession(true)
                .deleteCookies(
                    "JSESSIONID",
                    "XSRF-TOKEN"
                )
                .permitAll()
            );

        return http.build();
    }
}
