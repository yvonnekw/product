package com.auction.product.config.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "bidding")
public class BiddingConfig {
    private int durationDays;

    public Duration getBiddingDuration() {
        return Duration.ofDays(durationDays);
    }
}

