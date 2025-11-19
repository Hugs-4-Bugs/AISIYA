package code.withHarry.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import code.withHarry.dto.CustomChatRequest;
import code.withHarry.dto.MpAIResponse;
import code.withHarry.service.MpAIservice;
import code.withHarry.service.PerplexityFileService; 
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/perplexity")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MpAIcontroller {
    
    private static final Logger logger = LoggerFactory.getLogger(MpAIcontroller.class);
    private final MpAIservice mpAIservice;
    private final PerplexityFileService perplexityFileService; 
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of(
            "status", "ok", 
            "message", "Perplexity AI API is running",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    @PostMapping("/chat")
    public Mono<ResponseEntity<MpAIResponse>> multiModalChat(@RequestBody CustomChatRequest request) {
        
        List<CustomChatRequest.FileAttachment> files = request.getAttachedFiles();
        boolean hasFiles = files != null && !files.isEmpty();

        if (hasFiles) {
            logger.info("=== MULTI-MODAL (VISION/RAG) PATH SELECTED. Files: {} ===", files.size());
            return mpAIservice.multiModalChat(request.getMessage(), files)
                .map(this::processAndCleanResponse) 
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e -> {
                    logger.error("Multi-Modal Chat Error: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.ok(
                        createErrorResponse("Multi-Modal Chat Error: " + extractErrorMessage(e))
                    ));
                });
        } else {
            logger.info("=== TEXT-ONLY PATH SELECTED ===");
            return mpAIservice.chatWithVoiceModel(request.getMessage())
                .map(this::processAndCleanResponse) 
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e -> {
                    logger.error("Text Chat Error: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.ok(
                        createErrorResponse("Text Chat Error: " + extractErrorMessage(e))
                    ));
                });
        }
    }

    @PostMapping("/voice-chat")
    public Mono<ResponseEntity<MpAIResponse>> voiceChat(@RequestBody CustomChatRequest request) {
        logger.info("=== VOICE CHAT PATH SELECTED ===");
        return mpAIservice.chatWithVoiceModel(request.getMessage()) 
            .map(this::processAndCleanResponse)
            .map(ResponseEntity::ok)
            .onErrorResume(RuntimeException.class, e -> {
                logger.error("Voice Chat Error: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.ok(
                    createErrorResponse("Voice Chat Error: " + extractErrorMessage(e))
                ));
            });
    }
    
    /**
     * Helper method to clean the response content (e.g., remove citations and model info)
     */
    private MpAIResponse processAndCleanResponse(MpAIResponse response) {
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            String content = response.getChoices().get(0).getMessage().getContent();
        
            content = content.replaceAll("\\[\\d+\\]", "").trim(); 

            content = content.replaceAll("Model: [^|]+\\| Tokens: \\d+", "").trim();

            
            content = content.replaceAll("(?m)^\\s*---?\\s*$", "").trim(); 

            response.getChoices().get(0).getMessage().setContent(content);
        }
        return response;
    }

    private String extractErrorMessage(Throwable e) {
        String message = e.getMessage();
        
        if (message != null && message.contains("Perplexity API Error:")) {
            int detailIndex = message.indexOf("Details: ");
            if (detailIndex != -1) {
                return message.substring(detailIndex + 9);
            }
        }
        return message != null ? message : "Unknown error occurred";
    }
    
    private MpAIResponse createErrorResponse(String message) {
        MpAIResponse.ErrorDetail errorDetail = new MpAIResponse.ErrorDetail();
        errorDetail.setMessage(message);
        errorDetail.setType("application_error");
        
        MpAIResponse response = new MpAIResponse();
        response.setError(errorDetail);
        return response;
    }
}