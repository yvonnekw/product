package com.auction.product.controller;

import com.auction.product.dto.*;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.keycloak.KeycloakClient;
import com.auction.product.model.Product;
import com.auction.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
//@CrossOrigin("*")
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

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

    /*
    @GetMapping("/{productId}")
    public ResponseEntity<ProductCartResponse> getProductById(@PathVariable Long productId) {

        Product product = productService.findProductById(productId);

        if (product != null) {

            ProductCartResponse productCartResponse = new ProductCartResponse(
                    product.getProductId(),
                    product.getQuantity(),
                    product.getBuyNowPrice()
            );
            return ResponseEntity.ok(productCartResponse);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    */

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
    public ResponseEntity<?> purchaseProducts(@RequestHeader("Authorization") String token, @RequestBody Object requests) {
        if (requests instanceof List<?>) {
            // Convert LinkedHashMap elements into ProductPurchaseRequest objects
            List<ProductPurchaseRequest> purchaseRequests = ((List<?>) requests).stream()
                    .map(obj -> new ObjectMapper().convertValue(obj, ProductPurchaseRequest.class))
                    .collect(Collectors.toList());

            productService.purchaseProducts(purchaseRequests);
        } else if (requests instanceof LinkedHashMap) {
            ProductPurchaseRequest purchaseRequest = new ObjectMapper().convertValue(requests, ProductPurchaseRequest.class);

            // Handle single request
            productService.purchaseProducts(Collections.singletonList(purchaseRequest));
        } else {
            return ResponseEntity.badRequest().body("Invalid request format");
        }
        return ResponseEntity.ok().build();
    }


/*
    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductPurchaseResponse> purchase(@RequestBody @Valid List<ProductPurchaseRequest> productRequest) throws ProductNotFoundException {
        return productService.purchaseProducts(productRequest);
    }
*/

    /*
    // Endpoint to get product details by productId
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        ProductResponse productResponse = productService.findProductById(productId);
        return ResponseEntity.ok(productResponse);
    }
*/

    /*
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        Product product = productService.findProductById(productId);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }


     */

/*
    @PostMapping("/create-product")
    public ProductResponse createProduct( @RequestHeader(value = "X-Username", required = true) String username, @RequestBody ProductRequest productRequest) {
        log.info("username passed downstream to the create Product controller " + username);
        return productService.createProduct(username, productRequest);
    }
*/

//Authentication connectedUser
/*
    @PostMapping("/create-product")
   // @PreAuthorize("hasRole('user')")
    public ProductResponse createProduct(String token, @RequestBody ProductRequest productRequest) {
      //  String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        //String userId = getUserIdFromKeycloak(accessToken);
       // String userId =connectedUser.getName();
        // String userId = getUserIdFromKeycloak(accessToken);

        //retrieve the username from the token and pass it on
      //  String username = token.getUserId();
       // return productService.createProduct(username, productRequest);
        return null;
    }
*/
    /*
    @PostMapping("/create-product")
    //@SecurityRequirement()
    public ProductResponse createProduct(Authentication connectedUser, @RequestBody ProductRequest productRequest) {
        //  String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        //String userId = getUserIdFromKeycloak(accessToken);
        String userId =connectedUser.getName();
        // String userId = getUserIdFromKeycloak(accessToken);
        return productService.createProduct(userId, productRequest);
    }
*/

    /*

     */
/*
    @GetMapping("/get-user-products")
    public List<ProductResponse> getProductsForUser(@RequestHeader("Authorization") String authHeader) {
        String accessToken = extractAccessTokenFromAuthHeader(authHeader);
        String userId = getUserIdFromKeycloak(accessToken);
        return productService.getProductsForUser(userId);
    }
*/
    //private String extractAccessTokenFromAuthHeader(String authHeader) {
    //return authHeader.substring(7);
    //  }

    /*
    // Fetch the user ID from Keycloak using the access token
    private String getUserIdFromKeycloak(String accessToken) {
        UserResponse userResponse = keycloakClient.getUserByIdFromToken(accessToken);
        return userResponse.userId();
    }
    */




    /*
    private final ProductService productService;

    @PostMapping("/create-product")
    public ProductResponse createProduct(@RequestHeader("Authorization") String authHeader, @RequestBody ProductRequest productRequest) {
        String userId = extractUserIdFromAuthHeader(authHeader);
        return productService.createProduct(userId, productRequest);
    }

    @GetMapping("/get-all-products")
    public List<ProductResponse> getProductsForUser(@RequestHeader("Authorization") String authHeader) {
        String userId = extractUserIdFromAuthHeader(authHeader);
        return productService.getProductsForUser(userId);
    }

    private String extractUserIdFromAuthHeader(String authHeader) {
        String jwtToken = authHeader.substring(7);

        return extractUserIdFromJwt(jwtToken);
    }

    private String extractUserIdFromJwt(String jwtToken) {
        JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation("http://localhost:9098/realms/auction-realm");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        return jwt.getClaimAsString("sub");
    }

    @GetMapping("/get-all-products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }
*/

/*
    @PostMapping("/create-product")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }
*/
    /*
    @PostMapping("/buy-now")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse buyNow(@RequestBody @Valid ProductRequest productRequest) throws ProductNotFoundException {
        return productService.buyNow(productRequest);
    }
    */
/*
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findByProductId(@PathVariable("productId") Long productId) {
        ProductResponse productResponse = productService.findByProductId(productId);
        return ResponseEntity.ok(productResponse);
    }
*/
    /*
    @GetMapping("/{product-id")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponse> findByProductId(@PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(productService.findByProductId(productId));
    }
*/
/*
    @GetMapping("/get-all-products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }
*/


}
