package com.auction.product.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductCartResponse(
        @NotNull(message = "Product Id mandatory")
        Long productId,
        @NotNull(message = "Quantity Id mandatory")
        int quantity,

        BigDecimal price
) {
}
