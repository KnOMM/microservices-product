package org.development.orderservice.integration;

import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderDto;
import org.development.orderservice.dto.OrderLineItemsDto;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Transactional
public class WebClientTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:13-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnOrdersFromTestData() {
        ResponseEntity<List<OrderDto>> responseEntity = restTemplate.exchange(
                "/api/order",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(responseEntity.getBody()).isNotNull();
        List<OrderDto> allOrders = responseEntity.getBody();
        assertThat(allOrders.size()).isEqualTo(0);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Circuit breaker fallback test
    @Test
    void shouldReturnErrorAndResponseWhenInventoryServiceUnavailable() {

        OrderRequest orderRequest = new OrderRequest(List.of(
                OrderLineItemsDto.builder()
                        .skuCode("sku_code")
                        .quantity(3)
                        .price(BigDecimal.ONE)
                        .build()
        ));
        HttpEntity<OrderRequest> orderRequestRequestEntity = new HttpEntity<>(orderRequest);
        ResponseEntity<InventoryResponse[]> responseEntity = restTemplate.exchange(
                "/api/order",
                HttpMethod.POST,
                orderRequestRequestEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(responseEntity
                .getStatusCode()
                .isSameCodeAs(HttpStatusCode.valueOf(400)))
                .isTrue();
        assertThat(responseEntity.getBody()).isEqualTo(new InventoryResponse[]{});
    }
}
