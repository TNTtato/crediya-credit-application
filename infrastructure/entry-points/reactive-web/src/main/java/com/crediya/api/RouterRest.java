package com.crediya.api;

import com.crediya.api.model.ApiError;
import com.crediya.api.model.CreateApplicationRequest;
import com.crediya.model.creditapplication.CreditApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class RouterRest {

    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    method = RequestMethod.POST,
                    beanClass = HandlerV1.class,
                    beanMethod = "listenCreateCreditApplicationUseCase",
                    operation = @Operation(
                            operationId = "createCreditApplication",
                            summary = "Create a new credit application",
                            tags = {"CreditApplication"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = CreateApplicationRequest.class),
                                            examples = {
                                                    @ExampleObject(
                                                            name = "Nueva solicitud",
                                                            summary = "Ejemplo de registro de solicitud",
                                                            value = """
                                                                    {
                                                                        "documento_identidad" : "CC12345",
                                                                        "email": "jdoe@example.com",
                                                                        "monto": 7000000,
                                                                        "plazo": 12,
                                                                        "tipo_credito": "LIBRE_INVERSION"
                                                                    }
                                """
                                                    )
                                            }
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Credit Application created successfully",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = CreditApplication.class),
                                                    examples = {
                                                            @ExampleObject(
                                                                    name = "Solicitud creada",
                                                                    summary = "Ejemplo de respuesta de creación",
                                                                    value = """
                                                                            {
                                                                                "applicationId": 2,
                                                                                "creditAmount": 7000000.0,
                                                                                "installments": 12,
                                                                                "email": "jdoe@example.com",
                                                                                "statusId": 1,
                                                                                "creditTypeId": 1
                                                                            }
                                    """
                                                            )
                                                    }
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Validation Errors / Bad Request", content = @Content(schema = @Schema(implementation = ApiError.class))),
                                    @ApiResponse(responseCode = "404", description = "Some resources don't exist", content = @Content(schema = @Schema(implementation = ApiError.class))),
                                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ApiError.class)))
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> routerFunction(HandlerV1 handlerV1, HandlerV2 handlerV2) {
        return RouterFunctions
                .route()
                .path("/api/v1", builder -> builder.POST("/solicitud", handlerV1::listenCreateCreditApplicationUseCase))
                .build();
    }
}
