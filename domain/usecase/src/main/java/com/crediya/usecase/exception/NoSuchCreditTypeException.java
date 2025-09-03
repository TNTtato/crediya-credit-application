package com.crediya.usecase.exception;

public class NoSuchCreditTypeException extends IllegalArgumentException {

    private String creditType;

    public NoSuchCreditTypeException(String message, String creditType) {
        super(message);
        this.creditType = creditType;
    }

    public String getCreditType() {
        return creditType;
    }
}
