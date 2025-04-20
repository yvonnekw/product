package com.auction.product.controller;

import com.auction.product.dto.*;
import com.auction.product.model.Product;
import com.auction.product.service.IdempotencyService;
import com.auction.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController

@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String getProduct() {
        return "product api working ";
    }

    @PostMapping("/create-product")
    public ProductResponse createProduct(@RequestHeader("Authorization") String token, @RequestHeader("X-Username") String username, @RequestBody ProductRequest productRequest) {
        log.info("username passed downstream to the create Product controller " + username);
        log.debug("Token passed downstream: {}", token);
        return productService.createProduct(username, productRequest);
    }

    @PermitAll
    @GetMapping("/get-all-products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductCartResponse> getProductById(@PathVariable Long productId) {

        Product product = productService.findProductById(productId);

        if (product != null) {
            ProductCartResponse productCartResponse = new ProductCartResponse(
                    product.getProductId(),
                    product.getProductName(),
                    product.getDescription(),
                    product.getProductImageUrl(),
                    product.getBuyNowPrice(),
                    product.getQuantity()
            );
            return ResponseEntity.ok(productCartResponse);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/{productId}/mark-as-bought")
    public ResponseEntity<Void> markProductAsBought(@RequestHeader("Authorization") String token, @PathVariable Long productId) {
        productService.markProductAsBought(productId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateProduct(@RequestHeader("Authorization") String token, @PathVariable Long productId, @RequestBody ProductResponse productResponse) {

        if (!productId.equals(productResponse.productId())) {
            throw new IllegalArgumentException("Product ID in path and body must match.");
        }

        log.info("product response for product controller update {}", productResponse);

        productService.updateProduct(productResponse);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String query) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<ProductResponse> products = productService.searchProducts(query);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(products);
    }

    @Transactional
    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseProducts(
            @RequestHeader("Authorization") String token,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Object requests) {

        try {
            log.info("Received purchase request payload: {}", objectMapper.writeValueAsString(requests));
        } catch (Exception e) {
            log.error("Error logging request: {}", e.getMessage());
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Idempotency-Key header");
        }

        if (idempotencyService.isDuplicateRequest(idempotencyKey)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate request detected");
        }

        List<ProductPurchaseRequest> purchaseRequests;
        try {
            if (requests instanceof List<?>) {
                purchaseRequests = ((List<?>) requests).stream()
                        .map(obj -> objectMapper.convertValue(obj, ProductPurchaseRequest.class))
                        .collect(Collectors.toList());
            } else if (requests instanceof LinkedHashMap) {
                ProductPurchaseRequest singleRequest = objectMapper.convertValue(requests, ProductPurchaseRequest.class);
                purchaseRequests = Collections.singletonList(singleRequest);
            } else {
                return ResponseEntity.badRequest().body("Invalid request format");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing request format");
        }

        List<ProductPurchaseResponse> responses;
        try {
            responses = productService.purchaseProducts(purchaseRequests);
            idempotencyService.storeRequest(idempotencyKey);
        } catch (Exception e) {
            log.error("Error processing purchase request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing purchase request");
        }

        if (responses == null || responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/get-user-products")
    public List<ProductResponse> getProductsForUser(@RequestHeader("Authorization") String token, @RequestHeader("X-Username") String username) {

        return productService.getProductsForUser(username);
    }


}
