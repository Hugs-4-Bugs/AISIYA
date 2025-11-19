package code.withHarry.service;

import code.withHarry.dto.PerplexityFileUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;

@Service
public class PerplexityFileService {

    private static final Logger logger = LoggerFactory.getLogger(PerplexityFileService.class);
    private final WebClient perplexityFileWebClient;
    
    // Constructor Injection
    public PerplexityFileService(@Value("${perplexity.api.key}") String apiKey, WebClient.Builder webClientBuilder) {
        this.perplexityFileWebClient = webClientBuilder
            .baseUrl("https://api.perplexity.ai/files") // Corrected URL
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .build();
    }

    public Mono<String> uploadFile(String base64Data, String fileName) {
        String data = base64Data;
        if (data.contains(",")) {
            data = data.split(",", 2)[1];
        }
        
        final byte[] fileBytes = Base64.getDecoder().decode(data);
        final String mimeType = getMimeType(fileName);

        return Mono.fromCallable(() -> {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
                @Override
                public long contentLength() {
                    return fileBytes.length;
                }
            }, MediaType.parseMediaType(mimeType));
            
            return builder.build();

        })
        .subscribeOn(Schedulers.boundedElastic()) 
        .flatMap(multipartBody -> {
            return perplexityFileWebClient.post()
                    .uri("/upload") 
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(multipartBody)
                    .retrieve()
                    .onStatus(s -> s.isError(), clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.error("Perplexity File Upload Error: {}", errorBody);
                            return Mono.error(new RuntimeException("Perplexity File Upload Failed: " + errorBody));
                        }))
                    .bodyToMono(PerplexityFileUploadResponse.class)
                    .map(response -> {
                        if (response == null || response.getId() == null) {
                            throw new RuntimeException("File upload successful but File ID is missing in response.");
                        }
                        logger.info("File upload successful. File ID: {}", response.getId());
                        return response.getId(); 
                    });
        });
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) return "application/msword";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}