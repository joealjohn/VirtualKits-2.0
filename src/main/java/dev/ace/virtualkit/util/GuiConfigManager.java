package dev.ace.virtualkit.util;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

/**
 * Manages GUI configuration files from the gui/ folder.
 * Each GUI has its own separate YAML file for easy customization.
 */
public class GuiConfigManager {
    private static GuiConfigManager instance;
    private final Plugin plugin;

    private FileConfiguration mainMenuConfig;
    private FileConfiguration kitEditorConfig;
    private FileConfiguration kitRoomConfig;
    private FileConfiguration publicKitsConfig;
    private FileConfiguration inspectConfig;

    public GuiConfigManager(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
        loadConfigs();
    }

    public static GuiConfigManager get() {
        return instance;
    }

    public void loadConfigs() {
        mainMenuConfig = loadConfig("gui/main-menu.yml");
        kitEditorConfig = loadConfig("gui/kit-editor.yml");
        kitRoomConfig = loadConfig("gui/kit-room.yml");
        publicKitsConfig = loadConfig("gui/public-kits.yml");
        inspectConfig = loadConfig("gui/inspect.yml");
    }

    public void reload() {
        loadConfigs();
    }

    private FileConfiguration loadConfig(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(resourcePath, false);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    // MAIN MENU LAYOUT
    public int getMainMenuRows() {
        return mainMenuConfig.getInt("layout.rows", 6);
    }

    /**
     * Get kit slot positions from comma-separated string
     * 
     * @return array of slot positions
     */
    public int[] getMainMenuKitSlots() {
        String slotsStr = mainMenuConfig.getString("layout.kit-slots", "9,10,11,12,13,14,15,16,17");
        return parseSlots(slotsStr);
    }

    /**
     * Get EC slot positions from comma-separated string
     * 
     * @return array of slot positions
     */
    public int[] getMainMenuEcSlots() {
        String slotsStr = mainMenuConfig.getString("layout.ec-slots", "18,19,20,21,22,23,24,25,26");
        return parseSlots(slotsStr);
    }

    private int[] parseSlots(String slotsStr) {
        String[] parts = slotsStr.split(",");
        int[] slots = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                slots[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                slots[i] = 0;
            }
        }
        return slots;
    }

    public int getMainMenuKitRoomSlot() {
        return mainMenuConfig.getInt("layout.buttons.kit-room", 37);
    }

    public int getMainMenuPremadeKitsSlot() {
        return mainMenuConfig.getInt("layout.buttons.premade-kits", 38);
    }

    public int getMainMenuInfoSlot() {
        return mainMenuConfig.getInt("layout.buttons.info", 39);
    }

    public int getMainMenuClearInventorySlot() {
        return mainMenuConfig.getInt("layout.buttons.clear-inventory", 41);
    }

    public int getMainMenuShareKitsSlot() {
        return mainMenuConfig.getInt("layout.buttons.share-kits", 42);
    }

    public int getMainMenuRepairItemsSlot() {
        return mainMenuConfig.getInt("layout.buttons.repair-items", 43);
    }

    // MAIN MENU
    public String getMainMenuKitSlotName(int slot) {
        return mainMenuConfig.getString("kit-slot.name", "<#00FF00><bold>Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º %slot%").replace("%slot%",
                String.valueOf(slot));
    }

    public Material getMainMenuKitSlotMaterial() {
        return getMaterial(mainMenuConfig, "kit-slot.material", Material.CHEST);
    }

    public List<String> getMainMenuKitSlotLore() {
        return mainMenuConfig.getStringList("kit-slot.lore");
    }

    public String getMainMenuEcSlotName(int slot) {
        return mainMenuConfig
                .getString("enderchest-slot.name",
                        "<#FF69B4><bold>Ã¡Â´â€¡Ã‰Â´Ã¡Â´â€¦Ã¡Â´â€¡ÃŠâ‚¬Ã¡Â´â€žÃŠÅ“Ã¡Â´â€¡ÃªÅ“Â±Ã¡Â´â€º %slot%")
                .replace("%slot%",
                        String.valueOf(slot));
    }

