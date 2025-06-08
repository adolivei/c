package com.example.calculatorapi.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CalculatorOperationFactory {
    private final Map<String, CalculatorOperation> operations;

    public CalculatorOperationFactory(List<CalculatorOperation> operationServices) {
        this.operations = operationServices.stream()
            .collect(Collectors.toMap(
                CalculatorOperation::getOperationType,
                Function.identity()
            ));
    }

    public CalculatorOperation getOperation(String operationType) {
        CalculatorOperation operation = operations.get(operationType.toLowerCase());
        if (operation == null) {
            throw new IllegalArgumentException("Unsupported operation: " + operationType);
        }
        return operation;
    }
} 