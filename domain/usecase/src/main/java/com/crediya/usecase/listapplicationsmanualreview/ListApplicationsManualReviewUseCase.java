package com.crediya.usecase.listapplicationsmanualreview;

import com.crediya.model.applicationreviewitem.ApplicationReviewItem;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.page.ItemsPage;
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
                .flatMap(s -> {
                    return creditApplicationRepository.findByStateId(s.getStateId(), page, size)
                            .flatMap(r -> {
                                r.getItems().forEach(i -> {
                                    ApplicationReviewItem item = new ApplicationReviewItem();

                                    item.setInstallments(i.getInstallments());
                                    item.setApplicationState(s.getName());
                                    itemsPage.setPage(r.getPage());
                                    itemsPage.setTotalPages(r.getTotalPages());
                                    itemsPage.setTotalItems(r.getTotalItems());
                                    itemsPage.setSize(r.getSize());


                                    userGateway.getUserByEmail(i.getEmail()).subscribe(
                                            u -> {
                                                item.setEmail(u.getEmail());
                                                item.setName(u.getName());
                                                item.setBaseSalary(u.getBaseSalary());
                                            }
                                    );

                                    creditTypeRepository.findById(i.getCreditTypeId()).subscribe(
                                            c -> {
                                                item.setCreditType(c.getName());
                                                item.setInterestRate(c.getInterestRate());
                                            }
                                    );

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

                                    itemsPage.getItems().add(item);
                                });
                                return Mono.just(itemsPage);
                            });
                });
    }
}
