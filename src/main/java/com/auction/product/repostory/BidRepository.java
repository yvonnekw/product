package com.auction.product.repostory;

import com.auction.product.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByProductId(Long productId);

    List<Bid> findByUsername(String username);
}