package com.auction.product.service;

import com.auction.product.dto.ProductPurchaseResponse;
import com.auction.product.dto.ProductRequest;
import com.auction.product.dto.ProductResponse;
import com.auction.product.model.Product;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    /*
    public Product toProduct(ProductRequest productRequest) {
        return Product.builder()
                //.productId(productRequest.productId())
                .username(productRequest.username())
                .productName(productRequest.productName())
                .brandName(productRequest.brandName())
                .description(productRequest.description())
                .startingPrice(productRequest.startingPrice())
                .buyNowPrice(productRequest.buyNowPrice())
                .colour(productRequest.colour())
                .productSize(productRequest.productSize())
                .quantity(productRequest.quantity())
                .isAvailableForBuyNow(productRequest.isAvailableForBuyNow())
                .isSold(productRequest.isSold())
                .categoryId(productRequest.categoryId())
                .build();

    }

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getUsername(),
                product.getProductName(),
                product.getBrandName(),
                product.getDescription(),
                product.getStartingPrice(),
                product.getBuyNowPrice(),
                product.getProductSize(),
                product.getColour(),
                product.getQuantity(),
                product.isAvailableForBuyNow(),
                product.isSold(),
                product.getCategoryId()
        );
    }
*/
    public ProductPurchaseResponse toProductPurchaseResponse(Product product, double quantity) {
        return new ProductPurchaseResponse(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getBuyNowPrice(),
                quantity
        );
    }

    ProductResponse mapToResponse(Product product) {
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
                product.getCategory().getCategoryId()
        );
    }
}
