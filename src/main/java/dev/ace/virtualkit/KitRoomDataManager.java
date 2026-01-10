package dev.ace.virtualkit;

import dev.ace.virtualkit.gui.ItemUtil;
import dev.ace.virtualkit.util.IDUtil;
import dev.ace.virtualkit.util.Serializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Manages kit room data using SQL storage (SQLite/MySQL)
 */
public class KitRoomDataManager {

    private final ArrayList<ItemStack[]> kitroomData;
    private final Plugin plugin;
    private static KitRoomDataManager instance;

    public KitRoomDataManager(Plugin plugin) {
        this.plugin = plugin;
        kitroomData = new ArrayList<>();

        // Initialize with 5 empty pages
        for (int i = 0; i < 5; i++) {
            ItemStack[] emptyPage = new ItemStack[45];
            kitroomData.add(emptyPage);
        }

        instance = this;
        plugin.getLogger().info("KitRoomDataManager initialized with 5 empty pages");
    }

    public static KitRoomDataManager get() {
        if (instance == null) {
            throw new IllegalStateException("KitRoomDataManager has not been initialized yet!");
        }
        return instance;
    }

    public void setKitRoom(int page, ItemStack[] data) {
        // Clone the data to avoid reference issues
        ItemStack[] clonedData = new ItemStack[45];
        int itemCount = 0;
        for (int i = 0; i < Math.min(data.length, 45); i++) {
            if (data[i] != null && data[i].getType() != Material.AIR) {
                clonedData[i] = data[i].clone();
                itemCount++;
            }
        }

        kitroomData.set(page, clonedData);
        plugin.getLogger().info("[KitRoom] Set page " + page + " with " + itemCount + " items");

        // Update whitelist
        ItemFilter.get().clearWhitelist();
        ItemFilter.get().addToWhitelist(kitroomData);

        // Save to database immediately (sync to ensure it completes)
        final int finalPage = page;
        final ItemStack[] finalData = clonedData;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String output = Serializer.itemStackArrayToBase64(finalData);
                    if (output != null && !output.isEmpty()) {
                        String kitRoomId = IDUtil.getKitRoomId(finalPage);
                        VirtualKits.storageManager.saveKitDataByID(kitRoomId, output);
                        plugin.getLogger()
                                .info("[KitRoom] Saved page " + (finalPage + 1) + " to database with ID: " + kitRoomId);
                    } else {
                        plugin.getLogger().warning("[KitRoom] Failed to serialize page " + (finalPage + 1));
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("[KitRoom] Error saving page " + (finalPage + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public ItemStack[] getKitRoomPage(int page) {
        return kitroomData.get(page);
    }

    public void saveToDBAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    ItemStack[] pagedata = kitroomData.get(i);
                    String output = Serializer.itemStackArrayToBase64(pagedata);
                    VirtualKits.storageManager.saveKitDataByID(IDUtil.getKitRoomId(i), output);
                }
                plugin.getLogger().info("[KitRoom] Saved all 5 pages to database");
            }
        }.runTaskAsynchronously(plugin);
    }

    public void loadFromDB() {
        plugin.getLogger().info("[KitRoom] Loading kit room data from database...");
        ItemFilter.get().clearWhitelist();
        int totalLoaded = 0;

        for (int i = 0; i < 5; i++) {
            String kitRoomId = IDUtil.getKitRoomId(i);
            String input = VirtualKits.storageManager.getKitDataByID(kitRoomId);

            if (input != null && !input.isEmpty() && !input.equalsIgnoreCase("error")) {
                try {
                    ItemStack[] pagedata = Serializer.itemStackArrayFromBase64(input);
                    if (pagedata != null) {
                        kitroomData.set(i, pagedata);
                        int itemCount = 0;
                        for (ItemStack item : pagedata) {
                            if (item != null && item.getType() != Material.AIR) {
                                itemCount++;
                            }
                        }
                        plugin.getLogger().info("[KitRoom] Loaded page " + (i + 1) + " with " + itemCount + " items");
                        totalLoaded += itemCount;
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("[KitRoom] Failed to load page " + (i + 1) + ": " + e.getMessage());
                }
            } else {
                plugin.getLogger().info("[KitRoom] No saved data for page " + (i + 1));
            }
        }

        ItemFilter.get().addToWhitelist(kitroomData);
        plugin.getLogger().info("[KitRoom] Finished loading - total items: " + totalLoaded);
    }
}
