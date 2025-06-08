package com.example.calculatorapi.control;

import com.example.calculatorapi.dto.CalculationRequest;
import com.example.calculatorapi.dto.CalculationResponse;
import com.example.calculatorapi.dto.Operation;
import com.example.calculatorapi.dto.RestCalculationRequest;
import com.example.calculatorapi.dto.RestCalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "kafka.group.rest=test-group"
})
@EmbeddedKafka(partitions = 1, topics = {"calc-requests", "calc-responses"})
class CalculatorControllerTest {

    @Mock
    private KafkaTemplate<String, CalculationRequest> kafkaTemplate;

    @Captor
    private ArgumentCaptor<CalculationRequest> requestCaptor;

    private CalculatorController controller;

    @BeforeEach
    void setUp() {
        controller = new CalculatorController(kafkaTemplate);
    }

    @Test
    void calculate_ValidRequest_ReturnsSuccess() {

        RestCalculationRequest request = new RestCalculationRequest(
            new BigDecimal("10.5"),
            new BigDecimal("2.5"),
            Operation.SUM
        );

        // simulate correct response
        when(kafkaTemplate.send(eq("calc-requests"), any(CalculationRequest.class)))
            .thenAnswer(invocation -> {
                CalculationRequest capturedRequest = invocation.getArgument(1);
                CompletableFuture<CalculationResponse> future = controller.getPending().get(capturedRequest.correlationId());
                if (future != null) {
                    future.complete(new CalculationResponse(
                        capturedRequest.correlationId(),
                        new BigDecimal("13.0"),
                        null
                    ));
                }
                return null;
            });

        ResponseEntity<RestCalculationResponse> response = controller.calculate(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().result()).isEqualTo(new BigDecimal("13.0"));
        
        verify(kafkaTemplate).send(eq("calc-requests"), requestCaptor.capture());
        CalculationRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.operation()).isEqualTo("sum");
        assertThat(capturedRequest.a()).isEqualTo("10.5");
        assertThat(capturedRequest.b()).isEqualTo("2.5");
        assertThat(capturedRequest.correlationId()).isNotNull();
    }

    @Test
    void calculate_NullOperation_ReturnsBadRequest() {

        RestCalculationRequest request = new RestCalculationRequest(
            new BigDecimal("10.5"),
            new BigDecimal("2.5"),
            null
        );

        ResponseEntity<RestCalculationResponse> response = controller.calculate(request);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Operation cannot be null");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void calculate_NullFirstNumber_ReturnsBadRequest() {

        RestCalculationRequest request = new RestCalculationRequest(
            null,
            new BigDecimal("2.5"),
            Operation.SUM
        );

        ResponseEntity<RestCalculationResponse> response = controller.calculate(request);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("First number cannot be null");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void calculate_NullSecondNumber_ReturnsBadRequest() {

        RestCalculationRequest request = new RestCalculationRequest(
            new BigDecimal("10.5"),
            null,
            Operation.SUM
        );

        ResponseEntity<RestCalculationResponse> response = controller.calculate(request);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Second number cannot be null");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void listenResponses_ValidResponse_CompletesFuture() {

        String correlationId = "test-correlation-id";
        CalculationResponse calcResponse = new CalculationResponse(
            correlationId,
            new BigDecimal("13.0"),
            null
        );
        
        CompletableFuture<CalculationResponse> future = new CompletableFuture<>();
        controller.getPending().put(correlationId, future);

        controller.listenResponses(calcResponse);

        assertThat(future).isCompletedWithValue(calcResponse);
        assertThat(controller.getPending()).doesNotContainKey(correlationId);
    }

    @Test
    void listenResponses_ErrorResponse_CompletesFutureExceptionally() {

        String correlationId = "test-correlation-id";
        String errorMessage = "Division by zero";
        CalculationResponse calcResponse = new CalculationResponse(
            correlationId,
            null,
            errorMessage
        );
        
        CompletableFuture<CalculationResponse> future = new CompletableFuture<>();
        controller.getPending().put(correlationId, future);

        controller.listenResponses(calcResponse);

        assertThat(future).isCompletedExceptionally();
        assertThat(future)
            .failsWithin(1, TimeUnit.SECONDS)
            .withThrowableOfType(ExecutionException.class)
            .withCauseInstanceOf(RuntimeException.class)
            .withMessageContaining(errorMessage);
        assertThat(controller.getPending()).doesNotContainKey(correlationId);
    }

    @Test
    void calculate_Timeout_ReturnsBadRequest() {

        RestCalculationRequest request = new RestCalculationRequest(
            new BigDecimal("10.5"),
            new BigDecimal("2.5"),
            Operation.SUM
        );

        when(kafkaTemplate.send(eq("calc-requests"), any(CalculationRequest.class)))
            .thenReturn(null);

        ResponseEntity<RestCalculationResponse> response = controller.calculate(request);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Request timed out");
        assertThat(controller.getPending()).isEmpty();
    }
} 