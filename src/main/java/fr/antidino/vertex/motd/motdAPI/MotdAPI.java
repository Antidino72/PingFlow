package fr.antidino.vertex.motd.motdAPI;

import fr.antidino.vertex.motd.motdAPI.commands.MainCommand;
import fr.antidino.vertex.motd.motdAPI.listenners.ServerListPingListenner;
import fr.antidino.vertex.motd.motdAPI.manager.GeoIPManager;
import fr.antidino.vertex.motd.motdAPI.manager.IPDatabaseManager;
import fr.antidino.vertex.motd.motdAPI.manager.MinecraftVersionManager;
import fr.antidino.vertex.motd.motdAPI.manager.ScheduleManager;
import fr.antidino.vertex.motd.motdAPI.resolvers.IconResolver;
import fr.antidino.vertex.motd.motdAPI.resolvers.MotdResolver;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class MotdAPI extends JavaPlugin {
    public ScheduleManager scheduleManager;
    public FileConfiguration config;
    public IPDatabaseManager ipDatabaseManager;
    public GeoIPManager  geoIPManager;
    public MotdResolver motdResolver;
    public IconResolver  iconResolver;
    public FileConfiguration versionConfig;
    public FileConfiguration clockConfig;
    public MinecraftVersionManager versionManager;
    public static final Map<InetAddress, String> hostnameCache = new ConcurrentHashMap<>();


    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        saveResource("version.yml", false);
        versionConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "version.yml")
        );
        saveResource("clock_config.yml", false);
        clockConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "clock_config.yml")
        );
        versionManager = new MinecraftVersionManager(this);
        versionManager.load();
        ipDatabaseManager = new IPDatabaseManager(this);
        try {
            ipDatabaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        config = this.getConfig();
        geoIPManager = new GeoIPManager(this);
        geoIPManager.init();
        motdResolver = new MotdResolver(this);
        iconResolver = new IconResolver(this);
        scheduleManager = new ScheduleManager(this);
        scheduleManager.load();
        this.getCommand("motd").setExecutor(new MainCommand(this));
        this.getCommand("motd").setTabCompleter(new MainCommand(this));

        try {
            iconResolver.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new ServerListPingListenner(this),this);
        getLogger().info("Plugin has Enabled");

    }
    public void reload(){
        this.reloadConfig();
        ipDatabaseManager.disconnect();
        try {
            ipDatabaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        versionConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "version.yml")
        );
        clockConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "clock_config.yml")
        );
        versionManager.load();
        config = this.getConfig();
        geoIPManager.init();
        try {
            iconResolver.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        scheduleManager = new ScheduleManager(this);
        scheduleManager.load();
    }
    @Override
    public void onDisable() {
        ipDatabaseManager.disconnect();
        getLogger().info("Plugin has Disabled");
    }

}
