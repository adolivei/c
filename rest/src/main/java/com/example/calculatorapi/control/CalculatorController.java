package com.example.calculatorapi.control;

import com.example.calculatorapi.dto.CalculationRequest;
import com.example.calculatorapi.dto.CalculationResponse;
import com.example.calculatorapi.dto.RestCalculationRequest;
import com.example.calculatorapi.dto.RestCalculationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CalculatorController {

    private final KafkaTemplate<String, CalculationRequest> requestTemplate;
    private final Map<String, CompletableFuture<CalculationResponse>> pending = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 5000;

    Map<String, CompletableFuture<CalculationResponse>> getPending() {
        return pending;
    }

    @KafkaListener(topics = "calc-responses", groupId = "rest-group", containerFactory = "restKafkaListenerContainerFactory")
    public void listenResponses(CalculationResponse calcResponse) {
        try {
            MDC.put("correlationId", calcResponse.correlationId());
            log.debug("Received calculation response for correlation ID: {}", calcResponse.correlationId());
            CompletableFuture<CalculationResponse> future = pending.get(calcResponse.correlationId());
            if (future != null) {
                if (calcResponse.errorMessage() != null) {
                    future.completeExceptionally(new RuntimeException(calcResponse.errorMessage()));
                } else {
                    future.complete(calcResponse);
                }
                pending.remove(calcResponse.correlationId());
            }
        } finally {
            MDC.clear();
        }
    }

    @PostMapping("/calculate")
    @SneakyThrows
    public ResponseEntity<RestCalculationResponse> calculate(@Valid @RequestBody RestCalculationRequest request) {

        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        log.info("Received calculation request: {} {} {}", request.a(), request.operation(), request.b());

        if (request.operation() == null) {
            return ResponseEntity.badRequest()
                .body(new RestCalculationResponse(null, "Operation cannot be null"));
        }
        if (request.a() == null) {
            return ResponseEntity.badRequest()
                .body(new RestCalculationResponse(null, "First number cannot be null"));
        }
        if (request.b() == null) {
            return ResponseEntity.badRequest()
                .body(new RestCalculationResponse(null, "Second number cannot be null"));
        }

        var calcRequest = new CalculationRequest(
            request.operation().name().toLowerCase(),
            request.a(),
            request.b(),
            correlationId
        );

        CompletableFuture<CalculationResponse> future = new CompletableFuture<>();
        pending.put(correlationId, future);

        requestTemplate.send("calc-requests", calcRequest);

        try {
            CalculationResponse response = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            log.info("Calculation completed successfully: {} {} {} = {}",
                request.a(), request.operation(), request.b(), response.result());
            return ResponseEntity.ok(new RestCalculationResponse(response.result(), null));
        } catch (TimeoutException e) {
            log.error("Calculation timed out after {} ms", TIMEOUT_MS);
            pending.remove(correlationId);
            return ResponseEntity.badRequest()
                .body(new RestCalculationResponse(null, "Request timed out"));
        } catch (Exception e) {
            log.error("Calculation failed: {}", e.getMessage());
            pending.remove(correlationId);
            return ResponseEntity.badRequest()
                .body(new RestCalculationResponse(null, e.getMessage()));
        } finally {
            MDC.clear();
        }
    }
} 