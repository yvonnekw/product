package com.auction.product.dto;

import jakarta.validation.constraints.NotNull;

public record ProductCartResponse(
        @NotNull(message = "Product Id mandatory")
        Long productId,
        @NotNull(message = "Quantity Id mandatory")
        double quantity
) {
}
