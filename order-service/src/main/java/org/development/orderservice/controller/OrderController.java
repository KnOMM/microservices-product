package org.development.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse[] placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest);
    }
}
