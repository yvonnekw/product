package com.auction.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
        String username,
        @NotNull(message = "Product name required")
        String productName,
        String brandName,
        @NotNull(message = "Product description required")
        String description,
      String productImageUrl,
        @Positive(message = "Starting price should be positive")
        BigDecimal startingPrice,
        @Positive(message = "buy now price should be positive")
        BigDecimal buyNowPrice,
        String colour,
        String productSize,
        boolean isAvailableForBuyNow,
        boolean isSold,
        @Positive(message = "Product quantity should be positive")
        int quantity,
        @NotNull(message = "Category ID is required")
        Long categoryId

) {

}
