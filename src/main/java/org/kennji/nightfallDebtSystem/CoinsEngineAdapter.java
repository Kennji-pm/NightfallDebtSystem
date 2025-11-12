package org.kennji.nightfallDebtSystem;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.UUID;

/**
 * Placeholder adapter for CoinsEngine API. This class shows the expected methods used by the plugin.
 * Replace with actual CoinsEngine imports and API calls.
 */
public class CoinsEngineAdapter {
    private final NightfallDebtSystem plugin;
    private final Currency currency;

    public CoinsEngineAdapter(NightfallDebtSystem plugin) {
        this.plugin = plugin;
        this.currency = CoinsEngineAPI.getCurrency(plugin.getConfigManager().getConfig().getString("default-currency", "coins"));
    }

    public String getCurrencyName(){
        if (CoinsEngineAPI.hasCurrency(getCurrencyName())){
            return plugin.getConfigManager().getConfig().getString("default-currency", "coins");
        }
        return null;
    }

    public double getBalance(UUID player) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        return (int) CoinsEngineAPI.getBalance(p.getUniqueId(), currency);
    }

    public boolean withdraw(UUID player, double amount) {
        CoinsEngineAPI.removeBalance(player, currency, amount);
        return false;
    }

    public boolean deposit(UUID player, double amount) {
        CoinsEngineAPI.addBalance(player, currency, amount);
        return false;
    }
}

