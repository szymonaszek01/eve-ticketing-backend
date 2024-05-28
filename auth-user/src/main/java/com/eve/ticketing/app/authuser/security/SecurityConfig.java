package com.eve.ticketing.app.authuser.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PERMIT_ALL_LIST = {
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/error",
            "/api/v1/auth-user/login",
            "/api/v1/auth-user/register",
            "/api/v1/auth-user/validate-token/{token}",
            "/api/v1/auth-user/refresh-token"
    };

    private static final String ADMIN = "ADMIN";

    private static final String USER = "USER";

    private final AuthenticationProvider authenticationProvider;

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable().cors().and().csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(PERMIT_ALL_LIST).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth-user/id/{id}").hasAnyAuthority(ADMIN, USER)
                .requestMatchers("/api/v1/auth-user/update").hasAnyAuthority(ADMIN, USER)
                .requestMatchers("/api/v1/auth-user/all").hasAuthority(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/auth-user/id/{id}").hasAuthority(ADMIN)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
