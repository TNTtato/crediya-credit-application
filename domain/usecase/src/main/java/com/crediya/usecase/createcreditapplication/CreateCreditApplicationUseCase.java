package com.crediya.usecase.createcreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.usecase.exception.CreditAmmountNotInRangeException;
import com.crediya.usecase.exception.NoSuchCreditTypeException;
import com.crediya.usecase.exception.NoSuchStateException;
import com.crediya.usecase.util.DefaultValues;
import com.crediya.usecase.util.Messages;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateCreditApplicationUseCase {

    private final CreditApplicationRepository creditApplicationRepository;
    private final StateRepository stateRepository;
    private final CreditTypeRepository creditTypeRepository;

    public Mono<CreditApplication> execute(CreditApplication creditApplication, String creditType) {

        return creditTypeRepository.findByName(creditType)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NoSuchCreditTypeException(
                        Messages.CREDIT_TYPE_NOT_FOUND, creditType
                ))))
                .filter(t -> creditApplication.getCreditAmount() >= t.getMinAmount()
                        && creditApplication.getCreditAmount() <= t.getMaxAmount())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CreditAmmountNotInRangeException(
                        Messages.AMOUNT_NOT_IN_RANGE, creditApplication.getCreditAmount()
                ))))
                .flatMap( t -> {
                    creditApplication.setCreditType(t.getCreditTypeId());
                    return stateRepository.findByName(DefaultValues.DEFAULT_APPLICATION_STATE.getValue())
                            .switchIfEmpty(Mono.defer(() -> Mono.error(new NoSuchStateException(Messages.CREDIT_APPLICATION_NOT_FOUND,
                                    DefaultValues.DEFAULT_APPLICATION_STATE.getValue()))))
                            .flatMap(state -> {
                                creditApplication.setStatusId(state.getStateId());
                                return creditApplicationRepository.save(creditApplication);
                            });
                });
    }

}
