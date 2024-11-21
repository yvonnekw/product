package com.auction.product.controller;

import com.auction.product.dto.ProductRequest;
import com.auction.product.dto.ProductResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.keycloak.KeycloakClient;
import com.auction.product.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.PermitAll;
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
import com.auction.product.dto.UserResponse;
//import org.springframework.security.core.Authentication;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin("*")
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
    public ProductResponse createProduct(@RequestHeader("X-Username") String username, @RequestBody ProductRequest productRequest) {
        log.info("username passed downstream to the create Product controller " + username);
        return productService.createProduct(username, productRequest);
    }

    @PermitAll
    @GetMapping("/get-all-products")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

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
