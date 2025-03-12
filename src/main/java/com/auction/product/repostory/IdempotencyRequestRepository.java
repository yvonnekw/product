package com.auction.product.repostory;

import com.auction.product.model.IdempotencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRequestRepository extends JpaRepository<IdempotencyRequest, String> {
    boolean existsByKey(String key);
}
