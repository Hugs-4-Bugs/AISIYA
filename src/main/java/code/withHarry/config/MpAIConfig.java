package code.withHarry.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Configuration
@PropertySource("classpath:application.properties")
public class MpAIConfig {

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.url}")
    private String apiUrl;

    @Bean
    public WebClient perplexityWebClient() {

        // ========== CONNECTION POOL CONFIGURATION ==========
        ConnectionProvider connectionProvider = ConnectionProvider.builder("perplexity-pool")
                .maxConnections(50)  // Maximum connections
                .pendingAcquireMaxCount(100)  // Max pending requests
                .pendingAcquireTimeout(Duration.ofSeconds(45))  // Wait timeout
                .maxIdleTime(Duration.ofSeconds(30))  // Idle connection timeout
                .build();

        // ========== HTTP CLIENT WITH DNS FIX ==========
        HttpClient httpClient = HttpClient.create(connectionProvider)
                // ⭐ CRITICAL FIX: Use system DNS resolver instead of Netty's default
                // This solves "Failed to resolve" errors on some networks
                .resolver(DefaultAddressResolverGroup.INSTANCE)

                // Connection timeout
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)

                // Response timeout (for long vision API calls)
                .responseTimeout(Duration.ofSeconds(60))

                // Read/Write timeouts
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS))
                );

        // ========== WEBCLIENT BUILDER ==========
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")

                // Use custom HttpClient with DNS fix
                .clientConnector(new ReactorClientHttpConnector(httpClient))

                // Buffer size for large images
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024) // 10MB buffer
                )

                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {

        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE)  // Same DNS fix
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofSeconds(90))  // Longer for file uploads
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(90, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(90, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(20 * 1024 * 1024) // 20MB for file uploads
                );
    }
}
