package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.util.DisabledCommand;
import dev.ace.virtualkit.ItemFilter;
import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.util.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.ace.virtualkit.util.SoundManager;

import java.util.Collections;
import java.util.List;

/**
 * Command to save the premade kit
 * Usage: /savepk (saves player's current inventory as the premade kit)
 * Permission: virtualkit.admin
 */
public class SavePublicKitCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }

        if (DisabledCommand.isBlockedInWorld(p)) {
            return true;
        }

        // Check admin permission
        if (!p.hasPermission("virtualkit.admin")) {
            MessageManager.get().sendMessage(p, MessageManager.get().getNoPermission());
            return true;
        }

        Inventory inv = p.getInventory();

        ItemStack[] data = new ItemStack[41];
        // Copy inventory into data
        for (int i = 0; i < 41; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                data[i] = item.clone();
            }
        }

        data = ItemFilter.get().filterItemStack(data);

        KitManager kitManager = KitManager.get();
        // Save kit with fixed "premade" ID
        boolean success = kitManager.savePublicKit("premade", data);
        if (success) {
            kitManager.savePublicKitToDB("premade");
            MessageManager.get().sendMessage(p, "<#7cff6e>Premade kit saved successfully!");
            SoundManager.playSuccess(p);
        } else {
            MessageManager.get().sendMessage(p, "<#ffa6a6>Error saving premade kit!");
            SoundManager.playFailure(p);
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        return Collections.emptyList();
    }
}
