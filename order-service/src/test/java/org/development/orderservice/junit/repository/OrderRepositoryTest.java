package org.development.orderservice.junit.repository;

import org.development.orderservice.model.Order;
import org.development.orderservice.model.OrderLineItems;
import org.development.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:13-alpine");

    @Autowired
    OrderRepository orderRepository;

    @Test
    void connectionEstablished() {
        assertThat(container.isCreated()).isTrue();
        assertThat(container.isRunning()).isTrue();
    }

    @BeforeEach
    void setUp() {
        OrderLineItems item1 = OrderLineItems.builder()
                .skuCode("sku-code-1")
                .price(BigDecimal.ONE)
                .quantity(2)
                .build();

        Order order = Order.builder()
                .orderNumber("order-number1")
                .orderLineItemsList(List.of(item1))
                .build();

        orderRepository.save(order);
    }
}
