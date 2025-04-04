package com.auction.product.service;

import com.auction.product.config.constant.BiddingConfig;
import com.auction.product.dto.BidResponse;
import com.auction.product.kafka.BidWinnerProducer;
import com.auction.product.model.Bid;
import com.auction.product.model.Product;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.ProductRepository;
import com.auction.product.repostory.WinningBidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidMapper {


    private final ProductRepository productRepository;
    BidResponse mapBidToBidResponse(Bid bid) {
        Product product = productRepository.findById(bid.getProduct().getProductId()).orElse(null);
        return new BidResponse(
                bid.getBidId(),
                bid.getProduct().getProductId(),
                bid.getUsername(),
                bid.getBidAmount(),
                bid.getBidTime(),
                product != null ? product.getProductName() : null,
                product != null ? product.getBrandName() : null,
                product != null ? product.getDescription() : null,
                product != null ? product.getProductImageUrl() : null,
                product != null ? product.getStartingPrice() : null,
                product != null ? product.getBuyNowPrice() : null,
                product != null ? product.getColour() : null,
                product != null ? product.getProductSize() : null,
                product != null ? product.getQuantity() : 0,
                product != null && product.isAvailableForBuyNow(),
                product != null && product.isSold(),
                product != null ? product.getCategory().getCategoryId() : null
        );
    }

}
