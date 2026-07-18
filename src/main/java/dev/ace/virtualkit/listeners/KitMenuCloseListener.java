package dev.ace.virtualkit.listeners;

import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.gui.GUI;
import dev.ace.virtualkit.util.MessageManager;
import dev.ace.virtualkit.util.StyleManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class KitMenuCloseListener implements Listener {

    private FileConfiguration getMessagesConfig() {
        return MessageManager.get().getConfig();
    }

    private void sendSaveTitle(Player p, String type, int slot) {
        FileConfiguration msg = getMessagesConfig();
        String path = "titles." + type;

        if (!msg.getBoolean(path + ".enabled", true)) {
            return;
        }

        String title = msg.getString(path + ".title", "§a§lSAVED");
        String subtitle = msg.getString(path + ".subtitle", "§7Slot %slot%").replace("%slot%", String.valueOf(slot));
        int fadeIn = msg.getInt(path + ".fade-in", 5);
        int stay = msg.getInt(path + ".stay", 30);
        int fadeOut = msg.getInt(path + ".fade-out", 10);

        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    @EventHandler
    public void onKitEditorClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) {
            if (inv.getLocation() == null) {
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Kit: ")) {
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    int slot = Integer
                            .parseInt(view.getTitle().replace(StyleManager.get().getPrimaryColor() + "Kit: ", ""));
                    ItemStack[] kit = new ItemStack[41];
                    ItemStack[] chestitems = e.getInventory().getContents();

                    for (int i = 0; i < 41; i++) {
                        if (chestitems[i] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i].clone();
                        }
                    }
                    boolean saved = KitManager.get().savekit(uuid, slot, kit);
                    // Show title based on save result
                    if (saved) {
                        sendSaveTitle(p, "kit-saved", slot);
                    } else {
                        sendSaveTitle(p, "kit-empty", slot);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPublicKitEditorClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) {
            if (inv.getLocation() == null) {
                InventoryView view = e.getView();
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Public Kit: ")) {
                    Player player = (Player) e.getPlayer();
                    String publickit = view.getTitle().replace(StyleManager.get().getPrimaryColor() + "Public Kit: ",
                            "");
                    ItemStack[] kit = new ItemStack[41];
                    ItemStack[] chestitems = e.getInventory().getContents();

                    for (int i = 0; i < 41; i++) {
                        if (chestitems[i] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i].clone();
                        }
                    }
                    KitManager.get().savePublicKit(player, publickit, kit);
                    // Show title for premade kit saved
                    sendSaveTitle(player, "premade-kit-saved", 0);
                }
            }
        }
    }

    @EventHandler
    public void onEnderchestEditorClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getSize() == 54) {
            if (inv.getLocation() == null) {
                InventoryView view = e.getView();
                // Check for "Ender Chest: " (with space between Ender and Chest)
                if (view.getTitle().contains(StyleManager.get().getPrimaryColor() + "Ender Chest: ")) {
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    int slot = Integer.parseInt(
                            view.getTitle().replace(StyleManager.get().getPrimaryColor() + "Ender Chest: ", ""));
                    ItemStack[] kit = new ItemStack[27];
                    ItemStack[] chestitems = e.getInventory().getContents();

                    for (int i = 0; i < 27; i++) {
                        if (chestitems[i + 9] == null) {
                            kit[i] = null;
                        } else {
                            kit[i] = chestitems[i + 9].clone();
                        }
                    }
                    KitManager.get().saveEC(uuid, slot, kit);
                    // Show title message from messages.yml
                    sendSaveTitle(p, "ec-saved", slot);
                }
            }
        }
    }

    @EventHandler
    public void onInspectKitEditorClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getSize() != 54) return;
        if (inv.getLocation() != null) return;

        InventoryView view = e.getView();
        String title = view.getTitle();
        String primary = StyleManager.get().getPrimaryColor();

        // New title format: "<primary>Inspect:kit:<uuid>:<slot>"
        if (!title.startsWith(primary + "Inspect:kit:")) return;

        String payload = title.substring((primary + "Inspect:kit:").length()); // "<uuid>:<slot>"
        int lastColon = payload.lastIndexOf(':');
        if (lastColon < 0) return;

        UUID targetUuid;
        int slot;
        try {
            targetUuid = UUID.fromString(payload.substring(0, lastColon));
            slot = Integer.parseInt(payload.substring(lastColon + 1));
        } catch (IllegalArgumentException ex) {
            return;
        }

        Player p = (Player) e.getPlayer();
        if (!p.hasPermission("VirtualKits.admin")) return;

        if (GUI.removeKitDeletionFlag(p)) return;

        ItemStack[] kit = new ItemStack[41];
        ItemStack[] chestItems = e.getInventory().getContents();
        for (int i = 0; i < 41; i++) {
            kit[i] = chestItems[i] == null ? null : chestItems[i].clone();
        }

        String targetName = getPlayerName(targetUuid);
        if (KitManager.get().savekit(targetUuid, slot, kit, true)) {
            p.sendMessage(ChatColor.GREEN + "Kit " + slot + " updated for player " + targetName + "!");
        } else {
            p.sendMessage(ChatColor.RED + "Failed to update kit for player " + targetName + "!");
        }
    }

    @EventHandler
    public void onInspectEnderchestEditorClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getSize() != 54) return;
        if (inv.getLocation() != null) return;

        InventoryView view = e.getView();
        String title = view.getTitle();
        String primary = StyleManager.get().getPrimaryColor();

        // New title format: "<primary>Inspect:ec:<uuid>:<slot>"
        if (!title.startsWith(primary + "Inspect:ec:")) return;

        String payload = title.substring((primary + "Inspect:ec:").length());
        int lastColon = payload.lastIndexOf(':');
        if (lastColon < 0) return;

        UUID targetUuid;
        int slot;
        try {
            targetUuid = UUID.fromString(payload.substring(0, lastColon));
            slot = Integer.parseInt(payload.substring(lastColon + 1));
        } catch (IllegalArgumentException ex) {
            return;
        }

        Player p = (Player) e.getPlayer();
        if (!p.hasPermission("VirtualKits.admin")) return;

        if (GUI.removeKitDeletionFlag(p)) return;

        ItemStack[] kit = new ItemStack[27];
        ItemStack[] chestItems = e.getInventory().getContents();
        for (int i = 0; i < 27; i++) {
            kit[i] = chestItems[i + 9] == null ? null : chestItems[i + 9].clone();
        }

        String targetName = getPlayerName(targetUuid);
        if (KitManager.get().saveEC(targetUuid, slot, kit)) {
            p.sendMessage(ChatColor.GREEN + "Enderchest " + slot + " updated for player " + targetName + "!");
        } else {
            p.sendMessage(ChatColor.RED + "Failed to update enderchest for player " + targetName + "!");
        }
    }

    /** Resolve display name for a UUID — online first, then offline cache, then UUID string. */
    private String getPlayerName(UUID uuid) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) return online.getName();
        // getOfflinePlayer(uuid) never makes a network call — it just reads the cache
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        String name = op.getName();
        return name != null ? name : uuid.toString();
    }
}
