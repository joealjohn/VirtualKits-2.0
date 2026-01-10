package dev.ace.virtualkit.storage;

import dev.ace.virtualkit.storage.sql.MySQL;
import dev.ace.virtualkit.storage.sql.SQLite;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class StorageSelector {

    private StorageManager storageManager;
    private Plugin plugin;

    public StorageSelector(Plugin plugin, String storageType) {

        this.plugin = plugin;

        switch (storageType.toLowerCase()) {

            case "yml":
            case "yaml":
                storageManager = new YAMLStorage(plugin,
                        new File(plugin.getDataFolder(), "kits-data.yml").getAbsolutePath());
                break;
            case "redis":
                storageManager = new RedisStorage(plugin);
                break;
            case "mysql":
                storageManager = new MySQLStorage(new MySQL(plugin));
                break;
            case "sqlite":
            default:
                // default to sqlite
                storageManager = new SQLiteStorage(new SQLite(plugin));
                break;
        }

    }

    public StorageManager getDbManager() {
        return storageManager;
    }

}
