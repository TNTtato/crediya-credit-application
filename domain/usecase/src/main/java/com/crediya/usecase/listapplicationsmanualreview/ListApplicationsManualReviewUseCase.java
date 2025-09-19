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
import com.crediya.usecase.exception.NotValidFilterException;
import com.crediya.usecase.util.DefaultValues;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListApplicationsManualReviewUseCase {

    private final StateRepository stateRepository;
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditTypeRepository creditTypeRepository;
    private final UserGateway userGateway;

    public Mono<ItemsPage<ApplicationReviewItem>> execute(String filter, int page, int size) {
        return stateRepository.findByName(filter)
                .switchIfEmpty(Mono.error(new NotValidFilterException(filter)))
                .flatMap(state -> getApplicationReviewItemsPage(page, size, state));
    }

    private Mono<ItemsPage<ApplicationReviewItem>> getApplicationReviewItemsPage(
            int page, int size, State state) {

        return creditApplicationRepository.findByStateId(state.getStateId(), page, size)
                .flatMap(creditApplicationsPage -> {
                    // Mapea cada CreditApplication a un Mono<ApplicationReviewItem>
                    Flux<ApplicationReviewItem> reviewItems = Flux.fromIterable(creditApplicationsPage.getItems())
                            .flatMap(creditApplication -> buildApplicationReviewItem(state, creditApplication));

                    // Combina los resultados del Flux en una lista
                    return reviewItems.collectList()
                            .map(items -> {
                                // Construye el ItemsPage con todos los datos combinados
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
        // El Mono del usuario debe manejar el caso de que no se encuentre
        Mono<User> userMono = userGateway.getUserByEmail(creditApplication.getEmail())
                .doOnNext(user -> {
                    System.out.println("[buildApplicationReviewItem] User: " + user);
                })
                .onErrorResume(e -> {
                    // log para saber que falló
                    System.out.println("Error al obtener usuario del gateway" + e.getMessage());
                    return Mono.empty(); // devuelve Mono.empty() para que Mono.zip se complete como vacío
                })
                // Si el Mono del userGateway es vacío, crea un usuario por defecto
                .switchIfEmpty(Mono.defer(() -> {
                    System.out.println("[buildApplicationReviewItem] Usuario venía vacío: " + creditApplication.getEmail());
                    return Mono.just(new User());
                }));

        // El Mono del tipo de crédito debe manejar el caso de que no se encuentre
        Mono<CreditType> creditTypeMono = creditTypeRepository.findById(creditApplication.getCreditTypeId())
                .switchIfEmpty(Mono.just(new CreditType()));

        // El Mono de la deuda mensual ya lo arreglamos, pero asegúrate de que tiene un .switchIfEmpty(Mono.just(0.0));
        Mono<Double> monthlyDebtMono = calculateExistingMonthlyTotalDebt(state, creditApplication);

        return Mono.zip(userMono, creditTypeMono, monthlyDebtMono)
                .map(tuple -> {
                    User user = tuple.getT1();
                    CreditType creditType = tuple.getT2();
                    Double monthlyDebt = tuple.getT3();
                    System.out.println("Tipo Interes debug " + creditType);

                    // Tu lógica de construcción de ApplicationReviewItem...
                    ApplicationReviewItem item = new ApplicationReviewItem();

                    // Asegúrate de que tu lógica maneje los objetos por defecto
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
                // Si por alguna razón el zip es vacío (ej: si el usuario o tipo de crédito es null),
                // el item no se crea y el mono es vacío, por lo tanto usamos switchIfEmpty
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
                                    System.out.printf("Application: %d Installments: %d Debt: %f Monthly Rate: %.2f%n", creditApplication.getApplicationId(), n, totalDebt, monthlyRate);
                                    if (n <= 0) return 0.0;
                                    return (totalDebt * monthlyRate) / (1 - Math.pow(1 + monthlyRate, -n));
                                }))
                .reduce(0.0, Double::sum)
                .switchIfEmpty(Mono.just(0.0));
    }
}