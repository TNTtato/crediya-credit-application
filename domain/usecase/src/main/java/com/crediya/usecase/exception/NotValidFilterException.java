package com.crediya.usecase.exception;

import lombok.Getter;

@Getter
public class NotValidFilterException extends IllegalArgumentException {

    private final String filter;

    public NotValidFilterException(String filter) {
      super("The filter " + filter + " is not valid");
      this.filter = filter;
    }
}
