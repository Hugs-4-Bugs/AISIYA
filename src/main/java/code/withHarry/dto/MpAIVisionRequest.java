package code.withHarry.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// Yeh request body banegi jab hum image bhejenge
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpAIVisionRequest {
    private String model;
    private List<VisionMessage> messages;
    
    // Aap max_tokens, temperature etc. bhi yahaan add kar sakte hain
}
