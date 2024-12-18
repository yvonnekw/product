package com.auction.product.repostory;


import com.auction.product.model.WinningBid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WinningBidRepository extends JpaRepository<WinningBid, Long> {
}
