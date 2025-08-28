package com.crediya.model.state.gateways;

import com.crediya.model.state.State;
import reactor.core.publisher.Mono;

public interface StateRepository {
    Mono<State> findByName(String stateName);
}
