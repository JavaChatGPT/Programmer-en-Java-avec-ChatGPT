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


public class TechSupportBotDumb extends ListenerAdapter {

    static String discordToken = ““;
    static String channelToWatch = "q-et-r";

    public static void main(String[] args) throws IOException {
    
        EnumSet<GatewayIntent> intents = EnumSet.of(
                // Active MessageReceivedEvent pour les guildes (également connues sous le nom de serveurs)
                GatewayIntent.GUILD_MESSAGES,
                // Active l'événement pour les canaux privés (également connus sous le nom de messages directs)
                GatewayIntent.DIRECT_MESSAGES,
                // Permet l'accès à message.getContentRaw()
                GatewayIntent.MESSAGE_CONTENT);


        // Pour démarrer le bot, vous devez utiliser le JDABuilder.

        try {
            // En utilisant createLight(token, intents), nous utilisons un profil de cache minimaliste
            // (utilisation de RAM réduite)
            // et activons uniquement l'ensemble d'intentions fourni. Toutes les autres intentions sont désactivées,
            // vous ne recevrez donc pas d'événements pour celles-ci.
            JDA jda = JDABuilder.createLight(discordToken, intents)
                    // Sur ce builder, vous ajoutez tous vos écouteurs d'événements et votre configuration de session
                    .addEventListeners(new TechSupportBotDumb())
                    // Vous pouvez faire beaucoup de configuration avant de commencer, consultez tous les setters de la classe JDABuilder
                    .setActivity(Activity.customStatus("Prêt à répondre aux questions"))
                    // Une fois que vous avez terminé de configurer votre instance jda, appelez build pour démarrer et vous connecter au bot.
                    .build();


            jda.getRestPing().queue(ping ->
                
                // affiche le ping en millisecondes
                System.out.println("Connecté avec un ping de : " + ping)
            );

            // Si vous souhaitez accéder au cache, vous pouvez utiliser awaitReady() pour bloquer le thread principal
            // jusqu'à ce que l'instance jda soit entièrement chargée
            jda.awaitReady();

            // Maintenant, nous pouvons accéder au cache entièrement chargé et afficher des statistiques ou faire d'autres choses dépendant du cache
            System.out.println("Guildes : " + jda.getGuildCache().size());
        } catch (InterruptedException e) {
            // Lancé si l'appel awaitReady() est interrompu
            e.printStackTrace();
        }
    }

    
    @Override
    public void onMessageReceived(MessageReceivedEvent messageEvent) {

        // L'ID de l'expéditeur
        User senderDiscordID = messageEvent.getAuthor();
        // Le canal Discord où le message a été posté
        MessageChannelUnion channel = messageEvent.getChannel();
 
        // Ignorer les messages envoyés par le bot lui-même pour éviter une boucle infinie
        if (senderDiscordID.equals(messageEvent.getJDA().getSelfUser())) {
            return;
        } else 
        
        if (messageEvent.getChannelType() == ChannelType.TEXT) {
            // Ignorer tout message provenant d'un canal qui n'est PAS le canal "q-et-r"
            if (!channel.getName().equalsIgnoreCase(channelToWatch)) {
                return;
            }
        }


        // Si nous arrivons à ce point, nous devons répondre à l'expéditeur
        String reply = "Bonjour <@" + senderDiscordID.getId() + ">,  Je peux vous aider!";
        channel.sendMessage(reply).queue();
    }


}
