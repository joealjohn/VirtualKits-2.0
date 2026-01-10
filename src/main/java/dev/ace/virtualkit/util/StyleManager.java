package dev.ace.virtualkit.util;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * StyleManager handles all color and formatting conversions for VirtualKit.
 * Supports MiniMessage format including hex colors like <#00FF00>.
 */
public class StyleManager {
    private static StyleManager instance;
    private final Plugin plugin;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();

    private Material glassMaterial;
    private String titleColor;
    private String primaryColorRaw;

    public StyleManager(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
        loadConfig();
    }

    public static StyleManager get() {
        return instance;
    }

    public void loadConfig() {
        // Load glass material
        try {
            this.glassMaterial = Material.valueOf(
                    plugin.getConfig().getString("appearance.glass-material", "LIME_STAINED_GLASS_PANE"));
        } catch (IllegalArgumentException e) {
            this.glassMaterial = Material.LIME_STAINED_GLASS_PANE;
        }

        // Load primary color
        this.primaryColorRaw = plugin.getConfig().getString("appearance.primary-color", "<#00FF00>");
        this.titleColor = miniMessageToLegacy(primaryColorRaw);
    }

    /**
     * Converts MiniMessage format to legacy color codes for GUI titles.
     * Supports hex colors like <#00FF00> and named colors like <green>.
     */
    public static String miniMessageToLegacy(String miniMessageText) {
        if (miniMessageText == null || miniMessageText.isEmpty()) {
            return "\u00A7a"; // Default lime (section symbol + a)
        }
        try {
            Component component = miniMessage.deserialize(miniMessageText + "X");
            String result = legacySerializer.serialize(component);
            // Remove the test character
            if (result.endsWith("X")) {
                return result.substring(0, result.length() - 1);
            }
            return result;
        } catch (Exception e) {
            return "\u00A7a"; // Default to lime on error
        }
    }

    /**
     * Converts MiniMessage format to legacy color codes for item names/lore.
     * Supports hex colors like <#00FF00> and named colors like <green>.
     */
    public static String convertMiniMessage(String miniMessageText) {
        if (miniMessageText == null || miniMessageText.isEmpty()) {
            return "";
        }
        try {
            Component component = miniMessage.deserialize(miniMessageText);
            return legacySerializer.serialize(component);
        } catch (Exception e) {
            return miniMessageText;
        }
    }

    /**
     * Converts a list of MiniMessage strings to legacy format.
     */
    public static List<String> convertMiniMessageList(List<String> miniMessageList) {
        if (miniMessageList == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String line : miniMessageList) {
            result.add(convertMiniMessage(line));
        }
        return result;
    }

    /**
     * Gets a MiniMessage Component for use with Adventure API.
     */
    public static Component parseComponent(String miniMessageText) {
        if (miniMessageText == null || miniMessageText.isEmpty()) {
            return Component.empty();
        }
        try {
            return miniMessage.deserialize(miniMessageText);
        } catch (Exception e) {
            return Component.text(miniMessageText);
        }
    }

    /**
     * Gets an item name from the GUI config section.
     * Falls back to default if not found.
     */
    public String getGuiText(String path, String defaultValue) {
        return plugin.getConfig().getString("gui." + path, defaultValue);
    }

    /**
     * Gets item lore from the GUI config section.
     */
    public List<String> getGuiLore(String path) {
        return plugin.getConfig().getStringList("gui." + path);
    }

    /**
     * Gets a message from the messages config section.
     */
    public String getMessage(String path, String defaultValue) {
        return plugin.getConfig().getString("messages." + path, defaultValue);
    }

    /**
     * Gets a message with placeholder replacements.
     */
    public String getMessage(String path, String defaultValue, Object... replacements) {
        String message = getMessage(path, defaultValue);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            }
        }
        return message;
    }

    public Material getGlassMaterial() {
        return glassMaterial;
    }

    public String getPrimaryColor() {
        return titleColor;
    }

    public String getPrimaryColorRaw() {
        return primaryColorRaw;
    }
}
