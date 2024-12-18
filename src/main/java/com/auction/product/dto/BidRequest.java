package com.auction.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidRequest(
        Long productId,
        String username,
        BigDecimal bidAmount,
        LocalDateTime bidTime
) {
}
