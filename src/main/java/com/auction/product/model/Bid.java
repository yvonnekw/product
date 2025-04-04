package com.auction.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "bids")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bidId;
    //private Long productId;
    private String username;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    //private String productName;
    //private String brandName;
    //private String description;
    //private String productImageUrl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    //private BigDecimal startingPrice;
    //private BigDecimal buyNowPrice;
    //private String colour;
    //private String productSize;
    //private Integer quantity;
    //private boolean isAvailableForBuyNow;
    //private boolean isSold;
    //private Long categoryId;
}
