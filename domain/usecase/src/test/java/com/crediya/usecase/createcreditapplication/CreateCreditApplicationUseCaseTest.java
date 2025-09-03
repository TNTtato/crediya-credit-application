package com.crediya.usecase.createcreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.usecase.exception.CreditAmmountNotInRangeException;
import com.crediya.usecase.exception.NoSuchCreditTypeException;
import com.crediya.usecase.exception.NoSuchStateException;
import com.crediya.usecase.util.DefaultValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

class CreateCreditApplicationUseCaseTest {

    static final String FIXED_CREDIT_TYPE = "LIBRE_INVERSION";
    static final Double FIXED_MIN_AMOUNT = 5000000.0;
    static final Double FIXED_MAX_AMOUNT = 20000000.0;
    static final String FIXED_STATE = DefaultValues.DEFAULT_APPLICATION_STATE.getValue();

    CreditApplicationRepository appRepository;
    StateRepository stateRepository;
    CreditTypeRepository typeRepository;

    CreateCreditApplicationUseCase useCase;

    @BeforeEach
    void setUp() {
        appRepository = Mockito.mock(CreditApplicationRepository.class);
        stateRepository = Mockito.mock(StateRepository.class);
        typeRepository = Mockito.mock(CreditTypeRepository.class);
        useCase = new CreateCreditApplicationUseCase(appRepository, stateRepository, typeRepository);
    }

    @ParameterizedTest
    @MethodSource("registerTestCases")
    void shouldRegisterCreditApplication(CreditApplication input,
                                         CreditApplication saved,
                                         CreditType creditType,
                                         State state) {
        Mockito.when(appRepository.save(Mockito.any())).thenReturn(Mono.just(saved));
        Mockito.when(stateRepository.findByName(Mockito.anyString())).thenReturn(Mono.just(state));
        Mockito.when(typeRepository.findByName(Mockito.anyString())).thenReturn(Mono.just(creditType));

        StepVerifier.create(useCase.execute(input, FIXED_CREDIT_TYPE))
                .expectNextMatches(c -> c.equals(saved))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCreditTypeDoesNotExist() {
        CreditApplication input = new CreditApplication(1, 7000000.0, 12, "jdoe@example.com", null, null);

        Mockito.when(typeRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(input, FIXED_CREDIT_TYPE))
                .expectError(NoSuchCreditTypeException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCreditAmountIsOutOfRange() {
        CreditApplication input = new CreditApplication(1, 1000.0, 12, "jdoe@example.com", null, null);
        CreditType creditType = new CreditType(1, FIXED_CREDIT_TYPE, FIXED_MIN_AMOUNT, FIXED_MAX_AMOUNT, 0.25f, false);

        Mockito.when(typeRepository.findByName(Mockito.anyString()))
                .thenReturn(Mono.just(creditType));

        StepVerifier.create(useCase.execute(input, FIXED_CREDIT_TYPE))
                .expectError(CreditAmmountNotInRangeException.class)
                .verify();
    }

    @Test
    void shouldFailWhenDefaultStateDoesNotExist() {
        CreditApplication input = new CreditApplication(1, 7000000.0, 12, "jdoe@example.com", null, null);
        CreditType creditType = new CreditType(1, FIXED_CREDIT_TYPE, FIXED_MIN_AMOUNT, FIXED_MAX_AMOUNT, 0.25f, false);

        Mockito.when(typeRepository.findByName(Mockito.anyString())).thenReturn(Mono.just(creditType));
        Mockito.when(stateRepository.findByName(Mockito.anyString())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(input, FIXED_CREDIT_TYPE))
                .expectError(NoSuchStateException.class)
                .verify();
    }

    static Stream<Arguments> registerTestCases() {
        return Stream.of(
                Arguments.of(new CreditApplication(1, 7000000.0, 12, "jdoe@example.com", 1, 1),
                        new CreditApplication(1, 7000000.0, 12, "jdoe@example.com", 1, 1),
                        new CreditType(1, FIXED_CREDIT_TYPE, FIXED_MIN_AMOUNT, FIXED_MAX_AMOUNT, 0.25f, false),
                        new State(1, FIXED_STATE, "DEFAULT")
                )
        );
    }
}