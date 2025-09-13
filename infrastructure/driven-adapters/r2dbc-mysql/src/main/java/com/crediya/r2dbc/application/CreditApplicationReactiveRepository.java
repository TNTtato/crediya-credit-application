package com.crediya.r2dbc.application;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.r2dbc.entity.CreditApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationEntity, Integer>,
        ReactiveQueryByExampleExecutor<CreditApplicationEntity>,
        PagingAndSortingRepository<CreditApplicationEntity, Integer> {

    Mono<Page<CreditApplication>> findByStatusId(Integer stateId, Pageable pageable);

    Flux<CreditApplication> findByStatusIdAndEmail(Integer stateId, String email);
}
