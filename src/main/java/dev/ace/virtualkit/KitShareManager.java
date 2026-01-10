package dev.ace.virtualkit;

import dev.ace.virtualkit.util.BroadcastManager;
import dev.ace.virtualkit.util.StyleManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import dev.ace.virtualkit.util.SoundManager;
import dev.ace.virtualkit.util.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class KitShareManager {

    public static HashMap<String, ItemStack[]> kitShareMap;
    private static KitShareManager instance;
    private final Plugin plugin;

    public KitShareManager(Plugin plugin) {
        this.plugin = plugin;
        kitShareMap = new HashMap<>();
        instance = this;
    }

    public static KitShareManager get() {
        if (instance == null) {
            throw new IllegalStateException("KitShareManager has not been initialized");
        }
        return instance;
    }

    public List<String> getKitSlots(Player p) {
        ArrayList<String> slots = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (KitManager.get().hasKit(p.getUniqueId(), i)) {
                slots.add(String.valueOf(i));
            }
        }
        return slots;
    }

    public List<String> getECSlots(Player p) {
        ArrayList<String> slots = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (KitManager.get().hasEC(p.getUniqueId(), i)) {
                slots.add(String.valueOf(i));
            }
        }
        return slots;
    }

    private String getMessage(String path, String defaultValue) {
        return plugin.getConfig().getString("messages." + path, defaultValue);
    }

    private void sendCopyTitle(Player p) {
        FileConfiguration msg = MessageManager.get().getConfig();
        String path = "titles.kit-copied";

        if (!msg.getBoolean(path + ".enabled", true)) {
            return;
        }

        String title = msg.getString(path + ".title", "§aᴋɪᴛ ɪᴍᴘᴏʀᴛᴇᴅ");
        String subtitle = msg.getString(path + ".subtitle", "§7ꜱᴜᴄᴄᴇꜱꜱꜰᴜʟʟʏ");
        int fadeIn = msg.getInt(path + ".fade-in", 5);
        int stay = msg.getInt(path + ".stay", 40);
        int fadeOut = msg.getInt(path + ".fade-out", 10);

        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void shareKit(Player p, int slot) {
        UUID uuid = p.getUniqueId();
        KitManager kitManager = KitManager.get();
        if (kitManager.hasKit(uuid, slot)) {
            String id = RandomStringUtils.randomAlphanumeric(6).toUpperCase();

            if (kitShareMap.putIfAbsent(id, kitManager.getPlayerKit(uuid, slot).clone()) == null) {
                // Send clickable message with copy-to-clipboard
                sendClickableShareCode(p, id, "/copykit " + id);

                String expiresMsg = getMessage("share.code-expires", "<gray>Code expires in 15 minutes");
                BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(expiresMsg));
                SoundManager.playSuccess(p);

                // Auto-cleanup after 15 minutes
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        kitShareMap.remove(id);
                    }
                }.runTaskLater(plugin, 15 * 60 * 20);

            } else {
                String errorMsg = getMessage("error.unexpected-error",
                        "<#FF4444>Unexpected error occurred, please try again.");
                BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(errorMsg));
                SoundManager.playFailure(p);
            }

        } else {
            String errorMsg = getMessage("error.kit-not-found", "<#FF4444>Kit not found!");
            BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(errorMsg));
            SoundManager.playFailure(p);
        }
    }

    /**
     * Sends a clickable share code message that copies to clipboard on click
     */
    private void sendClickableShareCode(Player p, String code, String command) {
        // Create clickable component: "[CLICK TO COPY] /copykit CODE"
        net.kyori.adventure.text.Component message = net.kyori.adventure.text.Component.text()
                .append(net.kyori.adventure.text.Component
                        .text("[CLICK TO COPY] ", net.kyori.adventure.text.format.NamedTextColor.GREEN,
                                net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(command))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(net.kyori.adventure.text.Component.text("Click to copy command!",
                                        net.kyori.adventure.text.format.NamedTextColor.YELLOW))))
                .append(net.kyori.adventure.text.Component
                        .text(command, net.kyori.adventure.text.format.NamedTextColor.AQUA)
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand(command))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(net.kyori.adventure.text.Component.text("Click to paste in chat!",
                                        net.kyori.adventure.text.format.NamedTextColor.YELLOW))))
                .build();

        BroadcastManager.get().sendComponentMessage(p, message);
    }

    public void shareEC(Player p, int slot) {
        UUID uuid = p.getUniqueId();
        KitManager kitManager = KitManager.get();
        if (kitManager.hasEC(uuid, slot)) {
            String id = RandomStringUtils.randomAlphanumeric(6).toUpperCase();

            if (kitShareMap.putIfAbsent(id, kitManager.getPlayerEC(uuid, slot).clone()) == null) {
                // Send clickable message with copy-to-clipboard
                sendClickableShareCode(p, id, "/copyec " + id);

                String expiresMsg = getMessage("share.code-expires", "<gray>Code expires in 15 minutes");
                BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(expiresMsg));
                SoundManager.playSuccess(p);

                // Auto-cleanup after 15 minutes
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        kitShareMap.remove(id);
                    }
                }.runTaskLater(plugin, 15 * 60 * 20);

            } else {
                String errorMsg = getMessage("error.unexpected-error",
                        "<#FF4444>Unexpected error occurred, please try again.");
                BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(errorMsg));
                SoundManager.playFailure(p);
            }

        } else {
            String errorMsg = getMessage("error.kit-not-found", "<#FF4444>Enderchest not found!");
            BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(errorMsg));
            SoundManager.playFailure(p);
        }
    }

    public void copyKit(Player p, String str) {
        String id = str.toUpperCase();
        if (!kitShareMap.containsKey(id)) {
            String errorMsg = getMessage("error.kit-not-found", "<#FF4444>Kit does not exist or has expired!");
            BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(errorMsg));
            SoundManager.playFailure(p);
            return;
        }

        ItemStack[] data = kitShareMap.get(id);

        if (data.length == 27) {
            // enderchest
            p.getEnderChest().setContents(kitShareMap.get(id));
            BroadcastManager.get().broadcastPlayerCopiedEC(p);
            SoundManager.playSuccess(p);

        } else if (data.length == 41) {
            // inventory
            p.getInventory().setContents(kitShareMap.get(id));
            p.closeInventory(); // Auto-close menu so player can see imported kit
            sendCopyTitle(p); // Show non-bold title
            BroadcastManager.get().broadcastPlayerCopiedKit(p);
            SoundManager.playSuccess(p);
        } else {
            String errorMsg = getMessage("error.unexpected-error",
                    "<#FF4444>Unexpected error occurred, please try again.");
            BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent(errorMsg));
            SoundManager.playFailure(p);
        }
    }
}
