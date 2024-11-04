package com.auction.product.controller;

import com.auction.product.dto.ProductRequest;
import com.auction.product.dto.ProductResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create-product")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    /*
    @PostMapping("/buy-now")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse buyNow(@RequestBody @Valid ProductRequest productRequest) throws ProductNotFoundException {
        return productService.buyNow(productRequest);
    }
    */

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findByProductId(@PathVariable("productId") Long productId) {
        ProductResponse productResponse = productService.findByProductId(productId);
        return ResponseEntity.ok(productResponse);
    }

    /*
    @GetMapping("/{product-id")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponse> findByProductId(@PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(productService.findByProductId(productId));
    }
*/

    @GetMapping("/get-all-products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String getProduct() {
        return "product api working ";
    }


}
