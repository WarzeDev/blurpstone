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
                String content = message.signedContent(); // correct method

                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
                String avatarUrl = SKIN_SERVICE_BASE
                        + "?name=" + encodedName
                        + "&type=" + SKIN_TYPE;

                webhookManager.sendMinecraftMessage(name, content, avatarUrl);
            }
        });
    }
}