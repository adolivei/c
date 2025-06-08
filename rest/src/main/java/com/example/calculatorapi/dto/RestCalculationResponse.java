package com.example.calculatorapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestCalculationResponse(
    BigDecimal result,
    String error
) {
} 