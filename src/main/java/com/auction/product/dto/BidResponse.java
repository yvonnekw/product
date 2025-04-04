package com.auction.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponse(
        Long bidId,
        Long productId,
        String username,
        BigDecimal bidAmount,
        LocalDateTime bidTime,
        String productName,
        String brandName,
        String description,
        String productImageUrl,
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
