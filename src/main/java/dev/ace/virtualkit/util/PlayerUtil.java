package dev.ace.virtualkit.util;

import dev.ace.virtualkit.VirtualKits;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerUtil {

    public static void repairItem(ItemStack i) {
        if (i != null) {
            ItemMeta meta = i.getItemMeta();
            Damageable damageable = (Damageable) meta;
            if (damageable != null && damageable.hasDamage()) {
                damageable.setDamage(0);
            }
            i.setItemMeta(damageable);
        }

    }

    public static void repairAll(Player p) {
        for (ItemStack i : p.getInventory().getContents()) {
            repairItem(i);
        }
        String msg = VirtualKits.getPlugin().getConfig().getString("messages.success.items-repaired",
                "<#00FF00>All items repaired!");
        BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(msg));
    }

    public static void healPlayer(Player p) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);

        // Remove potion effects if configured to do so
        if (VirtualKits.getPlugin().getConfig().getBoolean("feature.heal-remove-effects", false)) {
            p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        }

        String msg = VirtualKits.getPlugin().getConfig().getString("messages.success.healed",
                "<#00FF00>You have been healed!");
        BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(msg));
    }

    public static void healPlayerSilent(Player p) {
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setSaturation(20);
    }

}

