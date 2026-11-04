package com.warzedev.blurpstone.listener;

import com.warzedev.blurpstone.discord.WebhookManager;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.level.ServerPlayer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ChatListener {
    private static final String SKIN_SERVICE_BASE = "https://cosm.warze.org/skin";
    private static final String SKIN_TYPE = "headiso";

    private final WebhookManager webhookManager;

    public ChatListener(WebhookManager webhookManager) {
        this.webhookManager = webhookManager;
    }

    public void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (sender instanceof ServerPlayer player) {
                String name = player.getName().getString();
                String content = sanitizeMentions(message.signedContent());

                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
                String avatarUrl = SKIN_SERVICE_BASE
                        + "?name=" + encodedName
                        + "&type=" + SKIN_TYPE;

                webhookManager.sendMinecraftMessage(name, content, avatarUrl);
            }
        });
    }

    /**
     * Webhooks bypass the "Mention Everyone" permission, so any ping in the
     * Minecraft chat would be relayed verbatim. We neutralize @everyone/@here
     * with a zero-width space (keeps the text readable) and strip Discord's
     * user/role/channel mention syntax entirely.
     */
    private static String sanitizeMentions(String content) {
        // Neutralize broadcast pings
        content = content.replace("@everyone", "@\u200Beveryone");
        content = content.replace("@here", "@\u200Bhere");
        // Strip <@id>, <@!id>, <@&id>, <#id>
        content = content.replaceAll("<@!?\\d+>", "");
        content = content.replaceAll("<@&\\d+>", "");
        content = content.replaceAll("<#\\d+>", "");
        return content;
    }
}