package com.pabloncf.threatlens.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * {@code /actuator/health/**} is public (Docker/Compose healthchecks) and {@code /demo/**} is
 * public (the demo attack surface - a real login/comment form has no auth of its own either).
 * Everything else, in particular {@code /api/**}, requires HTTP Basic.
 *
 * <p>CSRF is disabled: this API is stateless (HTTP Basic, no cookie-based session), which is
 * the standard case where CSRF protection doesn't apply.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health/**", "/demo/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .httpBasic(withDefaults());
        return http.build();
    }
}
