package code.withHarry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpAIResponse {
    
    private List<Choice> choices;
    private String id;
    private String model;
    private String object;
    private Usage usage;
    
    private ErrorDetail error; 
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private String finish_reason;
        private int index;
        private Message message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private int completion_tokens;
        private int prompt_tokens;
        private int total_tokens;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail { 
        private String message;
        private String type; 
        private String code;
    }
}