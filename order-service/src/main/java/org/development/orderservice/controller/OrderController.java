package org.development.orderservice.controller;

import brave.Span;
import brave.Tracer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderDto;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final Tracer tracer;

    @PostMapping
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    public CompletableFuture<ResponseEntity<InventoryResponse[]>> placeOrder(@RequestBody @Validated OrderRequest orderRequest) {
        Span span = tracer.currentSpan();
        return CompletableFuture.supplyAsync(() -> {
            // wrapping the method to have same traceId for distributed tracing
            try (Tracer.SpanInScope ignored = tracer.withSpanInScope(span)) {
                InventoryResponse[] inventoryResponses = orderService.placeOrder(orderRequest);
                boolean allProductsInStock = Arrays.stream(inventoryResponses)
                        .allMatch(InventoryResponse::isInStock);
                if (allProductsInStock) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponses);
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(inventoryResponses);
            }
        });
    }

    // error: service is unavailable
    public CompletableFuture<ResponseEntity<InventoryResponse[]>> fallbackMethod(OrderRequest orderRequest, WebClientResponseException exception) {
        log.error(exception.getMessage());
        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new InventoryResponse[0]));
    }

    @GetMapping
    public List<OrderDto> getOrders() {
        return orderService.getAllOrders();
    }
}
