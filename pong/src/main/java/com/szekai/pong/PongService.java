package com.szekai.pong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.broker.client.BrokerRSocketClient;
import io.rsocket.broker.client.BrokerRSocketConnector;
import io.rsocket.broker.common.Id;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import io.rsocket.util.RSocketProxy;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class PongService implements Ordered, ApplicationListener<ApplicationReadyEvent> {
    private final Id routeId = new Id(0, 3L);
    @Override
    public int getOrder() {
        return 1;
    }

    private final AtomicInteger pingsReceived = new AtomicInteger();
    private BrokerRSocketClient rSocketClient;

    public void onApplicationEventx(ApplicationReadyEvent event) {
        log.info("Skipping Pong");
    }

    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableEnvironment env = event.getApplicationContext().getEnvironment();
        Integer pongDelay = env.getProperty("pong.delay", Integer.class, 5000);
        try {
            Thread.sleep(pongDelay);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Starting Pong");
        String host = env.getProperty("pong.broker.host", String.class, "localhost");
        Integer port = env.getProperty("pong.broker.port", Integer.class, 8011);
        //MicrometerRSocketInterceptor interceptor = new MicrometerRSocketInterceptor(
        //		meterRegistry, Tag.of("component", "pong"));

        rSocketClient = BrokerRSocketConnector.create()
                .routeId(routeId)
                .serviceName("pong")
                .configure(connector -> {
                    /*.addRequesterPlugin(interceptor)*/
                    connector.acceptor((setup, sendingSocket) -> Mono
                            .just(accept(sendingSocket)));
                })
                .toRSocketClient(TcpClientTransport.create(host, port));

        rSocketClient.source() // proxy
                .block();

        startDaemonThread("pong", Sinks.one());
    }

    @SuppressWarnings("Duplicates")
    RSocket accept(RSocket rSocket) {
        return new RSocketProxy(rSocket) {

            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                return Flux.from(payloads).map(Payload::getDataUtf8).doOnNext(str -> {
                    int received = pingsReceived.incrementAndGet();
                    log.info("received {}({}) in Pong", str, received);
                }).map(PongService::reply).map(reply -> {
                    ByteBuf data = ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                            reply);
                    CompositeByteBuf routingMetadata = rSocketClient.allocator().compositeBuffer();
                    rSocketClient.encodeAddressMetadata(routingMetadata, "ping");
                    return DefaultPayload.create(data, routingMetadata);
                });
            }
        };
    }
    private static void startDaemonThread(String name, Sinks.One<Void> onClose) {
        Thread awaitThread =
                new Thread(name + "-thread") {

                    @Override
                    public void run() {
                        onClose.asMono().block();
                    }
                };
        awaitThread.setContextClassLoader(PongService.class.getClassLoader());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    static String reply(String in) {
        if (in.length() > 4) {
            in = in.substring(0, 4);
        }
        switch (in.toLowerCase()) {
            case "ping":
                return "pong";
            case "pong":
                return "ping";
            default:
                throw new IllegalArgumentException("Value must be ping or pong, not " + in);
        }
    }

}
