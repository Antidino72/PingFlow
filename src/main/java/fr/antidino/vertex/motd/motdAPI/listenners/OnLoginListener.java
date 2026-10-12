package fr.antidino.vertex.motd.motdAPI.listenners;


import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;


import java.util.List;


public class OnLoginListener implements Listener {
    private final MotdAPI plugin;
    public OnLoginListener(MotdAPI plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        String ip = event.getAddress().getHostAddress();
        String country_player = plugin.geoIPManager.resolveCountry(ip);
        if (country_player == null || country_player.isBlank()) {
            String result = plugin.countryListConfig.getString("unknown-result","DENY");
            switch (result){
                case "ALLOW": {
                    event.allow();
                }
                case "DENY" : {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Your Country isn't allowed on this server"));
                }
                default:
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Your Country isn't allowed on this server"));
            }
        }
        switch (plugin.serverMode.getServerside()){
            case BLACKLIST: {
                FileConfiguration blacklist = plugin.blacklistConfig;
                for (String country : blacklist.getStringList("country")) {
                    if (country.equalsIgnoreCase(country_player)){
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Your Country isn't allowed on this server"));
                    }
                }
                break;
            }
            case WHITELIST: {
                List<String> whitelist = plugin.whitelistConfig.getStringList("country");
                for (String country : whitelist) {
                    if (!country.equalsIgnoreCase(country_player)){
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Component.text("Your Country isn't allowed on this server"));
                    }
                }
                break;
            }
            case NONE: event.allow();
            case null: event.allow();

        }
    }
}
