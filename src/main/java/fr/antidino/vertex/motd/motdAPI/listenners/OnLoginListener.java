package fr.antidino.vertex.motd.motdAPI.listenners;


import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;

public class OnLoginListener implements Listener {
    MotdAPI plugin;
    public OnLoginListener(MotdAPI plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        String ip = event.getAddress().getHostAddress();
        String country = plugin.geoIPManager.resolveCountry(ip);
        FileConfiguration blacklist = plugin.blacklistConfig;

    }
}