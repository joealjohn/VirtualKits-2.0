package dev.ace.virtualkit.storage;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML Storage Manager with folder-based structure:
 * - storage/players/{uuid}/kit_{slot}.yml
 * - storage/players/{uuid}/ec_{slot}.yml
 * - storage/kitroom/page_{num}.yml
 * - storage/premade/premade.yml
 */
public class YAMLStorage implements StorageManager {

    private final File storageFolder;
    private final File playersFolder;
    private final File kitroomFolder;
    private final File premadeFolder;
    private final Plugin plugin;
    private Map<String, String> cache;

    public YAMLStorage(Plugin plugin, String basePath) {
        this.plugin = plugin;
        this.storageFolder = new File(plugin.getDataFolder(), "storage");
        this.playersFolder = new File(storageFolder, "players");
        this.kitroomFolder = new File(storageFolder, "kitroom");
        this.premadeFolder = new File(storageFolder, "premade");
        this.cache = new HashMap<>();
    }

    @Override
    public void connect() {
        // Create folder structure
        storageFolder.mkdirs();
        playersFolder.mkdirs();
        kitroomFolder.mkdirs();
        premadeFolder.mkdirs();
    }

    @Override
    public boolean isConnected() {
        return storageFolder.exists();
    }

    @Override
    public void init() {
        connect();
        plugin.getLogger()
                .info("[YAMLStorage] Initialized with folder structure at: " + storageFolder.getAbsolutePath());
        plugin.getLogger().info("[YAMLStorage] Players folder: " + playersFolder.getAbsolutePath());
    }

    @Override
    public void close() {
        // Backup on close
        backupStorage();
        plugin.getLogger().info("[YAMLStorage] Storage closed.");
    }

    private void backupStorage() {
        // Check if backup is enabled in config
        if (!plugin.getConfig().getBoolean("backup.enabled", true)) {
            plugin.getLogger().info("[YAMLStorage] Backup is disabled in config.");
            return;
        }

        // Check if storage folder exists and has content
        if (!storageFolder.exists() || storageFolder.listFiles() == null || storageFolder.listFiles().length == 0) {
            plugin.getLogger().info("[YAMLStorage] No data to backup.");
            return;
        }

        try {
            File backupFolder = new File(plugin.getDataFolder(), "backup");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            // Create timestamped backup folder
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = sdf.format(new java.util.Date());
            File backupDest = new File(backupFolder, "storage_" + timestamp);

            // Copy storage folder
            copyDirectory(storageFolder, backupDest);
            plugin.getLogger().info("[YAMLStorage] Created backup: " + backupDest.getName());

            // Clean old backups (keep only last 3)
            cleanOldBackups(backupFolder);
        } catch (IOException e) {
            plugin.getLogger().warning("[YAMLStorage] Failed to create backup: " + e.getMessage());
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!source.exists())
            return;

        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    copyDirectory(new File(source, child), new File(target, child));
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void cleanOldBackups(File backupFolder) {
        File[] backups = backupFolder.listFiles(file -> file.isDirectory() && file.getName().startsWith("storage_"));
        if (backups != null && backups.length > 3) {
            java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
            for (int i = 0; i < backups.length - 3; i++) {
                deleteDirectory(backups[i]);
                plugin.getLogger().info("[YAMLStorage] Deleted old backup: " + backups[i].getName());
            }
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    @Override
    public void keepAlive() {
        // No-op for file storage
    }

    @Override
    public void saveKitDataByID(String kitID, String data) {
        cache.put(kitID, data);
        File file = getFileForID(kitID);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(data);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[YAMLStorage] Failed to save " + kitID + ": " + e.getMessage());
        }
    }

    @Override
    public String getKitDataByID(String kitID) {
        // Check cache first
        if (cache.containsKey(kitID)) {
            return cache.get(kitID);
        }

        // Load from file
        File file = getFileForID(kitID);
        if (!file.exists()) {
            return "error";
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String data = sb.toString();
            cache.put(kitID, data);
            return data;
        } catch (IOException e) {
            plugin.getLogger().warning("[YAMLStorage] Failed to load " + kitID + ": " + e.getMessage());
            return "error";
        }
    }

    @Override
    public boolean doesKitExistByID(String kitID) {
        if (cache.containsKey(kitID)) {
            return true;
        }
        return getFileForID(kitID).exists();
    }

    @Override
    public void deleteKitByID(String kitID) {
        cache.remove(kitID);
        File file = getFileForID(kitID);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Get file for a given kitID
     * Format:
     * - Player kits: {uuid}kit{slot} -> storage/players/{uuid}/kits/{slot}.dat
     * - Player EC: {uuid}ec{slot} -> storage/players/{uuid}/ec/{slot}.dat
     * - Kit room: kitroom{page} -> storage/kitroom/page_{page}.dat
     * - Public kit: public{id} -> storage/premade/{id}.dat
     */
    private File getFileForID(String kitID) {
        // Kit room: kitroomX
        if (kitID.startsWith("kitroom")) {
            String page = kitID.substring(7);
            return new File(kitroomFolder, "page_" + page + ".dat");
        }

        // Public/Premade kit: publicX
        if (kitID.startsWith("public")) {
            String id = kitID.substring(6);
            return new File(premadeFolder, id + ".dat");
        }

        // Player kit: {uuid}kit{slot}
        if (kitID.contains("kit")) {
            int kitIndex = kitID.indexOf("kit");
            String uuid = kitID.substring(0, kitIndex);
            String slot = kitID.substring(kitIndex + 3);
            File playerFolder = new File(playersFolder, uuid);
            File kitsFolder = new File(playerFolder, "kits");
            return new File(kitsFolder, slot + ".dat");
        }

        // Player EC: {uuid}ec{slot}
        if (kitID.contains("ec")) {
            int ecIndex = kitID.indexOf("ec");
            String uuid = kitID.substring(0, ecIndex);
            String slot = kitID.substring(ecIndex + 2);
            File playerFolder = new File(playersFolder, uuid);
            File ecFolder = new File(playerFolder, "ec");
            return new File(ecFolder, slot + ".dat");
        }

        // Fallback
        return new File(storageFolder, kitID + ".dat");
    }
}
