package com.auction.product.dto;

import java.math.BigDecimal;

public record ProductPurchaseResponse(
        Long productId,
        String productName,
        String description,
        BigDecimal buyNowPrice,
        int quantity
) {
}
