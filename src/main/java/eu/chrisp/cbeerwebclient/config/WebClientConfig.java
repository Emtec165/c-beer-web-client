package eu.chrisp.cbeerwebclient.config;

import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import static eu.chrisp.cbeerwebclient.config.WebClientProperties.BASE_URL;

@Configuration
public class WebClientConfig {

    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
//                        .wiretap(true)))             // <---- WORKS JUST FINE
                        .wiretap("reactor.netty.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)))
                .build();
    }
}
