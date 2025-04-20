package com.auction.product.service;

import com.auction.product.config.constant.BiddingConfig;
import com.auction.product.dto.BidRequest;
import com.auction.product.dto.BidResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.kafka.BidWinnerConfirmation;
import com.auction.product.kafka.BidWinnerProducer;
import com.auction.product.model.Product;
import com.auction.product.model.WinningBid;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.ProductRepository;
import com.auction.product.repostory.WinningBidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.auction.product.model.Bid;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final ProductRepository productRepository;
    private final WinningBidRepository winningBidRepository;
    private final BidRepository bidRepository;
    private final BiddingConfig biddingConfig;
    private final BidWinnerProducer bidWinnerProducer;
    private final BidMapper bidMapper;

    public BidResponse submitBid(String username, String firstName, String lastName,String email, BidRequest bidRequest) throws ProductNotFoundException {
        Product product = productRepository.findById(bidRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (bidRequest.bidAmount().compareTo(product.getBuyNowPrice()) <= 0) {
            throw new IllegalArgumentException("Bid amount must be at least the Buy Now price.");
        }

        Bid bid = Bid.builder()
                .bidAmount(bidRequest.bidAmount())
                .username(username)
                .userFirstName(firstName)
                .userLastName(lastName)
                .userEmail(email)
                .bidTime(LocalDateTime.now())
                .product(product)
                .build();

        Bid savedBid = bidRepository.save(bid);
        return bidMapper.mapBidToBidResponse(savedBid);
    }

    public List<Bid> getBidsForProduct(Long productId) {
        return bidRepository.findByProduct_ProductId(productId);
    }

    public List<BidResponse> getBidsByUsername(String username) {
        return bidRepository.findByUsername(username)
                .stream()
                .map(bidMapper::mapBidToBidResponse)
                .collect(Collectors.toList());
    }

    public List<BidResponse> getAllBids() {
        return bidRepository.findAll()
                .stream()
                .map(bidMapper::mapBidToBidResponse)
                .collect(Collectors.toList());

    }

    public WinningBid determineWinningBid(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        if (product.isSold()) {
            throw new IllegalStateException("Bidding is not allowed for products bought via 'Buy Now'");
        }

        List<Bid> bidsForProduct = bidRepository.findAllByProductProductId(productId);
        if (bidsForProduct.isEmpty()) {
            throw new IllegalStateException("No bids found for this product");
        }

        Bid earliestBid = bidsForProduct.stream()
                .min(Comparator.comparing(Bid::getBidTime))
                .orElseThrow(() -> new IllegalStateException("No bids found for this product"));


        Duration biddingDuration = biddingConfig.getBiddingDuration();
        LocalDateTime bidEndTime = earliestBid.getBidTime().plus(biddingDuration);

        if (LocalDateTime.now().isBefore(bidEndTime)) {
            throw new IllegalStateException("Bidding period is not over yet");
        }

        Bid highestBid = bidsForProduct.stream()
                .max(Comparator.comparing(Bid::getBidAmount))
                .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

        WinningBid winningBid = new WinningBid();
        winningBid.setProduct(highestBid.getProduct());
        winningBid.setUsername(highestBid.getUsername());
        winningBid.setWinningAmount(highestBid.getBidAmount());
        winningBid.setBidEndTime(bidEndTime);

        winningBidRepository.save(winningBid);

        BidWinnerConfirmation confirmation = new BidWinnerConfirmation(
                winningBid.getWinningBidId(),
                highestBid.getBidId(),
                highestBid.getUsername(),
                highestBid.getUserFirstName(),
                highestBid.getUserLastName(),
                highestBid.getUserEmail(),
                highestBid.getBidAmount(),
                highestBid.getBidTime(),
                highestBid.getProduct().getProductId(),
                highestBid.getProduct().getProductName(),
                highestBid.getProduct().getBrandName(),
                highestBid.getProduct().getDescription()
        );
        bidWinnerProducer.sendBidWinnerConfirmation(confirmation);

        return winningBid;
    }

}

