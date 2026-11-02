package com.warzedev.blurpstone.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.warzedev.blurpstone.config.BlurpstoneConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class WebhookManager {
    private final BlurpstoneConfig config;
    private final JDA jda;
    private WebhookClient webhookClient;

    public WebhookManager(BlurpstoneConfig config, JDA jda) {
        this.config = config;
        this.jda = jda;
    }

    public void init() {
        TextChannel channel = jda.getTextChannelById(config.channelId);
        if (channel == null) {
            System.err.println("[Blurpstone] Cannot create webhook - channel not found.");
            return;
        }

        // Look for an existing webhook named "Blurpstone" or create a new one
        Webhook webhook = channel.retrieveWebhooks().complete().stream()
                .filter(w -> w.getName().equals("Blurpstone"))
                .findFirst()
                .orElseGet(() -> channel.createWebhook("Blurpstone").complete());

        String webhookUrl = webhook.getUrl();
        webhookClient = new WebhookClientBuilder(webhookUrl).build();
        System.out.println("[Blurpstone] Webhook ready: " + webhookUrl);
    }

    /**
     * Sends a Minecraft chat message to Discord via webhook.
     *
     * @param playerName The Minecraft player's name (used as webhook username)
     * @param message    The chat message content
     * @param avatarUrl  URL to the player's head (e.g., Crafatar)
     */
    public void sendMinecraftMessage(String playerName, String message, String avatarUrl) {
        if (webhookClient == null) return;

        WebhookMessageBuilder builder = new WebhookMessageBuilder()
                .setUsername(playerName)
                .setAvatarUrl(avatarUrl)
                .setContent(message);

        webhookClient.send(builder.build());
    }

    public void shutdown() {
        if (webhookClient != null) {
            webhookClient.close();
            webhookClient = null;
        }
    }
}