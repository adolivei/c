package com.example.calculatorapi.service.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SubtractionOperationTest {
    private final SubtractionOperation operation = new SubtractionOperation();

    @Test
    void getOperationType_returnsSubtraction() {
        assertThat(operation.getOperationType()).isEqualTo("subtraction");
    }

    @ParameterizedTest
    @CsvSource({
        "3.0, 2.0, 1.0",
        "0.0, 0.0, 0.0",
        "-1.0, 1.0, -2.0",
        "10.5, 5.5, 5.0",
        "-10.5, -5.5, -5.0"
    })
    void calculate_returnsCorrectSubtraction(String a, String b, String expected) {
        BigDecimal result = operation.calculate(new BigDecimal(a), new BigDecimal(b));
        assertThat(result).isEqualTo(new BigDecimal(expected));
    }
} 