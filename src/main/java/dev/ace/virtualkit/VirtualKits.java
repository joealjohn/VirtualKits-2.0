package dev.ace.virtualkit;

import dev.ace.virtualkit.commands.*;
import dev.ace.virtualkit.commands.extracommands.HealCommand;
import dev.ace.virtualkit.commands.extracommands.RepairCommand;
import dev.ace.virtualkit.commands.tabcompleters.KitSlotTabCompleter;
import dev.ace.virtualkit.listeners.*;
import dev.ace.virtualkit.listeners.antiexploit.CommandListener;
import dev.ace.virtualkit.listeners.antiexploit.ShulkerDropItemsListener;
import dev.ace.virtualkit.listeners.features.OldDeathDropListener;
import dev.ace.virtualkit.storage.StorageManager;
import dev.ace.virtualkit.storage.StorageSelector;
import dev.ace.virtualkit.storage.exceptions.StorageConnectionException;
import dev.ace.virtualkit.storage.exceptions.StorageOperationException;
import dev.ace.virtualkit.util.BackupManager;
import dev.ace.virtualkit.util.BroadcastManager;
import dev.ace.virtualkit.util.ECCooldownManager;
import dev.ace.virtualkit.util.GuiConfigManager;
import dev.ace.virtualkit.util.MessageManager;
import dev.ace.virtualkit.util.StyleManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.util.Collections;

public final class VirtualKits extends JavaPlugin {

    public static Plugin plugin;
    public static StorageManager storageManager;
    private BackupManager backupManager;

    public static Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        int bstatsId = 24380;
        Metrics metrics = new Metrics(this, bstatsId);

        plugin = this;
        ConfigManager configManager = new ConfigManager(this);
        configManager.loadConfig();

        new StyleManager(this);
        new GuiConfigManager(this);
        new MessageManager(this);

        new ItemFilter(this);
        new BroadcastManager(this);

        new KitManager(this);
        new KitShareManager(this);
        new KitRoomDataManager(this);

        loadPublicKitsIdsFromConfig();
        getLogger().info("Public Kit Configuration Loaded");

        String dbType = this.getConfig().getString("storage.type");

