package com.crediya.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateApplicationRequest(
        @JsonProperty("documento_identidad") String cardId,
        @JsonProperty("email") String email,
        @JsonProperty("monto") Double amount,
        @JsonProperty("plazo") Integer installments,
        @JsonProperty("tipo_credito") String creditType
) {}
