package dev.ace.virtualkit.commands;

import com.google.common.primitives.Ints;
import dev.ace.virtualkit.KitManager;
import org.bukkit.ChatColor;
import dev.ace.virtualkit.util.SoundManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SwapKitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only Players can use this!");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /swapkit <slot1> <slot2>");
            SoundManager.playFailure(player);
            return true;
        }

        Integer slot1 = Ints.tryParse(args[0]);
        Integer slot2 = Ints.tryParse(args[1]);

        if (slot1 == null || slot2 == null) {
            player.sendMessage(ChatColor.RED + "Usage: /swapkit <slot1> <slot2>");
            player.sendMessage(ChatColor.RED + "Select real numbers");
            SoundManager.playFailure(player);
            return true;
        }

        KitManager kitManager = KitManager.get();
        UUID uuid = player.getUniqueId();

        if (!kitManager.hasKit(uuid, slot1)) {
            player.sendMessage(ChatColor.RED + "Kit " + slot1 + " doesn't exist!");
            SoundManager.playFailure(player);
            return true;
        }

        if (!kitManager.hasKit(uuid, slot2)) {
            player.sendMessage(ChatColor.RED + "Kit " + slot2 + " doesn't exist!");
            SoundManager.playFailure(player);
            return true;
        }

        ItemStack[] tempkit = kitManager.getPlayerKit(uuid, slot1).clone();
        kitManager.savekit(uuid, slot1, kitManager.getPlayerKit(uuid, slot2), true);
        kitManager.savekit(uuid, slot2, tempkit.clone(), true);
        kitManager.saveEnderchestToDB(uuid, slot1);
        kitManager.saveEnderchestToDB(uuid, slot2);

        player.sendMessage(ChatColor.GREEN + "Kits " + slot1 + " and " + slot2 + " have been swapped!");
        SoundManager.playSuccess(player);
        return true;
    }
}

