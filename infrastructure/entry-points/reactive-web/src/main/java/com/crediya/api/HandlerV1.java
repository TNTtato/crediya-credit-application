package com.crediya.api;

import com.crediya.api.model.CreateApplicationRequest;
import com.crediya.api.util.MapperUtil;
import com.crediya.usecase.createcreditapplication.CreateCreditApplicationUseCase;
import com.crediya.usecase.listapplicationsmanualreview.ListApplicationsManualReviewUseCase;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HandlerV1 {
    private final CreateCreditApplicationUseCase useCase;
    private final ListApplicationsManualReviewUseCase manualReviewUseCase;
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

    public Mono<ServerResponse> listenManualReviewUseCase(ServerRequest serverRequest) {

        Optional<String> filter = serverRequest.queryParam("estado");

        Optional<Integer> page = serverRequest.queryParam("page").map(Integer::parseInt);
        Optional<Integer> size = serverRequest.queryParam("size").map(Integer::parseInt);

        return filter.map(s -> manualReviewUseCase.execute(s, page.orElse(0), size.orElse(10))
                .flatMap(paged -> ServerResponse
                        .ok()
                        .bodyValue(paged)))
                .orElseGet(() -> ServerResponse.badRequest().bodyValue("Must specify estado"));

    }
}
