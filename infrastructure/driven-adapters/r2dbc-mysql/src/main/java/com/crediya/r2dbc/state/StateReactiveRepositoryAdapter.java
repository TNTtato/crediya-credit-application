package com.crediya.r2dbc.state;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.r2dbc.entity.CreditApplicationEntity;
import com.crediya.r2dbc.entity.StateEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class StateReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        State,
        StateEntity,
        Integer,
        StateReactiveRepository
> implements StateRepository {

    public StateReactiveRepositoryAdapter(StateReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, State.class));
    }

    @Override
    public Mono<State> findByName(String stateName) {
        return repository.findByName(stateName).map(o -> mapper.map(o, State.class));
    }
}
