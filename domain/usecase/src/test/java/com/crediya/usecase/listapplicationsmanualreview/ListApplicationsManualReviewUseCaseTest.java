package com.crediya.usecase.listapplicationsmanualreview;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.page.ItemsPage;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserGateway;
import com.crediya.usecase.util.DefaultValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.crediya.usecase.util.DefaultValues.*;
import static org.junit.jupiter.api.Assertions.*;

class ListApplicationsManualReviewUseCaseTest {

    private static final State FIXED_STATE_PENDING = new State(1, DEFAULT_APPLICATION_STATE.getValue(), "Not reviews");
    private static final State FIXED_STATE_APPROVED = new State(2, APPROVED_APPLICATION.getValue(), "Approved");
    private static final User FIXED_USER = new User("jdoe@example.com", "John Doe", 10000000.0);
    private static final CreditType FREE_INVESTMENT_TYPE = new CreditType(1, "Free Investment", 3000000.0, 20000000.0, 0.12f, false);
    private static final ItemsPage<CreditApplication> FIXED_PAGED_CREDIT_APPLICATION = getFixedApplicationsManualReview();


    StateRepository stateRepository;
    CreditApplicationRepository creditApplicationRepository;
    CreditTypeRepository creditTypeRepository;
    UserGateway userGateway;

    ListApplicationsManualReviewUseCase useCase;

    @BeforeEach
    void setUp() {
        stateRepository = Mockito.mock(StateRepository.class);
        creditApplicationRepository = Mockito.mock(CreditApplicationRepository.class);
        creditTypeRepository = Mockito.mock(CreditTypeRepository.class);
        userGateway = Mockito.mock(UserGateway.class);

        useCase = new ListApplicationsManualReviewUseCase(
                stateRepository,
                creditApplicationRepository,
                creditTypeRepository,
                userGateway
        );
    }

    @Test
    void shouldReturnListApplicationsManualReview() {
        Mockito.when(stateRepository.findByName(DEFAULT_APPLICATION_STATE.getValue())).thenReturn(Mono.just(FIXED_STATE_PENDING));
        Mockito.when(stateRepository.findByName(APPROVED_APPLICATION.getValue())).thenReturn(Mono.just(FIXED_STATE_PENDING));
        Mockito.when(userGateway.getUserByEmail(Mockito.anyString())).thenReturn(Mono.just(FIXED_USER));
        Mockito.when(creditTypeRepository.findById(Mockito.anyInt())).thenReturn(Mono.just(FREE_INVESTMENT_TYPE));
        Mockito.when(creditApplicationRepository.findByStateId(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(Mono.just(FIXED_PAGED_CREDIT_APPLICATION));

        Mockito.when(creditApplicationRepository.findByStateIdAndEmail(Mockito.anyInt(), Mockito.anyString())).thenReturn(getFixedApprovedApplications());

        StepVerifier.create(useCase.execute(DEFAULT_APPLICATION_STATE.getValue(), 1, 1))
                .expectNextMatches(i -> i.getItems().getFirst().getTotalMonthlyDebtApprovedApplications().equals(30000000.0))
                .verifyComplete();
    }


    private static ItemsPage<CreditApplication> getFixedApplicationsManualReview() {
        ItemsPage<CreditApplication> itemsPage = new ItemsPage<>();

        itemsPage.setTotalPages(1);
        itemsPage.setSize(1);
        itemsPage.setTotalItems(1L);
        itemsPage.setPage(1);
        itemsPage.setItems(List.of(
                new CreditApplication(
                        1, 10000000.0, 12, "jdoe@example.com", 1, 1
                )
        ));

        return itemsPage;
    }

    private static Flux<CreditApplication> getFixedApprovedApplications() {
        return Flux.just(
                new CreditApplication(1, 10000000.0, 12, "jdoe@example.com", FIXED_STATE_APPROVED.getStateId(), 1),
                new CreditApplication(1, 20000000.0, 12, "jdoe@example.com", FIXED_STATE_APPROVED.getStateId(), 1)
        );
    }
}