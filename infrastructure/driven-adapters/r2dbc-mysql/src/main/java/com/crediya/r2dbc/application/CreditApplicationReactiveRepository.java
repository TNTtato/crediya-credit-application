package com.crediya.r2dbc.application;

import com.crediya.r2dbc.entity.CreditApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditApplicationReactiveRepository extends ReactiveCrudRepository<CreditApplicationEntity, Integer>, ReactiveQueryByExampleExecutor<CreditApplicationEntity> {

}
