package com.crediya.api;

import com.crediya.api.model.CreateApplicationRequest;
import com.crediya.api.util.MapperUtil;
import com.crediya.usecase.createcreditapplication.CreateCreditApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class HandlerV1 {
    private  final CreateCreditApplicationUseCase useCase;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Mono<ServerResponse> listenCreateCreditApplicationUseCase(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateApplicationRequest.class)
                .flatMap(r -> {
                    log.info("Received RegisterUserRequest [{}]", r);
                    return useCase.execute(MapperUtil.fromRequestToUserDomain(r), r.creditType());
                })
                .flatMap(
                        saved -> ServerResponse
                                .created(serverRequest.uri())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(saved));
    }
}