        if (dbType == null) {
            this.getLogger().warning("Database type not found in config, fix your config to continue!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storageManager = new StorageSelector(this, dbType).getDbManager();
        this.getLogger().info("Using storage type: " + storageManager.getClass().getName());

        if (storageManager == null) {
            this.getLogger().warning("Database error occurred, please check your config!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        attemptDatabaseConnection(true);

        try {
            storageManager.init();
        } catch (StorageOperationException e) {
            this.getLogger().warning("Failed to initialize the database. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize backup system for file-based storage methods
        if (isFileBasedStorage(dbType)) {
            backupManager = new BackupManager(this);
            if (backupManager.isEnabled()) {
                getLogger().info("Backup system initialized for file-based storage");
            } else {
                getLogger().info("Backup system disabled in configuration");
            }
        } else {
            getLogger().info("Backup system not needed for non-file-based storage: " + dbType);
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {

            if (storageManager.isConnected()) {
                try {
                    storageManager.keepAlive();
                } catch (StorageConnectionException e) {
                    this.getLogger().warning("Database keep alive failed: " + e.getMessage());
                }
            } else {
                this.getLogger().warning("Database connection failed. Attempting to reconnect.");
                attemptDatabaseConnection(false);
            }

        }, 30 * 20, 30 * 20); // runs every 30 seconds

        loadDatabaseData();
        getLogger().info("Database data loaded");

        UpdateChecker updateChecker = new UpdateChecker(this);

        // REGISTER THINGS START
        KitSlotTabCompleter kitSlotTabCompleter = new KitSlotTabCompleter();

        MainMenuCommand mainMenuCommand = new MainMenuCommand(plugin);
        this.getCommand("kit").setExecutor(mainMenuCommand);
        this.getCommand("kit").setTabCompleter((sender, cmd, alias, args) -> Collections.emptyList());

        this.getCommand("sharekit").setExecutor(new ShareKitCommand());
        this.getCommand("sharekit").setTabCompleter(kitSlotTabCompleter);

        this.getCommand("copykit").setExecutor(new CopyKitCommand());

        KitRoomCommand kitRoomCommand = new KitRoomCommand();
        this.getCommand("kitroom").setExecutor(kitRoomCommand);
        this.getCommand("kitroom").setTabCompleter(kitRoomCommand);

        this.getCommand("swapkit").setExecutor(new SwapKitCommand());
        this.getCommand("swapkit").setTabCompleter(kitSlotTabCompleter);

        this.getCommand("deletekit").setExecutor(new DeleteKitCommand());
        this.getCommand("deletekit").setTabCompleter(kitSlotTabCompleter);

        this.getCommand("inspectkit").setExecutor(new InspectKitCommand(plugin));
        this.getCommand("inspectkit").setTabCompleter(new InspectKitCommand(plugin));

        this.getCommand("inspectec").setExecutor(new InspectEcCommand(plugin));
        this.getCommand("inspectec").setTabCompleter(new InspectEcCommand(plugin));

        SavePublicKitCommand savePublicKitCommand = new SavePublicKitCommand();
        this.getCommand("savepublickit").setExecutor(savePublicKitCommand);
        this.getCommand("savepublickit").setTabCompleter(savePublicKitCommand);

        PublicKitCommand publicKitCommand = new PublicKitCommand(plugin);
        this.getCommand("publickit").setExecutor(publicKitCommand);
        this.getCommand("publickit").setTabCompleter(publicKitCommand);

        for (int i = 1; i <= 9; i++) {
            this.getCommand("k" + i).setExecutor(new ShortKitCommand());
        }

        for (int i = 1; i <= 9; i++) {
            this.getCommand("ec" + i).setExecutor(new ShortECCommand());
        }

        RegearCommand regearCommand = new RegearCommand(this);
        this.getCommand("regear").setExecutor(regearCommand);

        this.getCommand("heal").setExecutor(new HealCommand());
        this.getCommand("repair").setExecutor(new RepairCommand());
        this.getCommand("virtualkit").setExecutor(new VirtualKitsCommand(this));

        Bukkit.getPluginManager().registerEvents(regearCommand, this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this, updateChecker), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuFunctionListener(), this);
        Bukkit.getPluginManager().registerEvents(new KitMenuCloseListener(), this);
        Bukkit.getPluginManager().registerEvents(new KitRoomSaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new AutoRekitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AboutCommandListener(), this);

        // EC Cooldown manager - handles /ec1-9 cooldowns with kill reset
        ECCooldownManager ecCooldownManager = new ECCooldownManager(this);
        Bukkit.getPluginManager().registerEvents(ecCooldownManager, this);

        // features
        if (getConfig().getBoolean("feature.old-death-drops", false)) {
            Bukkit.getPluginManager().registerEvents(new OldDeathDropListener(), this);
        }

        if (getConfig().getBoolean("anti-exploit.block-spaces-in-commands", false)) {
            Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        }

        if (getConfig().getBoolean("anti-exploit.prevent-shulkers-dropping-items", false)) {
            Bukkit.getPluginManager().registerEvents(new ShulkerDropItemsListener(), this);
        }

        // REGISTER THINGS END

        BroadcastManager.get().startScheduledBroadcast();

        // Print startup banner
        printStartupBanner();

        updateChecker.printStartupStatus();

    }

    @Override
    public void onDisable() {
        closeDatabaseConnection();

        // Shutdown backup manager if it exists
        if (backupManager != null) {
            backupManager.shutdown();
        }
    }

    /**
     * Check if the storage type is file-based (requires backups)
     * 
     * @param storageType The storage type from configuration
     * @return true if file-based storage, false otherwise
     */
    private boolean isFileBasedStorage(String storageType) {
        return storageType.equalsIgnoreCase("sqlite") ||
                storageType.equalsIgnoreCase("yml") ||
                storageType.equalsIgnoreCase("yaml");
    }

    private void loadPublicKitsIdsFromConfig() {
        // Single premade kit - admin can save with /savepk, players load via GUI
        PublicKit kit = new PublicKit("premade", "<bold><#FFD700>PREMADE KIT</bold>", Material.CHEST);
        KitManager.get().getPublicKitList().add(kit);
        this.getLogger().info("Loaded single premade kit");
    }

    private void loadDatabaseData() {
        KitRoomDataManager.get().loadFromDB();
        KitManager.get().getPublicKitList().forEach(kit -> KitManager.get().loadPublicKitFromDB(kit.id));
        Bukkit.getOnlinePlayers().forEach(player -> KitManager.get().loadPlayerDataFromDB(player.getUniqueId()));

    }

    private void attemptDatabaseConnection(boolean disableOnFail) {
        try {
            storageManager.connect();
            if (!storageManager.isConnected()) {
                throw new StorageConnectionException("Expected to be connected to the database, but failed.");
            }
        } catch (StorageConnectionException e) {
            if (disableOnFail) {
                this.getLogger().warning("Database connection failed: " + e.getMessage());
                this.getLogger().warning("Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
            } else {
                this.getLogger().warning("Database connection failed: " + e.getMessage());
            }
        }
    }

    private void closeDatabaseConnection() {
        try {
            storageManager.close();
        } catch (StorageConnectionException e) {
            // retry once
            try {
                storageManager.close();
            } catch (StorageConnectionException ex) {
                this.getLogger().warning("Failed to close the database connection: " + e.getMessage());
            }
        }
    }

    private void printStartupBanner() {
        String pink = "\u001B[35m";
        String yellow = "\u001B[33m";
        String cyan = "\u001B[36m";
        String reset = "\u001B[0m";

        String[] banner = {
                "",
                pink + " ██╗   ██╗    ██╗  ██╗██╗████████╗███████╗" + reset,
                pink + " ██║   ██║    ██║ ██╔╝██║╚══██╔══╝██╔════╝" + reset,
                pink + " ██║   ██║    █████╔╝ ██║   ██║   ███████╗" + reset + "    " + cyan + "Virtual Kits" + reset,
                pink + " ╚██╗ ██╔╝    ██╔═██╗ ██║   ██║   ╚════██║" + reset + "    Per-player kit system",
                pink + "  ╚████╔╝     ██║  ██╗██║   ██║   ███████║" + reset,
                pink + "   ╚═══╝      ╚═╝  ╚═╝╚═╝   ╚═╝   ╚══════╝" + reset,
                "",
                "    " + yellow + "Version: " + reset + cyan + getDescription().getVersion() + reset,
                "    " + yellow + "Author: " + reset + cyan + "ACE" + reset,
                ""
        };

        for (String line : banner) {
            Bukkit.getConsoleSender().sendMessage(line);
        }
    }
}
