package com.crediya.model.creditapplication.gateways;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.page.ItemsPage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {

    public Mono<CreditApplication> save(CreditApplication creditApplication);

    public Mono<ItemsPage<CreditApplication>> findByStateId(Integer stateId, int page, int size);

    public Flux<CreditApplication> findByStateIdAndEmail(Integer stateId, String email);
}
