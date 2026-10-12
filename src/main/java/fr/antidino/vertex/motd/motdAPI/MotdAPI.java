package fr.antidino.vertex.motd.motdAPI;

import fr.antidino.vertex.motd.motdAPI.commands.MainCommand;
import fr.antidino.vertex.motd.motdAPI.listenners.ServerListPingListenner;
import fr.antidino.vertex.motd.motdAPI.manager.GeoIPManager;
import fr.antidino.vertex.motd.motdAPI.manager.IPDatabaseManager;
import fr.antidino.vertex.motd.motdAPI.manager.MinecraftVersionManager;
import fr.antidino.vertex.motd.motdAPI.manager.ScheduleManager;
import fr.antidino.vertex.motd.motdAPI.manager.countryList.ServerMode;
import fr.antidino.vertex.motd.motdAPI.resolvers.IconResolver;
import fr.antidino.vertex.motd.motdAPI.resolvers.MotdResolver;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;


public final class MotdAPI extends JavaPlugin {
    public ScheduleManager scheduleManager;
    public FileConfiguration config;
    public IPDatabaseManager ipDatabaseManager;
    public GeoIPManager  geoIPManager;
    public MotdResolver motdResolver;
    public IconResolver  iconResolver;

    public FileConfiguration versionConfig;
    public FileConfiguration clockConfig;

    public FileConfiguration blacklistConfig;
    public FileConfiguration countryListConfig;
    public FileConfiguration whitelistConfig;

    public ServerMode serverMode;

    public MinecraftVersionManager versionManager;

    @Override
    public void onEnable() {
        initConfigs();

        versionManager = new MinecraftVersionManager(this);
        versionManager.load();
        ipDatabaseManager = new IPDatabaseManager(this);
        try {
            ipDatabaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        serverMode = new ServerMode(this);
        serverMode.updateAccessServerMode();
        config = this.getConfig();
        geoIPManager = new GeoIPManager(this);
        geoIPManager.init();
        motdResolver = new MotdResolver(this);
        iconResolver = new IconResolver(this);
        scheduleManager = new ScheduleManager(this);
        scheduleManager.load();
        Objects.requireNonNull(this.getCommand("motd")).setExecutor(new MainCommand(this));
        Objects.requireNonNull(this.getCommand("motd")).setTabCompleter(new MainCommand(this));

        try {
            iconResolver.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new ServerListPingListenner(this),this);
        getLogger().info("Plugin has Enabled");

    }
    public void initConfigs(){
        this.saveDefaultConfig();

        /*
                Countrylist Syteme
        */
        saveResource("country_list/blacklist.yml",false);
        blacklistConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(),"country_list/blacklist.yml")
        );
        saveResource("country_list/whitelist.yml",false);
        whitelistConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(),"country_list/whitelist.yml")
        );
        saveResource("country_list/main.yml",false);
        countryListConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(),"country_list/main.yml")
        );
        /*

                MOTD Syteme
         */

        saveResource("version.yml", false);
        versionConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "version.yml")
        );
        saveResource("clock_config.yml", false);
        clockConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "clock_config.yml")
        );
    }
    public void reload(){
        this.reloadConfig();

        ipDatabaseManager.disconnect();
        try {
            ipDatabaseManager.connect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        countryListConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(),"country_list/main.yml")
        );
        blacklistConfig = YamlConfiguration.loadConfiguration(
          new File(getDataFolder(),"country_list/blacklist.yml")
        );
        whitelistConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(),"country_list/whitelist.yml")
        );
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
        serverMode.updateAccessServerMode();
    }
    @Override
    public void onDisable() {
        ipDatabaseManager.disconnect();
        getLogger().info("Plugin has Disabled");
    }

}
