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
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;

import java.util.Comparator;
import java.util.List;
import com.auction.product.model.Bid;

@Slf4j
@RestController
//@CrossOrigin(origins = "http://localhost:4200")
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
/*
    @PostMapping("/place-bid")
    public ProductWrapper placeBid(@RequestHeader("Authorization") String authHeader,
                                   @RequestParam Long productId,
                                   @RequestParam BigDecimal bidAmount) {
        String userId = extractUserIdFromAuthHeader(authHeader);  // Extract userId from the JWT
        Long buyerId = Long.parseLong(userId);  // Convert userId to Long

        return bidService.placeBid(buyerId, productId, bidAmount);
    }*/

    @GetMapping("/get-bids-for-product")
    public List<Bid> getBidsForProduct(@RequestHeader("Authorization") String token, @RequestParam Long productId) {

        return bidService.getBidsForProduct(productId);
    }

    @GetMapping("/get-bids-for-user")
    public List<BidResponse> getBidsForUser(@RequestHeader("Authorization") String token, @RequestHeader("X-Username") String username) {
        //String userId = extractUserIdFromAuthHeader(authHeader);
        log.info("username passed downstream from bid controller " + username);
        log.debug("Token passed downstream: {}", token);
       // String buyerId = String.valueOf(Long.parseLong(username));

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
            // Fetch all bids placed on the product
            List<Bid> bidsForProduct = bidRepository.findAllByProductProductId(productId);

            // If no bids are found, return a BAD_REQUEST response with a meaningful message
            if (bidsForProduct.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No bids found for the specified product. Bidding cannot be closed.");
            }

            // Find the highest bid from the placed bids
            Bid highestBid = bidsForProduct.stream()
                    .max(Comparator.comparing(Bid::getBidAmount))
                    .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

            // Call the determineWinningBid method with the productId
            WinningBid winningBid = bidService.determineWinningBid(productId);

            // Return the winning bid response
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


    /*

    @PostMapping("/determine-winning-bid")
    public ResponseEntity<WinningBid> determineWinningBid(@RequestHeader("Authorization") String token,
                                                          @RequestHeader("X-Username") String username,
                                                          @RequestBody DeterminedBidRequest determinedBidRequest) {
        try {
            WinningBid winningBid = bidService.determineWinningBid(username, determinedBidRequest);
            return ResponseEntity.ok(winningBid);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



    @PostMapping("/close-bidding/{productId}")
    public ResponseEntity<WinningBid> closeBidding(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Username") String username,
            @PathVariable Long productId) {
        try {
            // Fetch all bids placed on the product
            List<Bid> bidsForProduct = bidRepository.findAllByProductId(productId);

            // If no bids are found, return a BAD_REQUEST response
            if (bidsForProduct.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Find the highest bid from the placed bids
            Bid highestBid = bidsForProduct.stream()
                    .max(Comparator.comparing(Bid::getBidAmount))
                    .orElseThrow(() -> new IllegalStateException("No valid bids found for this product"));

            // Create a BidRequest with the highest bid amount
            DeterminedBidRequest determinedBidRequest = new DeterminedBidRequest(productId, highestBid.getBidAmount());

            // Call the determineWinningBid method with the BidRequest
            WinningBid winningBid = bidService.determineWinningBid(username, determinedBidRequest);

            // Return the winning bid response
            return ResponseEntity.ok(winningBid);
        } catch (Exception e) {
            // Log the exception with a meaningful message
            log.error("Error while determining the winning bid for productId: " + productId, e);

            // Handle any exceptions and return a BAD_REQUEST response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
*/


/*
    private String extractUserIdFromAuthHeader(String authHeader) {
        String jwtToken = authHeader.substring(7);

        return extractUserIdFromJwt(jwtToken);
    }
*//*
    private String extractUserIdFromJwt(String jwtToken) {
        JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation("http://localhost:9098/realms/auction-realm");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        return jwt.getClaimAsString("sub");
    }*/
}
