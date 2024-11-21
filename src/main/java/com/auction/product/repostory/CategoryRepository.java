package com.auction.product.repostory;

import com.auction.product.model.Category;
import com.auction.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
