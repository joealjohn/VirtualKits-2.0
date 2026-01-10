package dev.ace.virtualkit.util;

import dev.ace.virtualkit.VirtualKits;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DisabledCommand {




    private static boolean isBlockedInWorld(World world) {
        return VirtualKits.getPlugin().getConfig().getStringList("disabled-command-worlds").contains(world.getName());
    }


    public static boolean isBlockedInWorld(Player player) {
        if (isBlockedInWorld(player.getWorld())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', VirtualKits.getPlugin().getConfig().getString("disabled-command-message")));
            SoundManager.playFailure(player);
            return true;
        }
        return false;
    }
}

