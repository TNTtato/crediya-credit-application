package com.crediya.r2dbc.credittype;

import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.r2dbc.entity.CreditTypeEntity;
import com.crediya.r2dbc.entity.StateEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class CreditTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        CreditType,
        CreditTypeEntity,
        Integer,
        CreditTypeReactiveRepository
> implements CreditTypeRepository {

    public CreditTypeReactiveRepositoryAdapter(CreditTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, CreditType.class));
    }

    @Override
    public Mono<CreditType> findByName(String stateName) {
        return repository.findByName(stateName).map(o -> mapper.map(o, CreditType.class));
    }
}
