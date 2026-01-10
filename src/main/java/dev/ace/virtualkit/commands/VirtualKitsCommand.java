package dev.ace.virtualkit.commands;

import dev.ace.virtualkit.util.GuiConfigManager;
import dev.ace.virtualkit.util.MessageManager;
import dev.ace.virtualkit.util.importutil.KitsXImporter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VirtualKitsCommand implements CommandExecutor, TabCompleter {

    private Plugin plugin;

    public VirtualKitsCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "VirtualKit v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Use /virtualkit reload to reload configs");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "about":
                sender.sendMessage(
                        ChatColor.GREEN + "VirtualKits is a plugin that allows players to have their own kits.");
                return true;
            case "reload":
                if (!sender.hasPermission("virtualkit.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload.");
                    return true;
                }
                // Reload all configs
                plugin.reloadConfig();
                GuiConfigManager.get().reload();
                MessageManager.get().loadMessages();
                sender.sendMessage(ChatColor.GREEN + "VirtualKit configs reloaded!");
                if (sender instanceof Player) {
                    plugin.getLogger().info("Configs reloaded by " + sender.getName());
                }
                return true;
            case "import":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Missing import type!");
                    return true;
                }

                switch (args[1].toLowerCase()) {
                    case "kitsx":
                        sender.sendMessage(ChatColor.GREEN + "Starting import...");
                        KitsXImporter importer = new KitsXImporter(plugin, sender);
                        if (!importer.checkForFiles()) {
                            sender.sendMessage(ChatColor.RED + "Missing files to import");
                            sender.sendMessage(
                                    ChatColor.RED + "Copy data folder from KitsX into the VirtualKits folder");
                        }
                        importer.importFiles();
                        sender.sendMessage(ChatColor.GREEN + "Attempted import of KitsX data!");

                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid import type!");
                        break;
                }
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use: reload, about, import");
                return true;

        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("virtualkit.admin")) {
                if ("reload".startsWith(args[0].toLowerCase()))
                    completions.add("reload");
                if ("import".startsWith(args[0].toLowerCase()))
                    completions.add("import");
            }
            if ("about".startsWith(args[0].toLowerCase()))
                completions.add("about");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("import")) {
            return List.of("kitsx");
        }

        return null;
    }
}
