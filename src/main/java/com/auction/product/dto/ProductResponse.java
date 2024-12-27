package com.auction.product.dto;


import com.auction.product.model.Category;

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
        int quantity,
        boolean isAvailableForBuyNow,
        boolean isSold,
        Long categoryId
        ) {

}
