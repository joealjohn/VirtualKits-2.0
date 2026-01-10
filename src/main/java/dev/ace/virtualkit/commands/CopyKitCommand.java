package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.util.DisabledCommand;
import dev.ace.virtualkit.KitShareManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.ace.virtualkit.util.SoundManager;
import org.jetbrains.annotations.NotNull;

public class CopyKitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {

            if (DisabledCommand.isBlockedInWorld(player)) {
                return true;
            }


            if (args.length > 0) {
                KitShareManager.get().copyKit(player, args[0]);
            } else {
                player.sendMessage(ChatColor.RED + "Error, you must enter a kit code to copy");
                SoundManager.playFailure(player);
            }
        } else {
            sender.sendMessage("Only players can use this command");
        }

        return true;
    }
}
