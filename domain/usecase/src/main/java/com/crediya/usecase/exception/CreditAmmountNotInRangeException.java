package com.crediya.usecase.exception;

public class CreditAmmountNotInRangeException  extends IllegalArgumentException {

    private Double creditAmount;

    public CreditAmmountNotInRangeException(String message, Double creditAmount) {
        super(message);
        this.creditAmount = creditAmount;
    }

    public Double getCreditAmount() {
        return creditAmount;
    }
}
