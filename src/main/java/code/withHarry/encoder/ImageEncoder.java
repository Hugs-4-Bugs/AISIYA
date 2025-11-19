package code.withHarry.encoder;

import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class ImageEncoder {
    
    public String encodeImageToBase64(String imagePath) throws IOException {
        
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        
      
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return base64Image;
    }

 
    public String getMimeType(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
    
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            if (imagePath.endsWith(".png")) return "image/png";
            if (imagePath.endsWith(".jpg") || imagePath.endsWith(".jpeg")) return "image/jpeg";
            if (imagePath.endsWith(".webp")) return "image/webp";
            return "application/octet-stream"; // Fallback
        }
        return mimeType;
    }
}