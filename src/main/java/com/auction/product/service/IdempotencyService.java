package com.auction.product.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyService {

    // In-memory storage for processed idempotency keys
    private final ConcurrentHashMap<String, Boolean> processedRequests = new ConcurrentHashMap<>();

    /**
     * Check if the request with this key was already processed.
     */
    public boolean isDuplicateRequest(String key) {
        return processedRequests.containsKey(key);
    }

    /**
     * Store the idempotency key after successful processing.
     */
    public void storeRequest(String key) {
        processedRequests.put(key, true);
    }

    /**
     * Optional: Clear key if processing failed or rollback occurred.
     */
    public void removeKey(String key) {
        processedRequests.remove(key);
    }
}

