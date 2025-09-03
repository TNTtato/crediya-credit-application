package com.crediya.model.credittype;
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
public class CreditType {
    Integer creditTypeId;
    String name;
    Double minAmount;
    Double maxAmount;
    Float interestRate;
    Boolean automaticValidation;
}
