package com.example.calculatorapi.dto;

import java.math.BigDecimal;

public record CalculationResponse(
    String correlationId,
    BigDecimal result,
    String errorMessage
) {}