    public Material getMainMenuEcSlotMaterial() {
        return getMaterial(mainMenuConfig, "enderchest-slot.material", Material.ENDER_CHEST);
    }

    public List<String> getMainMenuEcSlotLore() {
        return mainMenuConfig.getStringList("enderchest-slot.lore");
    }

    public String getMainMenuEcCreateName(int slot) {
        return mainMenuConfig
                .getString("enderchest-create.name",
                        "<#FF69B4><bold>Ã¡Â´â€¡Ã‰Â´Ã¡Â´â€¦Ã¡Â´â€¡ÃŠâ‚¬Ã¡Â´â€žÃŠÅ“Ã¡Â´â€¡ÃªÅ“Â±Ã¡Â´â€º %slot%")
                .replace("%slot%",
                        String.valueOf(slot));
    }

    public Material getMainMenuEcCreateMaterial() {
        return getMaterial(mainMenuConfig, "enderchest-create.material", Material.ENDER_CHEST);
    }

    public List<String> getMainMenuEcCreateLore() {
        return mainMenuConfig.getStringList("enderchest-create.lore");
    }

    public String getMainMenuKitExistsName() {
        return mainMenuConfig.getString("kit-exists.name",
                "<#00FF00><bold>Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º Ã¡Â´â€¡xÃ‰ÂªÃªÅ“Â±Ã¡Â´â€ºÃªÅ“Â±");
    }

    public Material getMainMenuKitExistsMaterial() {
        return getMaterial(mainMenuConfig, "kit-exists.material", Material.KNOWLEDGE_BOOK);
    }

    public List<String> getMainMenuKitExistsLore() {
        return mainMenuConfig.getStringList("kit-exists.lore");
    }

    public String getMainMenuKitNotFoundName() {
        return mainMenuConfig.getString("kit-not-found.name",
                "<#FF4444><bold>Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º Ã‰Â´Ã¡Â´ÂÃ¡Â´â€º ÃªÅ“Â°Ã¡Â´ÂÃ¡Â´Å“Ã‰Â´Ã¡Â´â€¦");
    }

    public Material getMainMenuKitNotFoundMaterial() {
        return getMaterial(mainMenuConfig, "kit-not-found.material", Material.BOOK);
    }

    public List<String> getMainMenuKitNotFoundLore() {
        return mainMenuConfig.getStringList("kit-not-found.lore");
    }

    public String getMainMenuKitRoomButtonName() {
        return mainMenuConfig.getString("kit-room-button.name",
                "<#00FF00><bold>Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º ÃŠâ‚¬Ã¡Â´ÂÃ¡Â´ÂÃ¡Â´Â");
    }

    public Material getMainMenuKitRoomButtonMaterial() {
        return getMaterial(mainMenuConfig, "kit-room-button.material", Material.NETHER_STAR);
    }

    public String getMainMenuPremadeKitsButtonName() {
        return mainMenuConfig.getString("premade-kits-button.name",
                "<#FFD700><bold>Ã¡Â´ËœÃŠâ‚¬Ã¡Â´â€¡Ã¡Â´ÂÃ¡Â´â‚¬Ã¡Â´â€¦Ã¡Â´â€¡ Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€ºÃªÅ“Â±");
    }

    public Material getMainMenuPremadeKitsButtonMaterial() {
        return getMaterial(mainMenuConfig, "premade-kits-button.material", Material.BOOKSHELF);
    }

    public String getMainMenuInfoButtonName() {
        return mainMenuConfig.getString("info-button.name", "<#00FF00><bold>Ã‰ÂªÃ‰Â´ÃªÅ“Â°Ã¡Â´Â");
    }

    public Material getMainMenuInfoButtonMaterial() {
        return getMaterial(mainMenuConfig, "info-button.material", Material.OAK_SIGN);
    }

    public List<String> getMainMenuInfoButtonLore() {
        return mainMenuConfig.getStringList("info-button.lore");
    }

