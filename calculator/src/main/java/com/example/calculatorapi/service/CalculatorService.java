package com.example.calculatorapi.service;

import com.example.calculatorapi.dto.CalculationRequest;
import com.example.calculatorapi.dto.CalculationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorService {
    private final KafkaTemplate<String, CalculationResponse> kafkaTemplate;
    private final CalculatorOperationFactory operationFactory;

    @KafkaListener(topics = "calc-requests", groupId = "calculator-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleCalculation(CalculationRequest request) {
        try {
            MDC.put("correlationId", request.correlationId());
            log.info("Received calculation request: {} {} {}", request.a(), request.operation(), request.b());

            try {
                CalculatorOperation operation = operationFactory.getOperation(request.operation());
                BigDecimal result = operation.calculate(request.a(), request.b());

                log.info("Calculation result: {} {} {} = {}", request.a(), request.operation(), request.b(), result);
                sendResponse(request.correlationId(), result, null);

            } catch (ArithmeticException e) {
                log.error("Arithmetic error in calculation: {}", e.getMessage());
                sendResponse(request.correlationId(), null, e.getMessage());
            } catch (IllegalArgumentException e) {
                log.error("Invalid operation: {}", e.getMessage());
                sendResponse(request.correlationId(), null, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error in calculation: {}", e.getMessage());
                sendResponse(request.correlationId(), null, "Internal server error");
            }
        } finally {
            MDC.clear();
        }
    }

    private void sendResponse(String correlationId, BigDecimal result, String errorMessage) {
        CalculationResponse response = new CalculationResponse(correlationId, result, errorMessage);
        kafkaTemplate.send("calc-responses", response);
    }
}
