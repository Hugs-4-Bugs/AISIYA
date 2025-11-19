package code.withHarry.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisionMessage {
    private String role;
  
    private List<ContentPart> content; 
}
