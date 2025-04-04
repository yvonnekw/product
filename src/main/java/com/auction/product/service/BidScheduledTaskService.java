package com.auction.product.service;

import com.auction.product.config.constant.BiddingConfig;
import com.auction.product.model.Bid;
import com.auction.product.model.Product;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidScheduledTaskService {
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final BidService bidService;
    private final BiddingConfig biddingConfig;

    /*
    @Scheduled(fixedRate = 60000) // Run every minute (60,000 ms)
    public void checkAndDetermineWinningBids() {
        // Get the bidding duration from config
        Duration biddingDuration = biddingConfig.getBiddingDuration();

        // Calculate the cutoff time for when bidding ends
        LocalDateTime cutoffTime = LocalDateTime.now().minus(biddingDuration);

        // Find products whose bidding period has ended
        List<Product> productsWithBiddingPeriodEnded = productRepository.findByBiddingPeriodExpired(cutoffTime);

        // Process each product to determine winning bids
        for (Product product : productsWithBiddingPeriodEnded) {
            try {
                // Fetch all bids for the current product
                List<Bid> bidsForProduct = bidRepository.findAllByProductId(product.getProductId());

                // If there are bids, determine the winning bid
                if (!bidsForProduct.isEmpty()) {
                    // Find the highest bid (the winning bid)
                    Bid highestBid = bidsForProduct.stream()
                            .max(Comparator.comparing(Bid::getBidAmount))
                            .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

                    // Create the DeterminedBidRequest with only the productId
                    DeterminedBidRequest determinedBidRequest = new DeterminedBidRequest(product.getProductId());

                    // Call bidService to determine the winning bid
                    bidService.determineWinningBid(product.getProductId());

                    log.info("Winning bid determined for productId: {}", product.getProductId());
                } else {
                    log.warn("No bids found for productId: {}", product.getProductId());
                }
            } catch (Exception e) {
                log.error("Error determining winning bid for productId: {}", product.getProductId(), e);
            }
        }
    }
*/


    @Scheduled(fixedRate = 60000) // Run every minute (60,000 ms)
    public void checkAndDetermineWinningBids() {
        // Get the bidding duration from config
        Duration biddingDuration = biddingConfig.getBiddingDuration();

        // Calculate the cutoff time for when bidding ends
        LocalDateTime cutoffTime = LocalDateTime.now().minus(biddingDuration);

        // Find products whose bidding period has ended
        List<Product> productsWithBiddingPeriodEnded = productRepository.findByBiddingPeriodExpired(cutoffTime);

        // Process each product to determine winning bids
        for (Product product : productsWithBiddingPeriodEnded) {
            try {
                // Fetch all bids for the current product
                List<Bid> bidsForProduct = bidRepository.findAllByProductProductId(product.getProductId());

                // If there are bids, determine the winning bid
                if (!bidsForProduct.isEmpty()) {
                    // Find the highest bid (the winning bid)
                    Bid highestBid = bidsForProduct.stream()
                            .max(Comparator.comparing(Bid::getBidAmount))
                            .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

                    // Get the username of the highest bidder (winner)
                    String username = highestBid.getUsername();

                    // Create the DeterminedBidRequest with productId and the winning bidder's username
                    //DeterminedBidRequest determinedBidRequest = new DeterminedBidRequest(product.getProductId(), username);

                    // Call bidService to determine the winning bid
                    bidService.determineWinningBid(product.getProductId());

                    log.info("Winning bid determined for productId: {}, winner: {}", product.getProductId(), username);
                } else {
                    log.warn("No bids found for productId: {}", product.getProductId());
                }
            } catch (Exception e) {
                log.error("Error determining winning bid for productId: {}", product.getProductId(), e);
            }
        }
    }

    /*
    @Scheduled(fixedRate = 60000) // Run every minute (60,000 ms)
    public void checkAndDetermineWinningBids() {
        Duration biddingDuration = biddingConfig.getBiddingDuration();
        LocalDateTime cutoffTime = LocalDateTime.now().minus(biddingDuration);

        List<Product> productsWithBiddingPeriodEnded = productRepository.findByBiddingPeriodExpired(cutoffTime);

        for (Product product : productsWithBiddingPeriodEnded) {
            try {
                List<Bid> bidsForProduct = bidRepository.findAllByProductId(product.getProductId());

                if (!bidsForProduct.isEmpty()) {
                    Bid highestBid = bidsForProduct.stream()
                            .max(Comparator.comparing(Bid::getBidAmount))
                            .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

                    DeterminedBidRequest determinedBidRequest = new DeterminedBidRequest( username, product.getProductId());
                    bidService.determineWinningBid(username, bidRequest);
                    log.info("Winning bid determined for productId: {}", product.getProductId());
                } else {
                    log.warn("No bids found for productId: {}", product.getProductId());
                }
            } catch (Exception e) {
                log.error("Error determining winning bid for productId: {}", product.getProductId(), e);
            }
        }
    }
*/
/*
    @Scheduled(fixedRate = 60000) // Run every minute (60,000 ms)
    public void checkAndDetermineWinningBids() {
        // Query for products with bidding periods that have ended
        List<Product> productsWithBiddingPeriodEnded = productRepository.findByBiddingPeriodExpired();

        for (Product product : productsWithBiddingPeriodEnded) {
            try {
                // Trigger the winning bid determination for products whose bidding period is over
                List<Bid> bidsForProduct = bidRepository.findAllByProductId(product.getProductId());

                if (!bidsForProduct.isEmpty()) {
                    // Find the highest bid (the winning bid) from the list
                    Bid highestBid = bidsForProduct.stream()
                            .max(Comparator.comparing(Bid::getBidAmount))
                            .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

                    // Create a BidRequest with the highest bid amount
                    BidRequest bidRequest = new BidRequest(product.getProductId(), highestBid.getBidAmount());

                    // Determine the winning bid
                    bidService.determineWinningBid(bidRequest);
                    log.info("Winning bid determined for productId: {}", product.getProductId());
                } else {
                    log.warn("No bids found for productId: {}", product.getProductId());
                }
            } catch (Exception e) {
                // Log the exception with a meaningful message
                log.error("Error determining winning bid for productId: {}", product.getProductId(), e);
            }
        }
    }


 */
}
