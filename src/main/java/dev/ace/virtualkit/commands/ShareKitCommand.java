package dev.ace.virtualkit.commands;

import com.google.common.primitives.Ints;
import dev.ace.virtualkit.KitShareManager;
import dev.ace.virtualkit.util.CooldownManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.ace.virtualkit.util.SoundManager;
import org.jetbrains.annotations.NotNull;

public class ShareKitCommand implements CommandExecutor {

    private final CooldownManager shareKitCommandCooldown;

    public ShareKitCommand() {
        this.shareKitCommandCooldown = new CooldownManager(5);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Error, you must select a kit slot to share");
            SoundManager.playFailure(player);
            return true;
        }

        if (shareKitCommandCooldown.isOnCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Please don't spam the command (5 second cooldown)");
            SoundManager.playFailure(player);
            return true;
        }

        Integer slot = Ints.tryParse(args[0]);

        if (slot == null || slot < 1 || slot > 9) {
            player.sendMessage(ChatColor.RED + "Select a valid kit slot");
            SoundManager.playFailure(player);
            return true;
        }

        KitShareManager.get().shareKit(player, slot);
        shareKitCommandCooldown.setCooldown(player);

        return true;
    }
}

