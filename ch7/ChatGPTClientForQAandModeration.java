import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatGPTClientForQAandModeration {
    
    //
    //  Paramètres OpenAI que nous savons déjà utiliser
    //
    String openAIKey = "";
    String endpoint = "https://api.openai.com/v1/chat/completions";
    String model = "gpt-4";
    float temperature = 1.0f;
    int max_tokens = 1560;
    float top_p = 1.0f;
    int frequency_penalty = 0;
    int presence_penalty = 0;

    String systemMessage = null;
    String initialInstructionsToChatGPT = null;

    //
    // Le constructeur doit recevoir le contenu du fichier FAQ.txt
    // et le message système
    //
    public ChatGPTClientForQAandModeration(String systemMessage, String initialInstructionsToChatGPT) {
        this.systemMessage = systemMessage;
        this.initialInstructionsToChatGPT = initialInstructionsToChatGPT;
    }

    public String sendMessageFromDiscordUser(String discordMessageText) {

        String answerFromChatGPT = "";

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemMessage));
        messages.add(new Message("user", initialInstructionsToChatGPT));
        messages.add(new Message("user", discordMessageText));

        String jsonInput = null;
        try {
            ObjectMapper mapper = new ObjectMapper();

            Chat chat = Chat.builder()
                .model(model)
                .messages(messages)
                .temperature(temperature)
                .maxTokens(max_tokens)
                .topP(top_p)
                .frequencyPenalty(frequency_penalty)
                .presencePenalty(presence_penalty)
                .build();

            jsonInput = mapper.writeValueAsString(chat);
            System.out.println(jsonInput);
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

                // Afficher la réponse
                answerFromChatGPT = extractAnswerFromJSON(response.toString());
                System.out.println(answerFromChatGPT);
            } else {
                System.out.println("Erreur : " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answerFromChatGPT;
    }

    //
    // Nous ne sommes intéressés que par le "message.content" dans la réponse JSON
    // Voici donc un moyen facile de l'extraire
    //
    public String extractAnswerFromJSON(String jsonResponse) {
        String chatGPTAnswer = "";

         try {
            // Créer une instance d'ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // Analyser la chaîne JSON
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Extraire le paramètre "content"
            JsonNode contentNode = rootNode.at("/choices/0/message/content");
            chatGPTAnswer = contentNode.asText();

            System.out.println("Contenu : " + chatGPTAnswer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chatGPTAnswer;
    }

}
