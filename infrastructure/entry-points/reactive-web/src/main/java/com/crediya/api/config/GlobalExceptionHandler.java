package com.crediya.api.config;

import com.crediya.api.model.ApiError;
import com.crediya.usecase.exception.CreditAmmountNotInRangeException;
import com.crediya.usecase.exception.NoSuchCreditTypeException;
import com.crediya.usecase.exception.NoSuchStateException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Date;

@Configuration
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties.Resources resources,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        setMessageReaders(configurer.getReaders());
        setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderException);
    }

    private Mono<ServerResponse> renderException(ServerRequest request) {
        Throwable error = getError(request);

        if (error instanceof NoSuchStateException nss) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, request, nss.getMessage(), nss.getNotFoundValue());
        }

        if (error instanceof NoSuchCreditTypeException nsct) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, request, nsct.getMessage(), nsct.getCreditType());
        }

        if (error instanceof CreditAmmountNotInRangeException canir) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, request, canir.getMessage(), "Wanted amount " + canir.getCreditAmount());
        }

        // fallback
        return buildErrorResponse(request, error.getLocalizedMessage());
    }

    private Mono<ServerResponse> buildErrorResponse(ServerRequest request, String message) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, message, null);
    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus status, ServerRequest request, String message, String cause) {
        ApiError apiError = new ApiError(
                message,
                request.uri().toString(),
                new Date(),
                cause
        );
        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(apiError);
    }
}
