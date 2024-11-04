package com.auction.product.kafka;


import com.auction.product.dto.UserResponse;
import com.auction.product.dto.ProductResponse;

import java.math.BigDecimal;

public record BidWinnerConfirmation(
       Long bidId,
       BigDecimal bidAmount,
       UserResponse buyer,
       ProductResponse product
) {

}
