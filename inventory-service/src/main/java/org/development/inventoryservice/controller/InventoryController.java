package org.development.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.development.inventoryservice.dto.InventoryResponse;
import org.development.inventoryservice.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam Map<String, String> skuCodes) {

        return inventoryService.isInStock(skuCodes);
    }
}
