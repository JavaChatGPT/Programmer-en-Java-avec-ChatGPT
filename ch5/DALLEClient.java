import java.io.IOException ;
import java.util.concurrent.TimeUnit ;

import okhttp3.* ;

import com.fasterxml.jackson.annotation.JsonProperty ;
import com.fasterxml.jackson.databind.ObjectMapper ;


public class DALLEClient {

    public static void main(String[] args) {

        String openAIKey = "" ;
        String endpoint = "https://api.openai.com/v1/images/generations" ;
        String contentType = "application/json" ;
        String prompt = "créer un logo pour une entreprise de composants électroniques axée sur le développement durable. Fais en sorte que le logo soit vert et prenne la forme d'un MCU ou d'un semi-conducteur similaire. Mets l'accent sur la simplicité et un design propre. Utilise l'art vectoriel, le design minimaliste, le contraste élevé, l'illustration numérique, les lignes nettes, les bords propres" ;
        int numberOfImages = 1 ;
        String size = "1024x1024" ;
        String model = "dall-e-3" ;
        String quality = "standard" ;
        String style = "vivid" ;
      

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build() ;
        MediaType mediaType = MediaType.get(contentType) ;
        
        // Crée l'objet JSON Créer une image
        Créer une image createImage = new CreateImage(prompt, numberOfImages, size, model, style, quality) ;
        
        // Utilise le Jackson ObjectMapper pour convertir l'objet en string JSON.
        String json = "" ;
        essayer {
            ObjectMapper mapper = new ObjectMapper() ;
            json = mapper.writeValueAsString(createImage) ;
        } catch (Exception e) {
            e.printStackTrace() ;
            retour ;
        }

        RequestBody body = RequestBody.Companion.create(json, mediaType) ;
        Request request = new Request.Builder()
                .url(endpoint)
                .method("POST", body)
                .addHeader("Content-Type", contentType)
                .addHeader("Authorization", "Bearer " + openAIKey)
                .build() ;

        essayer {
            Response response = client.newCall(request).execute() ;
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response) ;
                System.out.println(response.body().string()) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
    }

    // Classe interne pour l'objet JSON CreateImage
    public static class CreateImage {

        @JsonProperty("prompt")
        private String prompt ;

        @JsonProperty("n")
        private int n ;

        @JsonProperty("size")
        private String size ;

        @JsonProperty("model")
        private String model ;

        @JsonProperty("style")
        private String style ;

        @JsonProperty("quality")
        private String quality;
    
        public CreateImage(String prompt, int n, String size, String model, String style, String quality) {
            this.prompt = prompt ;
            this.n = n ;
            this.size = size ;
            this.model = model ;
            this.style = style ;
            this.quality = quality ;
        }
    
    }
    
}
