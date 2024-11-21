package com.auction.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


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
    private boolean isSold;
    private String productName;
    private String brandName;
    private String description;
    private String colour;
    private String productSize;
    private double quantity;
    private BigDecimal startingPrice;
    private BigDecimal buyNowPrice;
    private String username;
    private boolean isAvailableForBuyNow;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
