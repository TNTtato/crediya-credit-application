package com.crediya.model.credittype;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class CreditType {
    Integer creditTypeId;
    String name;
    Double minAmount;
    Double maxAmount;
    Float interestRate;
    Boolean automaticValidation;
}
