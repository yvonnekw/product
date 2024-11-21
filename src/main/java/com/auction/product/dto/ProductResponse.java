package com.auction.product.dto;


import java.math.BigDecimal;


public record ProductResponse(
        Long productId,
        String username,
        String productName,
        String brandName,
        String description,
        BigDecimal startingPrice,
        BigDecimal buyNowPrice,
        String colour,
        String productSize,
        double quantity,
        boolean isAvailableForBuyNow,
        boolean isSold,
        Long categoryId,
        String categoryName,
        String categoryDescription

        ) {

}
