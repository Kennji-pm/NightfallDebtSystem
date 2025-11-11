package org.kennji.nightfallDebtSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.kennji.nightfallDebtSystem.CoinsEngineAdapter;
import org.kennji.nightfallDebtSystem.NightfallDebtSystem;
import org.kennji.nightfallDebtSystem.db.DebtDAO;
import org.kennji.nightfallDebtSystem.api.Debt;
import org.kennji.nightfallDebtSystem.util.ColorUtils;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DebtCommand implements CommandExecutor, TabCompleter {
    private final NightfallDebtSystem plugin;
    private final DebtDAO dao;
    private final CoinsEngineAdapter coins;

    public DebtCommand(NightfallDebtSystem plugin, DebtDAO dao, CoinsEngineAdapter coins) {
        this.plugin = plugin;
        this.dao = dao;
        this.coins = coins;
    }

    private String msg(String key) {
        return ColorUtils.colorize(plugin.getMessages().getString("prefix", "") + plugin.getMessages().getString("messages." + key, ""));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(msg("usage_main")); // Provide a main usage message
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "request":
                return handleRequest(sender, args);
            case "accept":
                return handleAccept(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                if (!sender.hasPermission("nfsdebt.admin")) { sender.sendMessage(msg("no_permission")); return true; }
                plugin.getConfigManager().reload();
                sender.sendMessage(msg("reloaded"));
                return true;
            default:
                sender.sendMessage(msg("unknown_command")); // Message for unknown subcommand
                return true;
        }
    }

    private boolean handleRequest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nfsdebt.borrow")) { sender.sendMessage(msg("no_permission")); return true; }
        // New usage: /debt request <player> <amount> <interest> <days>
        if (args.length < 5) { sender.sendMessage(msg("usage_request")); return true; }
        if (!(sender instanceof Player requester)) { sender.sendMessage(msg("only_players")); return true; }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (requester.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(msg("cannot_request_self"));
            return true;
        }

        double amount;
        double interest;
        int days;
        try {
            amount = Double.parseDouble(args[2]);
            interest = Double.parseDouble(args[3]);
            days = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(msg("invalid_number"));
            return true;
        }

        Debt d = new Debt();
        d.setBorrowerUUID(requester.getUniqueId());
        d.setLenderUUID(target.getUniqueId());
        d.setAmount(amount);
        d.setRemainingAmount(amount);
        d.setInterestRate(interest);
        d.setDueDate(Instant.now().plusSeconds(days * 24L * 3600L).toEpochMilli());
        d.setPaid(false);

        try {
            int id = dao.createDebt(d);
            sender.sendMessage(msg("request_sent").replace("{player}", target.getName() == null ? "(offline)" : target.getName()).replace("{id}", String.valueOf(id)));
            if (target.isOnline()) {
                var ply = target.getPlayer();
                if (ply != null) ply.sendMessage(msg("request_received").replace("{player}", requester.getName()).replace("{id}", String.valueOf(id)));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error while creating debt: " + e.getMessage());
            sender.sendMessage(msg("db_error"));
        }
        return true;
    }

    private boolean handleAccept(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nfsdebt.loan")) { sender.sendMessage(msg("no_permission")); return true; }
        if (args.length < 2) { sender.sendMessage(msg("usage_accept")); return true; }
        int id;
        try { id = Integer.parseInt(args[1]); } catch (NumberFormatException e) { sender.sendMessage(msg("invalid_number")); return true; }
        try {
            Debt d = dao.getDebt(id);
            if (d == null) { sender.sendMessage(msg("debt_not_found").replace("{id}", String.valueOf(id))); return true; }
            if (!(sender instanceof Player)) { sender.sendMessage(msg("only_players")); return true; }
            Player playerSender = (Player) sender;

            // Check if the sender is the lender of the debt
            if (!playerSender.getUniqueId().equals(d.getLenderUUID())) {
                sender.sendMessage(msg("not_lender_for_accept").replace("{id}", String.valueOf(id)));
                return true;
            }

            // Check if the debt is already accepted or paid
            if (d.isAccepted()) {
                sender.sendMessage(msg("debt_already_accepted").replace("{id}", String.valueOf(id)));
                return true;
            }
            if (d.isPaid()) {
                sender.sendMessage(msg("debt_already_paid").replace("{id}", String.valueOf(id)));
                return true;
            }

            // Transfer funds via CoinsEngine
            double balance = coins.getBalance(d.getLenderUUID());
            if (balance < d.getAmount()) { sender.sendMessage(msg("insufficient_funds")); return true; }
            boolean withdrawn = coins.withdraw(d.getLenderUUID(), d.getAmount());
            if (!withdrawn) { sender.sendMessage(msg("transfer_failed")); return true; }
            boolean deposited = coins.deposit(d.getBorrowerUUID(), d.getAmount());
            if (!deposited) { sender.sendMessage(msg("transfer_failed")); return true; }

            d.setAccepted(true); // Mark debt as accepted
            dao.updateDebt(d);
            sender.sendMessage(msg("accepted").replace("{id}", String.valueOf(id)).replace("{lender}", sender.getName()).replace("{amount}", String.valueOf(d.getAmount())));
            OfflinePlayer borrower = Bukkit.getOfflinePlayer(d.getBorrowerUUID());
            if (borrower.isOnline()) {
                var ply = borrower.getPlayer();
                if (ply != null) ply.sendMessage(msg("debt_accepted_by_lender").replace("{id}", String.valueOf(id)).replace("{lender}", sender.getName()).replace("{amount}", String.valueOf(d.getAmount())));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error while accepting debt: " + e.getMessage());
            sender.sendMessage(msg("db_error"));
        }
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nfsdebt.borrow")) { sender.sendMessage(msg("no_permission")); return true; }
        if (args.length < 3) { sender.sendMessage(msg("usage_pay")); return true; }
        int id; double amount;
        try { id = Integer.parseInt(args[1]); amount = Double.parseDouble(args[2]); } catch (NumberFormatException e) { sender.sendMessage(msg("invalid_number")); return true; }
        try {
            Debt d = dao.getDebt(id);
            if (d == null) { sender.sendMessage(msg("debt_not_found").replace("{id}", String.valueOf(id))); return true; }
            UUID payer = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;
            if (payer == null || !payer.equals(d.getBorrowerUUID())) { sender.sendMessage(msg("not_borrower")); return true; }
            double balance = coins.getBalance(payer);
            if (balance < amount) { sender.sendMessage(msg("insufficient_funds")); return true; }
            boolean withdrawn = coins.withdraw(payer, amount);
            if (!withdrawn) { sender.sendMessage(msg("transfer_failed")); return true; }
            boolean deposited = coins.deposit(d.getLenderUUID(), amount);
            if (!deposited) { sender.sendMessage(msg("transfer_failed")); return true; }

            d.setRemainingAmount(Math.max(0.0, d.getRemainingAmount() - amount));
            if (d.getRemainingAmount() <= 0.0) d.setPaid(true);
            dao.updateDebt(d);
            sender.sendMessage(msg("paid").replace("{remaining}", String.valueOf(d.getRemainingAmount())));
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error while paying debt: " + e.getMessage());
            sender.sendMessage(msg("db_error"));
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("nfsdebt.view")) { sender.sendMessage(msg("no_permission")); return true; }
        if (!(sender instanceof Player)) { sender.sendMessage(msg("only_players")); return true; }
        Player p = (Player) sender;
        try {
            List<Debt> list = dao.listDebtsFor(p.getUniqueId());
            if (list.isEmpty()) {
                sender.sendMessage(msg("no_debts_found"));
                return true;
            }
            sender.sendMessage(msg("list_header"));
            for (Debt d : list) {
                String borrowerName = Bukkit.getOfflinePlayer(d.getBorrowerUUID()).getName();
                String lenderName = Bukkit.getOfflinePlayer(d.getLenderUUID()).getName();
                sender.sendMessage(msg("list_entry")
                        .replace("{id}", String.valueOf(d.getDebtID()))
                        .replace("{borrower}", borrowerName == null ? "(unknown)" : borrowerName)
                        .replace("{lender}", lenderName == null ? "(unknown)" : lenderName)
                        .replace("{remaining}", String.valueOf(d.getRemainingAmount()))
                        .replace("{due}", String.valueOf(d.getDueDate())));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Database error while listing debts: " + e.getMessage());
            sender.sendMessage(msg("db_error"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>();
            if (sender.hasPermission("nfsdebt.borrow")) subcommands.add("request");
            if (sender.hasPermission("nfsdebt.loan")) subcommands.add("accept");
            if (sender.hasPermission("nfsdebt.borrow")) subcommands.add("pay");
            if (sender.hasPermission("nfsdebt.view")) subcommands.add("list");
            if (sender.hasPermission("nfsdebt.admin")) subcommands.add("reload");
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "request":
                    if (args.length == 2) { // /debt request <player>
                        return Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    // For amount, interest, days, no specific tab completion needed, maybe suggest defaults or ranges
                    break;
                case "accept":
                    if (args.length == 2 && sender instanceof Player) { // /debt accept <id>
                        Player p = (Player) sender;
                        try {
                            return dao.listDebtsFor(p.getUniqueId()).stream()
                                    .filter(debt -> debt.getLenderUUID().equals(p.getUniqueId()) && !debt.isAccepted() && !debt.isPaid())
                                    .map(debt -> String.valueOf(debt.getDebtID()))
                                    .filter(id -> id.startsWith(args[1]))
                                    .collect(Collectors.toList());
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Database error during tab completion for accept: " + e.getMessage());
                            return Collections.emptyList();
                        }
                    }
                    break;
                case "pay":
                    if (args.length == 2 && sender instanceof Player) { // /debt pay <id>
                        Player p = (Player) sender;
                        try {
                            return dao.listDebtsFor(p.getUniqueId()).stream()
                                    .filter(debt -> debt.getBorrowerUUID().equals(p.getUniqueId()) && debt.isAccepted() && !debt.isPaid())
                                    .map(debt -> String.valueOf(debt.getDebtID()))
                                    .filter(id -> id.startsWith(args[1]))
                                    .collect(Collectors.toList());
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Database error during tab completion for pay: " + e.getMessage());
                            return Collections.emptyList();
                        }
                    }
                    break;
            }
        }
        return Collections.emptyList();
    }
}
