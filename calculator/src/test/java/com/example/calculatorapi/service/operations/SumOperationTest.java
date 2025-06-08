package com.example.calculatorapi.service.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SumOperationTest {
    private final SumOperation operation = new SumOperation();

    @Test
    void getOperationType_returnsSum() {
        assertThat(operation.getOperationType()).isEqualTo("sum");
    }

    @ParameterizedTest
    @CsvSource({
        "1.0, 2.0, 3.0",
        "0.0, 0.0, 0.0",
        "-1.0, 1.0, 0.0",
        "10.5, 5.5, 16.0",
        "-10.5, -5.5, -16.0"
    })
    void calculate_returnsCorrectSum(String a, String b, String expected) {
        BigDecimal result = operation.calculate(new BigDecimal(a), new BigDecimal(b));
        assertThat(result).isEqualTo(new BigDecimal(expected));
    }
} 