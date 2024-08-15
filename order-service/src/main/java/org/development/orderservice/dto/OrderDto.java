package org.development.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.development.orderservice.model.OrderLineItems;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderDto {
    private long id;
    private String orderNumber;
    private List<OrderLineItems> orderLineItemsList;
}
