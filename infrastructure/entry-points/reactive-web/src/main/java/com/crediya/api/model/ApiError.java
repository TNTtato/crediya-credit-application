package com.crediya.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

@Schema(description = "Error response payload")
public record ApiError(
        @Schema(description = "Error message") String message,
        @Schema(description = "Request URI") String uri,
        @Schema(description = "Timestamp of error") Date timestamp,
        @Schema(description = "Optional causes when validation fails") String causes
) {}
