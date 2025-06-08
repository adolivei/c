package com.example.calculatorapi.service.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivisionOperationTest {
    private final DivisionOperation operation = new DivisionOperation(10, RoundingMode.HALF_UP);

    @Test
    void getOperationType_returnsDivision() {
        assertThat(operation.getOperationType()).isEqualTo("division");
    }

    @ParameterizedTest
    @CsvSource({
        "6.0, 2.0, 3.0000000000",
        "0.0, 5.0, 0.0000000000",
        "-6.0, 2.0, -3.0000000000",
        "5.0, 2.0, 2.5000000000",
        "-5.0, -2.0, 2.5000000000"
    })
    void calculate_returnsCorrectDivision(String a, String b, String expected) {
        BigDecimal result = operation.calculate(new BigDecimal(a), new BigDecimal(b));
        assertThat(result).isEqualTo(new BigDecimal(expected));
    }

    @Test
    void calculate_divisionByZero_throwsArithmeticException() {
        assertThatThrownBy(() -> 
            operation.calculate(new BigDecimal("10"), BigDecimal.ZERO)
        ).isInstanceOf(ArithmeticException.class)
         .hasMessage("Division by zero");
    }
} 