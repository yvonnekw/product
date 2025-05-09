package com.auction.product.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Data
@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String productName;
    private String brandName;
    private String description;
    private String productImageUrl;
    private String colour;
    private String productSize;
    private Integer quantity;
    private BigDecimal startingPrice;
    private BigDecimal buyNowPrice;
    private boolean isSold;
    private boolean isAvailableForBuyNow;
    private String username;
    private LocalDateTime biddingEndTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
