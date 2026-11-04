package com.warzedev.blurpstone.discord;

import com.warzedev.blurpstone.config.BlurpstoneConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;

import java.util.Comparator;
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

        int roleColor = resolveTopRoleColor(event.getMember());
        Component formatted = buildInGameMessage(author, content, roleColor);

        server.execute(() ->
                server.getPlayerList().broadcastSystemMessage(formatted, false)
        );
    }

    /**
     * Returns the RGB of the member's highest-positioned colored role,
     * or -1 if the member has no colored roles.
     */
    private static int resolveTopRoleColor(Member member) {
        if (member == null) return -1;
        return member.getRoles().stream()
                .filter(role -> role.getColor() != null)
                .max(Comparator.comparingInt(Role::getPosition))
                .map(role -> role.getColor().getRGB() & 0xFFFFFF)
                .orElse(-1);
    }

    private Component buildInGameMessage(String author, String content, int roleColor) {
        int bracketRgb = parseHex(config.bracketsColor, 0x5865F2);
        int discordRgb = parseHex(config.discordTextColor, 0xFFFFFF);
        int contentRgb = parseHex(config.contentColor, 0xFFFFFF);

        // Author name uses role color if present, otherwise falls back to content color.
        int authorRgb = roleColor >= 0 ? roleColor : contentRgb;

        return Component.empty()
                .append(styled("[", bracketRgb))
                .append(styled("Discord", discordRgb))
                .append(styled("] ", bracketRgb))
                .append(styled(author, authorRgb))
                .append(styled(": ", contentRgb))
                .append(styled(content, contentRgb));
    }

    private static Component styled(String text, int rgb) {
        return Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
    }

    private static int parseHex(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        String cleaned = value.trim();
        if (cleaned.startsWith("#")) cleaned = cleaned.substring(1);
        try {
            return Integer.parseInt(cleaned, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}