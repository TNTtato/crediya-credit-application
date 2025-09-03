package com.crediya.model.credittype.gateways;

import com.crediya.model.credittype.CreditType;
import reactor.core.publisher.Mono;

public interface CreditTypeRepository {

    public Mono<CreditType> findByName(String name);
}
