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

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CreateCreditApplicationUseCase {

    private final CreditApplicationRepository creditApplicationRepository;
    private final StateRepository stateRepository;
    private final CreditTypeRepository creditTypeRepository;

    private static final Logger log = Logger.getLogger(CreateCreditApplicationUseCase.class.getName());

    public Mono<CreditApplication> execute(CreditApplication creditApplication, String creditType) {

        log.info("Creating credit application of type: " + creditType);
        return creditTypeRepository.findByName(creditType)
                .switchIfEmpty(Mono.defer(() -> {
                    log.log(Level.SEVERE, "Credit type " + creditType + " not found!");
                    return Mono.error(new NoSuchCreditTypeException(
                            Messages.CREDIT_TYPE_NOT_FOUND, creditType
                    ));
                }))
                .filter(t -> creditApplication.getCreditAmount() >= t.getMinAmount()
                        && creditApplication.getCreditAmount() <= t.getMaxAmount())
                .switchIfEmpty(Mono.defer(() -> {
                    log.log(Level.SEVERE, "Credit amount " + creditApplication.getCreditAmount() + " is not within limits for credit type " + creditType);
                    return Mono.error(new CreditAmmountNotInRangeException(
                            Messages.AMOUNT_NOT_IN_RANGE, creditApplication.getCreditAmount()
                    ));
                }))
                .flatMap( t -> {
                    creditApplication.setCreditTypeId(t.getCreditTypeId());
                    return stateRepository.findByName(DefaultValues.DEFAULT_APPLICATION_STATE.getValue())
                            .switchIfEmpty(Mono.defer(() -> {

                                log.log(Level.SEVERE, "State " + DefaultValues.DEFAULT_APPLICATION_STATE.getValue() + " not found!");
                                return Mono.error(new NoSuchStateException(Messages.CREDIT_APPLICATION_NOT_FOUND,
                                        DefaultValues.DEFAULT_APPLICATION_STATE.getValue()));
                            }))
                            .flatMap(state -> {
                                creditApplication.setStatusId(state.getStateId());
                                return creditApplicationRepository.save(creditApplication);
                            });
                });
    }

}
