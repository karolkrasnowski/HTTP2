package com.example;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Slf4j
@SpringBootApplication
public class Http2Client {

    public static void main(String[] args) {
        SpringApplication.run(Http2Client.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    void onStartup() {
        log.info("Starting...");
        WebClient http2Client = WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .protocol(HttpProtocol.H2C)
                                        .wiretap("wiretap-logger", LogLevel.INFO, AdvancedByteBufFormat.HEX_DUMP)
                        )
                )
                .build();

        http2Client.post()
                .uri("http://localhost:8080/records")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(RandomUtils.nextBytes(20))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("Response: {}", response))
                .subscribe();
    }
}
