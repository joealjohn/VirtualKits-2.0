package dev.ace.virtualkit.util;

import dev.ace.virtualkit.VirtualKits;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DisabledCommand {




    private static boolean isBlockedInWorld(World world) {
        return VirtualKits.getPlugin().getConfig().getStringList("disabled-command-worlds").contains(world.getName());
    }


    public static boolean isBlockedInWorld(Player player) {
        if (isBlockedInWorld(player.getWorld())) {
            MessageManager.get().sendMessage(player, MessageManager.get().getCommandDisabled());
            SoundManager.playFailure(player);
            return true;
        }
        return false;
    }
}

