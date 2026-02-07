package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ProductClientService {

    private final RestTemplate restTemplate;
    private static final String PRODUCT_SERVICE_URL = "http://localhost:8082/api/products";

    public ProductClientService() {
        this.restTemplate = new RestTemplate();
    }

    public ProductDTO getProduct(Long productId) {
        log.info("Fetching product from Product Service: {}", productId);

        try {
            String url = PRODUCT_SERVICE_URL + "/" + productId;
            ProductDTO product = restTemplate.getForObject(url, ProductDTO.class);

            if (product == null) {
                throw new RuntimeException("Product not found: " + productId);
            }

            log.info("Product fetched successfully: {}", product.getName());
            return product;

        } catch (Exception e) {
            log.error("Error fetching product: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch product: " + productId);
        }
    }
}