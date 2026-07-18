package dev.ace.virtualkit.listeners;

import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.UpdateChecker;
import dev.ace.virtualkit.util.BroadcastManager;
import dev.ace.virtualkit.util.MessageManager;
import dev.ace.virtualkit.util.StyleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JoinListener implements Listener {

    private final Plugin plugin;
    private final UpdateChecker updateChecker;
    private final WorldJoinRekitListener worldJoinRekitListener;

    public JoinListener(Plugin plugin, UpdateChecker updateChecker, WorldJoinRekitListener worldJoinRekitListener) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
        this.worldJoinRekitListener = worldJoinRekitListener;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("virtualkit.admin")
                && plugin.getConfig().getBoolean("feature.send-update-message-on-join", true)) {
            updateChecker.notifyPlayer(player);
        }

        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {

            @Override
            public void run() {
                KitManager.get().loadPlayerDataFromDB(uuid);

                // After DB load finishes, trigger auto-rekit on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        worldJoinRekitListener.applyAutoRekitOnJoin(player);
                    }
                });
            }

        }.runTaskAsynchronously(plugin);

        // Check if MOTD is enabled and send MOTD messages
        if (MessageManager.get().isMotdEnabled()) {
            List<Component> motdMessages = new ArrayList<>();
            MessageManager.get().getMotdMessages()
                    .forEach(message -> motdMessages.add(StyleManager.parseComponent(message)));

            // Delay for sending the MOTD
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> motdMessages.forEach(message -> BroadcastManager.get().sendComponentMessage(player, message)),
                    MessageManager.get().getMotdDelay() * 20L);
        }
    }

}