    public String getMainMenuClearInvButtonName() {
        return mainMenuConfig.getString("clear-inventory-button.name",
                "<#FF4444><bold>Ã¡Â´â€žÃŠÅ¸Ã¡Â´â€¡Ã¡Â´â‚¬ÃŠâ‚¬ Ã‰ÂªÃ‰Â´Ã¡Â´Â Ã¡Â´â€¡Ã‰Â´Ã¡Â´â€ºÃ¡Â´ÂÃŠâ‚¬ÃŠÂ");
    }

    public Material getMainMenuClearInvButtonMaterial() {
        return getMaterial(mainMenuConfig, "clear-inventory-button.material", Material.REDSTONE_BLOCK);
    }

    public List<String> getMainMenuClearInvButtonLore() {
        return mainMenuConfig.getStringList("clear-inventory-button.lore");
    }

    public String getMainMenuShareKitsButtonName() {
        return mainMenuConfig.getString("share-kits-button.name",
                "<#00FF00><bold>ÃªÅ“Â±ÃŠÅ“Ã¡Â´â‚¬ÃŠâ‚¬Ã¡Â´â€¡ Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€ºÃªÅ“Â±");
    }

    public Material getMainMenuShareKitsButtonMaterial() {
        return getMaterial(mainMenuConfig, "share-kits-button.material", Material.COMPASS);
    }

    public List<String> getMainMenuShareKitsButtonLore() {
        return mainMenuConfig.getStringList("share-kits-button.lore");
    }

    public String getMainMenuRepairButtonName() {
        return mainMenuConfig.getString("repair-items-button.name",
                "<#00FF00><bold>ÃŠâ‚¬Ã¡Â´â€¡Ã¡Â´ËœÃ¡Â´â‚¬Ã‰ÂªÃŠâ‚¬ Ã‰ÂªÃ¡Â´â€ºÃ¡Â´â€¡Ã¡Â´ÂÃªÅ“Â±");
    }

    public Material getMainMenuRepairButtonMaterial() {
        return getMaterial(mainMenuConfig, "repair-items-button.material", Material.EXPERIENCE_BOTTLE);
    }

    // KIT EDITOR
    public String getKitEditorBootsLabel() {
        return kitEditorConfig.getString("boots-label.name", "<gray><bold>ÃŠâ„¢Ã¡Â´ÂÃ¡Â´ÂÃ¡Â´â€ºÃªÅ“Â±");
    }

    public String getKitEditorLeggingsLabel() {
        return kitEditorConfig.getString("leggings-label.name", "<gray><bold>ÃŠÅ¸Ã¡Â´â€¡Ã‰Â¢Ã‰Â¢Ã‰ÂªÃ‰Â´Ã‰Â¢ÃªÅ“Â±");
    }

    public String getKitEditorChestplateLabel() {
        return kitEditorConfig.getString("chestplate-label.name",
                "<gray><bold>Ã¡Â´â€žÃŠÅ“Ã¡Â´â€¡ÃªÅ“Â±Ã¡Â´â€ºÃ¡Â´ËœÃŠÅ¸Ã¡Â´â‚¬Ã¡Â´â€ºÃ¡Â´â€¡");
    }

    public String getKitEditorHelmetLabel() {
        return kitEditorConfig.getString("helmet-label.name", "<gray><bold>ÃŠÅ“Ã¡Â´â€¡ÃŠÅ¸Ã¡Â´ÂÃ¡Â´â€¡Ã¡Â´â€º");
    }

    public String getKitEditorOffhandLabel() {
        return kitEditorConfig.getString("offhand-label.name", "<gray><bold>Ã¡Â´ÂÃªÅ“Â°ÃªÅ“Â°ÃŠÅ“Ã¡Â´â‚¬Ã‰Â´Ã¡Â´â€¦");
    }

    public String getKitEditorImportButtonName() {
        return kitEditorConfig.getString("import-button.name", "<#00FF00><bold>Ã‰ÂªÃ¡Â´ÂÃ¡Â´ËœÃ¡Â´ÂÃŠâ‚¬Ã¡Â´â€º");
    }

