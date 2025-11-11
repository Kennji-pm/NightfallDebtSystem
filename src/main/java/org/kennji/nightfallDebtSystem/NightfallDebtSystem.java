package org.kennji.nightfallDebtSystem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.kennji.nightfallDebtSystem.commands.DebtCommand;
import org.kennji.nightfallDebtSystem.config.ConfigManager;
import org.kennji.nightfallDebtSystem.db.DatabaseManager;
import org.kennji.nightfallDebtSystem.db.DebtDAO;
import org.kennji.nightfallDebtSystem.placeholder.PlaceholderAPIExpansion;
import org.kennji.nightfallDebtSystem.tasks.OverdueTask;

import java.util.logging.Level;

public final class NightfallDebtSystem extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private DebtDAO debtDAO;
    private CoinsEngineAdapter coinsAdapter;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        // Plugin startup logic
        getLogger().log(Level.INFO, "════════════════════════════════════════════════════");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "      🌙 NIGHTFALL DEBT SYSTEM 🌙");
        getLogger().log(Level.INFO, "          Starting up...");
        getLogger().log(Level.INFO, "");
        getLogger().log(Level.INFO, "════════════════════════════════════════════════════");
        getLogger().log(Level.INFO, "  👤 Author: _kennji");
        getLogger().log(Level.INFO, "  📦 Version: " + getDescription().getVersion());
        getLogger().log(Level.INFO, "════════════════════════════════════════════════════");

        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        try {
            databaseManager.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.debtDAO = new DebtDAO(databaseManager);
        this.coinsAdapter = new CoinsEngineAdapter(this);
        getLogger().log(Level.INFO, "  ✓ Hooked into CoinsEngine!");

        var debtCommandExecutor = new DebtCommand(this, debtDAO, coinsAdapter);
        var cmd = this.getCommand("debt");
        if (cmd != null) {
            cmd.setExecutor(debtCommandExecutor);
            cmd.setTabCompleter(debtCommandExecutor);
        }

        int interval = getConfig().getInt("scheduler-interval-ticks", 900);
        getLogger().log(Level.INFO, "  ⏱ Scheduling overdue task every " + interval + " ticks");
        this.getServer().getScheduler().runTaskTimer(this, new OverdueTask(this, debtDAO), interval, interval);

        // Register PlaceholderAPI expansion if present
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new PlaceholderAPIExpansion(this).register();
                getLogger().info("Registered PlaceholderAPI expansion");
            } catch (NoClassDefFoundError | Exception ex) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + ex.getMessage());
            }
        }

        getLogger().log(Level.INFO, "════════════════════════════════════════════════════");
        getLogger().log(Level.INFO, "  ✓ Plugin enabled successfully!");
        getLogger().log(Level.INFO, "  ⏱ Startup time: " + (System.currentTimeMillis() - start) + "ms");
        getLogger().log(Level.INFO, "════════════════════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) databaseManager.close();
    }

    public DebtDAO getDebtDAO() { return debtDAO; }

    public ConfigManager getConfigManager() { return configManager; }
    public FileConfiguration getMessages() { return configManager.getMessages(); }
}
