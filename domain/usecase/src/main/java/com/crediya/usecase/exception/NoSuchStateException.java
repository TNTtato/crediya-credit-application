package com.crediya.usecase.exception;

public class NoSuchStateException extends IllegalArgumentException {
    private String notFoundValue;

    public NoSuchStateException(String message, String value) {
        super(message);
        this.notFoundValue = value;
    }

    public String getNotFoundValue() {
        return notFoundValue;
    }
}
