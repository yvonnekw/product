package com.auction.product.repostory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auction.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByProductIdIn(List<Long> productIds);
    //Product findById(Long productId);


    List<Product> findByUsername(String username);

    //List<Product> searchByQuery(String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByQuery(@Param("query") String query);

    //List<Product> findByBiddingPeriodExpired();
/*
    @Query("SELECT DISTINCT p FROM Product p " +
            "WHERE p.isSold = false AND p.quantity > 0 AND EXISTS (" +
            "SELECT b FROM Bid b WHERE b.product = p " +
            "AND b.bidTime <= :cutoffTime)")
    List<Product> findByBiddingPeriodExpired(@Param("cutoffTime") LocalDateTime cutoffTime);
*/

    @Query("SELECT p FROM Product p WHERE p.biddingEndTime <= :cutoffTime AND p.isSold = false")
    List<Product> findByBiddingPeriodExpired(@Param("cutoffTime") LocalDateTime cutoffTime);


}
