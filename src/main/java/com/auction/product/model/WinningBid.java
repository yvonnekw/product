package com.auction.product.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class WinningBid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long winningBidId;
    private Long bidId;
    private String username;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private BigDecimal winningAmount;
    private LocalDateTime bidEndTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
