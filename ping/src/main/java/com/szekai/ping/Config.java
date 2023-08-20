package com.szekai.ping;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    @ConditionalOnProperty(name = "ping.one.enabled", matchIfMissing = true)
    public PingService ping1() {
        return new PingService(1L);
    }

    @Bean
    @ConditionalOnProperty("ping.two.enabled")
    public PingService ping2() {
        return new PingService(2L);
    }
//
//    @Bean
//    @ConditionalOnProperty(name = "pong.enabled", matchIfMissing = true)
//    public Pong pong() {
//        return new Pong();
//    }
}
