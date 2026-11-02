package com.warzedev.blurpstone.discord;

import com.warzedev.blurpstone.config.BlurpstoneConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.EnumSet;

public class DiscordBot extends ListenerAdapter {
    private final BlurpstoneConfig config;
    private final MinecraftServer server;
    private JDA jda;
    private TextChannel channel;

    public DiscordBot(BlurpstoneConfig config, MinecraftServer server) {
        this.config = config;
        this.server = server;
    }

    public void start() {
        if (config.botToken == null || config.botToken.isBlank()) {
            System.err.println("[Blurpstone] Bot token is missing - Discord integration disabled.");
            return;
        }
        try {
            jda = JDABuilder.createLight(config.botToken, EnumSet.of(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT))
                    .addEventListeners(this)
                    .build();
            jda.awaitReady();

            channel = jda.getTextChannelById(config.channelId);
            if (channel == null) {
                System.err.println("[Blurpstone] Channel ID not found: " + config.channelId);
            } else {
                System.out.println("[Blurpstone] Connected to Discord channel: #" + channel.getName());
            }
        } catch (Exception e) {
            System.err.println("[Blurpstone] Failed to start Discord bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdownNow();
            jda = null;
            System.out.println("[Blurpstone] Discord bot stopped.");
        }
    }

    public boolean isReady() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public JDA getJda() {
        return jda;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(config.channelId)) return;

        String author = event.getAuthor().getName();
        String content = event.getMessage().getContentDisplay();
        String formatted = String.format("[Discord] %s: %s", author, content);

        server.execute(() ->
                server.getPlayerList().broadcastSystemMessage( // correct method
                        Component.literal(formatted), false
                )
        );
    }
}