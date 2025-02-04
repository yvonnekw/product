package com.auction.product.service;

import com.auction.product.config.constant.BiddingConfig;
import com.auction.product.dto.BidResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.kafka.BidWinnerConfirmation;
import com.auction.product.kafka.BidWinnerProducer;
import com.auction.product.model.Product;
import com.auction.product.model.WinningBid;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.ProductRepository;
import com.auction.product.dto.BidRequest;
import com.auction.product.repostory.WinningBidRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.auction.product.model.Bid;

@Service
@RequiredArgsConstructor
public class BidService {

    private final ProductRepository productRepository;
    private final WinningBidRepository winningBidRepository;
    private final BidRepository bidRepository;
    private final BiddingConfig biddingConfig;
    private final BidWinnerProducer bidWinnerProducer;

    @Transactional
    public BidResponse submitBid(String username, BidRequest bidRequest) throws ProductNotFoundException {
        Product product = productRepository.findById(bidRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (bidRequest.bidAmount().compareTo(product.getBuyNowPrice()) <= 0) {
            throw new IllegalArgumentException("Bid amount must be at least the Buy Now price.");
        }

        Bid bid = new Bid();
        bid.setProductId(bidRequest.productId());
        bid.setBidAmount(bidRequest.bidAmount());
        bid.setUsername(username);
        bid.setBidTime(LocalDateTime.now());

        Bid savedBid = bidRepository.save(bid);

        return new BidResponse(
                savedBid.getBidId(),
                savedBid.getProductId(),
                username,
                savedBid.getBidAmount(),
                savedBid.getBidTime()
        );
    }

    public List<Bid> getBidsForProduct(Long productId) {
        return bidRepository.findByProductId(productId);
    }

    public List<Bid> getBidsByUsername(String username) {
        return bidRepository.findByUsername(username);
    }

    public List<BidResponse> getAllBids() {
        return bidRepository.findAll()
                .stream()
                .map(bid -> new BidResponse(
                        bid.getBidId(),
                        bid.getProductId(),
                        bid.getUsername(),
                        bid.getBidAmount(),
                        bid.getBidTime()
                ))
                .collect(Collectors.toList());

    }

    public WinningBid determineWinningBid(BidRequest bidRequest) {

        Long productId = bidRequest.productId();

        // Fetch Product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        if (product.isBoughtOnBuyNow()) {
            throw new IllegalStateException("Bidding is not allowed for products bought via 'Buy Now'");
        }

        // Fetch the bids and check conditions
        List<Bid> bidsForProduct = bidRepository.findAllByProductId(productId);
        if (bidsForProduct.isEmpty()) {
            throw new IllegalStateException("No bids found for this product");
        }

        Bid earliestBid = bidsForProduct.stream()
                .min(Comparator.comparing(Bid::getBidTime))
                .orElseThrow(() -> new IllegalStateException("No bids found for this product"));

        // Calculate bid end time
        Duration biddingDuration = biddingConfig.getBiddingDuration();
        LocalDateTime bidEndTime = earliestBid.getBidTime().plus(biddingDuration);

        // Check if the bidding period has ended
        if (LocalDateTime.now().isBefore(bidEndTime)) {
            throw new IllegalStateException("Bidding period is not over yet");
        }

        // Find the highest bid
        Bid highestBid = bidsForProduct.stream()
                .max(Comparator.comparing(Bid::getBidAmount))
                .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

        // Create WinningBid
        WinningBid winningBid = new WinningBid();
        winningBid.setProductId(productId);
        winningBid.setUsername(highestBid.getUsername());
        winningBid.setWinningAmount(highestBid.getBidAmount());
        winningBid.setBidEndTime(bidEndTime);

        // Save the winning bid
        winningBidRepository.save(winningBid);

        // Notify the winner using the highest bid details
        BidWinnerConfirmation confirmation = new BidWinnerConfirmation(
                highestBid.getBidId(),
                highestBid.getUsername(),
                highestBid.getBidAmount(),
                product
        );
        bidWinnerProducer.sendBidWinnerConfirmation(confirmation);

        return winningBid;
    }


    /*
    public WinningBid determineWinningBid(@RequestHeader("X-Username") String username, BidRequest bidRequest, BidWinnerConfirmation bidWinnerConfirmation) {
        Long productId = bidRequest.productId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        Bid earliestBid = bidRepository.findEarliestBidByProduct(productId)
                .orElseThrow(() -> new IllegalStateException("No bids found for this product"));

        Duration biddingDuration = biddingConfig.getBiddingDuration();
        LocalDateTime bidEndTime = earliestBid.getBidTime().plus(biddingDuration);

        if (product.isBoughtOnBuyNow()) {
            throw new IllegalStateException("Bidding is not allowed for products bought via 'Buy Now'");
        }

        // Check if the bidding period has ended
        if (LocalDateTime.now().isBefore(bidEndTime)) {
            throw new IllegalStateException("Bidding period is not over yet");
        }

        // Find the highest bid
        Bid highestBid = bidRepository.findHighestBidByProduct(productId)
                .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

        // Create WinningBid
        WinningBid winningBid = new WinningBid();
        winningBid.setProductId(productId);
        winningBid.setUsername(highestBid.getUsername());
        winningBid.setWinningAmount(highestBid.getBidAmount());
        winningBid.setBidEndTime(bidEndTime); // Include derived bidEndTime

         winningBidRepository.save(winningBid);

        // Notify the winner
        bidWinnerProducer.sendBidWinnerConfirmation(
                new BidWinnerConfirmation(
                        bidWinnerConfirmation.bidId(),
                        username,
                        bidWinnerConfirmation.bidAmount(),
                        bidWinnerConfirmation.product()
                )
        );

        return winningBid;
    }
*/


    /*
    @Autowired
    private BidRepository bidRepository;

    public Bid placeBid(Long buyerId, Long productId, BigDecimal bidAmount) {
        Bid bid = Bid.builder()
                .buyerId(buyerId)
                .productId(productId)
                .bidAmount(bidAmount)
                .bidTime(LocalDateTime.now())
                .build();

        return bidRepository.save(bid);
    }

    public List<Bid> getBidsForProduct(Long productId) {
        return bidRepository.findByProductId(productId);
    }

    public List<Bid> getBidsByUser(Long buyerId) {
        return bidRepository.findByBuyerId(buyerId);
    }


     */

    /*
    public ProductWrapper placeBid(String username, Long productId, BigDecimal bidAmount) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Bid bid = Bid.builder()
                .username(username)
                .productId(productId)
                .bidAmount(bidAmount)
                .bidTime(LocalDateTime.now())
                .build();

        bidRepository.save(bid);

        return new ProductWrapper(product, bid);
    }
*/

}

