package com.auction.product.dto;

import java.math.BigDecimal;

public record BidRequest(
        Long productId,
        //String username,
        BigDecimal bidAmount
        //LocalDateTime bidTime
) {
}
