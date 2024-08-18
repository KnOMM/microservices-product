package org.development.orderservice.service;

import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.development.orderservice.dto.InventoryResponse;
import org.development.orderservice.dto.OrderDto;
import org.development.orderservice.dto.OrderLineItemsDto;
import org.development.orderservice.dto.OrderRequest;
import org.development.orderservice.event.OrderPlacedEvent;
import org.development.orderservice.model.Order;
import org.development.orderservice.model.OrderLineItems;
import org.development.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final Tracer tracer;
//    private final RestTemplate restTemplate;

    public InventoryResponse[] placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItemsStream = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(OrderService::mapFromDto)
                .toList();

        order.setOrderLineItemsList(orderLineItemsStream);

        Map<String, String> params = orderLineItemsStream.stream()
                .collect(Collectors.toMap(
                        OrderLineItems::getSkuCode,
                        o -> o.getQuantity().toString()
                ));
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        params.forEach(multiValueMap::add);


        // get current span value
        InventoryResponse[] inventoryResponses;

        URI uri = UriComponentsBuilder.fromUriString("/api/inventory").queryParams(multiValueMap).build().toUri();
        inventoryResponses = webClient.get()
//                .uri("http://inventory-service/api/inventory",
//                        uriBuilder -> uriBuilder.queryParams(multiValueMap).build())
                .uri(uri)
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        // alternative with restTemplate
//        URI uri = UriComponentsBuilder.fromUriString("http://inventory-service/api/inventory").queryParams(multiValueMap).build().toUri();
//        InventoryResponse[] inventoryResponses = restTemplate.getForObject(uri, InventoryResponse[].class);


        boolean allProductsInStock = Arrays.stream(inventoryResponses)
                .allMatch(InventoryResponse::isInStock);

        if (allProductsInStock) {
//            Span span = tracer.nextSpan().name("kafka");
//            try (Tracer.SpanInScope ignore = tracer.withSpan(span)) {
//                kafkaTemplate.send("notifications", new OrderPlacedEvent(order.getOrderNumber()));
//                 Get the current trace ID from the tracer
//                String traceId = tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "no-trace-id";
//
//                 Create headers with the trace ID
//                Headers headers = new RecordHeaders();
//                headers.add("traceId", traceId.getBytes());

            // Create ProducerRecord with headers
//                ProducerRecord<String, OrderPlacedEvent> record = new ProducerRecord<>("notifications", null, null, null, new OrderPlacedEvent(order.getOrderNumber()), headers);

            // Send message
            kafkaTemplate.send("notifications", new OrderPlacedEvent(order.getOrderNumber()));
//            }
            orderRepository.save(order);
            log.info("Order created: {}", order.getOrderNumber());
        } else log.error("Not all products in stock, only: " + Arrays.toString(inventoryResponses));
        return inventoryResponses;
    }

    public static OrderLineItems mapFromDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order ->
                        new OrderDto(order.getId(), order.getOrderNumber(), order.getOrderLineItemsList()))
                .collect(Collectors.toList());
    }
}
