package com.auction.product.repostory;

import com.auction.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByProductIdIn(List<Long> productIds);


    List<Product> findByUsername(String username);
}
