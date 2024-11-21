package com.auction.product.controller;

import com.auction.product.model.Product;
import com.auction.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String getProduct() {
        return "product admin api working ";
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody List<Product> products,
                                           @RequestHeader("X-Admin-Token") String adminToken) {
        if (adminToken == null || !validateAdminToken(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid admin token");
        }

        productService.saveAll(products);
        return ResponseEntity.ok("Product created successfully");
    }

    private boolean validateAdminToken(String token) {

        return true;
    }
}
