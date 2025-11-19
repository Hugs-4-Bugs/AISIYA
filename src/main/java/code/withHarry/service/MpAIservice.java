

package code.withHarry.service;

import code.withHarry.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class MpAIservice {

    private final PerplexityFileService perplexityFileService;
    private final WebClient perplexityWebClient;
	private static final Logger logger = LoggerFactory.getLogger(MpAIservice.class);
    

    private static final String MODEL_SONAR = "sonar"; 
    private static final String MODEL_SONAR_PRO = "sonar-pro";
    private static final String MODEL_VOICE_CHAT = "sonar"; 

       
        public MpAIservice(WebClient perplexityWebClient, PerplexityFileService perplexityFileService, PerplexityFileService perplexityFileService2) {
            this.perplexityWebClient = perplexityWebClient; 
            this.perplexityFileService = perplexityFileService;
        }

     
        public Mono<MpAIResponse> multiModalChat(String message, List<CustomChatRequest.FileAttachment> attachedFiles) {
            
            String visionSystemPrompt = "You are a world-class expert image analyst. " +
                                      "When a user uploads an image, analyze it in meticulous detail. " +
                                      "Identify the main subject, background, context, and any relevant details " +
                                      "with the highest possible accuracy. Be as precise as you can.";

           
            return chatInternalMultiModal(message, attachedFiles, MODEL_SONAR_PRO, visionSystemPrompt); 
        }

        public Mono<MpAIResponse> chatWithVoiceModel(String message) {
           
            String voiceSystemPrompt = "You are a friendly, concise, and highly conversational AI assistant. Respond briefly and clearly, avoiding long paragraphs. Your response will be spoken aloud.";
            return chatInternal(message, MODEL_VOICE_CHAT, voiceSystemPrompt);
        }

    
        private Mono<MpAIResponse> chatInternal(String message, String model, String systemPrompt) {
           
            if (message == null || message.trim().isEmpty()) {
                logger.warn("Chat message was empty, returning error response.");
                MpAIResponse errorResponse = new MpAIResponse();
                errorResponse.setError(new MpAIResponse.ErrorDetail(
                    "Message content was empty. Please type a message.",
                    "invalid_request_error",
                    "400"
                ));
                return Mono.just(errorResponse);
            }
            
            MpAIRequest request = new MpAIRequest();
            request.setModel(model);
            request.setMaxTokens(1024);
            request.setTemperature(0.7);

          
            List<MpAIRequest.Message> messages = new ArrayList<>();
            if (systemPrompt != null) {
                messages.add(new MpAIRequest.Message("system", systemPrompt));
            }
            messages.add(new MpAIRequest.Message("user", message));
            request.setMessages(messages);

            return callPerplexityApi(request, model);
        }

        private Mono<MpAIResponse> chatInternalMultiModal(String message, List<CustomChatRequest.FileAttachment> attachedFiles, String model, String systemPrompt) {
            
            List<CustomChatRequest.FileAttachment> visionFiles = attachedFiles.stream()
                .filter(f -> f.getFileName().toLowerCase().matches(".*\\.(png|jpe?g|webp)$"))
                .collect(Collectors.toList());

            List<CustomChatRequest.FileAttachment> ragFiles = attachedFiles.stream()
                .filter(f -> !visionFiles.contains(f)) 
                .collect(Collectors.toList());

           
            Mono<List<MpAIRequest.Attachment>> ragAttachmentsMono = Mono.just(new ArrayList<MpAIRequest.Attachment>())
                .flatMap(attachments -> {
                    if (!ragFiles.isEmpty()) {
                        CustomChatRequest.FileAttachment ragFile = ragFiles.get(0);

                        return perplexityFileService.uploadFile(ragFile.getBase64Data(), ragFile.getFileName())
                            .map(fileId -> {
                                attachments.add(new MpAIRequest.Attachment(fileId));
                                return attachments;
                            })
                            .onErrorResume(e -> {
                                logger.error("RAG File Upload Error: {}", e.getMessage());
                                return Mono.just(attachments); 
                            });
                    }
                    return Mono.just(attachments);
                });


            return ragAttachmentsMono.flatMap(ragAttachments -> {
                MpAIRequest request = new MpAIRequest();
                request.setModel(model);
                request.setMaxTokens(2048); 
                request.setTemperature(0.1);
                
         
                List<ContentPart> contentParts = new ArrayList<>();
                
                for (CustomChatRequest.FileAttachment vFile : visionFiles) {
                    
                    String base64DataUri = vFile.getBase64Data(); 
                    
                    contentParts.add(ContentPart.createImagePart(base64DataUri));
                }
                

                String userText = (message == null || message.trim().isEmpty()) 
                                ? "Analyze the attached file(s)." // Default prompt
                                : message;
                contentParts.add(ContentPart.createTextPart(userText));
                
              
                List<MpAIRequest.Message> messages = new ArrayList<>();
                
                if (systemPrompt != null) {
                    messages.add(new MpAIRequest.Message("system", systemPrompt));
                }

                MpAIRequest.Message userMessage = new MpAIRequest.Message();
                userMessage.setRole("user");
                userMessage.setContentParts(contentParts); 
                messages.add(userMessage);

                request.setMessages(messages);

                if (!ragAttachments.isEmpty()) {
                    request.setAttachments(ragAttachments);
                    request.setReturnCitations(true); 
                    request.setReturnImages(true); 
                }
                
                return callPerplexityApi(request, model);
            });
        }

       
         private Mono<MpAIResponse> callPerplexityApi(MpAIRequest request, String model) {
          
             request.setModel(model); 
             logger.info("Sending request to Perplexity API with model: {}", model);
             
             return perplexityWebClient.post()
                 .uri("/chat/completions") 
                 .bodyValue(request)
                 .retrieve()
                 .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), clientResponse -> {
                     return clientResponse.bodyToMono(String.class)
                         .flatMap(errorBody -> {
                             logger.error("Perplexity API Error Status: {}, Body: {}", clientResponse.statusCode(), errorBody);
                             String errorMessage = "Perplexity API Error: " + clientResponse.statusCode() + " | Details: " + errorBody;
                             return Mono.error(new RuntimeException(errorMessage)); 
                         });
                 })
                 .bodyToMono(MpAIResponse.class)
                 .doOnSuccess(response -> logger.info("✓ {} chat successful", model));
         }
    }