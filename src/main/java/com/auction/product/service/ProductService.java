package com.auction.product.service;

import com.auction.product.dto.ProductPurchaseRequest;
import com.auction.product.dto.ProductPurchaseResponse;
import com.auction.product.dto.ProductRequest;
import com.auction.product.dto.ProductResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.exception.ProductPurchaseException;
import com.auction.product.exception.ProductUnavailableException;
import com.auction.product.repostory.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Long createProduct(ProductRequest productRequestBody) {
        var product = productMapper.toProduct(productRequestBody);
        return  productRepository.save(product).getProductId();

/*
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
                );*/
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
}
