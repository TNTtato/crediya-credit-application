package com.crediya.consumer;

import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestConsumer implements UserGateway {
    private final WebClient client;

    private static final Logger logger = LoggerFactory.getLogger(RestConsumer.class);

    @CircuitBreaker(name = "getUserByEmail" /*, fallbackMethod = "testGetOk"*/)
    public Mono<User> getUserByEmail(String email) {
        logger.info("[getUserByEmail] Making request to get user by email: {}", email);
        return getCurrentToken()
                .flatMap(token -> client
                        .get()
                        .uri("/usuarios")
                        .header("X-EMAIL", email)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(User.class)
                        .doOnNext(response -> {
                            logger.info("[getUserByEmail] Got response: {}", response);})
                        .onErrorResume(err -> {
                            logger.error(err.getMessage());
                            return Mono.error(err);
                        }));
    }

    private Mono<String> getCurrentToken() {
        logger.info("[getCurrentToken] Intentando obtener token del contexto");
        return ReactiveSecurityContextHolder
                .getContext()
                .doOnNext(c -> logger.debug("[getCurrentToken] Contexto de seguridad encontrado: {}", c.getAuthentication() != null ? "Autenticado" : "No autenticado"))
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .doOnNext(auth -> logger.debug("[getCurrentToken] Authentication: {}", auth.getClass().getSimpleName()))
                .map(Authentication::getCredentials)
                .map(Object::toString)
                .doOnNext(token -> logger.info("[getCurrentToken] Token obtenido: {}...", token.substring(0, Math.min(token.length(), 10))))
                .doOnError(error -> logger.error("[getCurrentToken] Error al obtener token: {}", error.getMessage()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encontró autenticación en el contexto")));
    }
}
