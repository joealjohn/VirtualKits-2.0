package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.util.BroadcastManager;
import dev.ace.virtualkit.util.StyleManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InspectCommandUtil {
    public static final int MIN_SLOT = 1;
    public static final int MAX_SLOT = 9;
    public static final Component ERROR_PREFIX = StyleManager.parseComponent("<red>Error:</red> ");

    private InspectCommandUtil() {
        // Utility class
    }

    /**
     * Attempts to resolve a player identifier (name or UUID) to a UUID.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Parse as UUID literal (direct UUID input)</li>
     *   <li>Check online players (fast, synchronous)</li>
     *   <li>Check Bukkit's offline player cache asynchronously</li>
     *   <li>Generate an offline-mode UUID from the name — this is the same
     *       algorithm Bukkit uses in offline/cracked mode, so cracked players
     *       who have joined before will always resolve correctly.</li>
     * </ol>
     *
     * @param identifier Player name or UUID string
     * @return CompletableFuture containing UUID (never null — falls back to offline UUID)
     */
    public static CompletableFuture<UUID> resolvePlayerIdentifierAsync(String identifier) {
        // 1. Try to parse as UUID literal
        try {
            UUID uuid = UUID.fromString(identifier);
            return CompletableFuture.completedFuture(uuid);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID string, continue
        }

        // 2. Check online players (synchronous, fast)
        Player onlinePlayer = Bukkit.getPlayerExact(identifier);
        if (onlinePlayer != null) {
            return CompletableFuture.completedFuture(onlinePlayer.getUniqueId());
        }

        // 3. Search offline player cache + offline-mode UUID fallback (async)
        return CompletableFuture.supplyAsync(() -> {
            // Iterate the cache (players who have previously logged in)
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (identifier.equalsIgnoreCase(offlinePlayer.getName())) {
                    return offlinePlayer.getUniqueId();
                }
            }

            // 4. Offline/cracked mode fallback: generate the deterministic UUID
            //    that Bukkit assigns to players in offline mode.
            //    Formula: UUID.nameUUIDFromBytes("OfflinePlayer:<name>")
            //    This matches what CraftServer does internally.
            return UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + identifier).getBytes(StandardCharsets.UTF_8));
        });
    }

    /**
     * Gets a player's display name from their UUID.
     *
     * <p>Checks online players first, then Bukkit's offline cache (no network call),
     * and falls back to the UUID string if no name is known.
     *
     * @param uuid Player UUID
     * @return Player name or UUID string
     */
    public static String getPlayerName(@NotNull UUID uuid) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }
        // getOfflinePlayer(uuid) reads from the local usercache.json — no Mojang API call
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        return name != null ? name : uuid.toString();
    }

    /**
     * Shows command usage message to the player.
     *
     * @param player      Player to send message to
     * @param commandName Name of the command (e.g., "inspectkit" or "inspectec")
     */
    public static void showUsage(@NotNull Player player, @NotNull String commandName) {
        BroadcastManager.get().sendComponentMessage(player,
                ERROR_PREFIX.append(
                        StyleManager.parseComponent("<red>Usage: /" + commandName + " <player|uuid> <slot></red>")));
    }
}
