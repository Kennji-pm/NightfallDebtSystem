package org.kennji.nightfallDebtSystem.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private HikariDataSource ds;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() throws SQLException {
        var cfg = plugin.getConfig();
        String type = cfg.getString("database.type", "sqlite").toLowerCase();
        HikariConfig hikari = new HikariConfig();

        if ("mysql".equals(type)) {
            String host = cfg.getString("database.mysql.host", "localhost");
            int port = cfg.getInt("database.mysql.port", 3306);
            String database = cfg.getString("database.mysql.database", "nfs_debts");
            String user = cfg.getString("database.mysql.user", "root");
            String password = cfg.getString("database.mysql.password", "");
            String jdbc = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true", host, port, database);
            hikari.setJdbcUrl(jdbc);
            hikari.setUsername(user);
            hikari.setPassword(password);
            hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            // SQLite
            File dbfile = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.sqlite.file", "debts.db"));
            dbfile.getParentFile().mkdirs();
            String jdbc = "jdbc:sqlite:" + dbfile.getAbsolutePath();
            hikari.setJdbcUrl(jdbc);
            hikari.setDriverClassName("org.sqlite.JDBC");
        }

        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(1);
        hikari.setPoolName("NFS-DebtPool");

        this.ds = new HikariDataSource(hikari);

        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS debts (" +
                    "debtID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "borrowerUUID TEXT NOT NULL, " +
                    "lenderUUID TEXT NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "remainingAmount DOUBLE NOT NULL, " +
                    "dueDate BIGINT NOT NULL, " +
                    "interestRate DOUBLE NOT NULL, " +
                    "isPaid INTEGER NOT NULL DEFAULT 0" +
                    ")");
        }
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void close() {
        if (ds != null) ds.close();
    }
}

