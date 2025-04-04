package com.auction.product.repostory;

import com.auction.product.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    //@Query("SELECT b FROM Bid b WHERE b.productId = :productId ORDER BY b.bidTime ASC")
    //Optional<Bid> findEarliestBidByProduct(@Param("productId") Long productId);

    //@Query("SELECT b FROM Bid b WHERE b.productId = :productId ORDER BY b.bidAmount DESC")
    //Optional<Bid> findHighestBidByProduct(@Param("productId") Long productId);

    //List<Bid> findByProductId(Long productId);

    //List<Bid> findByUsername(String username);

    //List<Bid> findAllByProductProductId(Long productId);

    @Query("SELECT b FROM Bid b WHERE b.product.productId = :productId ORDER BY b.bidTime ASC")
    Optional<Bid> findEarliestBidByProduct(@Param("productId") Long productId);

    @Query("SELECT b FROM Bid b WHERE b.product.productId = :productId ORDER BY b.bidAmount DESC")
    Optional<Bid> findHighestBidByProduct(@Param("productId") Long productId);

    List<Bid> findByProduct_ProductId(Long productId);  // Fixed method

    List<Bid> findByUsername(String username);

    List<Bid> findAllByProductProductId(Long productId);  // ✅ This is fine

}