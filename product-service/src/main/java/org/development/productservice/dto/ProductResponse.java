package org.development.productservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

//@AllArgsConstructor
//@NoArgsConstructor
@Builder
@Data
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}

