package org.kennji.nightfallDebtSystem.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.kennji.nightfallDebtSystem.NightfallDebtSystem;
import org.kennji.nightfallDebtSystem.db.DebtDAO;
import org.kennji.nightfallDebtSystem.api.Debt;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    private final NightfallDebtSystem plugin;
    private final DebtDAO dao;

    public PlaceholderAPIExpansion(NightfallDebtSystem plugin) {
        this.plugin = plugin;
        this.dao = plugin.getDebtDAO();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "nfsdebt";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(",", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String onPlaceholderRequest(@NotNull Player player, @NotNull String identifier) {
        if (player == null) return "";
        try {
            if (identifier.equalsIgnoreCase("total_debt")) {
                // sum of remainingAmount where borrower == player
                List<Debt> debts = dao.listDebtsFor(player.getUniqueId());
                double sum = 0.0;
                for (Debt d : debts) {
                    if (d.getBorrowerUUID().equals(player.getUniqueId())) sum += d.getRemainingAmount();
                }
                return String.format("%.2f", sum);
            }
            if (identifier.equalsIgnoreCase("total_lent")) {
                List<Debt> debts = dao.listDebtsFor(player.getUniqueId());
                double sum = 0.0;
                for (Debt d : debts) {
                    if (d.getLenderUUID().equals(player.getUniqueId())) sum += d.getAmount();
                }
                return String.format("%.2f", sum);
            }
            if (identifier.startsWith("debt_remaining_")) {
                String idStr = identifier.substring("debt_remaining_".length());
                try {
                    int id = Integer.parseInt(idStr);
                    Debt d = dao.getDebt(id);
                    if (d == null) return "0";
                    return String.format("%.2f", d.getRemainingAmount());
                } catch (NumberFormatException ex) {
                    return "0";
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("Placeholder failed to fetch data: " + ex.getMessage());
        }
        return "";
    }
}
