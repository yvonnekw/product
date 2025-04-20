package com.auction.product.service;

import com.auction.product.dto.ProductPurchaseRequest;
import com.auction.product.dto.ProductPurchaseResponse;
import com.auction.product.dto.ProductRequest;
import com.auction.product.dto.ProductResponse;
import com.auction.product.exception.ProductPurchaseException;
import com.auction.product.model.Category;
import com.auction.product.model.Product;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.CategoryRepository;
import com.auction.product.repostory.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final BidRepository bidRepository;

    private final Set<Long> processedRequestIds = new HashSet<>();

    public ProductResponse createProduct(String username, ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        log.info("username passed downstream to the create Product service: {}", username);

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
                .isSold(false)
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        return productMapper.mapProductToProductResponse(savedProduct);
    }


    public List<ProductResponse> getProductsForUser(String username) {
        List<Product> products = productRepository.findByUsername(username);

        return products
                .stream()
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());

    }

    public ProductResponse findByProductId(Long productId) {
        return productRepository.findById(productId)
                .map(productMapper::mapProductToProductResponse)
                .orElseThrow(() -> new ProductPurchaseException("Product not found with the Id provided: " + productId));
    }


    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ProductPurchaseException("Empty purchase request");
        }

        Map<Long, Integer> productQuantityMap = new HashMap<>();
        for (ProductPurchaseRequest request : requests) {
            productQuantityMap.merge(request.productId(), request.quantity(), Integer::sum);
        }

        List<Long> uniqueProductIds = new ArrayList<>(productQuantityMap.keySet());
        List<Product> storedProducts = productRepository.findAllByProductIdIn(uniqueProductIds);

        if (storedProducts.size() != uniqueProductIds.size()) {
            throw new ProductPurchaseException("One or more products does not exist");
        }

        List<ProductPurchaseResponse> purchasedProducts = new ArrayList<>();

        for (Product product : storedProducts) {
            Long productId = product.getProductId();
            Integer requestedQuantity = productQuantityMap.get(productId);

            System.out.println("Product ID: " + productId + ", Initial Quantity: " + product.getQuantity() + ", Requested Quantity: " + requestedQuantity);

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductPurchaseException("Insufficient stock quantity for product with ID: " + productId);
            }

            int newAvailableQuantity = product.getQuantity() - requestedQuantity;
            product.setQuantity(newAvailableQuantity);
            productRepository.save(product);

            System.out.println("Product ID: " + productId + ", New Available Quantity: " + newAvailableQuantity);

            purchasedProducts.add(productMapper.toProductPurchaseResponse(product, requestedQuantity));
        }

        return purchasedProducts;
    }


    @Transactional
    public void saveAllProducts(List<Product> products) {
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

    public Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public void markProductAsBought(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.isSold()) {
            throw new IllegalStateException("Product has already been sold");
        }

        if (product.getQuantity() == 0) {
            product.setSold(true);
            productRepository.save(product);

            log.info("Product with ID {} marked as bought via Buy Now", productId);
        }
    }

    public void updateProduct(ProductResponse productResponse) {
        Product product = productRepository.findById(productResponse.productId())
                .orElseThrow(() -> new ProductPurchaseException("Product not found with ID: " + productResponse.productId()));

        if (product.getQuantity() == 0) {
            product.setAvailableForBuyNow(productResponse.isAvailableForBuyNow());

            productRepository.save(product);
            log.info("Updated product availability for product ID: {}", productResponse.productId());
        }

    }

    public List<ProductResponse> searchProducts(String query) {
        List<Product> products = productRepository.searchByQuery(query);

        return products.stream()
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());
    }

}



