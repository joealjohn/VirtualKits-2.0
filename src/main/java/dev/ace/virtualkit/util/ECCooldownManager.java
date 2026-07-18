package dev.ace.virtualkit.util;

import dev.ace.virtualkit.VirtualKits;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages cooldowns for /ec1, /ec2, etc. commands
 * Cooldown resets on kill or after time expires
 */
public class ECCooldownManager implements Listener {

    private static ECCooldownManager instance;
    private final Plugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ECCooldownManager(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static ECCooldownManager get() {
        return instance;
    }

    /**
     * Check if EC cooldown is enabled in config
     */
    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("ec-cooldown.enabled", true);
    }

    /**
     * Get cooldown time in seconds from config
     */
    public int getCooldownSeconds() {
        return plugin.getConfig().getInt("ec-cooldown.time-seconds", 60);
    }

    /**
     * Check if cooldown should reset on kill
     */
    public boolean isResetOnKill() {
        return plugin.getConfig().getBoolean("ec-cooldown.reset-on-kill", true);
    }

    /**
     * Get cooldown message from config
     */
    public String getCooldownMessage() {
        return plugin.getConfig().getString("ec-cooldown.cooldown-message",
                "<#FF4444>ᴇᴄ ᴄᴏᴏʟᴅᴏᴡɴ: <white>%time%s <#FF4444>ᴏʀ ɢᴇᴛ ᴀ ᴋɪʟʟ");
    }

    /**
     * Check if player is on cooldown
     * 
     * @return remaining seconds, or 0 if not on cooldown
     */
    public int getRemainingCooldown(UUID playerId) {
        if (!isEnabled())
            return 0;

        Long cooldownEnd = cooldowns.get(playerId);
        if (cooldownEnd == null)
            return 0;

        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) {
            cooldowns.remove(playerId);
            return 0;
        }

        return (int) remaining;
    }

    /**
     * Check if player can use EC command
     */
    public boolean canUseEC(Player player) {
        if (!isEnabled())
            return true;
        return getRemainingCooldown(player.getUniqueId()) == 0;
    }

    /**
     * Start cooldown for player
     */
    public void startCooldown(UUID playerId) {
        if (!isEnabled())
            return;
        int seconds = getCooldownSeconds();
        if (seconds > 0) {
            cooldowns.put(playerId, System.currentTimeMillis() + (seconds * 1000L));
        }
    }

    /**
     * Reset cooldown for player (e.g., after kill)
     */
    public void resetCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }

    /**
     * Send cooldown message to player
     */
    public void sendCooldownMessage(Player player) {
        int remaining = getRemainingCooldown(player.getUniqueId());
        String message = getCooldownMessage().replace("%time%", String.valueOf(remaining));
        BroadcastManager.get().sendComponentMessage(player, StyleManager.parseComponent(message));
    }

    /**
     * Handle player kill - reset cooldown
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!isEnabled() || !isResetOnKill())
            return;

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            resetCooldown(killer.getUniqueId());
        }
    }
}
