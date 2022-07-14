package com.example;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.Base64;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@SpringBootApplication
public class Http2Server {

    public static void main(String[] args) {
        SpringApplication.run(Http2Server.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> routes() {
        return route()
                .POST("/records", req -> req.bodyToMono(byte[].class)
                        .doFirst(() -> log.info("Received request..."))
                        .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                        .doOnNext(base64Bytes -> log.info("Request body: {}", base64Bytes))
                        .flatMap(it -> ServerResponse.ok().bodyValue("OK")))
                .build();
    }
}

@Component
class NettyFactoryCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(server -> server.wiretap("wiretap-logger", LogLevel.INFO, AdvancedByteBufFormat.HEX_DUMP));
    }
}
