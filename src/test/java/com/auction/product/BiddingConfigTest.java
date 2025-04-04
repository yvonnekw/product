package com.auction.product;

import com.auction.product.config.constant.BiddingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class BiddingConfigTest {

    @TestConfiguration
    @EnableConfigurationProperties(BiddingConfig.class) // Enable your configuration properties class
    static class TestConfig {
    }

    @Autowired
    private BiddingConfig biddingConfig;

    @Test
    public void testBiddingDuration() {
        // Validate if the biddingDuration property was injected correctly
        assertThat(biddingConfig.getBiddingDuration()).isEqualTo(Duration.ofMinutes(5));    }
}
