package com.crediya.r2dbc.state;

import com.crediya.r2dbc.entity.CreditApplicationEntity;
import com.crediya.r2dbc.entity.StateEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface StateReactiveRepository extends ReactiveCrudRepository<StateEntity, Integer>, ReactiveQueryByExampleExecutor<StateEntity> {

    Mono<StateEntity> findByName(String name);
}
