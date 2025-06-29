package com.example.calculatorapi.service.operations;

import com.example.calculatorapi.service.CalculatorOperation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SubtractionOperation implements CalculatorOperation {
    @Override
    public BigDecimal calculate(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }

    @Override
    public String getOperationType() {
        return "subtraction";
    }
} 