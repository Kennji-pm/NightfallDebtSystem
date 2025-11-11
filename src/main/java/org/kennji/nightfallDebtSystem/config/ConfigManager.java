package org.kennji.nightfallDebtSystem.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfigs();
        reload();
    }

    public void saveDefaultConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }
}
