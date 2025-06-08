package com.example.calculatorapi.dto;

import lombok.NonNull;

import java.math.BigDecimal;

public record CalculationRequest(
    @NonNull String operation,
    @NonNull BigDecimal a,
    @NonNull BigDecimal b,
    @NonNull String correlationId
) {}
