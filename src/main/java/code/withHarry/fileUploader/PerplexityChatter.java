package code.withHarry.fileUploader;
import okhttp3.*;
import java.io.IOException;

public class PerplexityChatter {

    private static final String API_KEY = "YOUR_PERPLEXITY_API_KEY";
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public String askWithFile(String prompt, String fileId) throws IOException {
        // **Sabse zaroori cheez**: File ID ko prompt mein `file:` prefix ke saath include karein.
        String userContent = prompt + " file:" + fileId;

        // JSON body banayein
        String jsonBody = "{"
            + "\"model\": \"pplx-70b-online\"," // Ya koi aur model
            + "\"messages\": ["
            + "  {\"role\": \"system\", \"content\": \"Be helpful and precise.\"}"
            + ", {\"role\": \"user\", \"content\": \"" + userContent + "\"}"
            + "]"
            + "}";

        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url("https://api.perplexity.ai/chat/completions")
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " : " + response.body().string());
            }
            return response.body().string();
        }
    }

    // Example: Isse aise call karein
    public static void main(String[] args) {
        try {
            // Step 1: File upload karein
            PerplexityFileUploader uploader = new PerplexityFileUploader();
            String fileId = uploader.uploadFile("path/to/your/document.pdf");
            System.out.println("File uploaded with ID: " + fileId);

            // Step 2: Uss file ID ke saath chat karein
            PerplexityChatter chatter = new PerplexityChatter();
            String prompt = "Is document ko summarize karo:"; // Aapka prompt
            String chatResponse = chatter.askWithFile(prompt, fileId);
            
            System.out.println("AI Response: " + chatResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
