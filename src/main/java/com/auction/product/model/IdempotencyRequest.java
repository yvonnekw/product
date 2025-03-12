package com.auction.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "idempotency_requests")
public class IdempotencyRequest {

    @Id
    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
