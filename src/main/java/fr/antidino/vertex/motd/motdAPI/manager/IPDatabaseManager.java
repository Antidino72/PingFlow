package fr.antidino.vertex.motd.motdAPI.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.antidino.vertex.motd.motdAPI.MotdAPI;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IPDatabaseManager {

    private final MotdAPI motdAPI;
    private HikariDataSource dataSource;

    public IPDatabaseManager(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }

    // ------------------------------------------
    // Connection
    // ------------------------------------------

    public void connect() throws SQLException {
        HikariConfig hikari = new HikariConfig();
        String type = motdAPI.getConfig().getString("database.type", "sqlite");

        if (type.equalsIgnoreCase("mysql")) {
            String host     = motdAPI.getConfig().getString("database.host", "localhost");
            int    port     = motdAPI.getConfig().getInt("database.port", 3306);
            String name     = motdAPI.getConfig().getString("database.name", "motdapi");
            String user     = motdAPI.getConfig().getString("database.user", "root");
            String password = motdAPI.getConfig().getString("database.password", "");

            hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&autoReconnect=true");
            hikari.setUsername(user);
            hikari.setPassword(password);
            hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
            motdAPI.getLogger().info("[MotdAPI]  MySQL mode enabled");
        } else {
            File dbFile = new File(motdAPI.getDataFolder(), "ip_motd.db");
            hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikari.setDriverClassName("org.sqlite.JDBC");
            hikari.setMaximumPoolSize(1); // SQLite = 1 seule connexion
            motdAPI.getLogger().info("[PingFlow]  SQLite mode enabled");
        }

        // Pool settings
        hikari.setPoolName("PingFlow-Pool");
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(30000);
        hikari.setIdleTimeout(600000);
        hikari.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(hikari);
        createTable();
        motdAPI.getLogger().info("[PingFlow] Database connected.");
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            motdAPI.getLogger().info("[PingFlow] Database disconnected");
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createTable() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ip_motd (
                    ip    VARCHAR(45)  PRIMARY KEY,
                    motd  TEXT         NOT NULL,
                    icon  VARCHAR(255) NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS motds (
                    id    INTEGER      PRIMARY KEY AUTOINCREMENT,
                    name  VARCHAR(64)  NOT NULL UNIQUE,
                    motd  TEXT         NOT NULL
                )
            """);
        }
    }

    // ------------------------------------------
    // CRUD
    // ------------------------------------------

    public void set(String ip, String motd, String icon) {
        String sql = isMySQL()
                ? "INSERT INTO ip_motd (ip, motd, icon) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE motd = VALUES(motd), icon = VALUES(icon)"
                : "INSERT INTO ip_motd (ip, motd, icon) VALUES (?, ?, ?) ON CONFLICT(ip) DO UPDATE SET motd = excluded.motd, icon = excluded.icon";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ip);
            stmt.setString(2, motd);
            stmt.setString(3, icon);
            stmt.executeUpdate();
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[MotdAPI] Error set : " + e.getMessage());
        }
    }

    public void remove(String ip) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM ip_motd WHERE ip = ?")) {
            stmt.setString(1, ip);
            stmt.executeUpdate();
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[MotdAPI] Erreur remove : " + e.getMessage());
        }
    }

   //_________________________MOTD________________________________
    public Optional<IPMotdEntry> get(String ip) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT motd, icon FROM ip_motd WHERE ip = ?")) {
            stmt.setString(1, ip);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new IPMotdEntry(rs.getString("motd"), rs.getString("icon")));
            }
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[MotdAPI] Error get : " + e.getMessage());
        }
        return Optional.empty();
    }
    // Ajouter / mettre à jour un motd
    public void setMotd(String name, String motd) {
        String sql = isMySQL()
                ? "INSERT INTO motds (name, motd) VALUES (?, ?) ON DUPLICATE KEY UPDATE motd = VALUES(motd)"
                : "INSERT INTO motds (name, motd) VALUES (?, ?) ON CONFLICT(name) DO UPDATE SET motd = excluded.motd";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, motd);
            stmt.executeUpdate();
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[PingFlow] Error setMotd : " + e.getMessage());
        }
    }

    // Supprimer un motd
    public void removeMotd(String name) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM motds WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[PingFlow] Error removeMotd : " + e.getMessage());
        }
    }

    // Récupérer un motd par nom
    public String getMotd(String name) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT motd FROM motds WHERE name = ?")) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("motd");
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[PingFlow] Error getMotd : " + e.getMessage());
        }
        return "null";
    }

    // Récupérer tous les noms pour le tab complete
    public List<String> getMotdNames() {
        List<String> names = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM motds")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) names.add(rs.getString("name"));
        } catch (SQLException e) {
            motdAPI.getLogger().severe("[PingFlow] Error getMotdNames : " + e.getMessage());
        }
        return names;
    }
    // ------------------------------------------
    // Helpers
    // ------------------------------------------

    private boolean isMySQL() {
        return motdAPI.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("mysql");
    }

    public record IPMotdEntry(String motd, String icon) {}
}