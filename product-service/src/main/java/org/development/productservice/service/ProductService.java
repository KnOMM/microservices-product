package org.development.productservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.development.productservice.dto.ProductRequest;
import org.development.productservice.dto.ProductResponse;
import org.development.productservice.model.Product;
import org.development.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .description(productRequest.getDescription())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("User {} is saved", savedProduct.getId());
        return product;
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }


    public ProductResponse mapToProductResponse(Product product) {
        return ProductResponse
                .builder()
                .name(product.getName())
                .id(product.getId())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }
}
