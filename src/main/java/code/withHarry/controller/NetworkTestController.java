package code.withHarry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/network")
public class NetworkTestController {

    @Autowired
    private WebClient perplexityWebClient;

    /**
     * Complete network diagnostic test
     */
    @GetMapping("/test")
    public Mono<Map<String, Object>> testNetwork() {
        Map<String, Object> result = new HashMap<>();
        
        // Test 1: DNS Resolution
        try {
            InetAddress address = InetAddress.getByName("api.perplexity.ai");
            result.put("dns_resolution", "✓ SUCCESS");
            result.put("resolved_ip", address.getHostAddress());
        } catch (UnknownHostException e) {
            result.put("dns_resolution", "✗ FAILED: " + e.getMessage());
            result.put("error", "Cannot resolve api.perplexity.ai");
            return Mono.just(result);
        }
        
        // Test 2: Google DNS Test
        try {
            InetAddress google = InetAddress.getByName("8.8.8.8");
            result.put("internet_connectivity", "✓ Can reach 8.8.8.8");
        } catch (Exception e) {
            result.put("internet_connectivity", "✗ No internet");
        }
        
        // Test 3: WebClient Configuration
        result.put("webclient_baseurl", perplexityWebClient
            .mutate()
            .build()
            .get()
            .uri("")
            .retrieve()
            .toBodilessEntity()
            .map(response -> "✓ WebClient configured")
            .onErrorReturn("✗ WebClient error")
            .block());
        
        return Mono.just(result);
    }
    
    /**
     * Simple ping test
     */
    @GetMapping("/ping")
    public Mono<Map<String, String>> ping() {
        return perplexityWebClient
            .get()
            .uri("/")
            .retrieve()
            .toBodilessEntity()
            .map(response -> Map.of(
                "status", "✓ SUCCESS",
                "message", "Can reach Perplexity API",
                "http_status", response.getStatusCode().toString()
            ))
            .onErrorResume(e -> Mono.just(Map.of(
                "status", "✗ FAILED",
                "error", e.getMessage(),
                "solution", "Check network connectivity and DNS settings"
            )));
    }
    
    /**
     * DNS resolution test
     */
    @GetMapping("/dns")
    public Map<String, String> testDNS() {
        Map<String, String> result = new HashMap<>();
        
        String[] hosts = {
            "api.perplexity.ai",
            "google.com",
            "8.8.8.8"
        };
        
        for (String host : hosts) {
            try {
                InetAddress addr = InetAddress.getByName(host);
                result.put(host, "✓ " + addr.getHostAddress());
            } catch (UnknownHostException e) {
                result.put(host, "✗ Cannot resolve");
            }
        }
        
        return result;
    }
}