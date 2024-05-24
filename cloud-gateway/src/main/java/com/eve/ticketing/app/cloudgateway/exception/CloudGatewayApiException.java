package com.eve.ticketing.app.cloudgateway.exception;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CloudGatewayApiException {

    private Integer status;

    private String message;

    private List<Error> errors;
}
