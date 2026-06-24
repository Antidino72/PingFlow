package fr.antidino.vertex.motd.motdAPI.manager;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import org.bukkit.configuration.file.FileConfiguration;


import java.util.HashMap;
import java.util.Map;

public final class MinecraftVersionManager {
    MotdAPI motdAPI;

    private static final Map<Integer, String> VERSIONS = new HashMap<>();

    public MinecraftVersionManager(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }
    public void load() {
        FileConfiguration config = motdAPI.versionConfig;

        if (config == null) {
            motdAPI.getLogger().severe("[MotdAPI] versionConfig est null !");

        }

        VERSIONS.clear();

        for (String key : config.getKeys(false)) {
            try {
                String version = config.getString(key);
                VERSIONS.put(Integer.parseInt(key), version);

                motdAPI.getLogger().info(
                        "Loaded version " + key + " -> " + version
                );

            } catch (NumberFormatException ignored) {}
        }

        motdAPI.getLogger().info(
                "Loaded " + VERSIONS.size() + " protocol versions."
        );

    }


    public static String getVersionName(int protocol) {

        return VERSIONS.getOrDefault(protocol, "Version inconnue (" + protocol + ")");
    }
}