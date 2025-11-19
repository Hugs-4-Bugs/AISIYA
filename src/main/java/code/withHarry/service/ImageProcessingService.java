package code.withHarry.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class ImageProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);

 
    public String describeImage(String base64Image) {
        try {
         
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.decodeBase64(imageData);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

         
            int width = image.getWidth();
            int height = image.getHeight();

            boolean hasColors = analyzeColors(image);
                       StringBuilder description = new StringBuilder();
            description.append("Image uploaded with dimensions: ")
                      .append(width).append("x").append(height).append(" pixels. ");
            
            if (hasColors) {
                description.append("The image appears to be colorful. ");
            } else {
                description.append("The image appears to be monochrome or grayscale. ");
            }
            
            description.append("Please analyze this image and provide relevant information.");

            logger.info("Generated image description: {}", description);
            return description.toString();

        } catch (IOException e) {
            logger.error("Error processing image: {}", e.getMessage());
            return "An image was uploaded but could not be processed. Please describe what you see in the image.";
        }
    }

    public String extractTextFromImage(String base64Image) {
        try {
            String imageData = base64Image;
            if (base64Image.contains(",")) {
                imageData = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.decodeBase64(imageData);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);

            // Try OCR if Tesseract is available
            try {
                Tesseract tesseract = new Tesseract();
                // tesseract.setDatapath("C:/tessdata"); // Windows path
                // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata"); // Linux
                String text = tesseract.doOCR(image);
                if (text != null && !text.trim().isEmpty()) {
                    logger.info("OCR extracted text: {}", text);
                    return "Text found in image: " + text;
                }
            } catch (Exception e) {
                logger.warn("OCR not available: {}", e.getMessage());
            }

            return describeImage(base64Image);

        } catch (IOException e) {
            logger.error("Error processing image: {}", e.getMessage());
            return "Image processing failed";
        }
    }
    
    /**
     * Simple color analysis
     */
    private boolean analyzeColors(BufferedImage image) {
        // Sample a few pixels to check for colors
        int samples = Math.min(100, image.getWidth() * image.getHeight());
        int coloredPixels = 0;
        
        for (int i = 0; i < samples; i++) {
            int x = (int) (Math.random() * image.getWidth());
            int y = (int) (Math.random() * image.getHeight());
            int rgb = image.getRGB(x, y);
            
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            
            // Check if pixel has color variation
            if (Math.abs(red - green) > 20 || Math.abs(green - blue) > 20 || Math.abs(red - blue) > 20) {
                coloredPixels++;
            }
        }
        
        return coloredPixels > samples * 0.3; // 30% threshold
    }
}

