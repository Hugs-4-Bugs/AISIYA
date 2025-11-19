package code.withHarry.service;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ImageAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisService.class);
    private final WebClient perplexityWebClient; 

    public ImageAnalysisService(WebClient perplexityWebClient) {
        this.perplexityWebClient = perplexityWebClient;
    }

    public String analyzeImage(String base64Image) {
        try {
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.split(",", 2)[1];
            }

            byte[] imageBytes = Base64.decodeBase64(imageData);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                return "[Image data could not be read. Please ensure it is a valid image file (JPEG/PNG).]";
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            logger.info("Image analysis successful. Dimensions: {}x{}", width, height);
            
            return String.format(
                "[Image uploaded: %dx%d pixels. Please ask your question about this image.]", 
                width, height
            );
            
        } catch (IOException e) {
            logger.error("Error processing image data for analysis: {}", e.getMessage(), e);
            return "[Image processing failed. Please try a different image file.]"; 
        } catch (Exception e) {
            logger.error("Unexpected error in analyzeImage: {}", e.getMessage(), e);
            return "[An internal error occurred during image preparation.]";
        }
    }
}