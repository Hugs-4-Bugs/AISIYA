package code.withHarry.fileUploader;
import okhttp3.*; // Sabhi OkHttp classes import karein
import java.io.File;
import java.io.IOException;

public class PerplexityFileUploader {

    private static final String API_KEY = "YOUR_PERPLEXITY_API_KEY"; // Apna API Key yahaan daalein
    private static final OkHttpClient client = new OkHttpClient();

    public String uploadFile(String filePath) throws IOException {
        File file = new File(filePath);
        
        // File type ko dynamically detect karna behtar hai, abhi ke liye example
        MediaType mediaType;
        if (filePath.endsWith(".pdf")) {
            mediaType = MediaType.parse("application/pdf");
        } else if (filePath.endsWith(".txt")) {
            mediaType = MediaType.parse("text/plain");
        } else {
            // Aap aur file types add kar sakte hain
            mediaType = MediaType.parse("application/octet-stream");
        }

        // Request body ko multipart/form-data banayein
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, mediaType))
                .addFormDataPart("purpose", "passages") // 'passages' retrieval ke liye zaroori hai
                .build();

        // Request banayein
        Request request = new Request.Builder()
                .url("https://api.perplexity.ai/files")
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        // Request execute karein aur response lein
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " : " + response.body().string());
            }

            // Response JSON mein se file ID nikaalein
            // Aapko ek JSON parsing library (jaise Jackson ya Gson) use karni chahiye
            // Abhi ke liye simple string parsing:
            String responseBody = response.body().string();
            System.out.println("Upload Response: " + responseBody);

            // Example response: {"id":"file-abc...","purpose":"passages",...}
            // Yahaan JSON se 'id' nikaalein.
            // Simple example (production ke liye JSON library use karein):
            String fileId = responseBody.split("\"id\":\"")[1].split("\"")[0];
            return fileId;
        }
    }
}