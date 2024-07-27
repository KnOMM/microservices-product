package org.development.inventoryservice;

import org.development.inventoryservice.model.Inventory;
import org.development.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadDate(InventoryRepository inventoryRepository) {
        return args -> {
            Inventory inventory1 = new Inventory();
            inventory1.setQuantity(100);
            inventory1.setSkuCode("iphone_13");

            Inventory inventory2 = new Inventory();
            inventory2.setQuantity(0);
            inventory2.setSkuCode("iphone_13_red");

            inventoryRepository.save(inventory1);
            inventoryRepository.save(inventory2);
        };
    }
}
