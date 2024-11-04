package com.auction.product.dto;

//@Data

public record UserResponse(
        Long userId,
        String firstName,
        String lastName,
        String emailAddress) {

}
