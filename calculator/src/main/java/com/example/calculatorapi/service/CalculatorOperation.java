package com.example.calculatorapi.service;

import java.math.BigDecimal;

public interface CalculatorOperation {

    BigDecimal calculate(BigDecimal a, BigDecimal b);
    String getOperationType();
} 