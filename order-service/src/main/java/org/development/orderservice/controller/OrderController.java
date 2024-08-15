package org.development.orderservice.controller;

import brave.Span;
import brave.Tracer;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderDto;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    // Providing additional info in the span
//    @Observed(name = "order.name",
//            contextualName = "Order checks Inventory",
//            lowCardinalityKeyValues = {"test", "value"})
    public CompletableFuture<InventoryResponse[]> placeOrder(@RequestBody OrderRequest orderRequest) {
        Span span = tracer.currentSpan();
        return CompletableFuture.supplyAsync(() -> {
            // wrapping the method to have same traceId for distributed tracing
            try (Tracer.SpanInScope ignored = tracer.withSpanInScope(span)) {
                return orderService.placeOrder(orderRequest);
            }
        });
    }

    // error: service is unavailable
    public CompletableFuture<InventoryResponse[]> fallbackMethod(OrderRequest orderRequest, WebClientResponseException exception) {
        log.error(exception.getMessage());
        return CompletableFuture.completedFuture(new InventoryResponse[0]);
    }

    @GetMapping
    public List<OrderDto> getOrders() {
        return orderService.getAllOrders();
    }
}
