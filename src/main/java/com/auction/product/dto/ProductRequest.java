package com.auction.product.dto;

import com.auction.product.model.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
       // Long productId,
        Long sellerId,
        @NotNull(message = "Product name required")
        String productName,
        String brandName,
        @NotNull(message = "Product description required")
        String description,
        @Positive(message = "Starting price should be positive")
        BigDecimal startingPrice,
        @Positive(message = "buy now price should be positive")
        BigDecimal buyNowPrice,
        String colour,
        String productSize,
        boolean isAvailableForBuyNow,
        boolean isSold,
        @Positive(message = "Product quantity should be positive")
        double quantity,
        @NotNull(message = "Product category is require")
        Category category

) {

}
