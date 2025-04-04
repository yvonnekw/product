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
                //.product(getBidId)
                //.bidId(bidRequest.bi)
                .bidAmount(bidRequest.bidAmount())
                .username(username)
                .userFirstName(firstName)
                .userLastName(lastName)
                .userEmail(email)
                .bidTime(LocalDateTime.now())
                .product(product)
                //.brandName(product.getBrandName())
                //.description(product.getDescription())
                //.productImageUrl(product.getProductImageUrl())
               // .startingPrice(product.getStartingPrice())
               // .buyNowPrice(product.getBuyNowPrice())
                //.colour(product.getColour())
                //.productSize(product.getProductSize())
                //.quantity(product.getQuantity())
               // .isAvailableForBuyNow(product.isAvailableForBuyNow())
               // .isSold(product.isSold())
                //.categoryId(product.getCategory().getCategoryId())
                .build();

        Bid savedBid = bidRepository.save(bid);
        return bidMapper.mapBidToBidResponse(savedBid);
/*
        return new BidResponse(
                savedBid.getBidId(),
                savedBid.getProductId(),
                username,
                savedBid.getBidAmount(),
                savedBid.getBidTime(),
                savedBid.getProductName(),
                savedBid.getBrandName(),
                savedBid.getDescription(),
                savedBid.getProductImageUrl(),
                savedBid.getStartingPrice(),
                savedBid.getBuyNowPrice(),
                savedBid.getColour(),
                savedBid.getProductSize(),
                savedBid.getQuantity(),
                savedBid.isAvailableForBuyNow(),
                savedBid.isSold(),
                savedBid.getCategoryId()
        );*/
    }


    /*
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
                savedBid.getBidTime(),
                savedBid.getProductName(),
                savedBid.getBrandName(),
                savedBid.getDescription(),
                savedBid.getProductImageUrl(),
                savedBid.getStartingPrice(),
                savedBid.getBuyNowPrice(),
                savedBid.getColour(),
                savedBid.getProductSize(),
                savedBid.getQuantity(),
                savedBid.isAvailableForBuyNow(),
                savedBid.isSold(),
                savedBid.getCategoryId()
        );
    }
*/
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
                /*
                .stream()
                .map(bid -> new BidResponse(
                        bid.getBidId(),
                        bid.getProductId(),
                        bid.getUsername(),
                        bid.getBidAmount(),
                        bid.getBidTime(),
                        bid.getProductName(),
                        bid.getBrandName(),
                        bid.getDescription(),
                        bid.getProductImageUrl(),
                        bid.getStartingPrice(),
                        bid.getBuyNowPrice(),
                        bid.getColour(),
                        bid.getProductSize(),
                        bid.getQuantity(),
                        bid.isAvailableForBuyNow(),
                        bid.isSold(),
                        bid.getCategoryId()
                ))
                .collect(Collectors.toList());
*/
    }

    public WinningBid determineWinningBid(Long productId) {

        //Long productId = determinedBidRequest.productId();

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

