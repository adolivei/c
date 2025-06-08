package com.example.calculatorapi.dto;

import com.example.calculatorapi.dto.Operation;
import java.math.BigDecimal;

public record RestCalculationRequest(
    BigDecimal a,
    BigDecimal b,
    Operation operation
) {
} 