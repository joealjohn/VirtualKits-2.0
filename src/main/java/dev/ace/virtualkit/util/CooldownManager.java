package dev.ace.virtualkit.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {

    private final long cooldownInSeconds;
    private final HashMap<String, Long> cooldownMap;

    public CooldownManager(long cooldownInSeconds) {
        this.cooldownInSeconds = cooldownInSeconds;
        this.cooldownMap = new HashMap<>();
    }

    // ─── Check ──────────────────────────────────────────────────────────────────

    public boolean isOnCooldown(String key) {
        Long stamp = cooldownMap.get(key);
        if (stamp == null) return false;
        if (System.currentTimeMillis() - stamp >= cooldownInSeconds * 1000L) {
            // Expired – evict now so the map never accumulates stale entries
            cooldownMap.remove(key);
            return false;
        }
        return true;
    }

    public boolean isOnCooldown(UUID uuid) {
        return isOnCooldown(uuid.toString());
    }

    public boolean isOnCooldown(Player player) {
        return isOnCooldown(player.getUniqueId());
    }

    // ─── Set ────────────────────────────────────────────────────────────────────

    public void setCooldown(String key) {
        cooldownMap.put(key, System.currentTimeMillis());
    }

    public void setCooldown(UUID uuid) {
        setCooldown(uuid.toString());
    }

    public void setCooldown(Player player) {
        setCooldown(player.getUniqueId());
    }

    // ─── Remove (call on player quit) ───────────────────────────────────────────

    public void remove(String key) {
        cooldownMap.remove(key);
    }

    public void remove(UUID uuid) {
        cooldownMap.remove(uuid.toString());
    }

    public void remove(Player player) {
        remove(player.getUniqueId());
    }

    // ─── Time left ──────────────────────────────────────────────────────────────

    public int getTimeLeft(String key) {
        Long stamp = cooldownMap.get(key);
        if (stamp == null) return 0;
        long remaining = cooldownInSeconds - (System.currentTimeMillis() - stamp) / 1000L;
        return (int) Math.max(0, remaining);
    }

    public int getTimeLeft(UUID uuid) {
        return getTimeLeft(uuid.toString());
    }

    public int getTimeLeft(Player player) {
        return getTimeLeft(player.getUniqueId());
    }
}
