package com.auction.product.service;

import com.auction.product.dto.ProductPurchaseResponse;
import com.auction.product.dto.ProductResponse;
import com.auction.product.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {


    public ProductResponse mapProductToProductResponse(Product product) {
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
                product.getCategory() != null ? product.getCategory().getCategoryId() : null
        );
    }

    public ProductPurchaseResponse toProductPurchaseResponse(Product product, int quantity) {
        return new ProductPurchaseResponse(
                product.getProductId(),
                product.getProductName(),
                product.getBrandName(),
                product.getDescription(),
                product.getColour(),
                product.getProductSize(),
                product.getStartingPrice(),
                product.getBuyNowPrice(),
                quantity,
                product.getProductImageUrl()
        );
    }

    ProductResponse mapToResponse(Product product) {
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
                product.getCategory().getCategoryId()
        );
    }
}
