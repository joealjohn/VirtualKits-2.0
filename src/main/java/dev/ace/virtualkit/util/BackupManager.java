package dev.ace.virtualkit.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Simplified backup system.
 *
 * <p>Creates a single {@code .zip} file per backup run containing all relevant
 * data files (database.db, kits-data.yml, config.yml, etc.). Old zips are
 * deleted automatically so only the configured number of backups is kept on disk.
 *
 * <p>Config keys (under {@code backup:}):
 * <ul>
 *   <li>{@code enabled}           – master toggle (default: true)</li>
 *   <li>{@code interval-minutes}  – how often to back up (default: 60)</li>
 *   <li>{@code max-backups}       – maximum zip files to keep (default: 5)</li>
 * </ul>
 */
public class BackupManager {

    private static BackupManager instance;
    private final Plugin plugin;
    private final File backupDir;
    private final boolean enabled;
    private final int intervalMinutes;
    private final int maxBackups;

    private BukkitTask backupTask;

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public BackupManager(Plugin plugin) {
        this.plugin = plugin;
        this.enabled         = plugin.getConfig().getBoolean("backup.enabled", true);
        this.intervalMinutes = plugin.getConfig().getInt("backup.interval-minutes", 60);
        this.maxBackups      = plugin.getConfig().getInt("backup.max-backups", 5);
        this.backupDir       = new File(plugin.getDataFolder(), "backups");

        if (enabled) {
            initDirectory();
            scheduleBackup();
        }

        instance = this;
    }

    public static BackupManager get() {
        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public File getBackupDirectory() {
        return backupDir;
    }

    // ─── Scheduling ─────────────────────────────────────────────────────────────

    private void initDirectory() {
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            plugin.getLogger().warning("[Backup] Could not create backup directory: "
                    + backupDir.getAbsolutePath());
        }
    }

    private void scheduleBackup() {
        long intervalTicks = intervalMinutes * 60L * 20L;
        backupTask = new BukkitRunnable() {
            @Override
            public void run() {
                runBackup();
            }
        }.runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);

        plugin.getLogger().info("[Backup] Scheduled every " + intervalMinutes
                + " min, keeping last " + maxBackups + " backup(s).");
    }

    // ─── Core backup logic ───────────────────────────────────────────────────────

    /**
     * Runs a backup immediately (async-safe – call from an async thread or use
     * {@link #performManualBackup()} to schedule it).
     */
    public void runBackup() {
        if (!enabled) return;

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        File zipFile = new File(backupDir, "backup_" + timestamp + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {

            // Files to include in the zip
            zipIfExists(zos, "database.db");
            zipIfExists(zos, "database.db-wal");
            zipIfExists(zos, "database.db-shm");
            zipIfExists(zos, "kits-data.yml");
            zipIfExists(zos, "please-use-a-real-database.yml"); // legacy name
            zipIfExists(zos, "config.yml");
            zipIfExists(zos, "messages.yml");

        } catch (IOException e) {
            plugin.getLogger().severe("[Backup] Failed to create backup zip: " + e.getMessage());
            // Clean up an incomplete zip
            if (zipFile.exists()) zipFile.delete();
            return;
        }

        plugin.getLogger().info("[Backup] Created: " + zipFile.getName());
        pruneOldBackups();
    }

    /** Add a file from the plugin data folder into the zip, if it exists. */
    private void zipIfExists(ZipOutputStream zos, String filename) throws IOException {
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists() || !file.isFile()) return;

        try (FileInputStream fis = new FileInputStream(file)) {
            zos.putNextEntry(new ZipEntry(filename));
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
        }
    }

    /** Delete the oldest backup zips so only {@code maxBackups} remain. */
    private void pruneOldBackups() {
        File[] zips = backupDir.listFiles(
                f -> f.isFile() && f.getName().startsWith("backup_") && f.getName().endsWith(".zip"));

        if (zips == null || zips.length <= maxBackups) return;

        // Sort oldest-first
        Arrays.sort(zips, Comparator.comparingLong(File::lastModified));

        int toDelete = zips.length - maxBackups;
        int deleted = 0;
        for (int i = 0; i < toDelete; i++) {
            if (zips[i].delete()) {
                deleted++;
            } else {
                plugin.getLogger().warning("[Backup] Could not delete old backup: " + zips[i].getName());
            }
        }

        if (deleted > 0) {
            plugin.getLogger().info("[Backup] Pruned " + deleted
                    + " old backup(s) (keeping " + maxBackups + ").");
        }
    }

    // ─── Public API ──────────────────────────────────────────────────────────────

    /** Trigger an immediate backup from any thread. */
    public void performManualBackup() {
        if (!enabled) {
            plugin.getLogger().warning("[Backup] Backups are disabled in config.");
            return;
        }
        plugin.getLogger().info("[Backup] Manual backup triggered...");
        new BukkitRunnable() {
            @Override
            public void run() {
                runBackup();
            }
        }.runTaskAsynchronously(plugin);
    }

    /** Returns the number of backup zip files currently stored. */
    public int getBackupCount() {
        if (!backupDir.exists()) return 0;
        File[] zips = backupDir.listFiles(
                f -> f.isFile() && f.getName().startsWith("backup_") && f.getName().endsWith(".zip"));
        return zips == null ? 0 : zips.length;
    }

    /** Cancel scheduled tasks on plugin disable. */
    public void shutdown() {
        if (backupTask != null) backupTask.cancel();
        if (enabled) plugin.getLogger().info("[Backup] Shutdown complete.");
    }
}
