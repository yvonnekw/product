package com.auction.product.controller;

import com.auction.product.dto.BidRequest;
import com.auction.product.dto.BidResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.model.WinningBid;
import com.auction.product.repostory.BidRepository;
import com.auction.product.repostory.ProductRepository;
import com.auction.product.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Comparator;
import java.util.List;
import com.auction.product.model.Bid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bids")
public class BidController {


    private final BidService bidService;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String getBids() {
        return "bid api working ";
    }

    @PostMapping("/submit-bid")
    @ResponseStatus(HttpStatus.OK)
    public BidResponse submitBid(@RequestHeader("Authorization") String token,
                                 @RequestHeader("X-Username") String username,
                                 @RequestHeader("X-FirstName") String firstName,
                                 @RequestHeader("X-LastName") String lastName,
                                 @RequestHeader("X-Email") String email,
                                 @RequestBody @Valid BidRequest bidRequest) throws ProductNotFoundException {
        log.info("username passed downstream to the bid controller - submit bid " + username);
        log.debug("Token passed downstream: {}", token);
        return bidService.submitBid(username, firstName, lastName, email, bidRequest);
    }

    @GetMapping("/get-bids-for-product")
    public List<Bid> getBidsForProduct(@RequestHeader("Authorization") String token, @RequestParam Long productId) {

        return bidService.getBidsForProduct(productId);
    }

    @GetMapping("/get-bids-for-user")
    public List<BidResponse> getBidsForUser(@RequestHeader("Authorization") String token, @RequestHeader("X-Username") String username) {
        log.info("username passed downstream from bid controller " + username);
        log.debug("Token passed downstream: {}", token);

        return bidService.getBidsByUsername(username);
    }


    @GetMapping("/get-all-bids")
    @ResponseStatus(HttpStatus.OK)
    public List<BidResponse> getAllProducts(@RequestHeader("Authorization") String token) {
        return bidService.getAllBids();
    }

    @PostMapping("/determine-winning-bid")
    public ResponseEntity<?> determineWinningBid(
            @RequestHeader("Authorization") String token,
            @RequestBody Long productId) {
        try {
            WinningBid winningBid = bidService.determineWinningBid(productId);
            return ResponseEntity.ok(winningBid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid product ID provided.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while determining the winning bid. Please try again later.");
        }
    }

    @PostMapping("/close-bidding/{productId}")
    public ResponseEntity<?> closeBidding(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {
        try {
            List<Bid> bidsForProduct = bidRepository.findAllByProductProductId(productId);

            if (bidsForProduct.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No bids found for the specified product. Bidding cannot be closed.");
            }

            Bid highestBid = bidsForProduct.stream()
                    .max(Comparator.comparing(Bid::getBidAmount))
                    .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

            WinningBid winningBid = bidService.determineWinningBid(productId);

            return ResponseEntity.ok(winningBid);
        } catch (IllegalStateException e) {
            log.error("No valid bids found for productId: " + productId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No valid bids available for this product.");
        } catch (Exception e) {
            log.error("Error while determining the winning bid for productId: " + productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while closing the bidding. Please try again later.");
        }
    }


}
