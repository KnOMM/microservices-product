package org.development.orderservice.junit.controller;

import org.development.orderservice.controller.OrderController;
import org.development.orderservice.dto.OrderDto;
import org.development.orderservice.model.OrderLineItems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// slice test for presentation layer
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    List<OrderDto> ordersDto = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ordersDto = List.of(
                new OrderDto(1L, "order_number_1", List.of(
                        new OrderLineItems(1L, "sku_1", BigDecimal.ONE, 2),
                        new OrderLineItems(2L, "sku_2", BigDecimal.TEN, 4),
                        new OrderLineItems(3L, "sku_3", BigDecimal.ONE, 1)
                ))
        );
    }
}