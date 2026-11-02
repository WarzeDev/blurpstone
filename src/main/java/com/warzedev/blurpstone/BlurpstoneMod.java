package com.warzedev.blurpstone;

import com.warzedev.blurpstone.config.BlurpstoneConfig;
import com.warzedev.blurpstone.discord.DiscordBot;
import com.warzedev.blurpstone.discord.WebhookManager;
import com.warzedev.blurpstone.listener.ChatListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class BlurpstoneMod implements ModInitializer {
    private DiscordBot discordBot;
    private WebhookManager webhookManager;

    @Override
    public void onInitialize() {
        System.out.println("[Blurpstone] Initializing…");

        // Load config once (server‑side only)
        BlurpstoneConfig config = BlurpstoneConfig.load();

        // Register server started event to start the bot
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            discordBot = new DiscordBot(config, server);
            discordBot.start();

            // Give the bot a moment to connect before creating the webhook
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // simple wait for JDA to be ready
                    if (discordBot.isReady()) {
                        webhookManager = new WebhookManager(config, discordBot.getJda());
                        webhookManager.init();

                        // Register chat listener after webhook is ready
                        new ChatListener(webhookManager).register();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        // Register server stopping event to shut down the bot cleanly
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (webhookManager != null) {
                webhookManager.shutdown();
            }
            if (discordBot != null) {
                discordBot.stop();
            }
        });
    }
}