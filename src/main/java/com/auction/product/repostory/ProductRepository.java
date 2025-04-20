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

    List<Product> findByUsername(String username);

    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByQuery(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE p.biddingEndTime <= :cutoffTime AND p.isSold = false")
    List<Product> findByBiddingPeriodExpired(@Param("cutoffTime") LocalDateTime cutoffTime);


}
