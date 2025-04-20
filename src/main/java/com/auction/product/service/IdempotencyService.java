package com.auction.product.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyService {

    private final ConcurrentHashMap<String, Boolean> processedRequests = new ConcurrentHashMap<>();

    public boolean isDuplicateRequest(String key) {
        return processedRequests.containsKey(key);
    }

    public void storeRequest(String key) {
        processedRequests.put(key, true);
    }

    public void removeKey(String key) {
        processedRequests.remove(key);
    }
}

