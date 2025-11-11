package org.kennji.nightfallDebtSystem.db;

import org.kennji.nightfallDebtSystem.api.Debt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DebtDAO {
    private final DatabaseManager db;

    public DebtDAO(DatabaseManager db) {
        this.db = db;
    }

    public int createDebt(Debt d) throws SQLException {
        String sql = "INSERT INTO debts (borrowerUUID, lenderUUID, amount, remainingAmount, dueDate, interestRate, isPaid, isAccepted) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getBorrowerUUID().toString());
            ps.setString(2, d.getLenderUUID().toString());
            ps.setDouble(3, d.getAmount());
            ps.setDouble(4, d.getRemainingAmount());
            ps.setLong(5, d.getDueDate());
            ps.setDouble(6, d.getInterestRate());
            ps.setInt(7, d.isPaid() ? 1 : 0);
            ps.setInt(8, d.isAccepted() ? 1 : 0); // Add isAccepted
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public Debt getDebt(int id) throws SQLException {
        String sql = "SELECT * FROM debts WHERE debtID = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Debt> listDebtsFor(UUID player) throws SQLException {
        String sql = "SELECT * FROM debts WHERE borrowerUUID = ? OR lenderUUID = ?";
        List<Debt> out = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.toString());
            ps.setString(2, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public void updateDebt(Debt d) throws SQLException {
        String sql = "UPDATE debts SET remainingAmount = ?, isPaid = ?, isAccepted = ? WHERE debtID = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, d.getRemainingAmount());
            ps.setInt(2, d.isPaid() ? 1 : 0);
            ps.setInt(3, d.isAccepted() ? 1 : 0); // Add isAccepted
            ps.setInt(4, d.getDebtID());
            ps.executeUpdate();
        }
    }

    public List<Debt> getOverdueDebts(long now) throws SQLException {
        String sql = "SELECT * FROM debts WHERE remainingAmount > 0 AND dueDate < ? AND isPaid = 0";
        List<Debt> out = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, now);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public void deleteDebt(int id) throws SQLException {
        String sql = "DELETE FROM debts WHERE debtID = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Debt map(ResultSet rs) throws SQLException {
        Debt d = new Debt();
        d.setDebtID(rs.getInt("debtID"));
        d.setBorrowerUUID(UUID.fromString(rs.getString("borrowerUUID")));
        d.setLenderUUID(UUID.fromString(rs.getString("lenderUUID")));
        d.setAmount(rs.getDouble("amount"));
        d.setRemainingAmount(rs.getDouble("remainingAmount"));
        d.setDueDate(rs.getLong("dueDate"));
        d.setInterestRate(rs.getDouble("interestRate"));
        d.setPaid(rs.getInt("isPaid") != 0);
        d.setAccepted(rs.getInt("isAccepted") != 0); // Retrieve isAccepted
        return d;
    }
}
