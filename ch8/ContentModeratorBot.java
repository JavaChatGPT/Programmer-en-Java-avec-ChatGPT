import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// Cette classe étend ListenerAdapter pour gérer les événements de messages sur Discord.
public class ContentModeratorBot extends ListenerAdapter {

    // Le token Discord du bot pour l'authentification.
    static String discordToken = "";
    
    // le message système
    // Ceci est une notation de chaîne multilignes Java 13+. À la fin de la journée, c'est toujours une chaîne de caractères
    static String systemMessage = """
        Vous êtes l'assistant modérateur automatisé pour un serveur Discord.
        Passez en revue chaque message pour les violations de règles suivantes :
        1. Informations sensibles
        2. Abus
        3. Commentaires inappropriés
        4. Spam, par exemple ; un message en majuscules, la même phrase ou mot répété encore et encore, plus de 3 points d'exclamation ou points d'interrogation.
        5. Publicité
        6. Liens externes
        7. Messages ou débats politiques
        8. Messages ou débats religieux
        
        Si l'une de ces violations est détectée, répondez par "FLAG" (en majuscules sans guillemets). Si le message respecte les règles, répondez par "SAFE" (en majuscules sans guillemets).
        """;

    static String instructionsToChatGPT = "Analysez le message suivant pour les violations de règles :";
    
    // ceci est notre client Chat Endpoint
    static ChatGPTClientForQAandModeration chatGPTClient = null;
    // ceci est notre client Moderations Endpoint
    static ModerationClient moderationClient = null;

    public static void main(String[] args) throws IOException {
    
        // Ensemble d'intentions déclarant quels types d'événements le bot a l'intention d'écouter.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,   // pour accéder aux membres du serveur Discord
                GatewayIntent.GUILD_MODERATION, // pour bannir et débannir des membres
                GatewayIntent.GUILD_MESSAGES, // Pour les messages dans les guildes
                GatewayIntent.MESSAGE_CONTENT // Pour permettre l'accès au contenu des messages
        );

        // créer un nouveau ChatGPTClientForQAandModeration
        chatGPTClient = new ChatGPTClientForQAandModeration(systemMessage, instructionsToChatGPT);

        // créer un nouveau ModerationClient
        moderationClient = new ModerationClient();

        // Initialiser le bot avec une configuration minimale et les intentions spécifiées.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new ContentModeratorBot()) // Ajouter la classe actuelle en tant qu'écouteur d'événements.
                    .setActivity(Activity.customStatus("Aider à maintenir un serveur Discord amical")) // Définir le statut personnalisé du bot.
                    .build();

            // Obtenir de manière asynchrone le ping REST de l'API Discord et l'afficher.
            jda.getRestPing().queue(ping -> System.out.println("Connecté avec un ping de : " + ping));

            // Bloquer le thread principal jusqu'à ce que JDA soit entièrement chargé.
            jda.awaitReady();

            // Afficher le nombre de guildes auxquelles le bot est connecté.
            System.out.println("Guildes : " + jda.getGuildCache().size());
            // Afficher l'ID utilisateur du bot Discord
            System.out.println("ID du bot : " + jda.getSelfUser());
        } catch (InterruptedException e) {
            // Gérer les exceptions si le thread est interrompu pendant le processus awaitReady.
            e.printStackTrace();
        }
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent){

        String chatGPTResponse = "";
        ModerationClient.ModerationResponse moderationResponse = null;
        User senderDiscordID = messageEvent.getAuthor();

        // Le canal Discord où le message a été posté
        MessageChannelUnion channel = messageEvent.getChannel();
        net.dv8tion.jda.api.entities.Message message = messageEvent.getMessage();

        // Ignorer les messages envoyés par le bot pour éviter les auto-réponses.
        if (senderDiscordID.equals(messageEvent.getJDA().getSelfUser())) {
            return;
        } 

        // cette ligne prend le message de l'utilisateur Discord et invoque le Moderation Endpoint
        moderationResponse = moderationClient.checkForObjectionalContent(message.getContentDisplay());

        // cette ligne prend le message de l'utilisateur Discord et invoque le Chat Endpoint
        chatGPTResponse = chatGPTClient.sendMessageFromDiscordUser(message.getContentDisplay());

        // Vérifier si le message a été envoyé dans une guilde / un serveur
        if (messageEvent.isFromGuild()){

            // Vérifiez à la fois le Chat Endpoint et le Moderation Endpoint pour voir si le message est signalé

            if (chatGPTResponse.equals("FLAG") || moderationResponse.isFlagged ){

                // Supprimer le message
                message.delete().queue();

                // Mentionner l'utilisateur qui a envoyé le message inapproprié
                String authorMention = senderDiscordID.getAsMention();

                // Envoyer un message mentionnant l'utilisateur et expliquant pourquoi il était inapproprié
                channel.sendMessage(authorMention + " Ce commentaire a été jugé inapproprié pour ce canal. " +
                        "Si vous pensez qu'il s'agit d'une erreur, veuillez contacter l'un des modérateurs humains du serveur.").queue();
            }

        }

    }


}
