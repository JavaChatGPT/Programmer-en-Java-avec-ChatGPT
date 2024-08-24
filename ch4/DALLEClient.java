import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

public class DALLEClient {

    public static void main(String[] args) {

        String openAIKey = "";
        String endpoint = "https://api.openai.com/v1/images/generations";
        String contentType = "application/json";
        String prompt = "une photo macro 35 mm de 3 adorables chiots rottweiler sans collier allongés dans un champ.";
        int numberOfImages = 2;
        String size = "1024x1024";
      

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.get(contentType);
        
        // Créer l'objet JSON Create Image
        CreateImage createImage = new CreateImage(prompt, numberOfImages, size);
        
        // Utiliser Jackson ObjectMapper pour convertir l'objet en chaîne JSON
        String json = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(createImage);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.Companion.create(json, mediaType);
        Request request = new Request.Builder()
                .url(endpoint)
                .method("POST", body)
                .addHeader("Content-Type", contentType)
                .addHeader("Authorization", "Bearer " + openAIKey)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Code inattendu " + response);
                System.out.println(response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Classe interne pour l'objet JSON CreateImage
    public static class CreateImage {

        @JsonProperty("prompt")
        private String prompt;

        @JsonProperty("n")
        private int n;

        @JsonProperty("size")
        private String size;
    
        public CreateImage(String prompt, int n, String size) {
            this.prompt = prompt;
            this.n = n;
            this.size = size;
        }
    
    }
    
}
