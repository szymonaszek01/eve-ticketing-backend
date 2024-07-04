package com.eve.ticketing.app.cloudgateway.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Order(-2)
public class CloudGatewayApiExceptionHandler extends AbstractErrorWebExceptionHandler {

    public CloudGatewayApiExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::handleCloudGatewayProcessingException);
    }

    private Mono<ServerResponse> handleCloudGatewayProcessingException(ServerRequest request) {
        ErrorAttributeOptions errorAttributeOptions = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.values());
        Map<String, Object> errorAttributes = getErrorAttributes(request, errorAttributeOptions);
        int status = Integer.parseInt(errorAttributes.get("status").toString());
        Error error = Error.builder().method(request.method().toString()).field("").value("").build();

        if (getError(request) instanceof CloudGatewayProcessingException) {
            error = ((CloudGatewayProcessingException) getError(request)).getError();
            status = ((CloudGatewayProcessingException) getError(request)).getStatus().value();
        } else {
            String message = (String) errorAttributes.get("message");
            error.setDescription(message != null ? message : "");
        }

        CloudGatewayApiException cloudGatewayApiException = CloudGatewayApiException.builder()
                .status(status)
                .message("request can not be processed, because an error occurred")
                .errors(List.of(error))
                .build();

        return ServerResponse.status(HttpStatusCode.valueOf(status)).body(BodyInserters.fromValue(cloudGatewayApiException));
    }
}
