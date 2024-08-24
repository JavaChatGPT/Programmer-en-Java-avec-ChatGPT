import java.io.IOException;
import java.util.EnumSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

// Cette classe étend ListenerAdapter pour gérer les événements de messages sur Discord.
public class ContentModeratorBotDumb extends ListenerAdapter {

    // Le token Discord du bot pour l'authentification.
    static String discordToken = "";
    static String bannedWord = "chiots";

    public static void main(String[] args) throws IOException {
    
        // Ensemble d'intentions déclarant quels types d'événements le bot a l'intention d'écouter.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,   // pour accéder aux membres du serveur Discord
                GatewayIntent.GUILD_MODERATION, // pour bannir et débannir des membres
                GatewayIntent.GUILD_MESSAGES, // Pour les messages dans les guildes
                GatewayIntent.MESSAGE_CONTENT // Pour permettre l'accès au contenu des messages
        );

        // Initialiser le bot avec une configuration minimale et les intentions spécifiées.
        try {
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    .addEventListeners(new ContentModeratorBotDumb()) // Ajouter la classe actuelle en tant qu'écouteur d'événements.
                    .setActivity(Activity.customStatus("Aider à maintenir un serveur Discord amical")) // Définir le statut personnalisé du bot.
                    .build();

            // Obtenir de manière asynchrone le ping REST de l'API Discord et l'afficher.
            jda.getRestPing().queue(ping -> System.out.println("Connecté avec ping: " + ping));

            // Bloquer le thread principal jusqu'à ce que JDA soit entièrement chargé.
            jda.awaitReady();

            // Afficher le nombre de guildes auxquelles le bot est connecté.
            System.out.println("Guildes : " + jda.getGuildCache().size());
            // Afficher l'ID utilisateur Discord du bot
            System.out.println("ID du bot : " + jda.getSelfUser());
        } catch (InterruptedException e) {
            // Gérer les exceptions si le thread est interrompu pendant le processus awaitReady.
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent){

        User senderDiscordID = messageEvent.getAuthor();
        MessageChannelUnion channel = messageEvent.getChannel();
        Message message = messageEvent.getMessage();

        // Vérifier si le message a été envoyé dans une guilde / un serveur
        if (messageEvent.isFromGuild()){

            String content = message.getContentDisplay();
            // Vérifier si le message contient le mot interdit
            if (content.contains(bannedWord)){

                // Supprimer le message
                message.delete().queue();

                // Mentionner l'utilisateur qui a envoyé le message inapproprié
                String authorMention = senderDiscordID.getAsMention();

                // Envoyer un message mentionnant l'utilisateur et expliquant pourquoi il était inapproprié
                channel.sendMessage(authorMention + " Ce commentaire a été jugé inapproprié pour ce canal. " +
                        "Si vous pensez qu'il s'agit d'un bogue, veuillez contacter l'un des modérateurs humains du serveur.").queue();
            }

        }

    }

}
