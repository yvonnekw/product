package com.auction.product.dto;

import com.auction.product.model.Product;


public record CategoryRequest(
        String name,
        String description,
       Product products
) {
}
