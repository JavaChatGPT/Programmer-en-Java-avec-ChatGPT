import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// Cette classe étend ListenerAdapter pour gérer les événements de messages sur Discord.
public class TechSupportBot extends ListenerAdapter {

    // Le token Discord du bot pour l'authentification.
    static String discordToken = "";
    // Le nom du canal que le bot doit surveiller et avec lequel interagir.
    static String channelToWatch = "q-et-r";
    // Variable pour stocker le contenu de la FAQ
    static String contentsFromFAQ = "";
    static String pathToFAQFile = "/Users/Desktop/questions.txt";
    // le message système
    static String systemMessage = "Tu es un assistant virtuel qui assure l'assistance de l'appli bancaire de la Crooks Bank";
 
    // notre client ChatGPT
    static ChatGPTClientForQAandModeration chatGPTClient = null;

    public static void main(String[] args) throws IOException {

        // Ensemble d'intentions déclarant quels types d'événements le bot a l'intention d'écouter.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES, // Pour les messages dans les guildes.
                GatewayIntent.DIRECT_MESSAGES, // Pour les messages directs privés.
                GatewayIntent.MESSAGE_CONTENT // Pour permettre l'accès au contenu des messages.
        );

        
        // Lire le contenu d'un fichier texte externe dans la variable FAQContents
        contentsFromFAQ = readFileContents(pathToFAQFile);

        // créer un nouveau ChatGPTClientForQAandModeration
        chatGPTClient = new ChatGPTClientForQAandModeration(contentsFromFAQ, systemMessage);

        // Initialiser le bot avec une configuration minimale et les intentions spécifiées.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new TechSupportBot()) // Ajouter la classe actuelle en tant qu'écouteur d'événements.
                    .setActivity(Activity.customStatus("Prêt à répondre aux questions")) // Définir le statut personnalisé du bot.
                    .build();

            // Obtenir de manière asynchrone le ping REST de l'API Discord et l'afficher.
            jda.getRestPing().queue(ping -> System.out.println("Connecté avec ping: " + ping));

            // Bloquer le thread principal jusqu'à ce que JDA soit entièrement chargé.
            jda.awaitReady();

            // Afficher le nombre de guildes auxquelles le bot est connecté.
            System.out.println("Guildes : " + jda.getGuildCache().size());
            System.out.println("Utilisateur du bot : " + jda.getSelfUser());
        } catch (InterruptedException e) {
            // Gérer les exceptions si le thread est interrompu pendant le processus awaitReady.
            e.printStackTrace();
        }
    }

    // Cette méthode gère les messages entrants.
    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent) {

        // L'ID de l'expéditeur
        User senderDiscordID = messageEvent.getAuthor();
        // Le canal Discord où le message a été posté
        MessageChannelUnion channel = messageEvent.getChannel();
        net.dv8tion.jda.api.entities.Message message = messageEvent.getMessage();
        String reply = null;

        // Ignorer les messages envoyés par le bot pour éviter les auto-réponses.
        if (senderDiscordID.equals(messageEvent.getJDA().getSelfUser())) {
            return;
        } else if (messageEvent.getChannelType() == ChannelType.TEXT) {
            // Ignorer les messages qui ne sont pas dans le canal spécifié "q-et-r".
            if (!channel.getName().equalsIgnoreCase(channelToWatch)) {
                return;
            }
        }

        // Afficher le statut "en train de taper" pendant que le bot travaille
        channel.sendTyping().queue(); 

        // cette ligne prend la question des utilisateurs Discord et la pose à ChatGPT
        reply = chatGPTClient.sendMessageFromDiscordUser(message.getContentDisplay());
        channel.sendMessage(reply).queue();
    }

    // Nouvelle méthode pour lire le contenu du fichier
    private static String readFileContents(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Échec de la lecture du contenu de la FAQ.";
        }
    }
}
