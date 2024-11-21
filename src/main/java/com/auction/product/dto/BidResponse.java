package com.auction.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidResponse(
        Long bidId,
        Long productId,
        String username,
        BigDecimal bidAmount,
        LocalDateTime bidTime
) {
}
