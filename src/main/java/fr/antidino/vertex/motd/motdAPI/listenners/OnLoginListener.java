package fr.antidino.vertex.motd.motdAPI.listenners;


import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;


public class OnLoginListener implements Listener {
    MotdAPI plugin;
    public OnLoginListener(MotdAPI plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        String ip = event.getAddress().getHostAddress();
        String country_player = plugin.geoIPManager.resolveCountry(ip);
        FileConfiguration blacklist = plugin.blacklistConfig;
        for (String country : blacklist.getStringList("country")) {
            if (blacklist.getBoolean("country."+country) && country.equalsIgnoreCase(country_player)){
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, Component.text("Your Country isn't allowed on this server"));
            }
        }

    }
}