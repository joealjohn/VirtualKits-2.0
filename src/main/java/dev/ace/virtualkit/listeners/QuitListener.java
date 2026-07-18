package dev.ace.virtualkit.listeners;

import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.gui.GUI;
import dev.ace.virtualkit.util.ECCooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class QuitListener implements Listener {

    private final Plugin plugin;

    public QuitListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // Save kit data to DB asynchronously, then evict from memory to prevent leaks
        new BukkitRunnable() {
            @Override
            public void run() {
                KitManager.get().savePlayerKitsToDB(uuid);
                // Evict player data from in-memory map after saving
                KitManager.get().evictPlayerData(uuid);
            }
        }.runTaskAsynchronously(plugin);

        // Clean up per-player entries in other managers
        KitManager.get().clearLastKitUsed(uuid);
        ECCooldownManager.get().resetCooldown(uuid);
        GUI.removeKitDeletionFlag(player); // clear any lingering inspect-flow flag
    }
}