    public Material getKitEditorImportButtonMaterial() {
        return getMaterial(kitEditorConfig, "import-button.material", Material.CHEST);
    }

    public List<String> getKitEditorImportButtonLore() {
        return kitEditorConfig.getStringList("import-button.lore");
    }

    public String getKitEditorImportEcButtonName() {
        return kitEditorConfig.getString("import-ec-button.name", "<#FF69B4><bold>Ã‰ÂªÃ¡Â´ÂÃ¡Â´ËœÃ¡Â´ÂÃŠâ‚¬Ã¡Â´â€º");
    }

    public Material getKitEditorImportEcButtonMaterial() {
        return getMaterial(kitEditorConfig, "import-ec-button.material", Material.ENDER_CHEST);
    }

    public List<String> getKitEditorImportEcButtonLore() {
        return kitEditorConfig.getStringList("import-ec-button.lore");
    }

    public String getKitEditorClearButtonName() {
        return kitEditorConfig.getString("clear-button.name",
                "<#FF4444><bold>Ã¡Â´â€žÃŠÅ¸Ã¡Â´â€¡Ã¡Â´â‚¬ÃŠâ‚¬ Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º");
    }

    public Material getKitEditorClearButtonMaterial() {
        return getMaterial(kitEditorConfig, "clear-button.material", Material.BARRIER);
    }

    public List<String> getKitEditorClearButtonLore() {
        return kitEditorConfig.getStringList("clear-button.lore");
    }

    public String getKitEditorBackButtonName() {
        return kitEditorConfig.getString("back-button.name", "<#FF4444><bold>ÃŠâ„¢Ã¡Â´â‚¬Ã¡Â´â€žÃ¡Â´â€¹");
    }

    public Material getKitEditorBackButtonMaterial() {
        return getMaterial(kitEditorConfig, "back-button.material", Material.OAK_DOOR);
    }

    public String getKitEditorCloseButtonName() {
        return kitEditorConfig.getString("close-button.name", "<#FF4444><bold>Ã¡Â´â€žÃŠÅ¸Ã¡Â´ÂÃªÅ“Â±Ã¡Â´â€¡");
    }

    public Material getKitEditorCloseButtonMaterial() {
        return getMaterial(kitEditorConfig, "close-button.material", Material.OAK_DOOR);
    }

    // KIT ROOM
    public String getKitRoomRefillButtonName() {
        return kitRoomConfig.getString("refill-button.name", "<#00FF00><bold>ÃŠâ‚¬Ã¡Â´â€¡ÃªÅ“Â°Ã‰ÂªÃŠÅ¸ÃŠÅ¸");
    }

    public Material getKitRoomRefillButtonMaterial() {
        return getMaterial(kitRoomConfig, "refill-button.material", Material.BEACON);
    }

    public String getKitRoomEditMenuButtonName() {
        return kitRoomConfig.getString("edit-menu-button.name",
                "<#FF4444><bold>Ã¡Â´â€¡Ã¡Â´â€¦Ã‰ÂªÃ¡Â´â€º Ã¡Â´ÂÃ¡Â´â€¡Ã‰Â´Ã¡Â´Å“");
    }

    public Material getKitRoomEditMenuButtonMaterial() {
        return getMaterial(kitRoomConfig, "edit-menu-button.material", Material.BARRIER);
    }

    public List<String> getKitRoomEditMenuButtonLore() {
        return kitRoomConfig.getStringList("edit-menu-button.lore");
    }

    // PUBLIC KITS
    public String getPublicKitsComingSoonName() {
        return publicKitsConfig.getString("coming-soon.name",
                "<gray><bold>Ã¡Â´ÂÃ¡Â´ÂÃŠâ‚¬Ã¡Â´â€¡ Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€ºÃªÅ“Â± Ã¡Â´â€žÃ¡Â´ÂÃ¡Â´ÂÃ‰ÂªÃ‰Â´Ã‰Â¢ ÃªÅ“Â±Ã¡Â´ÂÃ¡Â´ÂÃ‰Â´");
    }

