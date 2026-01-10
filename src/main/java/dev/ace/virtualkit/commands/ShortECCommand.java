package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.util.DisabledCommand;
import dev.ace.virtualkit.util.ECCooldownManager;
import dev.ace.virtualkit.VirtualKits;
import dev.ace.virtualkit.gui.GUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Short commands for enderchest: /ec1, /ec2, etc.
 * Opens a view GUI where player can take items from their saved enderchest
 */
public class ShortECCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (DisabledCommand.isBlockedInWorld(player)) {
            return true;
        }

        // Check cooldown
        ECCooldownManager cooldown = ECCooldownManager.get();
        if (cooldown != null && !cooldown.canUseEC(player)) {
            cooldown.sendCooldownMessage(player);
            return true;
        }

        int ecNumber = -1;

        if (label.matches("ec[1-9]")) {
            ecNumber = Integer.parseInt(label.substring(2));
        } else if (label.matches("enderchest[1-9]")) {
            ecNumber = Integer.parseInt(label.substring(10));
        }

        if (ecNumber > 0) {
            // Start cooldown
            if (cooldown != null) {
                cooldown.startCooldown(player.getUniqueId());
            }

            // Open view-only GUI where player can take items
            GUI gui = new GUI(VirtualKits.getPlugin());
            gui.OpenEnderchestView(player, ecNumber);
        } else {
            player.sendMessage("Invalid command label.");
        }

        return true;
    }
}
