package org.kennji.nightfallDebtSystem;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.UUID;

/**
 * CoinsEngine adapter for economy operations
 */
public class CoinsEngineAdapter {
    private final NightfallDebtSystem plugin;
    private final Currency currency;

    public CoinsEngineAdapter(NightfallDebtSystem plugin) {
        this.plugin = plugin;
        String currencyName = plugin.getConfigManager().getConfig().getString("default-currency", "coins");
        Currency foundCurrency = CoinsEngineAPI.getCurrency(currencyName);
        
        if (foundCurrency == null) {
            plugin.getLogger().warning("Currency '" + currencyName + "' not found! Using default 'coins'.");
            foundCurrency = CoinsEngineAPI.getCurrency("coins");
        }
        this.currency = foundCurrency;
    }

    public String getCurrencyName(){
        if (this.currency != null) {
            return this.currency.getId();
        }
        return null;
    }

    public double getBalance(UUID player) {
        try {
            OfflinePlayer p = Bukkit.getOfflinePlayer(player);
            if (p == null) return 0.0;
            
            double balance = CoinsEngineAPI.getBalance(p.getUniqueId(), currency);
            return balance;
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting balance for player " + player + ": " + e.getMessage());
            return 0.0;
        }
    }

    public boolean withdraw(UUID player, double amount) {
        try {
            if (amount <= 0) return false;
            
            double currentBalance = getBalance(player);
            if (currentBalance < amount) {
                return false;
            }
            
            boolean success = CoinsEngineAPI.removeBalance(player, currency, amount);
            return success;
        } catch (Exception e) {
            plugin.getLogger().severe("Error withdrawing from player " + player + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deposit(UUID player, double amount) {
        try {
            if (amount <= 0) return false;
            
            boolean success = CoinsEngineAPI.addBalance(player, currency, amount);
            return success;
        } catch (Exception e) {
            plugin.getLogger().severe("Error depositing to player " + player + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Transfer money from one player to another
     * @param fromPlayer UUID of the player sending money
     * @param toPlayer UUID of the player receiving money
     * @param amount Amount to transfer
     * @return true if transfer successful, false otherwise
     */
    public boolean transfer(UUID fromPlayer, UUID toPlayer, double amount) {
        if (fromPlayer.equals(toPlayer)) {
            return false; // Cannot transfer to self
        }
        
        // Withdraw from sender
        boolean withdrawn = withdraw(fromPlayer, amount);
        if (!withdrawn) {
            plugin.getLogger().warning("Transfer failed: could not withdraw from " + fromPlayer);
            return false;
        }
        
        // Deposit to receiver
        boolean deposited = deposit(toPlayer, amount);
        if (!deposited) {
            plugin.getLogger().warning("Transfer partially failed: withdrawn from " + fromPlayer + " but failed to deposit to " + toPlayer);
            // Try to refund the sender
            deposit(fromPlayer, amount);
            return false;
        }
        return true;
    }
}
