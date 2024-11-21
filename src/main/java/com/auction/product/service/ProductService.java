package com.auction.product.service;

import com.auction.product.dto.ProductPurchaseRequest;
import com.auction.product.dto.ProductPurchaseResponse;
import com.auction.product.dto.ProductRequest;
import com.auction.product.dto.ProductResponse;
import com.auction.product.exception.ProductPurchaseException;
import com.auction.product.model.Category;
import com.auction.product.model.Product;
import com.auction.product.repostory.CategoryRepository;
import com.auction.product.repostory.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;  // Assuming you have a CategoryRepository
    private final ProductMapper productMapper;

    //private final JwtDecoder jwtDecoder;


    /**
     * Creates a product and associates it with the logged-in user (via userId).
     */
    public ProductResponse createProduct(String username, ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.category().getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        log.info("username passed downstream to the create Product service" + username);

        Product product = Product.builder()
                .username(username)
                .productName(productRequest.productName())
                .brandName(productRequest.brandName())
                .description(productRequest.description())
                .colour(productRequest.colour())
                .productSize(productRequest.productSize())
                .quantity(productRequest.quantity())
                .startingPrice(productRequest.startingPrice())
                .buyNowPrice(productRequest.buyNowPrice())
                .isAvailableForBuyNow(productRequest.isAvailableForBuyNow())
                .category(category)
                .isSold(false)
                .build();

        productRepository.save(product);

        return new ProductResponse(
                product.getProductId(),
                product.getUsername(),
                product.getProductName(),
                product.getBrandName(),
                product.getDescription(),
                product.getStartingPrice(),
                product.getBuyNowPrice(),
                product.getColour(),
                product.getProductSize(),
                product.getQuantity(),
                product.isAvailableForBuyNow(),
                product.isSold(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCategory().getDescription()
        );
    }
    /*
    public ProductResponse createProduct(String userId, ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.category().getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Create product from request data
        Product product = Product.builder()
                .sellerId(Long.parseLong(userId))  // Set the sellerId from the Keycloak userId
                .productName(productRequest.productName())
                .brandName(productRequest.brandName())
                .description(productRequest.description())
                .colour(productRequest.colour())
                .productSize(productRequest.productSize())
                .quantity(productRequest.quantity())
                .startingPrice(productRequest.startingPrice())
                .buyNowPrice(productRequest.buyNowPrice())
                .isAvailableForBuyNow(productRequest.isAvailableForBuyNow())
                .category(category)  // Set the category from request
                .isSold(false)  // Default to false, assuming products are unsold at creation
                .build();

        // Save the product
        productRepository.save(product);

        // Return response with product data, including category details
        return new ProductResponse(
                product.getProductId(),
                product.getSellerId(),
                product.getProductName(),
                product.getBrandName(),
                product.getDescription(),
                product.getStartingPrice(),
                product.getBuyNowPrice(),
                product.getColour(),
                product.getProductSize(),
                product.getQuantity(),
                product.isAvailableForBuyNow(),
                product.isSold(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCategory().getDescription()
        );
    }*/
    /**
     * Fetches all products for the logged-in user based on the userId.
     */
    public List<ProductResponse> getProductsForUser(String username) {
        List<Product> products = productRepository.findByUsername(username);

        return products.stream()
                .map(product -> new ProductResponse(
                        product.getProductId(),
                        product.getUsername(),
                        product.getProductName(),
                        product.getBrandName(),
                        product.getDescription(),
                        product.getStartingPrice(),
                        product.getBuyNowPrice(),
                        product.getColour(),
                        product.getProductSize(),
                        product.getQuantity(),
                        product.isAvailableForBuyNow(),
                        product.isSold(),
                        product.getCategory().getCategoryId(),
                        product.getCategory().getName(),
                        product.getCategory().getDescription()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Fetches all products across all sellers.
     */
    public List<ProductResponse> getAllProducts() {
        // Get all products from the repository
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(
                        product.getProductId(),
                        product.getUsername(),
                        product.getProductName(),
                        product.getBrandName(),
                        product.getDescription(),
                        product.getStartingPrice(),
                        product.getBuyNowPrice(),
                        product.getColour(),
                        product.getProductSize(),
                        product.getQuantity(),
                        product.isAvailableForBuyNow(),
                        product.isSold(),
                        product.getCategory().getCategoryId(),
                        product.getCategory().getName(),
                        product.getCategory().getDescription()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Fetches product by its productId.
     */
    public ProductResponse findByProductId(Long productId) {
        return productRepository.findById(productId)
                .map(product -> new ProductResponse(
                        product.getProductId(),
                        product.getUsername(),
                        product.getProductName(),
                        product.getBrandName(),
                        product.getDescription(),
                        product.getStartingPrice(),
                        product.getBuyNowPrice(),
                        product.getColour(),
                        product.getProductSize(),
                        product.getQuantity(),
                        product.isAvailableForBuyNow(),
                        product.isSold(),
                        product.getCategory().getCategoryId(),
                        product.getCategory().getName(),
                        product.getCategory().getDescription()
                ))
                .orElseThrow(() -> new ProductPurchaseException("Product not found with the Id provided: " + productId));
    }

    /**
     * Purchases products and handles updating the product quantities.
     */
    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        var productIds = request.stream().map(ProductPurchaseRequest::productId).collect(Collectors.toList());
        var storedProducts = productRepository.findAllByProductIdIn(productIds);

        if (productIds.size() != storedProducts.size()) {
            throw new ProductPurchaseException("One or more products does not exist");
        }

        var storedRequest = request.stream().sorted(Comparator.comparing(ProductPurchaseRequest::productId)).collect(Collectors.toList());
        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();

        for (int i = 0; i < storedProducts.size(); i++) {
            var product = storedProducts.get(i);
            var productRequest = storedRequest.get(i);

            // Check for sufficient quantity
            if (product.getQuantity() < productRequest.quantity()) {
                throw new ProductPurchaseException("Insufficient stock quantity for product with ID:: " + productRequest.productId());
            }

            // Update the available quantity
            var newAvailableQuantity = product.getQuantity() - productRequest.quantity();
            product.setQuantity(newAvailableQuantity);
            productRepository.save(product);

            purchasedProducts.add(productMapper.toProductPurchaseResponse(product, productRequest.quantity()));
        }

        return purchasedProducts;
    }

    @Transactional
    public void saveAll(List<Product> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products list must not be null or empty.");
        }

        try {
            productRepository.saveAll(products);
        } catch (Exception e) {
            log.error("Error saving products", e);
            throw new RuntimeException("Failed to save products", e);
        }
    }


    /*
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(String userId, ProductRequest productRequest) {
        Product product = Product.builder()
                .sellerId(Long.parseLong(userId))  // Set the sellerId from the Keycloak userId
                .productName(productRequest.productName())
                .brandName(productRequest.brandName())
                .description(productRequest.description())
                .colour(productRequest.colour())
                .productSize(productRequest.productSize())
                .quantity(productRequest.quantity())
                .startingPrice(productRequest.startingPrice())
                .buyNowPrice(productRequest.buyNowPrice())
                .isAvailableForBuyNow(productRequest.isAvailableForBuyNow())
                .category(productRequest.category())  // Assuming you pass category
                .isSold(false)  // Default to false, assuming products are unsold at creation
                .build();

        productRepository.save(product);

        return ProductResponse.builder()
                .productId(product.getProductId())
                .sellerId(product.getSellerId())
                .productName(product.getProductName())
                .brandName(product.getBrandName())
                .description(product.getDescription())
                .colour(product.getColour())
                .productSize(product.getProductSize())
                .quantity(product.getQuantity())
                .startingPrice(product.getStartingPrice())
                .buyNowPrice(product.getBuyNowPrice())
                .isAvailableForBuyNow(product.isAvailableForBuyNow())
                .category(product.getCategory())
                .build();
    }

    public List<ProductResponse> getProductsForUser(String userId) {
        List<Product> products = productRepository.findBySellerId(Long.parseLong(userId));  // Fetch products by sellerId

        return products.stream()
                .map(product -> ProductResponse.builder()
                        .productId(product.getProductId())
                        .sellerId(product.getSellerId())
                        .productName(product.getProductName())
                        .brandName(product.getBrandName())
                        .description(product.getDescription())
                        .colour(product.getColour())
                        .productSize(product.getProductSize())
                        .quantity(product.getQuantity())
                        .startingPrice(product.getStartingPrice())
                        .buyNowPrice(product.getBuyNowPrice())
                        .isAvailableForBuyNow(product.isAvailableForBuyNow())
                        .category(product.getCategory())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(
                        product.getProductId(),
                        product.getSellerId(),
                        product.getProductName(),
                        product.getBrandName(),
                        product.getDescription(),
                        product.getStartingPrice(),
                        product.getBuyNowPrice(),
                        product.getColour(),
                        product.getProductSize(),
                        product.getQuantity(),
                        product.isAvailableForBuyNow(),
                        product.isSold(),
                        product.getCategory().getCategoryId(),
                        product.getCategory().getName(),
                        product.getCategory().getDescription()))

                .toList();
    }

    public ProductResponse findByProductId(Long productId) {
        return productRepository.findById(productId)
                .map(productMapper::toProductResponse).orElseThrow(() -> new ProductPurchaseException("Product not found with the Id provided: " + productId));
    }

    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> request) {
        var productIds = request
                .stream().map(ProductPurchaseRequest::productId)
                .toList();
        var storedProducts = productRepository.findAllByProductIdIn(productIds);
        if (productIds.size() != storedProducts.size()){
            throw new ProductPurchaseException("One or more products does not exists");
        }
        var storedRequest = request
                .stream()
                .sorted(Comparator.comparing(ProductPurchaseRequest::productId))
                .toList();
        var purchasedProducts = new ArrayList<ProductPurchaseResponse>();
        for(int i = 0; i < storedProducts.size(); i++) {
            var product = storedProducts.get(i);
            var productRequest = storedRequest.get(i);
            if (product.getQuantity() < productRequest.quantity()) {
                throw new ProductPurchaseException("Insufficient stock quantity for product with ID:: " + productRequest.productId());
            }
            var newAvailableQuantity = product.getQuantity() - productRequest.quantity();
            product.setQuantity(newAvailableQuantity);
            productRepository.save(product);
            purchasedProducts.add(productMapper.toProductPurchaseResponse(product, productRequest.quantity()));
        }
        return purchasedProducts;

    }
*/
    /*
    public Long createProduct(String userId, ProductRequest productRequestBody) {
        var product = productMapper.toProduct(productRequestBody);
        return  productRepository.save(product).getProductId();


        Product product = Product.builder()
                .productName(productRequestBody.productName())
                .description(productRequestBody.description())
                .startingPrice(productRequestBody.startingPrice())
                .brandName(productRequestBody.brandName())
                .productSize(productRequestBody.productSize())
                .colour(productRequestBody.colour())
                .build();
        productRepository.save(product);
        log.info("Product with id {} is saved. ", product.getProductId());
        return  map(productMapper::toProductResponse);

                /*new ProductResponse(
               product.getProductId(),
                product.getSellerId(),
                product.getProductName(),
                product.getBrandName(),
                product.getDescription(),
                product.getStartingPrice(),
                product.getBuyNowPrice(),
                product.getColour(),
                product.getProductSize(),
                product.getQuantity(),
                product.isAvailableForBuyNow(),
                product.isSold(),
                product.getCategories().getCategoryId(),
                product.getCategories().getName(),
                product.getCategories().getDescription()
                );
}*/


/*
    private ProductResponse mapToProductResponse(Product product) {
        return  ProductResponse.builder()
                .productId(product.getProductId())
                .description(product.getDescription())
                .productName(product.getProductName())
                .brandName(product.getBrandName())
                .colour(product.getColour())
                .productSize(product.getProductSize())
                .startingPrice(product.getStartingPrice())
                .build();
    }*/
/*
    public ProductResponse buyNow(ProductRequest productRequest) throws ProductNotFoundException {
       var product = productRequest;
        var productID = product.get
                = productRepository.findById(productRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("No product found with the provided Id: %s", productRequest.productId())
                ));

        if (!product.isAvailableForBuyNow()) {
            throw new ProductUnavailableException(
                    String.format("Product with Id: %s is not available for 'Buy Now'", productRequest.productId())
            );
        }
        //order service not implemented yet
        /*
        Order order = createOrderForProduct(product, productRequest);

        product.setAvailable(false);
        productRepository.save(product);

        return new ProductResponse(order, product);
        return  null;
    }
*/


}
