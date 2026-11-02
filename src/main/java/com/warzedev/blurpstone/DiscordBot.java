package com.warzedev.blurpstone.discord;

import com.warzedev.blurpstone.config.BlurpStoneConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.server.MinecraftServer;

import java.util.EnumSet;

public class DiscordBot extends ListenerAdapter {
    private final BlurpStoneConfig config;
    private final MinecraftServer server;
    private JDA jda;
    private TextChannel channel;

    public DiscordBot(BlurpStoneConfig config, MinecraftServer server) {
        this.config = config;
        this.server = server;
    }

    public void start() {
        if (config.botToken == null || config.botToken.isBlank()) {
            System.err.println("[BlurpStone] Bot token is missing - Discord integration disabled.");
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
                System.err.println("[BlurpStone] Channel ID not found: " + config.channelId);
            } else {
                System.out.println("[BlurpStone] Connected to Discord channel: #" + channel.getName());
            }
        } catch (Exception e) {
            System.err.println("[BlurpStone] Failed to start Discord bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdownNow();
            jda = null;
            System.out.println("[BlurpStone] Discord bot stopped.");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore messages from bots (including our own webhook messages)
        if (event.getAuthor().isBot()) return;
        // Only relay messages from the configured channel
        if (!event.getChannel().getId().equals(config.channelId)) return;

        String author = event.getAuthor().getName();
        String content = event.getMessage().getContentDisplay();
        String formatted = String.format("[Discord] %s: %s", author, content);

        // Broadcast to all online players
        server.execute(() ->
                server.getPlayerManager().broadcast(
                        net.minecraft.text.Text.literal(formatted), false
                )
        );
    }
}