import java.io.*;
import java.nio.file.*;
import okhttp3.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Classe client pour transcrire des fichiers MP3 en utilisant le modèle Whisper d'OpenAI.
 */
public class WhisperClient {

    public static void main(String[] args) throws IOException {
        // Clé API pour OpenAI (cela doit être remplacé par votre véritable clé API)
        String openAIKey = "";
        // Point de terminaison de transcription d'OpenAI
        String endpoint = "https://api.openai.com/v1/audio/transcriptions";
        // Modèle utilisé pour la transcription
        String model = "whisper-1";
        // Type de média pour les fichiers MP3
        MediaType MEDIA_TYPE_MP3 = MediaType.parse("audio/mpeg");
        // Dossier contenant les fichiers MP3 à transcrire
        String mp3FolderPath = "/Users/me/audio/segments";
        // Format souhaité pour la réponse de transcription
        String responseFormat = "text";

        // Configurer le client HTTP avec des délais spécifiés
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

        // Liste pour stocker tous les fichiers mp3 du répertoire
        List<File> mp3Files = new ArrayList<>();

        // Essayer de collecter tous les fichiers mp3 dans le répertoire et les stocker dans la liste
        try (Stream<Path> paths = Files.walk(Paths.get(mp3FolderPath))) {
            mp3Files = paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".mp3"))
                .map(Path::toFile)
                .sorted(Comparator.comparing(File::getName)) // Trier les fichiers par ordre alphabétique
                .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println("Erreur de lecture de fichier : " + e.getMessage());
            return; // Sortir s'il y a une erreur de lecture des fichiers
        }

        // Itérer sur chaque fichier MP3, le transcrire, et imprimer la réponse
        for (File mp3File : mp3Files) {
            // Construire le corps de la requête pour la transcription
            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    mp3File.getName(),
                    RequestBody.create(mp3File, MEDIA_TYPE_MP3))
                .addFormDataPart("model", model)
                .addFormDataPart("response_format", responseFormat)
                .build();

            // Construire la requête HTTP
            Request request = new Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer " + openAIKey)
                .post(requestBody)
                .build();

            // Faire la requête et traiter la réponse
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Code inattendu " + response);
                System.out.println(response.body().string());
            } catch (IOException e) {
                System.out.println("Erreur de requête pour le fichier : " + mp3File.getName() + " - " + e.getMessage());
            }
        }
    }
}
