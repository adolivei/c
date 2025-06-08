package com.example.calculatorapi.service.operations;

import com.example.calculatorapi.service.CalculatorOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DivisionOperation implements CalculatorOperation {
    private final int scale;
    private final RoundingMode roundingMode;

    public DivisionOperation(
            @Value("${calculator.division.scale:10}") int scale,
            @Value("${calculator.division.rounding-mode:HALF_UP}") RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    @Override
    public BigDecimal calculate(BigDecimal a, BigDecimal b) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a.divide(b, scale, roundingMode);
    }

    @Override
    public String getOperationType() {
        return "division";
    }
} 