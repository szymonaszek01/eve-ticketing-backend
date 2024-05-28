package com.eve.ticketing.app.cloudgateway.security;

import com.eve.ticketing.app.cloudgateway.dto.AuthUserDto;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RouteValidator {

    private static final String USER = "USER";

    private static final String ADMIN = "ADMIN";

    private static final List<Route> SECURED_EVENT_API = List.of(
            Route.builder().method("POST").path("/api/v1/event/create").roleList(List.of(ADMIN)).build(),
            Route.builder().method("PUT").path("/api/v1/event/update").roleList(List.of(ADMIN)).build(),
            Route.builder().method("DELETE").path("/api/v1/event/id/").roleList(List.of(ADMIN)).build()
    );

    private static final List<Route> SECURED_TICKET_API = List.of(
            Route.builder().method("GET").path("/api/v1/ticket/all").roleList(List.of(USER, ADMIN)).build(),
            Route.builder().method("GET").path("/api/v1/ticket/id/").roleList(List.of(USER, ADMIN)).build(),
            Route.builder().method("POST").path("/api/v1/ticket/create").roleList(List.of(USER, ADMIN)).build(),
            Route.builder().method("PUT").path("/api/v1/ticket/update").roleList(List.of(USER, ADMIN)).build(),
            Route.builder().method("DELETE").path("/api/v1/ticket/id/").roleList(List.of(USER, ADMIN)).build()
    );

    private static final List<Route> SECURED_SEAT_API = List.of(
            Route.builder().method("GET").path("/api/v1/seat/all").roleList(List.of(ADMIN)).build(),
            Route.builder().method("GET").path("/api/v1/seat/id/").roleList(List.of(USER, ADMIN)).build(),
            Route.builder().method("POST").path("/api/v1/seat/create").roleList(List.of(ADMIN)).build(),
            Route.builder().method("PUT").path("/api/v1/seat/update").roleList(List.of(ADMIN)).build(),
            Route.builder().method("DELETE").path("/api/v1/seat/id/").roleList(List.of(ADMIN)).build()
    );

    private static final List<Route> SECURED_SMS_NOTIFICATION_API = List.of(
            Route.builder().method("GET").path("/api/v1/sms-notification/all").roleList(List.of(ADMIN)).build(),
            Route.builder().method("GET").path("/api/v1/sms-notification/id/").roleList(List.of(USER, ADMIN)).build(),
            Route.builder().method("DELETE").path("/api/v1/sms-notification/id/").roleList(List.of(ADMIN)).build()
    );

    public boolean isSecured(ServerHttpRequest request) {
        List<Route> securedApi = getSecuredApiList();
        String method = request.getMethod().toString().toUpperCase();
        String path = request.getPath().toString();
        return securedApi.stream().anyMatch(route -> method.equals(route.getMethod()) && path.contains(route.getPath()));
    }

    public boolean isValidRole(ServerHttpRequest request, AuthUserDto authUserDto) {
        List<Route> securedApi = getSecuredApiList();
        String method = request.getMethod().toString().toUpperCase();
        String path = request.getPath().toString();
        String role = authUserDto.getRole();
        return securedApi.stream().anyMatch(route -> method.equals(route.getMethod()) && path.contains(route.getPath()) && route.getRoleList().contains(role));
    }

    private List<Route> getSecuredApiList() {
        List<Route> securedApi = new ArrayList<>();
        securedApi.addAll(SECURED_EVENT_API);
        securedApi.addAll(SECURED_SEAT_API);
        securedApi.addAll(SECURED_TICKET_API);
        securedApi.addAll(SECURED_SMS_NOTIFICATION_API);
        return securedApi;
    }
}
