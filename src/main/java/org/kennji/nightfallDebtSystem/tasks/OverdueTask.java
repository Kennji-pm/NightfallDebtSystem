package org.kennji.nightfallDebtSystem.tasks;

import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.kennji.nightfallDebtSystem.NightfallDebtSystem;
import org.kennji.nightfallDebtSystem.db.DebtDAO;
import org.kennji.nightfallDebtSystem.api.Debt;
import org.kennji.nightfallDebtSystem.util.ColorUtils;
import org.kennji.nightfallDebtSystem.util.TimeUtils;

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
            // Check for overdue debts
            List<Debt> overdue = dao.getOverdueDebts(now);
            if (!overdue.isEmpty()) {
                processOverdueDebts(overdue, now);
            }
        } catch (SQLException ex) {
            plugin.getLogger().severe("Error checking debts: " + ex.getMessage());
        }
    }

    private void processOverdueDebts(List<Debt> overdue, long now) throws SQLException {
        var cfg = plugin.getConfig();
        String effectName = cfg.getString("debuff.effect", "SLOWNESS");
        int level = cfg.getInt("debuff.level", 1);
        int duration = cfg.getInt("debuff.duration-ticks", 200);
        double percent = cfg.getDouble("overdue-interest-percent", 1.0);

        PotionEffectType pet = PotionEffectType.getByName(effectName);
        if (pet == null) {
            plugin.getLogger().warning("Invalid potion effect type specified in config: " + effectName + ". Defaulting to SLOWNESS.");
            pet = PotionEffectType.SLOWNESS;
        }

        for (Debt d : overdue) {
            // Apply interest
            double add = d.getRemainingAmount() * (percent / 100.0);
            d.setRemainingAmount(d.getRemainingAmount() + add);
            dao.updateDebt(d);

            // Apply debuff if borrower online
            var borrower = Bukkit.getPlayer(d.getBorrowerUUID());
            if (borrower != null && pet != null) {
                borrower.addPotionEffect(new PotionEffect(pet, duration, Math.max(0, level - 1)));
            }

            // Send detailed overdue message to borrower
            if (borrower != null) {
                String dueDateFormatted = TimeUtils.formatDueDate(d.getDueDate());
                long overdueTime = now - d.getDueDate();
                String overdueDuration = TimeUtils.getRemainingTime(now + overdueTime);
                
                String overdueMessage = ColorUtils.colorize(
                    plugin.getMessages().getString("prefix", "") + 
                    plugin.getMessages().getString("messages.overdue_applied_detailed",
                        "&c🚨 KHOẢN NỢ QUÁ HẠN! 🚨\\n" +
                        "&eID: {id} &c| &eSố tiền: {amount} &c| &eLãi đã thêm: {interest}\\n" +
                        "&eĐã quá hạn: {overdue_time} &c| &eHạn thanh toán: {due_date}\\n" +
                        "&eVui lòng thanh toán ngay để tránh bị phạt thêm!")
                        .replace("{id}", String.valueOf(d.getDebtID()))
                        .replace("{amount}", String.valueOf(d.getRemainingAmount()))
                        .replace("{interest}", String.format("%.2f", add))
                        .replace("{overdue_time}", overdueDuration)
                        .replace("{due_date}", dueDateFormatted)
                );
                borrower.sendMessage(overdueMessage);
            }

            // Notify lender about overdue
            var lender = Bukkit.getPlayer(d.getLenderUUID());
            if (lender != null) {
                String dueDateFormatted = TimeUtils.formatDueDate(d.getDueDate());
                long overdueTime = now - d.getDueDate();
                String overdueDuration = TimeUtils.getRemainingTime(now + overdueTime);
                
                String lenderMessage = ColorUtils.colorize(
                    plugin.getMessages().getString("prefix", "") + 
                    plugin.getMessages().getString("messages.debt_overdue_lender",
                        "&e⚠️ Thông báo! Khoản nợ {id} đã quá hạn {overdue_time}.\\n" +
                        "&eSố tiền hiện tại: {amount} &c| &eHạn thanh toán: {due_date}")
                        .replace("{id}", String.valueOf(d.getDebtID()))
                        .replace("{overdue_time}", overdueDuration)
                        .replace("{amount}", String.valueOf(d.getRemainingAmount()))
                        .replace("{due_date}", dueDateFormatted)
                );
                lender.sendMessage(lenderMessage);
            }

            // Log to console for admin tracking
            plugin.getLogger().info("Applied overdue penalty to debt " + d.getDebtID() + 
                ": interest added " + String.format("%.2f", add) + 
                ", new amount: " + String.format("%.2f", d.getRemainingAmount()));
        }
    }
}
