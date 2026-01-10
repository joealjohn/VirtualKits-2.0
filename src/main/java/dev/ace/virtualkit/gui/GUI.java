package dev.ace.virtualkit.gui;

import dev.ace.virtualkit.ItemFilter;
import dev.ace.virtualkit.KitManager;
import dev.ace.virtualkit.KitRoomDataManager;
import dev.ace.virtualkit.PublicKit;
import dev.ace.virtualkit.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.ace.virtualkit.gui.ItemUtil.addHideFlags;
import static dev.ace.virtualkit.gui.ItemUtil.createItem;
import static dev.ace.virtualkit.gui.ItemUtil.createGlassPane;

public class GUI {
    private final Plugin plugin;
    private final boolean filterItemsOnImport;
    private static final Set<UUID> kitDeletionFlag = new HashSet<>();

    public GUI(Plugin plugin) {
        this.plugin = plugin;
        this.filterItemsOnImport = plugin.getConfig().getBoolean("anti-exploit.import-filter", false);
    }

    private GuiConfigManager cfg() {
        return GuiConfigManager.get();
    }

    private String getMessage(String path, String defaultValue) {
        return plugin.getConfig().getString("messages." + path, defaultValue);
    }

    public static void addLoadPublicKit(Slot slot, String id) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            KitManager.get().loadPublicKit(player, id);
            info.getClickedMenu().close();
        });
    }

    public static Menu createPublicKitMenu() {
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Public Kit Room").redraw(true)
                .build();
    }

    public static boolean removeKitDeletionFlag(Player player) {
        return kitDeletionFlag.remove(player.getUniqueId());
    }

    public void OpenKitMenu(Player p, int slot) {
        Menu menu = createKitMenu(slot);

        if (KitManager.get().getItemStackArrayById(p.getUniqueId().toString() + slot) != null) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(p.getUniqueId().toString() + slot);
            for (int i = 0; i < 41; i++) {
                menu.getSlot(i).setItem(kit[i]);
            }
        }
        for (int i = 0; i < 41; i++) {
            allowModification(menu.getSlot(i));
        }
        for (int i = 41; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, cfg().getKitEditorBootsLabel()));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, cfg().getKitEditorLeggingsLabel()));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, cfg().getKitEditorChestplateLabel()));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, cfg().getKitEditorHelmetLabel()));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, cfg().getKitEditorOffhandLabel()));

        List<String> importLore = cfg().getKitEditorImportButtonLore();
        menu.getSlot(51)
                .setItem(createItem(cfg().getKitEditorImportButtonMaterial(), 1, cfg().getKitEditorImportButtonName(),
                        importLore.isEmpty() ? new String[] { "<gray>Import from inventory" }
                                : importLore.toArray(new String[0])));

        List<String> clearLore = cfg().getKitEditorClearButtonLore();
        menu.getSlot(52)
                .setItem(createItem(cfg().getKitEditorClearButtonMaterial(), 1, cfg().getKitEditorClearButtonName(),
                        clearLore.isEmpty() ? new String[] { "<gray>Shift click to clear" }
                                : clearLore.toArray(new String[0])));

        menu.getSlot(53)
                .setItem(createItem(cfg().getKitEditorBackButtonMaterial(), 1, cfg().getKitEditorBackButtonName()));
        addCloseButton(menu.getSlot(53));
        addClear(menu.getSlot(52));
        addImport(menu.getSlot(51));
        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);

        menu.open(p);
    }

    public void OpenPublicKitEditor(Player p, String kitId) {
        Menu menu = createPublicKitMenu(kitId);

        if (KitManager.get().getItemStackArrayById(IDUtil.getPublicKitId(kitId)) != null) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(IDUtil.getPublicKitId(kitId));
            for (int i = 0; i < 41; i++) {
                menu.getSlot(i).setItem(kit[i]);
            }
        }
        for (int i = 0; i < 41; i++) {
            allowModification(menu.getSlot(i));
        }
        for (int i = 41; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, cfg().getKitEditorBootsLabel()));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, cfg().getKitEditorLeggingsLabel()));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, cfg().getKitEditorChestplateLabel()));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, cfg().getKitEditorHelmetLabel()));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, cfg().getKitEditorOffhandLabel()));

        List<String> importLore = cfg().getKitEditorImportButtonLore();
        menu.getSlot(51)
                .setItem(createItem(cfg().getKitEditorImportButtonMaterial(), 1, cfg().getKitEditorImportButtonName(),
                        importLore.isEmpty() ? new String[] { "<gray>Import from inventory" }
                                : importLore.toArray(new String[0])));

        List<String> clearLore = cfg().getKitEditorClearButtonLore();
        menu.getSlot(52)
                .setItem(createItem(cfg().getKitEditorClearButtonMaterial(), 1, cfg().getKitEditorClearButtonName(),
                        clearLore.isEmpty() ? new String[] { "<gray>Shift click to clear" }
                                : clearLore.toArray(new String[0])));

        menu.getSlot(53)
                .setItem(createItem(cfg().getKitEditorBackButtonMaterial(), 1, cfg().getKitEditorBackButtonName()));
        addCloseButton(menu.getSlot(53));
        addClear(menu.getSlot(52));
        addImport(menu.getSlot(51));
        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);

        menu.open(p);
    }

    public void OpenECKitKenu(Player p, int slot) {
        Menu menu = createECMenu(slot);

        for (int i = 0; i < 9; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        for (int i = 36; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        if (KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot) != null) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot);
            for (int i = 9; i < 36; i++) {
                menu.getSlot(i).setItem(kit[i - 9]);
            }
        }
        for (int i = 9; i < 36; i++) {
            allowModification(menu.getSlot(i));
        }

        List<String> importEcLore = cfg().getKitEditorImportEcButtonLore();
        menu.getSlot(51)
                .setItem(createItem(cfg().getKitEditorImportEcButtonMaterial(), 1,
                        cfg().getKitEditorImportEcButtonName(),
                        importEcLore.isEmpty() ? new String[] { "<gray>Import from ender chest" }
                                : importEcLore.toArray(new String[0])));

        List<String> clearLore = cfg().getKitEditorClearButtonLore();
        menu.getSlot(52)
                .setItem(createItem(cfg().getKitEditorClearButtonMaterial(), 1, cfg().getKitEditorClearButtonName(),
                        clearLore.isEmpty() ? new String[] { "<gray>Shift click to clear" }
                                : clearLore.toArray(new String[0])));

        menu.getSlot(53)
                .setItem(createItem(cfg().getKitEditorBackButtonMaterial(), 1, cfg().getKitEditorBackButtonName()));
        addCloseButton(menu.getSlot(53));
        addClear(menu.getSlot(52), 9, 36);
        addImportEC(menu.getSlot(51));
        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
    }

    public void InspectKit(Player p, UUID target, int slot) {
        String playerName = getPlayerName(target);
        Menu menu = createInspectMenu(slot, playerName);

        if (KitManager.get().hasKit(target, slot)) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(target.toString() + slot);
            for (int i = 0; i < 41; i++) {
                menu.getSlot(i).setItem(kit[i]);
            }
        }
        for (int i = 41; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, cfg().getKitEditorBootsLabel()));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, cfg().getKitEditorLeggingsLabel()));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, cfg().getKitEditorChestplateLabel()));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, cfg().getKitEditorHelmetLabel()));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, cfg().getKitEditorOffhandLabel()));

        menu.getSlot(53)
                .setItem(createItem(cfg().getKitEditorCloseButtonMaterial(), 1, cfg().getKitEditorCloseButtonName()));
        menu.getSlot(53).setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });

        if (p.hasPermission("virtualkit.admin")) {
            for (int i = 0; i < 41; i++) {
                allowModification(menu.getSlot(i));
            }
            List<String> clearKitLore = cfg().getInspectClearKitButtonLore();
            menu.getSlot(52)
                    .setItem(createItem(cfg().getInspectClearKitButtonMaterial(), 1,
                            cfg().getInspectClearKitButtonName(),
                            clearKitLore.isEmpty() ? new String[] { "<gray>Shift click to delete kit" }
                                    : clearKitLore.toArray(new String[0])));
            addClearKit(menu.getSlot(52), target, slot);
        }

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
        SoundManager.playOpenGui(p);
    }

    public void InspectEc(Player p, UUID target, int slot) {
        String playerName = getPlayerName(target);
        Menu menu = createInspectEcMenu(slot, playerName);

        for (int i = 0; i < 9; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        for (int i = 36; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        if (KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot) != null) {
            ItemStack[] kit = KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot);
            for (int i = 9; i < 36; i++) {
                menu.getSlot(i).setItem(kit[i - 9]);
            }
        }

        menu.getSlot(53)
                .setItem(createItem(cfg().getKitEditorCloseButtonMaterial(), 1, cfg().getKitEditorCloseButtonName()));
        menu.getSlot(53).setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });

        if (p.hasPermission("virtualkit.admin")) {
            for (int i = 9; i < 36; i++) {
                allowModification(menu.getSlot(i));
            }
            List<String> clearEcLore = cfg().getInspectClearEcButtonLore();
            menu.getSlot(52)
                    .setItem(createItem(cfg().getInspectClearEcButtonMaterial(), 1, cfg().getInspectClearEcButtonName(),
                            clearEcLore.isEmpty() ? new String[] { "<gray>Shift click to delete ender chest" }
                                    : clearEcLore.toArray(new String[0])));
            addClearEnderchest(menu.getSlot(52), target, slot);
        }

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
        SoundManager.playOpenGui(p);
    }

    /**
     * Opens a view-only enderchest GUI where player can take items out.
     * Used by /ec1, /ec2, etc. commands
     */
    public void OpenEnderchestView(Player p, int slot) {
        Menu menu = ChestMenu.builder(4).title(StyleManager.get().getPrimaryColor() + "Enderchest " + slot).redraw(true)
                .build();

        // Fill top and bottom rows with glass
        for (int i = 0; i < 9; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        for (int i = 27; i < 36; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        // Load enderchest items into middle rows (slots 9-26 = 18 items)
        ItemStack[] ec = KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + slot);
        if (ec != null) {
            for (int i = 0; i < Math.min(ec.length, 18); i++) {
                if (ec[i] != null) {
                    menu.getSlot(i + 9).setItem(ec[i].clone());
                }
            }
        }

        // Allow players to take items from slots 9-26 (view-only mode - no saving)
        for (int i = 9; i < 27; i++) {
            menu.getSlot(i).setClickOptions(ClickOptions.ALLOW_ALL);
            // No save handler - items taken are gone from player's view but EC storage is
            // not modified
            // Players can only truly edit EC through the kit editor
        }

        // Close button
        menu.getSlot(35)
                .setItem(createItem(cfg().getKitEditorCloseButtonMaterial(), 1, cfg().getKitEditorCloseButtonName()));
        menu.getSlot(35).setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
        SoundManager.playOpenGui(p);
    }

    public void OpenMainMenu(Player p) {
        Menu menu = createMainMenu(p);
        int totalSlots = cfg().getMainMenuRows() * 9;

        // Fill all slots with glass panes
        for (int i = 0; i < totalSlots; i++) {
            menu.getSlot(i).setItem(createGlassPane());
        }

        // Kit slots - configurable positions from comma-separated list
        int[] kitSlots = cfg().getMainMenuKitSlots();
        List<String> kitSlotLore = cfg().getMainMenuKitSlotLore();
        for (int i = 0; i < kitSlots.length; i++) {
            int slot = kitSlots[i];
            int kitNum = i + 1;
            boolean hasKit = KitManager.get().hasKit(p.getUniqueId(), kitNum);

            // Get appropriate material from config based on whether kit exists
            Material kitMaterial = hasKit
                    ? Material.valueOf(plugin.getConfig().getString("slot-icons.kit-saved", "LIME_SHULKER_BOX"))
                    : Material.valueOf(plugin.getConfig().getString("slot-icons.kit-empty", "RED_SHULKER_BOX"));

            menu.getSlot(slot)
                    .setItem(createItem(kitMaterial, 1, cfg().getMainMenuKitSlotName(kitNum),
                            kitSlotLore.isEmpty()
                                    ? new String[] { "<gray>Left click to load kit", "<gray>Right click to edit kit" }
                                    : kitSlotLore.toArray(new String[0])));
            addEditLoad(menu.getSlot(slot), kitNum);
        }

        // Ender chest slots - configurable positions from comma-separated list
        int[] ecSlots = cfg().getMainMenuEcSlots();
        List<String> ecSlotLore = cfg().getMainMenuEcSlotLore();
        List<String> ecCreateLore = cfg().getMainMenuEcCreateLore();

        for (int i = 0; i < ecSlots.length; i++) {
            int slot = ecSlots[i];
            int ecNum = i + 1;
            boolean hasEC = KitManager.get().getItemStackArrayById(p.getUniqueId() + "ec" + ecNum) != null;

            // Get appropriate material from config based on whether EC exists
            Material ecMaterial = hasEC
                    ? Material.valueOf(plugin.getConfig().getString("slot-icons.ec-saved", "ENDER_CHEST"))
                    : Material.valueOf(plugin.getConfig().getString("slot-icons.ec-empty", "ENDER_CHEST"));

            if (hasEC) {
                menu.getSlot(slot)
                        .setItem(createItem(ecMaterial, 1, cfg().getMainMenuEcSlotName(ecNum),
                                ecSlotLore.isEmpty()
                                        ? new String[] { "<gray>Left click to load", "<gray>Right click to edit" }
                                        : ecSlotLore.toArray(new String[0])));
                addEditLoadEC(menu.getSlot(slot), ecNum);
            } else {
                menu.getSlot(slot)
                        .setItem(createItem(ecMaterial, 1,
                                cfg().getMainMenuEcCreateName(ecNum),
                                ecCreateLore.isEmpty() ? new String[] { "<gray>Click to create" }
                                        : ecCreateLore.toArray(new String[0])));
                addEditEC(menu.getSlot(slot), ecNum);
            }
        }

        // Bottom row buttons - configurable positions
        int kitRoomSlot = cfg().getMainMenuKitRoomSlot();
        int premadeSlot = cfg().getMainMenuPremadeKitsSlot();
        int infoSlot = cfg().getMainMenuInfoSlot();
        int clearSlot = cfg().getMainMenuClearInventorySlot();
        int shareSlot = cfg().getMainMenuShareKitsSlot();
        int repairSlot = cfg().getMainMenuRepairItemsSlot();

        menu.getSlot(kitRoomSlot)
                .setItem(createItem(cfg().getMainMenuKitRoomButtonMaterial(), 1, cfg().getMainMenuKitRoomButtonName()));
        menu.getSlot(premadeSlot).setItem(
                createItem(cfg().getMainMenuPremadeKitsButtonMaterial(), 1, cfg().getMainMenuPremadeKitsButtonName()));

        List<String> infoLore = cfg().getMainMenuInfoButtonLore();
        menu.getSlot(infoSlot)
                .setItem(createItem(cfg().getMainMenuInfoButtonMaterial(), 1, cfg().getMainMenuInfoButtonName(),
                        infoLore.isEmpty() ? new String[] { "<gray>Click a kit slot to load" }
                                : infoLore.toArray(new String[0])));

        List<String> clearInvLore = cfg().getMainMenuClearInvButtonLore();
        menu.getSlot(clearSlot).setItem(createItem(cfg().getMainMenuClearInvButtonMaterial(), 1,
                cfg().getMainMenuClearInvButtonName(),
                clearInvLore.isEmpty() ? new String[] { "<gray>Shift click" } : clearInvLore.toArray(new String[0])));

        List<String> shareKitsLore = cfg().getMainMenuShareKitsButtonLore();
        menu.getSlot(shareSlot)
                .setItem(createItem(cfg().getMainMenuShareKitsButtonMaterial(), 1,
                        cfg().getMainMenuShareKitsButtonName(),
                        shareKitsLore.isEmpty() ? new String[] { "<gray>/sharekit <slot>" }
                                : shareKitsLore.toArray(new String[0])));

        menu.getSlot(repairSlot)
                .setItem(createItem(cfg().getMainMenuRepairButtonMaterial(), 1, cfg().getMainMenuRepairButtonName()));

        addRepairButton(menu.getSlot(repairSlot));
        addKitRoom(menu.getSlot(kitRoomSlot));
        addLoadPremadeKit(menu.getSlot(premadeSlot));
        addClearButton(menu.getSlot(clearSlot));

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
    }

    public void OpenKitRoom(Player p) {
        OpenKitRoom(p, 0);
    }

    public void OpenKitRoom(Player p, int page) {
        Menu menu = createKitRoom();
        for (int i = 0; i < 45; i++) {
            allowModification(menu.getSlot(i));
        }
        for (int i = 45; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }
        if (KitRoomDataManager.get().getKitRoomPage(page) != null) {
            for (int i = 0; i < 45; i++) {
                menu.getSlot(i).setItem(KitRoomDataManager.get().getKitRoomPage(page)[i]);
            }
        }

        menu.getSlot(45)
                .setItem(createItem(cfg().getKitRoomRefillButtonMaterial(), 1, cfg().getKitRoomRefillButtonName()));
        addKitRoom(menu.getSlot(45), page);

        if (!p.hasPermission("virtualkit.editkitroom")) {
            menu.getSlot(53)
                    .setItem(createItem(cfg().getKitEditorBackButtonMaterial(), 1, cfg().getKitEditorBackButtonName()));
            addMainButton(menu.getSlot(53));
        } else {
            // Admin edit menu button: right click = save, shift right click = back
            menu.getSlot(53)
                    .setItem(createItem(cfg().getKitRoomEditMenuButtonMaterial(), page + 1,
                            cfg().getKitRoomEditMenuButtonName(),
                            "<gray>Right click to save", "<gray>Shift right click to go back"));

            // Allow right clicks on this slot
            menu.getSlot(53).setClickOptions(ClickOptions.DENY_ALL);

            final int finalPage = page;
            menu.getSlot(53).setClickHandler((player, info) -> {
                SoundManager.playClick(player);
                if (info.getClickType().isShiftClick() && info.getClickType().isRightClick()) {
                    // Shift right click = go back to main menu
                    info.getClickedMenu().close();
                    OpenMainMenu(player);
                } else if (info.getClickType().isRightClick()) {
                    // Right click = save kit room
                    ItemStack[] data = new ItemStack[45];
                    org.bukkit.inventory.Inventory inv = player.getOpenInventory().getTopInventory();
                    for (int i = 0; i < 45; i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && item.getType() != org.bukkit.Material.AIR) {
                            data[i] = item.clone();
                        }
                    }
                    KitRoomDataManager.get().setKitRoom(finalPage, data);
                    BroadcastManager.get().sendComponentMessage(player,
                            StyleManager.parseComponent("<#00FF00>Saved kitroom page: " + (finalPage + 1)));
                    SoundManager.playSuccess(player);
                }
            });
        }
        addKitRoom(menu.getSlot(47), 0);
        addKitRoom(menu.getSlot(48), 1);
        addKitRoom(menu.getSlot(49), 2);
        addKitRoom(menu.getSlot(50), 3);
        addKitRoom(menu.getSlot(51), 4);

        for (int i = 1; i < 6; i++) {
            menu.getSlot(46 + i).setItem(addHideFlags(createItem(
                    Material.valueOf(plugin.getConfig().getString("kitroom.items." + i + ".material")),
                    "<reset>" + plugin.getConfig().getString("kitroom.items." + i + ".name"))));
        }

        menu.getSlot(page + 47).setItem(ItemUtil.addEnchantLook(menu.getSlot(page + 47).getItem(p)));

        menu.setCursorDropHandler(Menu.ALLOW_CURSOR_DROPPING);
        menu.open(p);
    }

    public Menu ViewPublicKitMenu(Player p, String id) {
        ItemStack[] kit = KitManager.get().getPublicKit(id);

        if (kit == null) {
            BroadcastManager.get().sendComponentMessage(p, StyleManager.parseComponent("<#FF4444>Kit not found!"));
            if (p.hasPermission("virtualkit.admin")) {
                BroadcastManager.get().sendComponentMessage(p,
                        StyleManager.parseComponent("<#FF4444>To assign a kit use /savepublickit <id>"));
            }
            return null;
        }
        Menu menu = ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Viewing Public Kit: " + id)
                .redraw(true).build();

        for (int i = 0; i < 54; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        for (int i = 9; i < 36; i++) {
            menu.getSlot(i).setItem(kit[i]);
        }
        for (int i = 0; i < 9; i++) {
            menu.getSlot(i + 36).setItem(kit[i]);
        }
        for (int i = 36; i < 41; i++) {
            menu.getSlot(i + 9).setItem(kit[i]);
        }

        menu.getSlot(45).setItem(createItem(Material.CHAINMAIL_BOOTS, 1, cfg().getKitEditorBootsLabel()));
        menu.getSlot(46).setItem(createItem(Material.CHAINMAIL_LEGGINGS, 1, cfg().getKitEditorLeggingsLabel()));
        menu.getSlot(47).setItem(createItem(Material.CHAINMAIL_CHESTPLATE, 1, cfg().getKitEditorChestplateLabel()));
        menu.getSlot(48).setItem(createItem(Material.CHAINMAIL_HELMET, 1, cfg().getKitEditorHelmetLabel()));
        menu.getSlot(49).setItem(createItem(Material.SHIELD, 1, cfg().getKitEditorOffhandLabel()));

        menu.getSlot(52)
                .setItem(createItem(cfg().getPublicKitsLoadButtonMaterial(), 1, cfg().getPublicKitsLoadButtonName()));
        menu.getSlot(53)
                .setItem(createItem(cfg().getKitEditorBackButtonMaterial(), 1, cfg().getKitEditorBackButtonName()));
        addMainButton(menu.getSlot(53));
        addLoadPublicKit(menu.getSlot(52), id);

        menu.open(p);

        return menu;
    }

    public void OpenPublicKitMenu(Player player) {
        int rows = cfg().getPublicKitsRows();
        Menu menu = ChestMenu.builder(rows).title(StyleManager.get().getPrimaryColor() + "Public Kit Room").redraw(true)
                .build();

        // Fill with glass panes
        for (int i = 0; i < rows * 9; i++) {
            menu.getSlot(i).setItem(ItemUtil.createGlassPane());
        }

        // Get configurable kit slots
        int[] kitSlots = cfg().getPublicKitsKitSlots();
        java.util.List<PublicKit> publicKitList = KitManager.get().getPublicKitList();
        String unassignedTag = cfg().getPublicKitsUnassignedTag();
        java.util.List<String> unassignedLore = cfg().getPublicKitsUnassignedLore();
        String adminEditLore = cfg().getPublicKitsAdminEditLore();

        // Place each public kit at its configured slot position
        for (int i = 0; i < publicKitList.size() && i < kitSlots.length; i++) {
            int slot = kitSlots[i];
            if (KitManager.get().hasPublicKit(publicKitList.get(i).id)) {
                if (player.hasPermission("virtualkit.admin")) {
                    menu.getSlot(slot).setItem(createItem(publicKitList.get(i).icon, 1,
                            "<reset>" + publicKitList.get(i).name, adminEditLore));
                } else {
                    menu.getSlot(slot)
                            .setItem(createItem(publicKitList.get(i).icon, 1, "<reset>" + publicKitList.get(i).name));
                }
                addPublicKitButton(menu.getSlot(slot), publicKitList.get(i).id);
            } else {
                String[] loreArray;
                if (player.hasPermission("virtualkit.admin")) {
                    if (!unassignedLore.isEmpty()) {
                        String[] baseLore = unassignedLore.toArray(new String[0]);
                        loreArray = new String[baseLore.length + 1];
                        System.arraycopy(baseLore, 0, loreArray, 0, baseLore.length);
                        loreArray[baseLore.length] = adminEditLore;
                    } else {
                        loreArray = new String[] { "<gray>Admins have not yet setup this kit", adminEditLore };
                    }
                } else {
                    loreArray = !unassignedLore.isEmpty() ? unassignedLore.toArray(new String[0])
                            : new String[] { "<gray>Admins have not yet setup this kit" };
                }
                menu.getSlot(slot).setItem(createItem(publicKitList.get(i).icon, 1,
                        "<reset>" + publicKitList.get(i).name + " " + unassignedTag, loreArray));
            }

            if (player.hasPermission("virtualkit.admin")) {
                addAdminPublicKitButton(menu.getSlot(slot), publicKitList.get(i).id);
            }
        }

        // Fill remaining kit slots with "coming soon" books
        for (int i = publicKitList.size(); i < kitSlots.length; i++) {
            int slot = kitSlots[i];
            menu.getSlot(slot).setItem(ItemUtil.createItem(cfg().getPublicKitsComingSoonMaterial(), 1,
                    cfg().getPublicKitsComingSoonName()));
        }
        // Close/back button - configurable position
        int closeSlot = cfg().getPublicKitsCloseButtonSlot();
        addMainButton(menu.getSlot(closeSlot));
        menu.getSlot(closeSlot)
                .setItem(createItem(cfg().getKitEditorBackButtonMaterial(), 1, cfg().getKitEditorBackButtonName()));
        menu.open(player);
    }

    public void addClear(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                Menu m = info.getClickedMenu();
                for (int i = 0; i < 41; i++) {
                    m.getSlot(i).setItem((org.bukkit.inventory.ItemStack) null);
                }
            }
        });
    }

    public void addClear(Slot slot, int start, int end) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                Menu m = info.getClickedMenu();
                for (int i = start; i < end; i++) {
                    m.getSlot(i).setItem((org.bukkit.inventory.ItemStack) null);
                }
            }
        });
    }

    public void addClearKit(Slot slot, UUID target, int slotNum) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                KitManager.get().deleteKit(target, slotNum);
                BroadcastManager.get().sendComponentMessage(player,
                        StyleManager.parseComponent("<#00FF00>Kit " + slotNum + " deleted!"));
                SoundManager.playSuccess(player);
                kitDeletionFlag.add(player.getUniqueId());
                info.getClickedMenu().close();
                SoundManager.playCloseGui(player);
            }
        });
    }

    public void addClearEnderchest(Slot slot, UUID target, int slotNum) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                KitManager.get().deleteEnderchest(target, slotNum);
                BroadcastManager.get().sendComponentMessage(player,
                        StyleManager.parseComponent("<#FF69B4>Ender Chest " + slotNum + " deleted!"));
                SoundManager.playSuccess(player);
                kitDeletionFlag.add(player.getUniqueId());
                info.getClickedMenu().close();
                SoundManager.playCloseGui(player);
            }
        });
    }

    public void addPublicKitButton(Slot slot, String id) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT) {
                KitManager.get().loadPublicKit(player, id);
                info.getClickedMenu().close();
            } else if (info.getClickType() == ClickType.RIGHT) {
                Menu m = ViewPublicKitMenu(player, id);
                if (m != null) {
                    m.open(player);
                }
            }
        });
    }

    public void addAdminPublicKitButton(Slot slot, String id) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                OpenPublicKitEditor(player, id);
                return;
            }
            if (info.getClickType() == ClickType.LEFT) {
                KitManager.get().loadPublicKit(player, id);
            } else if (info.getClickType() == ClickType.RIGHT) {
                Menu m = ViewPublicKitMenu(player, id);
                if (m != null) {
                    m.open(player);
                }
            }
        });
    }

    public void addMainButton(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenMainMenu(player);
        });
    }

    public void addCloseButton(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            info.getClickedMenu().close();
            SoundManager.playCloseGui(player);
        });
    }

    public void addKitRoom(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenKitRoom(player);
            BroadcastManager.get().broadcastPlayerOpenedKitRoom(player);
        });
    }

    public void addKitRoom(Slot slot, int page) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenKitRoom(player, page);
        });
    }

    // NEW: Load single premade kit directly (kit1)
    public void addLoadPremadeKit(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            // Load the first public kit (kit1) directly
            java.util.List<PublicKit> publicKitList = KitManager.get().getPublicKitList();
            if (!publicKitList.isEmpty() && KitManager.get().hasPublicKit(publicKitList.get(0).id)) {
                KitManager.get().loadPublicKit(player, publicKitList.get(0).id);
                info.getClickedMenu().close();
            } else {
                BroadcastManager.get().sendComponentMessage(player,
                        StyleManager.parseComponent("<#FF4444>No premade kit available!"));
                SoundManager.playFailure(player);
            }
        });
    }

    public void addPublicKitMenu(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            OpenPublicKitMenu(player);
        });
    }

    public void addKitRoomSaveButton(Slot slot, int page) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isRightClick() && info.getClickType().isShiftClick()) {
                ItemStack[] data = new ItemStack[45];
                for (int i = 0; i < 41; i++) {
                    data[i] = player.getInventory().getContents()[i];
                }
                KitRoomDataManager.get().setKitRoom(page, data);
                BroadcastManager.get().sendComponentMessage(player,
                        StyleManager.parseComponent("<#00FF00>Saved kitroom page: " + (page + 1)));
                SoundManager.playSuccess(player);
            }
        });
    }

    public void addRepairButton(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            BroadcastManager.get().broadcastPlayerRepaired(player);
            PlayerUtil.repairAll(player);
            player.updateInventory();
            SoundManager.playSuccess(player);
        });
    }

    public void addClearButton(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isShiftClick()) {
                player.getInventory().clear();
                BroadcastManager.get().sendComponentMessage(player,
                        StyleManager.parseComponent("<#00FF00>Inventory cleared!"));
                SoundManager.playSuccess(player);
            }
        });
    }

    public void addImport(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            Menu m = info.getClickedMenu();
            ItemStack[] inv;
            if (filterItemsOnImport) {
                inv = ItemFilter.get().filterItemStack(player.getInventory().getContents());
            } else {
                inv = player.getInventory().getContents();
            }
            for (int i = 0; i < 41; i++) {
                m.getSlot(i).setItem(inv[i]);
            }
        });
    }

    public void addImportEC(Slot slot) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            Menu m = info.getClickedMenu();
            ItemStack[] inv;
            if (filterItemsOnImport) {
                inv = ItemFilter.get().filterItemStack(player.getEnderChest().getContents());
            } else {
                inv = player.getEnderChest().getContents();
            }
            for (int i = 0; i < 27; i++) {
                m.getSlot(i + 9).setItem(inv[i]);
            }
        });
    }

    public void addEdit(Slot slot, int i) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isLeftClick() || info.getClickType().isRightClick()) {
                OpenKitMenu(player, i);
            }
        });
    }

    public void addEditEC(Slot slot, int i) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType().isLeftClick() || info.getClickType().isRightClick()) {
                OpenECKitKenu(player, i);
            }
        });
    }

    public void addLoad(Slot slot, int i) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT || info.getClickType() == ClickType.SHIFT_LEFT) {
                KitManager.get().loadKit(player, i);
                info.getClickedMenu().close();
                SoundManager.playCloseGui(player);
            }
        });
    }

    public void addEditLoad(Slot slot, int i) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT || info.getClickType() == ClickType.SHIFT_LEFT) {
                KitManager.get().loadKit(player, i);
                info.getClickedMenu().close();
            } else if (info.getClickType() == ClickType.RIGHT || info.getClickType() == ClickType.SHIFT_RIGHT) {
                OpenKitMenu(player, i);
            }
        });
    }

    public void addEditLoadEC(Slot slot, int i) {
        slot.setClickHandler((player, info) -> {
            SoundManager.playClick(player);
            if (info.getClickType() == ClickType.LEFT || info.getClickType() == ClickType.SHIFT_LEFT) {
                KitManager.get().loadEnderchest(player, i);
                info.getClickedMenu().close();
            } else if (info.getClickType() == ClickType.RIGHT || info.getClickType() == ClickType.SHIFT_RIGHT) {
                OpenECKitKenu(player, i);
            }
        });
    }

    public Menu createKitMenu(int slot) {
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Kit: " + slot).build();
    }

    public Menu createPublicKitMenu(String id) {
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Public Kit: " + id).build();
    }

    public Menu createECMenu(int slot) {
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Ender Chest: " + slot).build();
    }

    public Menu createInspectMenu(int slot, String playerName) {
        return ChestMenu.builder(6)
                .title(StyleManager.get().getPrimaryColor() + "Inspecting " + playerName + "'s Kit " + slot).build();
    }

    public Menu createInspectEcMenu(int slot, String playerName) {
        return ChestMenu.builder(6)
                .title(StyleManager.get().getPrimaryColor() + "Inspecting " + playerName + "'s Ender Chest " + slot)
                .build();
    }

    public Menu createMainMenu(Player p) {
        int rows = cfg().getMainMenuRows();
        return ChestMenu.builder(rows).title(StyleManager.get().getPrimaryColor() + p.getName() + "'s Kits").build();
    }

    public Menu createKitRoom() {
        return ChestMenu.builder(6).title(StyleManager.get().getPrimaryColor() + "Kit Room").redraw(true).build();
    }

    public void allowModification(Slot slot) {
        ClickOptions options = ClickOptions.ALLOW_ALL;
        slot.setClickOptions(options);
    }

    private String getPlayerName(UUID uuid) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        return name != null ? name : uuid.toString();
    }
}
