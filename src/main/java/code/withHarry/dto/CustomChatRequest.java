package code.withHarry.dto;

import java.util.List;

public class CustomChatRequest {
    private String message;
    private List<FileAttachment> attachedFiles;

    // ⭐ FIX 1: Add a default (no-argument) constructor for Jackson
    public CustomChatRequest() {
    }

    // Inner Class for File Attachment
    public static class FileAttachment {
        private String base64Data; // Matches JS payload key 'base64Data'
        private String fileName;   // Matches JS payload key 'fileName'

        // ⭐ FIX 2: Add a default (no-argument) constructor for Jackson
        public FileAttachment() {
        }
        
  
        public String getBase64Data() { return base64Data; }
        public void setBase64Data(String base64Data) { this.base64Data = base64Data; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }


    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<FileAttachment> getAttachedFiles() { return attachedFiles; }
    public void setAttachedFiles(List<FileAttachment> attachedFiles) { this.attachedFiles = attachedFiles; }
}