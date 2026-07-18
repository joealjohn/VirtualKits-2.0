/*
 * Copyright 2022-2025 ACE
 *
 * This file is part of VirtualKit.
 */
package dev.ace.virtualkit.util;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

/**
 * Manages all plugin messages from messages.yml
 */
public class MessageManager {
    private static MessageManager instance;
    private final Plugin plugin;
    private FileConfiguration messagesConfig;
    private BukkitAudiences audience;

    public MessageManager(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
        audience = BukkitAudiences.create(plugin);
        loadMessages();
    }

    public static MessageManager get() {
        return instance;
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Get the raw messages.yml configuration
     */
    public FileConfiguration getConfig() {
        return messagesConfig;
    }

    /**
     * Sends a message to a player with the configured prefix
     */
    public void sendMessage(Player player, String message) {
        Component prefixComponent = StyleManager.parseComponent(getPrefix());
        Component messageComponent = StyleManager.parseComponent(message);
        audience.player(player).sendMessage(prefixComponent.append(messageComponent));
    }

    /**
     * Sends a message to a player with the configured prefix, replacing %slot%
     */
    public void sendMessage(Player player, String message, int slot) {
        String formattedMessage = message.replace("%slot%", String.valueOf(slot));
        sendMessage(player, formattedMessage);
    }

    /**
     * Sends a raw message (without prefix) to a player
     */
    public void sendRawMessage(Player player, String message) {
        Component messageComponent = StyleManager.parseComponent(message);
        audience.player(player).sendMessage(messageComponent);
    }

    public String getPrefix() {
        return messagesConfig.getString("prefix", "<bold><#00FF00>KITS</bold> <dark_gray>></dark_gray> ");
    }

    public String get(String path, String defaultValue) {
        return messagesConfig.getString(path, defaultValue);
    }

    public String get(String path) {
        return messagesConfig.getString(path, "");
    }

    public List<String> getList(String path) {
        return messagesConfig.getStringList(path);
    }

    // Success messages
    public String getKitLoaded() {
        return get("messages.kit-loaded", "<#7cff6e>Kit loaded successfully!");
    }

    public String getKitLoaded(int slot) {
        return get("messages.kit-loaded", "<#7cff6e>Kit %slot% loaded successfully!").replace("%slot%",
                String.valueOf(slot));
    }

    public String getEcLoaded(int slot) {
        return get("messages.ec-loaded", "<#7cff6e>Enderchest %slot% loaded successfully!").replace("%slot%",
                String.valueOf(slot));
    }

    public String getPublicKitLoaded() {
        return get("messages.public-kit-loaded", "<#7cff6e>Premade kit loaded successfully!");
    }

    public String getPremadeFallback() {
        return get("messages.premade-fallback", "<gray>Your kit is empty. Loaded the <#7cff6e>Premade Kit</#7cff6e> instead!");
    }

    public String getKitSaved() {
        return get("messages.kit-saved", "<#7cff6e>Kit saved successfully!");
    }

    public String getKitSaved(int slot) {
        return get("messages.kit-saved", "<#7cff6e>Kit %slot% saved successfully!").replace("%slot%",
                String.valueOf(slot));
    }

    public String getEcSaved(int slot) {
        return get("messages.ec-saved", "<#7cff6e>Enderchest %slot% saved successfully!").replace("%slot%",
                String.valueOf(slot));
    }

    public String getKitDeleted(int slot) {
        return get("messages.kit-deleted", "<#7cff6e>Kit %slot% deleted!").replace("%slot%", String.valueOf(slot));
    }

    public String getEcDeleted(int slot) {
        return get("messages.ec-deleted", "<#7cff6e>Enderchest %slot% deleted!").replace("%slot%",
                String.valueOf(slot));
    }

    public String getInventoryCleared() {
        return get("messages.inventory-cleared", "<#7cff6e>Inventory cleared!");
    }

    public String getItemsRepaired() {
        return get("messages.items-repaired", "<#7cff6e>All items repaired!");
    }

    public String getHealed() {
        return get("messages.healed", "<#7cff6e>You have been healed!");
    }

    public String getRegeared() {
        return get("messages.regeared", "<#7cff6e>Regeared!");
    }

    public String getKitroomSaved(int page) {
        return get("messages.kitroom-saved", "<#7cff6e>Saved kitroom page %page%").replace("%page%",
                String.valueOf(page));
    }

    // Error messages
    public String getKitNotFound() {
        return get("messages.kit-not-found", "<#ffa6a6>Kit does not exist!");
    }

    public String getKitNotFound(int slot) {
        return get("messages.kit-not-found", "<#ffa6a6>Kit %slot% does not exist!").replace("%slot%",
                String.valueOf(slot));
    }

    public String getPlayerNotFound(String player) {
        return get("messages.player-not-found", "<#ffa6a6>Could not find player %player%").replace("%player%", player);
    }

    public String getNoPermission() {
        return get("messages.no-permission", "<#ffa6a6>You do not have permission to use this command.");
    }

    public String getCommandDisabled() {
        return get("messages.command-disabled", "<#ffa6a6>Kits are disabled in this world!");
    }

    public String getUnexpectedError() {
        return get("messages.unexpected-error", "<#ffa6a6>An unexpected error occurred!");
    }

    // Share messages
    public String getShareCodeCreated(String code) {
        return get("share.code-created", "<#00FF00>Use /copykit %code% to copy this kit").replace("%code%", code);
    }

    public String getShareCodeExpires() {
        return get("share.code-expires", "<gray>Code expires in 15 minutes");
    }

    public String getShareEcCodeCreated(String code) {
        return get("share.ec-code-created", "<#FF69B4>Use /copyec %code% to copy this enderchest").replace("%code%",
                code);
    }

    // MOTD
    public boolean isMotdEnabled() {
        return messagesConfig.getBoolean("motd.enabled", true);
    }

    public int getMotdDelay() {
        return messagesConfig.getInt("motd.delay", 3);
    }

    public List<String> getMotdMessages() {
        return getList("motd.message");
    }

    // Scheduled broadcast
    public boolean isScheduledBroadcastEnabled() {
        return messagesConfig.getBoolean("scheduled-broadcast.enabled", false);
    }

    public int getScheduledBroadcastPeriod() {
        return messagesConfig.getInt("scheduled-broadcast.period", 90);
    }

    public List<String> getScheduledBroadcastMessages() {
        return getList("scheduled-broadcast.messages");
    }

    // Broadcast messages
    public String getBroadcastPermission() {
        return get("broadcast.permission", "virtualkit.kitnotify");
    }

    public boolean isBroadcastEnabled(String type) {
        return messagesConfig.getBoolean("broadcast." + type + ".enabled", true);
    }

    public String getBroadcastMessage(String type, String player, int slot) {
        return get("broadcast." + type + ".message", "")
                .replace("%player%", player)
                .replace("%slot%", String.valueOf(slot));
    }

    public String getBroadcastMessage(String type, String player) {
        return get("broadcast." + type + ".message", "").replace("%player%", player);
    }
}
