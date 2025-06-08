package com.example.calculatorapi.service;

import com.example.calculatorapi.dto.CalculationRequest;
import com.example.calculatorapi.dto.CalculationResponse;
import com.example.calculatorapi.service.CalculatorOperation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceTest {
    @InjectMocks
    private CalculatorService calculatorService;

    @Mock
    private KafkaTemplate<String, CalculationResponse> kafkaTemplate;

    @Mock
    private CalculatorOperationFactory operationFactory;

    @Mock
    private CalculatorOperation calculatorOperation;

    @Captor
    private ArgumentCaptor<CalculationResponse> responseCaptor;

    private String correlationId;
    private CalculationRequest request;

    @BeforeEach
    void setUp() {
        correlationId = UUID.randomUUID().toString();
        request = new CalculationRequest(
            "sum",
            new BigDecimal("10.5"),
            new BigDecimal("5.5"),
            correlationId
        );
    }

    @Test
    @SneakyThrows
    void handleCalculation_successfulOperation_sendsSuccessResponse() {
        // Given
        when(operationFactory.getOperation("sum")).thenReturn(calculatorOperation);
        when(calculatorOperation.calculate(request.a(), request.b()))
            .thenReturn(new BigDecimal("16.0"));

        // When
        calculatorService.handleCalculation(request);

        // Then
        verify(kafkaTemplate).send(eq("calc-responses"), responseCaptor.capture());
        CalculationResponse response = responseCaptor.getValue();
        assertThat(response.correlationId()).isEqualTo(correlationId);
        assertThat(response.result()).isEqualTo(new BigDecimal("16.0"));
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    @SneakyThrows
    void handleCalculation_arithmeticError_sendsErrorResponse() {
        // Given
        when(operationFactory.getOperation("division")).thenReturn(calculatorOperation);
        when(calculatorOperation.calculate(any(), any()))
            .thenThrow(new ArithmeticException("Division by zero"));

        request = new CalculationRequest(
            "division",
            new BigDecimal("10"),
            BigDecimal.ZERO,
            correlationId
        );

        // When
        calculatorService.handleCalculation(request);

        // Then
        verify(kafkaTemplate).send(eq("calc-responses"), responseCaptor.capture());
        CalculationResponse response = responseCaptor.getValue();
        assertThat(response.correlationId()).isEqualTo(correlationId);
        assertThat(response.result()).isNull();
        assertThat(response.errorMessage()).isEqualTo("Division by zero");
    }

    @Test
    @SneakyThrows
    void handleCalculation_unsupportedOperation_sendsErrorResponse() {
        // Given
        when(operationFactory.getOperation("unsupported"))
            .thenThrow(new IllegalArgumentException("Unsupported operation: unsupported"));

        request = new CalculationRequest(
            "unsupported",
            new BigDecimal("10"),
            new BigDecimal("5"),
            correlationId
        );

        // When
        calculatorService.handleCalculation(request);

        // Then
        verify(kafkaTemplate).send(eq("calc-responses"), responseCaptor.capture());
        CalculationResponse response = responseCaptor.getValue();
        assertThat(response.correlationId()).isEqualTo(correlationId);
        assertThat(response.result()).isNull();
        assertThat(response.errorMessage()).isEqualTo("Unsupported operation: unsupported");
    }

    @Test
    @SneakyThrows
    void handleCalculation_unexpectedError_sendsInternalErrorResponse() {
        // Given
        when(operationFactory.getOperation("sum")).thenReturn(calculatorOperation);
        when(calculatorOperation.calculate(any(), any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When
        calculatorService.handleCalculation(request);

        // Then
        verify(kafkaTemplate).send(eq("calc-responses"), responseCaptor.capture());
        CalculationResponse response = responseCaptor.getValue();
        assertThat(response.correlationId()).isEqualTo(correlationId);
        assertThat(response.result()).isNull();
        assertThat(response.errorMessage()).isEqualTo("Internal server error");
    }
} 