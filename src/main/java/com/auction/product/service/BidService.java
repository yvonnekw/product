package com.auction.product.service;

import com.auction.product.dto.BidResponse;
import com.auction.product.dto.ProductResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.model.Product;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.ProductRepository;
import com.auction.product.dto.BidRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.auction.product.model.Bid;

@Service
@RequiredArgsConstructor
public class BidService {


    //private final WinningBidRepository winningBidRepository;

    private final ProductRepository productRepository;
    private final BidRepository bidRepository;

    @Transactional
    public BidResponse submitBid(String username, BidRequest bidRequest) throws ProductNotFoundException {
        // Validate the product exists
        Product product = productRepository.findById(bidRequest.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        // Validate bid amount
        if (bidRequest.bidAmount().compareTo(product.getBuyNowPrice()) <=0) {
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

    public List<Bid> getBidsForProduct(Long productId) {
        return bidRepository.findByProductId(productId);
    }

    public List<Bid> getBidsByUsername(String username) {
        return bidRepository.findByUsername(username);
    }

    public List<BidResponse> getAllProducts() {
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
}

