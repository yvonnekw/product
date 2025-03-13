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
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
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
                .isSold(false)
                .category(category)
                .build();

        productRepository.save(product);

        return new ProductResponse(
                product.getProductId(),
                product.getUsername(),
                product.getProductName(),
                product.getBrandName(),
                product.getDescription(),
                product.getProductImageUrl(),
                product.getStartingPrice(),
                product.getBuyNowPrice(),
                product.getColour(),
                product.getProductSize(),
                product.getQuantity(),
                product.isAvailableForBuyNow(),
                product.isSold(),
                category.getCategoryId()

        );
    }


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
                        product.getProductImageUrl(),
                        product.getStartingPrice(),
                        product.getBuyNowPrice(),
                        product.getColour(),
                        product.getProductSize(),
                        product.getQuantity(),
                        product.isAvailableForBuyNow(),
                        product.isSold(),
                        product.getCategory().getCategoryId()
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
                .map(productMapper::mapToResponse)
                .collect(Collectors.toList());

                /*
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
                        product.getCategory().getCategoryId()
                ))
                .collect(Collectors.toList());

        */
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
                        product.getProductImageUrl(),
                        product.getStartingPrice(),
                        product.getBuyNowPrice(),
                        product.getColour(),
                        product.getProductSize(),
                        product.getQuantity(),
                        product.isAvailableForBuyNow(),
                        product.isSold(),
                        product.getCategory().getCategoryId()
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

            if (processedRequestIds.contains(productRequest.productId())) {
                System.out.println("Duplicate request detected for Product ID: " + productRequest.productId());
                continue;
            }

            System.out.println("Product ID: " + product.getProductId() + ", Initial Quantity: " + product.getQuantity() + ", Requested Quantity: " + productRequest.quantity());

            if (product.getQuantity() < productRequest.quantity()) {
                throw new ProductPurchaseException("Insufficient stock quantity for product with ID: " + productRequest.productId());
            }

            var newAvailableQuantity = product.getQuantity() - productRequest.quantity();
            product.setQuantity(newAvailableQuantity);
            productRepository.save(product);

            System.out.println("Product ID: " + product.getProductId() + ", New Available Quantity: " + product.getQuantity());

            purchasedProducts.add(productMapper.toProductPurchaseResponse(product, productRequest.quantity()));
            processedRequestIds.add(productRequest.productId());
        }

        return purchasedProducts;
    }

    /*
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
        // Find the product by ID using the ProductRepository
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
           // product.setBoughtOnBuyNow(true);
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



/*

    public void updateProduct(ProductResponse productResponse) {
        // Find the existing product by its ID
        Product product = productRepository.findById(productResponse.productId())
                .orElseThrow(() -> new ProductPurchaseException("Product not found with ID: " + productResponse.productId()));

        // Update the fields with the new values from ProductResponse
        product.setProductName(productResponse.productName());
        product.setBrandName(productResponse.brandName());
        product.setDescription(productResponse.description());
        product.setColour(productResponse.colour());
        product.setProductSize(productResponse.productSize());
        product.setQuantity(productResponse.quantity());
        product.setStartingPrice(productResponse.startingPrice());
        product.setBuyNowPrice(productResponse.buyNowPrice());
        product.setAvailableForBuyNow(productResponse.isAvailableForBuyNow());

        // If the product is sold, mark it as sold
        if (productResponse.isSold()) {
            product.setSold(true);
        }

        // Save the updated product entity
        productRepository.save(product);

        log.info("Product with ID {} updated successfully", productResponse.productId());
    }

 */
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
/*
    public WinningBid determineWinningBid(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Optional<Bid> highestBid = bidRepository.findHighestBidByProduct(productId);
        if (highestBid.isPresent()) {
            Bid winningBid = highestBid.get();

            WinningBid winningBidResult = new WinningBid();
            winningBidResult.setProduct(product);
            winningBidResult.setBid(winningBid);
            winningBidResult.setUsername(winningBid.getUsername());
            winningBidResult.setWinningAmount(winningBid.getBidAmount());
            winningBidResult.setBidEndTime(product.getBidEndTime());

            winningBidRepository.save(winningBidResult);

            // Trigger notification
            notificationService.notifyWinner(winningBid.getUsername(), productId);

            return winningBidResult;
        }

        throw new NoWinningBidException("No valid bids found for this product");
    }
    */
//}


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



}
*/


