package com.auction.product.controller;

import com.auction.product.dto.BidRequest;
import com.auction.product.dto.BidResponse;
import com.auction.product.exception.ProductNotFoundException;
import com.auction.product.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;

import java.util.List;
import com.auction.product.model.Bid;

@Slf4j
@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@RequestMapping("/api/v1/bids")
public class BidController {


    private final BidService bidService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String getBids() {
        return "bid api working ";
    }

    @PostMapping("/submit-bid")
            @ResponseStatus(HttpStatus.OK)
            public BidResponse submitBid(@RequestHeader("Authorization") String token, @RequestHeader("X-Username") String username,
                                 @RequestBody @Valid BidRequest bidRequest) throws ProductNotFoundException {
        log.info("username passed downstream to the bid controller - submit bid " + username);
        log.debug("Token passed downstream: {}", token);
        return bidService.submitBid(username, bidRequest);
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
    public List<Bid> getBidsForUser(@RequestHeader("Authorization") String token, @RequestHeader("X-Username") String username) {
        //String userId = extractUserIdFromAuthHeader(authHeader);
        log.info("username passed downstream from bid controller " + username);
        log.debug("Token passed downstream: {}", token);
        String buyerId = String.valueOf(Long.parseLong(username));

        return bidService.getBidsByUsername(buyerId);
    }


    @GetMapping("/get-all-bids")
    @ResponseStatus(HttpStatus.OK)
    public List<BidResponse> getAllProducts(@RequestHeader("Authorization") String token) {
        return bidService.getAllBids();
    }

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
