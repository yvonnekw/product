package com.auction.product.service;

import com.auction.product.model.Bid;
import com.auction.product.model.Product;
import lombok.*;

@Builder
@RequiredArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ProductWrapper {
    private Product product;
    private Bid bid;
}
