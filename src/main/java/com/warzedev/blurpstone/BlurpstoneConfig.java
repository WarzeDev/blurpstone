package com.warzedev.blurpstone.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlurpStoneConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("blurpstone.json");

    // Fields matching the JSON keys
    public String botToken = "YOUR_BOT_TOKEN";
    public String guildId = "YOUR_GUILD_ID";
    public String channelId = "YOUR_CHANNEL_ID";
    public String discordOwnerUserId = "YOUR_DISCORD_USER_ID";

    public static BlurpStoneConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            BlurpStoneConfig defaultConfig = new BlurpStoneConfig();
            defaultConfig.save();
            return defaultConfig;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            return GSON.fromJson(json, BlurpStoneConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load blurpstone.json", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save blurpstone.json", e);
        }
    }
}