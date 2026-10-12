package fr.antidino.vertex.motd.motdAPI.manager.countryList;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import fr.antidino.vertex.motd.motdAPI.models.ServerModeEnum;
import org.bukkit.configuration.file.FileConfiguration;

public class ServerMode {
    MotdAPI plugin;
    private ServerModeEnum serverside;
    public ServerMode(MotdAPI plugin){
        this.plugin = plugin;
    }
    public void updateAccessServerMode(){
        FileConfiguration configuration = plugin.countryListConfig;
        switch (configuration.getString("mode","NONE")){
            case "NONE": serverside = ServerModeEnum.NONE;
            case "BLACKLIST" : serverside = ServerModeEnum.BLACKLIST;
            case "WHITELIST" : serverside = ServerModeEnum.WHITELIST;
        }
    }
    public ServerModeEnum getServerside(){
        return  serverside;
    }
}
