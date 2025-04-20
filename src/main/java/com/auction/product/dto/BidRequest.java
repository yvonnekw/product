package com.auction.product.dto;

import java.math.BigDecimal;

public record BidRequest(
        Long productId,
        BigDecimal bidAmount
) {
}
