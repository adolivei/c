package com.example.calculatorapi.service;

import com.example.calculatorapi.service.operations.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatorOperationFactoryTest {
    @Mock
    private SumOperation sumOperation;
    @Mock
    private SubtractionOperation subtractionOperation;
    @Mock
    private MultiplicationOperation multiplicationOperation;
    @Mock
    private DivisionOperation divisionOperation;

    private CalculatorOperationFactory factory;

    @BeforeEach
    void setUp() {
        when(sumOperation.getOperationType()).thenReturn("sum");
        when(subtractionOperation.getOperationType()).thenReturn("subtraction");
        when(multiplicationOperation.getOperationType()).thenReturn("multiplication");
        when(divisionOperation.getOperationType()).thenReturn("division");

        factory = new CalculatorOperationFactory(List.of(
            sumOperation,
            subtractionOperation,
            multiplicationOperation,
            divisionOperation
        ));
    }

    @Test
    void getOperation_returnsCorrectOperation() {
        assertThat(factory.getOperation("sum")).isSameAs(sumOperation);
        assertThat(factory.getOperation("subtraction")).isSameAs(subtractionOperation);
        assertThat(factory.getOperation("multiplication")).isSameAs(multiplicationOperation);
        assertThat(factory.getOperation("division")).isSameAs(divisionOperation);
    }

    @Test
    void getOperation_caseInsensitive() {
        assertThat(factory.getOperation("SUM")).isSameAs(sumOperation);
        assertThat(factory.getOperation("Subtraction")).isSameAs(subtractionOperation);
    }

    @Test
    void getOperation_unsupportedOperation_throwsException() {
        assertThatThrownBy(() -> factory.getOperation("unsupported"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported operation: unsupported");
    }
} 