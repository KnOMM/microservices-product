package org.development.orderservice.junit.controller;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.development.orderservice.controller.OrderController;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderDto;
import org.development.orderservice.dto.OrderLineItemsDto;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.model.OrderLineItems;
import org.development.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// slice test for presentation layer
@WebMvcTest(OrderController.class)
//@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;
    @MockBean
    private Tracer tracer;

    List<OrderDto> ordersDto = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ordersDto = List.of(
                new OrderDto(1L, "order_number_1", List.of(
                        new OrderLineItems(1L, "sku_1", BigDecimal.ONE, 2)
                        , new OrderLineItems(2L, "sku_2", BigDecimal.TEN, 4)
                        , new OrderLineItems(3L, "sku_3", BigDecimal.ONE, 1)
                ))
        );
    }

    @Test
    void shouldReturnAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(ordersDto);

        mockMvc.perform(get("/api/order"))
                .andDo(print())
                .andExpectAll(
                        status().isOk()
                        , content().json(objectMapper.writeValueAsString(ordersDto))
                );
    }

    @Test
    void shouldSaveValidOrder() throws Exception {
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(
                        List.of(
                                OrderLineItemsDto.builder()
                                        .skuCode("sku_4")
                                        .price(BigDecimal.TEN)
                                        .quantity(2)
                                        .build(),
                                OrderLineItemsDto.builder()
                                        .skuCode("sku_5")
                                        .price(BigDecimal.ONE)
                                        .quantity(1)
                                        .build()
                        )
                )
                .build();

        InventoryResponse[] inventoryResponses = orderRequest.getOrderLineItemsDtoList().stream()
                .map(orderR ->
                        new InventoryResponse(orderR.getSkuCode(), true)
                ).toArray(InventoryResponse[]::new);

        when(orderService.placeOrder(orderRequest)).thenReturn(inventoryResponses);

        String json = (objectMapper.writeValueAsString(inventoryResponses));

        // for asynchronous testing
        MvcResult mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpectAll(
                        status().isOk()
                        , request().asyncStarted()
                        , request().asyncResult
                                (ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponses))
                )
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andDo(print())
                .andExpectAll(
                        status().isCreated()
                        , content().json(json)
                );
    }

    @Test
    void shouldNotSaveInvalidOrder() throws Exception {
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(
                        List.of(
                                OrderLineItemsDto.builder()
                                        .skuCode("sku_4")
                                        .price(BigDecimal.TEN)
                                        .quantity(2)
                                        .build(),
                                OrderLineItemsDto.builder()
                                        .skuCode("sku_5")
                                        .price(BigDecimal.ONE)
                                        .quantity(1)
                                        .build()
                        )
                )
                .build();

        InventoryResponse[] inventoryResponses = orderRequest.getOrderLineItemsDtoList().stream()
                .map(orderR ->
                        new InventoryResponse(orderR.getSkuCode(), false)
                ).toArray(InventoryResponse[]::new);

        when(orderService.placeOrder(orderRequest)).thenReturn(inventoryResponses);

        String json = (objectMapper.writeValueAsString(inventoryResponses));

        // for asynchronous testing
        MvcResult mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpectAll(
                        status().isOk()
                        , request().asyncStarted()
                        , request().asyncResult
                                (ResponseEntity.status(HttpStatus.CONFLICT).body(inventoryResponses))
                )
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andDo(print())
                .andExpectAll(
                        status().isConflict()
                        , content().json(json)
                );
    }
}