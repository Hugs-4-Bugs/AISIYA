package code.withHarry.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentPart {
    private String type;
    private String text;
    private Map<String, String> image_url; // Map to handle {"url": "base64_data"}

    // Factory method for text part
    public static ContentPart createTextPart(String text) {
        return new ContentPart("text", text, null);
    }

    // Factory method for image part
    public static ContentPart createImagePart(String base64DataUri) {
        return new ContentPart("image_url", null, Map.of("url", base64DataUri));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {
        private String url; 
    }
}