package com.szekai.pong;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
public class PongController {
    private final AtomicInteger pingsReceived = new AtomicInteger();

    @MessageMapping("pong-rr")
    public Mono<String> pong(String ping) {
        logPings(ping);
        return Mono.just(reply(ping));
    }

    @MessageMapping("pong-rc")
    public Flux<String> pong(Flux<PingValue> pings) {
        return pings.map(PingValue::getValue)
                    .doOnNext(this::logPings)
                    .map(this::reply);
    }

    private void logPings(String str) {
        int received = pingsReceived.incrementAndGet();
        log.info("received " + str + "(" + received + ") in Pong");
    }

    String reply(String in) {
        if (in.length() > 4) {
            in = in.substring(0, 4);
        }
        return switch (in.toLowerCase()) {
            case "ping" -> "pong";
            default -> throw new IllegalArgumentException("Value must be ping, not " + in);
        };
    }

}