    public Material getPublicKitsComingSoonMaterial() {
        return getMaterial(publicKitsConfig, "coming-soon.material", Material.BOOK);
    }

    public String getPublicKitsUnassignedTag() {
        return publicKitsConfig.getString("unassigned-tag",
                "<#FF4444><bold>[Ã¡Â´Å“Ã‰Â´Ã¡Â´â‚¬ÃªÅ“Â±ÃªÅ“Â±Ã‰ÂªÃ‰Â¢Ã‰Â´Ã¡Â´â€¡Ã¡Â´â€¦]");
    }

    public List<String> getPublicKitsUnassignedLore() {
        return publicKitsConfig.getStringList("unassigned-lore");
    }

    public String getPublicKitsAdminEditLore() {
        return publicKitsConfig.getString("admin-edit-lore",
                "<gray>Ã¢â€”Â [Ã¡Â´â‚¬Ã¡Â´â€¦Ã¡Â´ÂÃ‰ÂªÃ‰Â´] ÃªÅ“Â±ÃŠÅ“Ã‰ÂªÃªÅ“Â°Ã¡Â´â€º Ã¡Â´â€žÃŠÅ¸Ã‰ÂªÃ¡Â´â€žÃ¡Â´â€¹ Ã¡Â´â€ºÃ¡Â´Â Ã¡Â´â€¡Ã¡Â´â€¦Ã‰ÂªÃ¡Â´â€º");
    }

    public int getPublicKitsRows() {
        return publicKitsConfig.getInt("layout.rows", 3);
    }

    public int[] getPublicKitsKitSlots() {
        String slotsStr = publicKitsConfig.getString("layout.kit-slots", "9,10,11,12,13,14,15,16,17");
        return parseSlots(slotsStr);
    }

    public int getPublicKitsCloseButtonSlot() {
        return publicKitsConfig.getInt("layout.close-button-slot", 22);
    }

    public String getPublicKitsLoadButtonName() {
        return publicKitsConfig.getString("load-button.name",
                "<#00FF00><bold>ÃŠÅ¸Ã¡Â´ÂÃ¡Â´â‚¬Ã¡Â´â€¦ Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º");
    }

    public Material getPublicKitsLoadButtonMaterial() {
        return getMaterial(publicKitsConfig, "load-button.material", Material.APPLE);
    }

    // INSPECT
    public String getInspectClearKitButtonName() {
        return inspectConfig.getString("clear-kit-button.name",
                "<#FF4444><bold>Ã¡Â´â€žÃŠÅ¸Ã¡Â´â€¡Ã¡Â´â‚¬ÃŠâ‚¬ Ã¡Â´â€¹Ã‰ÂªÃ¡Â´â€º");
    }

    public Material getInspectClearKitButtonMaterial() {
        return getMaterial(inspectConfig, "clear-kit-button.material", Material.BARRIER);
    }

    public List<String> getInspectClearKitButtonLore() {
        return inspectConfig.getStringList("clear-kit-button.lore");
    }

    public String getInspectClearEcButtonName() {
        return inspectConfig.getString("clear-ec-button.name",
                "<#FF69B4><bold>Ã¡Â´â€žÃŠÅ¸Ã¡Â´â€¡Ã¡Â´â‚¬ÃŠâ‚¬ Ã¡Â´â€¡Ã‰Â´Ã¡Â´â€¦Ã¡Â´â€¡ÃŠâ‚¬Ã¡Â´â€žÃŠÅ“Ã¡Â´â€¡ÃªÅ“Â±Ã¡Â´â€º");
    }

    public Material getInspectClearEcButtonMaterial() {
        return getMaterial(inspectConfig, "clear-ec-button.material", Material.BARRIER);
    }

    public List<String> getInspectClearEcButtonLore() {
        return inspectConfig.getStringList("clear-ec-button.lore");
    }

    private Material getMaterial(FileConfiguration config, String path, Material defaultMaterial) {
        String materialName = config.getString(path);
        if (materialName == null)
            return defaultMaterial;
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultMaterial;
        }
    }
}
