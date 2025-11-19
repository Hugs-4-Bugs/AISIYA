package code.withHarry.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Collections;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class MpAIRequest {
    private String model;
    private List<Message> messages;
    private List<Attachment> attachments; 
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;
    
    @JsonProperty("return_citations")
    private Boolean returnCitations;
    
    @JsonProperty("return_images")
    private Boolean returnImages;

    
    @Data
    @NoArgsConstructor
    public static class Message {
        private String role;
        
        @JsonProperty("content")
        private List<ContentPart> contentParts; 

        public Message(String role, String textContent) {
            this.role = role;
            this.contentParts = Collections.singletonList(ContentPart.createTextPart(textContent));
        }
        
        public Message(String role, List<ContentPart> contentParts) {
            this.role = role;
            this.contentParts = contentParts;
        }

        public List<ContentPart> getContentParts() {
            return this.contentParts;
        }
        
        public void setContentParts(List<ContentPart> contentParts) {
             this.contentParts = contentParts;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        
        @JsonProperty("file_id") 
        private String fileId; 
    }
}