package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.util.BroadcastManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InspectCommandUtil {
    public static final int MIN_SLOT = 1;
    public static final int MAX_SLOT = 9;
    public static final MiniMessage mm = MiniMessage.miniMessage();
    public static final Component ERROR_PREFIX = mm.deserialize("<red>Error:</red> ");

    private InspectCommandUtil() {
        // Utility class
    }

    /**
     * Attempts to resolve a player identifier (name or UUID) to a UUID asynchronously.
     * This method first tries to parse as UUID, then checks online players synchronously,
     * and finally searches offline players asynchronously.
     *
     * @param identifier Player name or UUID string
     * @return CompletableFuture containing UUID if found, null otherwise
     */
    public static CompletableFuture<UUID> resolvePlayerIdentifierAsync(String identifier) {
        // First try to parse as UUID
        try {
            UUID uuid = UUID.fromString(identifier);
            return CompletableFuture.completedFuture(uuid);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, continue
        }

        // Try to find online player (this is fast and safe to do synchronously)
        Player onlinePlayer = Bukkit.getPlayerExact(identifier);
        if (onlinePlayer != null) {
            return CompletableFuture.completedFuture(onlinePlayer.getUniqueId());
        }

        // Search offline players asynchronously (this can be slow)
        return CompletableFuture.supplyAsync(() -> {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (identifier.equalsIgnoreCase(offlinePlayer.getName())) {
                    return offlinePlayer.getUniqueId();
                }
            }
            return null;
        });
    }

    /**
     * Gets a player's name from their UUID, falling back to UUID string if name is not available.
     *
     * @param uuid Player UUID
     * @return Player name or UUID string
     */
    public static String getPlayerName(@NotNull UUID uuid) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        return name != null ? name : uuid.toString();
    }

    /**
     * Shows command usage message to the player.
     *
     * @param player Player to send message to
     * @param commandName Name of the command (e.g., "inspectkit" or "inspectec")
     */
    public static void showUsage(@NotNull Player player, @NotNull String commandName) {
        BroadcastManager.get().sendComponentMessage(player,
                ERROR_PREFIX.append(
                        mm.deserialize("<red>Usage: /" + commandName + " <player|uuid> <slot></red>")));
    }
}

