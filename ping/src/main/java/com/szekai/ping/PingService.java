package com.szekai.ping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.broker.client.BrokerRSocketClient;
import io.rsocket.broker.client.BrokerRSocketConnector;
import io.rsocket.broker.common.Id;
import io.rsocket.broker.common.WellKnownKey;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class PingService implements Ordered, ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PingService.class);
    private final Id id;

    public PingService(Long id) {
        this.id = new Id(0, id);
    }

    private final AtomicInteger pongsReceived = new AtomicInteger();

    @Override
    public int getOrder() {
        return 0;
    }

    public void onApplicationEventx(ApplicationReadyEvent event) {
        logger.info("Skipping Ping {}", id);
    }

    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Starting Ping {}", id);
        ConfigurableEnvironment env = event.getApplicationContext().getEnvironment();
        Integer take = env.getProperty("ping.take", Integer.class, null);
        String host = env.getProperty("ping.broker.host", String.class, "localhost");
        Integer port = env.getProperty("ping.broker.port", Integer.class, 8011);

        logger.debug("ping.take: {}", take);

        BrokerRSocketClient client = BrokerRSocketConnector.create()
                .routeId(id)
                .serviceName("ping")
                .toRSocketClient(TcpClientTransport.create(host, port));

        Flux<? extends String> pongFlux = Flux.from(doPing(take, client));

        boolean subscribe = env.getProperty("ping.subscribe", Boolean.class, true);

        if (subscribe) {
            pongFlux.subscribe();
        }

        startDaemonThread("ping" + id, Sinks.one());
    }

    //Publisher<? extends String> doPing(Integer take, RSocket socket) {
    Publisher<? extends String> doPing(Integer take, BrokerRSocketClient client) {
        Flux<String> pong = client
                .requestChannel(Flux.interval(Duration.ofSeconds(1)).map(i -> {
                            ByteBuf data = ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                                    "ping" + id);
                            CompositeByteBuf compositeMetadata = client.allocator().compositeBuffer();
                            //client.encodeAddressMetadata(compositeMetadata, "pong");
                            client.encodeAddressMetadata(compositeMetadata, tags -> tags.with(WellKnownKey.SERVICE_NAME, "pong"));
                            logger.debug("Sending ping" + id);
                            return DefaultPayload.create(data, compositeMetadata);
                            // onBackpressure is needed in case pong is not available yet
                        }).log("doPing")
                        .onBackpressureDrop(payload -> logger
                                .debug("Dropped payload {}", payload.getDataUtf8())))
                .map(Payload::getDataUtf8).doOnNext(str -> {
                    int received = pongsReceived.incrementAndGet();
                    logger.info("received {}({}) in Ping {}", str, received, id);
                }).doFinally(signal -> client.dispose());
        if (take != null) {
            return pong.take(take);
        }
        return pong;
    }

    private static void startDaemonThread(String name, Sinks.One<Void> onClose) {
        Thread awaitThread =
                new Thread(name + "-thread") {

                    @Override
                    public void run() {
                        onClose.asMono().block();
                    }
                };
        awaitThread.setContextClassLoader(PingService.class.getClassLoader());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }
}
