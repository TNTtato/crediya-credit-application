package com.crediya.r2dbc.credittype;

import com.crediya.r2dbc.entity.CreditTypeEntity;
import com.crediya.r2dbc.entity.StateEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CreditTypeReactiveRepository extends ReactiveCrudRepository<CreditTypeEntity, Integer>, ReactiveQueryByExampleExecutor<CreditTypeEntity> {

    Mono<CreditTypeEntity> findByName(String name);
}
