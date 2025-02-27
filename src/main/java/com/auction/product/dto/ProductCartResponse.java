package com.auction.product.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductCartResponse(

        Long productId,
        String productName,
        String description,
        String productImageUrl,
        BigDecimal getBuyNowPrice,
        Integer quantity

) {
}
