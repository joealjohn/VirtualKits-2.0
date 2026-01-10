package dev.ace.virtualkit.commands;

import com.google.common.primitives.Ints;
import dev.ace.virtualkit.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.ace.virtualkit.util.SoundManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DeleteKitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();

            if (args.length == 1) {
                Integer slot = Ints.tryParse(args[0]);
                KitManager kitManager = KitManager.get();
                if (slot == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /deletekit <slot>");
                    player.sendMessage(ChatColor.RED + "Select a real number");
                    SoundManager.playFailure(player);
                    return true;
                }

                if (kitManager.hasKit(uuid, slot)) {

                    if (kitManager.deleteKit(uuid, slot)) {
                        player.sendMessage(ChatColor.GREEN + "Kit " + slot + " deleted!");
                        SoundManager.playSuccess(player);
                    } else {
                        player.sendMessage(ChatColor.RED + "Kit deletion failed!");
                        SoundManager.playFailure(player);
                    }

                } else {
                    player.sendMessage(ChatColor.RED + "Kit " + slot + " doesnt exist!");
                    SoundManager.playFailure(player);
                }


            } else {
                player.sendMessage(ChatColor.RED + "Usage: /deletekit <slot>");
                SoundManager.playFailure(player);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only Players can use this!");
            if (sender instanceof Player s) SoundManager.playFailure(s);

        }


        return true;
    }
}

