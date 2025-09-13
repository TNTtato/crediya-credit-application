package com.crediya.model.applicationreviewitem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class ApplicationReviewItem {

    private Double amount;
    private Integer installments;
    private String email;
    private String name;
    private String creditType;
    private Float interestRate;
    private String applicationState;
    private Double baseSalary;
    private Double totalMonthlyDebtApprovedApplications;
}
