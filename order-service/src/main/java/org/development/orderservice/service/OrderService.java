package org.development.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.MultiMap;
import org.development.orderservice.config.WebClientConfig;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderLineItemsDto;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.model.Order;
import org.development.orderservice.model.OrderLineItems;
import org.development.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public InventoryResponse[] placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItemsStream = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapFromDto)
                .toList();

        order.setOrderLineItemsList(orderLineItemsStream);

//        UriBuilder uriBuilder = UriBuilder.
        Map<String, String> params = orderLineItemsStream.stream()
                .collect(Collectors.toMap(
                        OrderLineItems::getSkuCode,
                        o -> o.getQuantity().toString()
                ));
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        params.forEach(multiValueMap::add);

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParams(multiValueMap).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponses)
                .allMatch(InventoryResponse::isInStock);

        if (allProductsInStock){
            orderRepository.save(order);
            log.info("Order created: {}", order.getOrderNumber());
        }
        else log.error("Not all products in stock, only: " + Arrays.toString(inventoryResponses));
        return inventoryResponses;
    }

    private OrderLineItems mapFromDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
