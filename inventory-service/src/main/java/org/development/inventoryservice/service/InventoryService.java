package org.development.inventoryservice.service;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.development.inventoryservice.dto.InventoryResponse;
import org.development.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;


    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(Map<String, String> skuCodes) {

        Map<String, Integer> skuCodesToInteger = new HashMap<>();
        for (Map.Entry<String, String> entry : skuCodes.entrySet()) {
            try{
                int value = Integer.parseInt(String.valueOf(entry.getValue()));
                skuCodesToInteger.put(entry.getKey(), value);

            } catch (NumberFormatException e) {
                log.error("Cannot cast {} to Integer", entry.getValue());
            }
        }

        log.info("Checking stock inventory");
        return inventoryRepository.findBySkuCodeIn(skuCodesToInteger.keySet()
                        .stream()
                        .toList())
                .stream()
                .map(inventory -> InventoryResponse
                        .builder()
                        .isInStock(inventory.getQuantity() >= skuCodesToInteger.get(inventory.getSkuCode()))
                        .skuCode(inventory.getSkuCode())
                        .build()).toList();
    }
}
