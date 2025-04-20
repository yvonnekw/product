package com.auction.product.config.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "bidding")
@Getter
@Setter
public class BiddingConfig {
    private Integer durationMinutes;
    private Integer durationDays;

    public Duration getBiddingDuration() {
        if (durationMinutes != null) {
            return Duration.ofMinutes(durationMinutes);
        } else if (durationDays != null) {
            return Duration.ofDays(durationDays);
        } else {
            throw new IllegalStateException("No bidding duration configured");
        }
    }
}

