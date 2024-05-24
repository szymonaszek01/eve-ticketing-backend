package com.eve.ticketing.app.cloudgateway.security;

import com.eve.ticketing.app.cloudgateway.dto.AuthUserDto;
import com.eve.ticketing.app.cloudgateway.exception.CloudGatewayProcessingException;
import com.eve.ticketing.app.cloudgateway.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;

    private final RestTemplate restTemplate;

    public AuthenticationFilter(RouteValidator validator, RestTemplate restTemplate) {
        super(Config.class);
        this.validator = validator;
        this.restTemplate = restTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (!validator.isSecured(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            Error error = Error.builder().method(exchange.getRequest().getMethod().toString().toUpperCase()).field("auth_token").build();
            List<String> authorizationHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || authorizationHeader.get(0) == null) {
                error.setDescription("missing jwt token");
                log.error(error.toString());
                throw new CloudGatewayProcessingException(HttpStatus.UNAUTHORIZED, error);
            }
            if (!authorizationHeader.get(0).startsWith("Bearer ")) {
                error.setDescription("jwt token does not start from \"Bearer \"");
                log.error(error.toString());
                throw new CloudGatewayProcessingException(HttpStatus.UNAUTHORIZED, error);
            }

            String token = authorizationHeader.get(0).substring(7);
            AuthUserDto authUserDto;
            try {
                authUserDto = restTemplate.getForObject(
                        "http://AUTH-USER/api/v1/auth-user/validate-token/{token}",
                        AuthUserDto.class,
                        token
                );
            } catch (RestClientException e) {
                error.setDescription("invalid jwt token");
                log.error(error.toString());
                throw new CloudGatewayProcessingException(HttpStatus.UNAUTHORIZED, error);
            }

            if (authUserDto == null || !validator.isValidRole(exchange.getRequest(), authUserDto)) {
                error.setDescription("invalid role");
                log.error(error.toString());
                throw new CloudGatewayProcessingException(HttpStatus.UNAUTHORIZED, error);
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
