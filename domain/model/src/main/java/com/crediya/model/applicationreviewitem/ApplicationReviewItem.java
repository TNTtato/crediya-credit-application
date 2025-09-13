package com.crediya.model.applicationreviewitem;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
