package com.auction.product.dto;

import java.math.BigDecimal;

public record ProductPurchaseResponse(
        Long productId,
        String productName,
        String brandName,
        String description,
        String colour,
        String productSize,
        BigDecimal startingPrice,
        BigDecimal buyNowPrice,
        Integer quantity,
        String productImageUrl

) {
}
