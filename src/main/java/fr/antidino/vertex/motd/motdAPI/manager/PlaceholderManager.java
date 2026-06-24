package fr.antidino.vertex.motd.motdAPI.manager;

import fr.antidino.vertex.motd.motdAPI.models.PlaceholderData;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderManager {

    private static final Map<String, PlaceholderData> DATA = new HashMap<>();

    public static void register(
            String ip,
            int protocol,
            String hostname
    ) {
        DATA.put(ip, new PlaceholderData(
                protocol,
                hostname,
                ip
        ));
    }
    public static Boolean isIpRegister(String ip){
        return DATA.containsKey(ip);
    }
    public static String build(String template, String ip) {
        PlaceholderData data = DATA.get(ip);

        if (data == null) {
            return template;
        }

        return template
                .replace("%version%", MinecraftVersionManager.getVersionName(data.getProtocol()))
                .replace("%protocol%", String.valueOf(data.getProtocol()))
                .replace("%hostname%", data.getHostname())
                .replace("%ip%", data.getIp());
    }

    public static void remove(String ip) {
        DATA.remove(ip);
    }
}