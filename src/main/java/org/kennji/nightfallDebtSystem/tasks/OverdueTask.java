package org.kennji.nightfallDebtSystem.tasks;

import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.kennji.nightfallDebtSystem.NightfallDebtSystem;
import org.kennji.nightfallDebtSystem.db.DebtDAO;
import org.kennji.nightfallDebtSystem.api.Debt;
import org.kennji.nightfallDebtSystem.util.ColorUtils;

import java.sql.SQLException;
import java.util.List;

public class OverdueTask implements Runnable {
    private final NightfallDebtSystem plugin;
    private final DebtDAO dao;

    public OverdueTask(NightfallDebtSystem plugin, DebtDAO dao) {
        this.plugin = plugin;
        this.dao = dao;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        try {
            List<Debt> overdue = dao.getOverdueDebts(now);
            if (overdue.isEmpty()) return;
            var cfg = plugin.getConfig();
            String effectName = cfg.getString("debuff.effect", "SLOW");
            int level = cfg.getInt("debuff.level", 1);
            int duration = cfg.getInt("debuff.duration-ticks", 200);
            double percent = cfg.getDouble("overdue-interest-percent", 1.0);

            PotionEffectType pet = PotionEffectType.getByName(effectName);
            if (pet == null) {
                plugin.getLogger().warning("Invalid potion effect type specified in config: " + effectName + ". Defaulting to SLOW.");
                pet = PotionEffectType.SLOWNESS; // Fallback to a default if the configured effect is invalid
            }

            for (Debt d : overdue) {
                // apply interest
                double add = d.getRemainingAmount() * (percent / 100.0);
                d.setRemainingAmount(d.getRemainingAmount() + add);
                dao.updateDebt(d);

                // apply debuff if borrower online
                var borrower = Bukkit.getPlayer(d.getBorrowerUUID());
                if (borrower != null && pet != null) {
                    borrower.addPotionEffect(new PotionEffect(pet, duration, Math.max(0, level - 1)));
                }

                String raw = plugin.getMessages().getString("messages.overdue_applied", "Overdue applied");
                String msg = ColorUtils.colorize(plugin.getMessages().getString("prefix", "") + raw
                        .replace("{id}", String.valueOf(d.getDebtID()))
                        .replace("{interest}", String.format("%.2f", add)));
                if (borrower != null) borrower.sendMessage(msg);
            }
        } catch (SQLException ex) {
            plugin.getLogger().severe("Error checking overdue debts: " + ex.getMessage());
        }
    }
}
