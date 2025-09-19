package com.crediya.r2dbc.application;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.page.ItemsPage;
import com.crediya.r2dbc.entity.CreditApplicationEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class CreditApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditApplication,
        CreditApplicationEntity,
        Integer,
        CreditApplicationReactiveRepository
> implements CreditApplicationRepository {

    private final TransactionalOperator txOp;
    public CreditApplicationReactiveRepositoryAdapter(CreditApplicationReactiveRepository repository, ObjectMapper mapper, TransactionalOperator txOp) {
        super(repository, mapper, d -> mapper.map(d, CreditApplication.class));
        this.txOp = txOp;
    }

    @Override
    public Mono<CreditApplication> save(CreditApplication c) {
        return super.save(c).as(txOp::transactional);
    }

    @Override
    public Mono<ItemsPage<CreditApplication>> findByStateId(Integer stateId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repository.findByStatusId(stateId, pageable)
                .collectList()
                .zipWith(repository.countByStatusId(stateId))
                .map(tuple -> new PageImpl(tuple.getT1(), pageable, tuple.getT2()))
                .map(p -> new ItemsPage<>(
                        p.getNumber(),
                        p.getSize(),
                        p.getTotalElements(),
                        p.getTotalPages(),
                        p.getContent()));

        /*
        return repository.findByStatusId(stateId, pageable)
                .map(p -> new ItemsPage<>(
                        p.getNumber(),
                        p.getSize(),
                        p.getTotalElements(),
                        p.getTotalPages(),
                        p.getContent()
                ));

         */
    }

    @Override
    public Flux<CreditApplication> findByStateIdAndEmail(Integer stateId, String email) {
        return repository.findByStatusIdAndEmail(stateId, email);
    }
}
