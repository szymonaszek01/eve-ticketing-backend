package com.eve.ticketing.app.cloudgateway.security;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Route {

    private String method;

    private String path;

    private List<String> roleList;
}
