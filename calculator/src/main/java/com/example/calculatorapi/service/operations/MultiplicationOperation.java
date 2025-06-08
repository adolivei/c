package com.example.calculatorapi.service.operations;

import com.example.calculatorapi.service.CalculatorOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MultiplicationOperation implements CalculatorOperation {

    private final int scale;
    private final RoundingMode roundingMode;

    public MultiplicationOperation(
            @Value("${calculator.multiplication.scale:1}") int scale,
            @Value("${calculator.multiplication.rounding-mode:HALF_UP}") RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    @Override
    public BigDecimal calculate(BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(scale, roundingMode);
    }

    @Override
    public String getOperationType() {
        return "multiplication";
    }
} 