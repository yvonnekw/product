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


    @Scheduled(fixedRate = 60000)
    public void checkAndDetermineWinningBids() {
        Duration biddingDuration = biddingConfig.getBiddingDuration();

        LocalDateTime cutoffTime = LocalDateTime.now().minus(biddingDuration);

        List<Product> productsWithBiddingPeriodEnded = productRepository.findByBiddingPeriodExpired(cutoffTime);

        for (Product product : productsWithBiddingPeriodEnded) {
            try {
                List<Bid> bidsForProduct = bidRepository.findAllByProductProductId(product.getProductId());

                if (!bidsForProduct.isEmpty()) {
                    Bid highestBid = bidsForProduct.stream()
                            .max(Comparator.comparing(Bid::getBidAmount))
                            .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

                    String username = highestBid.getUsername();

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

}
