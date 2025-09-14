package com.crediya.usecase.listapplicationsmanualreview;

import com.crediya.model.applicationreviewitem.ApplicationReviewItem;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.page.ItemsPage;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.model.user.gateways.UserGateway;
import com.crediya.usecase.exception.NotValidFilterException;
import com.crediya.usecase.util.DefaultValues;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListApplicationsManualReviewUseCase {

    private final StateRepository stateRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditTypeRepository creditTypeRepository;
    private final UserGateway userGateway;

    public Mono<ItemsPage<ApplicationReviewItem>> execute(String filter, int page, int size) {

        ItemsPage<ApplicationReviewItem> itemsPage = new ItemsPage<>();

        return stateRepository.findByName(filter)
                .switchIfEmpty(Mono.error(new NotValidFilterException(filter)))
                .flatMap(s -> getApplicationReviewItemsPage(page, size, s, itemsPage));
    }

    private Mono<ItemsPage<ApplicationReviewItem>> getApplicationReviewItemsPage(
            int page, int size, State s, ItemsPage<ApplicationReviewItem> itemsPage) {

        return creditApplicationRepository.findByStateId(s.getStateId(), page, size)
                .flatMap(r -> {

                    itemsPage.setPage(r.getPage());
                    itemsPage.setTotalPages(r.getTotalPages());
                    itemsPage.setTotalItems(r.getTotalItems());
                    itemsPage.setSize(r.getSize());

                    r.getItems().forEach(i -> buildApplicationReviewItem(s, itemsPage, i));

                    return Mono.just(itemsPage);
                });
    }

    private void buildApplicationReviewItem(State s, ItemsPage<ApplicationReviewItem> itemsPage, CreditApplication i) {
        ApplicationReviewItem item = new ApplicationReviewItem();

        item.setInstallments(i.getInstallments());
        item.setApplicationState(s.getName());
        item.setAmount(i.getCreditAmount());
        getUserInformationForItem(i, item);

        getCreditTypeInformationForItem(i, item);

        calculateExistingMonthlyTotalDebt(s, i, item);

        itemsPage.getItems().add(item);
    }

    private void calculateExistingMonthlyTotalDebt(State s, CreditApplication i, ApplicationReviewItem item) {
        stateRepository.findByName(DefaultValues.APPROVED_APPLICATION.getValue()).subscribe(
                a -> {
                    creditApplicationRepository.findByStateIdAndEmail(s.getStateId(), i.getEmail())
                            .collectList()
                            .subscribe(
                                    list -> {
                                        item.setTotalMonthlyDebtApprovedApplications(
                                                list.stream().map(CreditApplication::getCreditAmount)
                                                        .reduce(0.0, Double::sum)
                                        );
                                    }
                            );
                }
        );
    }

    private void getCreditTypeInformationForItem(CreditApplication i, ApplicationReviewItem item) {
        creditTypeRepository.findById(i.getCreditTypeId()).subscribe(
                c -> {
                    item.setCreditType(c.getName());
                    item.setInterestRate(c.getInterestRate());
                }
        );
    }

    private void getUserInformationForItem(CreditApplication i, ApplicationReviewItem item) {
        userGateway.getUserByEmail(i.getEmail()).subscribe(
                u -> {
                    item.setEmail(u.getEmail());
                    item.setName(u.getName());
                    item.setBaseSalary(u.getBaseSalary());
                }
        );
    }
}
