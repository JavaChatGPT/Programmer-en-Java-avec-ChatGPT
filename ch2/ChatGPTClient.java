import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatGPTClient {

	public static void main(String[] args) {
    	String openAIKey = "";
    	String endpoint = "https://api.openai.com/v1/chat/completions";
      String model = "gpt-4-1106-preview";
      float temperature = 1.0f;
      int max_tokens = 256;
      float top_p = 1.0f;
      int frequency_penalty = 0;
      int presence_penalty = 0;

    	List<Message> messages = new ArrayList<>();
		  messages.add(new Message("system", "Tu es un commercialisateur de produits"));
    	messages.add(new Message("user", "Explique pourquoi Java est si largement utilisé dans l'entreprise"));

    	String jsonInput = null;
    	try {
        	ObjectMapper mapper = new ObjectMapper();
            Chat chat = new Chat(model, messages, temperature, max_tokens, top_p, frequency_penalty, presence_penalty);
        	jsonInput = mapper.writeValueAsString(chat);
    	} catch (JsonProcessingException e) {
        	e.printStackTrace();
    	}

    	try {
        	URL url = new URL(endpoint);
        	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        	connection.setRequestMethod("POST");
        	connection.setRequestProperty("Content-Type", "application/json");
        	connection.setRequestProperty("Authorization", "Bearer " + openAIKey);
        	connection.setDoOutput(true);

        	OutputStream outputStream = connection.getOutputStream();
        	outputStream.write(jsonInput.getBytes());
        	outputStream.flush();
        	outputStream.close();

        	int responseCode = connection.getResponseCode();
        	if (responseCode == HttpURLConnection.HTTP_OK) {
            	BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            	StringBuilder response = new StringBuilder();
            	String line;
            	while ((line = reader.readLine()) != null) {
                	response.append(line);
            	}
            	reader.close();

            	// Imprimer la réponse
            	System.out.println(response.toString());
        	} else {
            	System.out.println("Error: " + responseCode);
        	}
        	connection.disconnect();
    	} catch (IOException e) {
        	e.printStackTrace();
    	}
	}

	// Classe d'aide pour représenter l'objet Chat
	static class Chat {


    	@JsonProperty("model")
    	private String model;

    	@JsonProperty("messages")
    	private List<Message> messages;

      @JsonProperty("temperature")
    	private float temperature;

      @JsonProperty("max_tokens")
    	private int max_tokens;

      @JsonProperty("top_p")
    	private float top_p;

      @JsonProperty("frequency_penalty")
    	private int frequency_penalty;

      @JsonProperty("presence_penalty")
    	private int presence_penalty;




    	public Chat(String model, List<Message> messages, float temperature, int max_tokens, float top_p, int frequency_penalty, int presence_penalty) {
        	this.model = model;
        	this.messages = messages;
          this.temperature = temperature;
          this.max_tokens = max_tokens;
          this.top_p = top_p;
          this.frequency_penalty = frequency_penalty;
          this.presence_penalty = presence_penalty;

    	}

    	// Getters et setters (facultatifs, mais utiles si vous devez modifier l'objet ultérieurement)
	}

    // Classe d'aide pour représenter le message de chat
    static class Message {
        @JsonProperty("role")
        private String role;
    
        @JsonProperty("content")
        private String content;
    
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

}
