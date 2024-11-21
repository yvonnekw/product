package com.auction.product.dto;

import com.auction.product.model.Product;

import java.util.List;

public record CategoryResponse(
        Long categoryId,
        String name,
        String description,
     Product products
) {
}
