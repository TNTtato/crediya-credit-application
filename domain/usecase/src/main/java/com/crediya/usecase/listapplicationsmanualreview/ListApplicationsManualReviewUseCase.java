package com.crediya.usecase.listapplicationsmanualreview;

import com.crediya.model.applicationreviewitem.ApplicationReviewItem;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.page.ItemsPage;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserGateway;
import com.crediya.usecase.exception.BaseException;
import com.crediya.usecase.exception.NotValidFilterException;
import com.crediya.usecase.util.DefaultValues;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ListApplicationsManualReviewUseCase {

    private final StateRepository stateRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditTypeRepository creditTypeRepository;
    private final UserGateway userGateway;

    private static final Logger log = Logger.getLogger(ListApplicationsManualReviewUseCase.class.getName());

    public Mono<ItemsPage<ApplicationReviewItem>> execute(String filter, int page, int size) {
        log.info("Listing applications manual review with filter: " + filter);
        return stateRepository.findByName(filter)
                .switchIfEmpty(Mono.defer(() -> {
                    log.log(Level.SEVERE, "No applications found with name: " + filter);
                    return Mono.error(new NotValidFilterException(filter));
                }))
                .flatMap(state -> getApplicationReviewItemsPage(page, size, state))
                .doOnNext(itemsPage -> {
                    log.info("Returning " + itemsPage.getTotalItems() + " application review items. Filter: " + filter);
                })
                .onErrorResume(err -> {
                    log.log(Level.SEVERE, "Error getting application review items. Filter: " + filter, err);
                    return Mono.error(new BaseException("Error getting application review items. Filter: " + filter + " Error: " + err.getMessage()));
                });
    }

    private Mono<ItemsPage<ApplicationReviewItem>> getApplicationReviewItemsPage(
            int page, int size, State state) {

        return creditApplicationRepository.findByStateId(state.getStateId(), page, size)
                .flatMap(creditApplicationsPage -> {
                    Flux<ApplicationReviewItem> reviewItems = Flux.fromIterable(creditApplicationsPage.getItems())
                            .flatMap(creditApplication -> buildApplicationReviewItem(state, creditApplication));

                    return reviewItems.collectList()
                            .map(items -> {
                                ItemsPage<ApplicationReviewItem> itemsPage = new ItemsPage<>();
                                itemsPage.setPage(creditApplicationsPage.getPage());
                                itemsPage.setTotalPages(creditApplicationsPage.getTotalPages());
                                itemsPage.setTotalItems(creditApplicationsPage.getTotalItems());
                                itemsPage.setSize(creditApplicationsPage.getSize());
                                itemsPage.setItems(items);
                                return itemsPage;
                            });
                });
    }

    private Mono<ApplicationReviewItem> buildApplicationReviewItem(State state, CreditApplication creditApplication) {

        Mono<User> userMono = userGateway.getUserByEmail(creditApplication.getEmail())
                .onErrorResume(e -> {
                    log.log(Level.SEVERE, "Could not get user from gateway: " + e.getMessage());
                    return Mono.empty(); // returns empty so zip can complete
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.log(Level.SEVERE,"Could not find user: " + creditApplication.getEmail() + ". Returning default");
                    return Mono.just(new User());
                }));

        Mono<CreditType> creditTypeMono = creditTypeRepository.findById(creditApplication.getCreditTypeId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.log(Level.SEVERE,"Could not find credit type: " + creditApplication.getCreditTypeId() + ". Returning default");
                    return Mono.just(new CreditType());
                }));

        Mono<Double> monthlyDebtMono = calculateExistingMonthlyTotalDebt(state, creditApplication);

        return Mono.zip(userMono, creditTypeMono, monthlyDebtMono)
                .map(tuple -> {
                    User user = tuple.getT1();
                    CreditType creditType = tuple.getT2();
                    Double monthlyDebt = tuple.getT3();

                    ApplicationReviewItem item = new ApplicationReviewItem();

                    item.setInstallments(creditApplication.getInstallments());
                    item.setApplicationState(state.getName());
                    item.setAmount(creditApplication.getCreditAmount());

                    item.setEmail(user.getEmail());
                    item.setName(user.getName());
                    item.setBaseSalary(user.getBaseSalary());

                    item.setCreditType(creditType.getName());
                    item.setInterestRate(creditType.getInterestRate());

                    item.setTotalMonthlyDebtApprovedApplications(monthlyDebt);
                    return item;
                })
                .switchIfEmpty(Mono.just(new ApplicationReviewItem()));
    }

    private Mono<Double> calculateExistingMonthlyTotalDebt(State state, CreditApplication i) {
        return stateRepository.findByName(DefaultValues.APPROVED_APPLICATION.getValue())
                .flatMapMany(approvedState -> creditApplicationRepository.findByStateIdAndEmail(approvedState.getStateId(), i.getEmail()))
                .flatMap(creditApplication ->
                        creditTypeRepository.findById(creditApplication.getCreditTypeId())
                                .map(creditType -> {
                                    Double totalDebt = creditApplication.getCreditAmount();
                                    Double monthlyRate = Math.pow(1 + creditType.getInterestRate(), 1.0 / 12) - 1;
                                    Integer n = creditApplication.getInstallments();
                                    if (n <= 0) return 0.0;
                                    return (totalDebt * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -n));
                                }))
                .reduce(0.0, Double::sum)
                .switchIfEmpty(Mono.just(0.0));
    }
}