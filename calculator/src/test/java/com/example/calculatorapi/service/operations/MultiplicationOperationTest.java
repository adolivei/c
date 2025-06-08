package com.example.calculatorapi.service.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class MultiplicationOperationTest {
    private final MultiplicationOperation operation = new MultiplicationOperation(1, RoundingMode.HALF_UP);

    @Test
    void getOperationType_returnsMultiplication() {
        assertThat(operation.getOperationType()).isEqualTo("multiplication");
    }

    @ParameterizedTest
    @CsvSource({
        "2.0, 3.0, 6.0",
        "0.0, 5.0, 0.0",
        "-2.0, 3.0, -6.0",
        "2.5, 2.0, 5.0",
        "-2.5, -2.0, 5.0"
    })
    void calculate_returnsCorrectMultiplication(String a, String b, String expected) {
        BigDecimal result = operation.calculate(new BigDecimal(a), new BigDecimal(b));
        assertThat(result).isEqualTo(new BigDecimal(expected));
    }

    @Test
    void calculate_preservesConfiguredScale() {
        BigDecimal result = operation.calculate(new BigDecimal("2.55"), new BigDecimal("2.0"));
        assertThat(result).isEqualTo(new BigDecimal("5.1"));
        assertThat(result.scale()).isEqualTo(1);
    }
} 