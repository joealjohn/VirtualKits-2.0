package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.gui.GUI;
import dev.ace.virtualkit.util.BroadcastManager;
import dev.ace.virtualkit.util.SoundManager;
import dev.ace.virtualkit.util.StyleManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.ace.virtualkit.commands.InspectCommandUtil.*;

public class InspectEcCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;

    public InspectEcCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ERROR_PREFIX.append(
                    StyleManager.parseComponent("<red>This command can only be executed by players.</red>")).toString());
            return true;
        }

        if (!player.hasPermission("VirtualKits.inspect")) {
            BroadcastManager.get().sendComponentMessage(player,
                    ERROR_PREFIX.append(
                            StyleManager.parseComponent("<red>You don't have permission to use this command.</red>")));
            SoundManager.playFailure(player);
            return true;
        }

        if (args.length < 2) {
            showUsage(player, "inspectec");
            return true;
        }

        // Parse slot number
        int slot;
        try {
            slot = Integer.parseInt(args[1]);
            if (slot < MIN_SLOT || slot > MAX_SLOT) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            BroadcastManager.get().sendComponentMessage(player,
                    ERROR_PREFIX.append(
                            StyleManager.parseComponent("<red>Slot must be a number between " +
                                    MIN_SLOT + " and " + MAX_SLOT + ".</red>")));
            SoundManager.playFailure(player);
            return true;
        }

        // Resolve player identifier asynchronously
        CompletableFuture<Void> future = resolvePlayerIdentifierAsync(args[0])
                .thenCompose(targetUuid -> {
                    if (targetUuid == null) {
                        // Player not found - schedule error message on main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            BroadcastManager.get().sendComponentMessage(player,
                                    ERROR_PREFIX.append(
                                            StyleManager.parseComponent("<red>Could not find a player with that name or UUID.</red>")));
                            SoundManager.playFailure(player);
                        });
                        return CompletableFuture.completedFuture(null);
                    }

                    // Check if player is online first
                    Player targetPlayer = Bukkit.getPlayer(targetUuid);

                    // Load player data asynchronously
                    return CompletableFuture.runAsync(() -> {
                        if (targetPlayer == null) {
                            // Only load from DB if player is offline
                            KitManager.get().loadPlayerDataFromDB(targetUuid);
                        }
                    }).thenRun(() -> {
                        // Run on the main thread after data is loaded
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (KitManager.get().hasEC(targetUuid, slot)) {
                                GUI gui = new GUI(plugin);
                                gui.InspectEc(player, targetUuid, slot);
                            } else {
                                String targetName = getPlayerName(targetUuid);

                                BroadcastManager.get().sendComponentMessage(player,
                                        ERROR_PREFIX.append(
                                                StyleManager.parseComponent("<red>" + targetName +
                                                        " does not have an enderchest in slot " + slot + "</red>")));
                                SoundManager.playFailure(player);
                            }
                        });
                    });
                });

        // Handle exceptions
        future.exceptionally(ex -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().severe("Error loading enderchest data: " + ex.getMessage());
                BroadcastManager.get().sendComponentMessage(player,
                        ERROR_PREFIX.append(
                                StyleManager.parseComponent("<red>An error occurred while loading enderchest data. " +
                                        "See console for details.</red>")));
                SoundManager.playFailure(player);
            });
            return null;
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("VirtualKits.inspect")) {
            return List.of();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList());
            if (input.length() >= 4 && input.contains("-")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getUniqueId)
                        .map(UUID::toString)
                        .filter(uuid -> uuid.startsWith(input))
                        .toList());
            }
            return completions;
        } else if (args.length == 2) {
            return IntStream.rangeClosed(MIN_SLOT, MAX_SLOT)
                    .mapToObj(String::valueOf)
                    .filter(slot -> slot.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
