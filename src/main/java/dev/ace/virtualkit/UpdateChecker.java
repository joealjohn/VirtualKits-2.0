package dev.ace.virtualkit;

import org.bukkit.plugin.Plugin;

/**
 * Checks for updates from a remote version endpoint.
 * Update checking is disabled for VirtualKit.
 */
public class UpdateChecker {
    private final Plugin plugin;
    private boolean updateAvailable = false;
    private String latestVersion = "";

    public UpdateChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    public void printStartupStatus() {
        plugin.getLogger().info("VirtualKit v" + plugin.getDescription().getVersion() + " enabled!");
        plugin.getLogger().info("Created by ACE");
    }

    public boolean isUpdateAvailable() {
        return false;
    }

    public void notifyPlayer(org.bukkit.entity.Player player) {
        // Update notifications disabled
    }
}

