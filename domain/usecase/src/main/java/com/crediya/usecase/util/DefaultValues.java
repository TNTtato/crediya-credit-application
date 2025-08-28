package com.crediya.usecase.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DefaultValues {
    DEFAULT_APPLICATION_STATE("PENDING_REVIEW");

    private String value;
}
