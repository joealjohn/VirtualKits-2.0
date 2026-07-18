package dev.ace.virtualkit.listeners;

import dev.ace.virtualkit.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Automatically loads a configured kit slot when a player enters a world.
 * Also handles RTP/RTPQueue commands within the same world.
 *
 * <p>Config path: {@code auto-rekit-on-world-join}
 * <ul>
 *   <li>{@code enabled} – master toggle (default: false)</li>
 *   <li>{@code only-if-empty} – only apply kit when the player's inventory is empty</li>
 *   <li>{@code delay-ticks} – ticks to wait before applying (handles spawn-teleport lag)</li>
 *   <li>{@code worlds.<worldName>} – kit slot (1-9) to load; 0 = skip this world</li>
 * </ul>
 *
 * <p>Permission: {@code virtualkit.autorekit} to receive auto-rekit<br>
 * Bypass: {@code virtualkit.autorekit.bypass} to skip auto-rekit
 */
public class WorldJoinRekitListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> rtpPlayers = new HashMap<>();

    public WorldJoinRekitListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        String newWorld = event.getPlayer().getWorld().getName();
        applyAutoRekit(event.getPlayer(), newWorld);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();
        if (msg.startsWith("/rtp") || msg.startsWith("/wild") || msg.startsWith("/randomtp")) {
            rtpPlayers.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        Long commandTime = rtpPlayers.remove(uuid);
        if (commandTime == null) {
            return;
        }

        // Only handle within-world teleportation (cross-world is handled by PlayerChangedWorldEvent)
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }

        // Must be within 60 seconds of running the command
        if (System.currentTimeMillis() - commandTime < 60000L) {
            applyAutoRekit(player, event.getTo().getWorld().getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        rtpPlayers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Called from {@link JoinListener} after kit data has been loaded from the DB,
     * so the kit is actually available when we apply it on first join.
     */
    public void applyAutoRekitOnJoin(Player player) {
        applyAutoRekit(player, player.getWorld().getName());
    }

    private void applyAutoRekit(Player player, String worldName) {
        if (!plugin.getConfig().getBoolean("auto-rekit-on-world-join.enabled", false)) {
            return;
        }

        // Respect the global disabled-command-worlds blacklist
        if (plugin.getConfig().getStringList("disabled-command-worlds").contains(worldName)) {
            return;
        }

        // Permission check – player must have the receive permission
        if (!player.hasPermission("virtualkit.autorekit")) {
            return;
        }

        // Bypass permission – skip auto-rekit
        if (player.hasPermission("virtualkit.autorekit.bypass")) {
            return;
        }

        // Look up configured slot for this world
        int slot = plugin.getConfig().getInt("auto-rekit-on-world-join.worlds." + worldName, -1);
        if (slot <= 0) {
            // 0 or -1 means disabled for this world
            return;
        }

        long delayTicks = plugin.getConfig().getLong("auto-rekit-on-world-join.delay-ticks", 5L);
        boolean onlyIfEmpty = plugin.getConfig().getBoolean("auto-rekit-on-world-join.only-if-empty", false);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Safety: make sure player is still online and still in that world
            if (!player.isOnline()) return;
            if (!player.getWorld().getName().equals(worldName)) return;

            if (onlyIfEmpty && !isInventoryEmpty(player)) {
                return;
            }

            KitManager.get().loadKitSilent(player, slot);

        }, delayTicks);
    }

    /** Returns true only if every slot in the player's inventory (including armour) is empty. */
    private boolean isInventoryEmpty(Player player) {
        for (var item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) return false;
        }
        return true;
    }
}
