package org.development.inventoryservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="inventory")
@Data
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private Integer quantity;
}
