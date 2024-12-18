package com.auction.product.kafka;

import com.auction.product.model.Product;

import java.math.BigDecimal;

public record BidWinnerConfirmation(
       Long bidId,
       String buyer,
       BigDecimal bidAmount,
       Product product
) {

}
