package org.development.orderservice.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Observed(name = "order.name",
            contextualName = "Order checks Inventory",
            lowCardinalityKeyValues = {"test", "value"})
    public CompletableFuture<InventoryResponse[]> placeOrder(@RequestBody OrderRequest orderRequest) {

        log.info("Checking values: {}",orderRequest );
        return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));
    }

    // error: service is unavailable
    public CompletableFuture<InventoryResponse[]> fallbackMethod(OrderRequest orderRequest, WebClientResponseException exception) {
        log.error(exception.getMessage());
        return CompletableFuture.completedFuture(new InventoryResponse[0]);
    }
}
