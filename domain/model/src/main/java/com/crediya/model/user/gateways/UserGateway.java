package com.crediya.model.user.gateways;

import com.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface UserGateway {
    public Mono<User> getUserByEmail(String email);
}
