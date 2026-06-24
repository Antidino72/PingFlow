package fr.antidino.vertex.motd.motdAPI.manager;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import fr.antidino.vertex.motd.motdAPI.MotdAPI;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;

/**
 * Résout le code pays (ISO) d'une adresse IP via une base MaxMind GeoLite2-Country.
 * Si la base n'est pas trouvée ou désactivée, retourne toujours "??".
 */
public class GeoIPManager {

    private final MotdAPI plugin;
    private DatabaseReader reader;
    private boolean enabled;

    public GeoIPManager(MotdAPI plugin) {
        this.plugin = plugin;
    }

    public void init() {
        enabled = plugin.getConfig().getBoolean("geoip.enabled", true);
        if (!enabled) return;

        String fileName = plugin.getConfig().getString("geoip.database-file", "GeoLite2-Country.mmdb");
        File dbFile = new File(plugin.getDataFolder(), fileName);

        if (!dbFile.exists()) {
            plugin.getLogger().warning("Base GeoIP introuvable (" + dbFile.getPath() + "). " +
                    "Télécharge GeoLite2-Country.mmdb depuis MaxMind et place-la dans le dossier du plugin. " +
                    "Les conditions basées sur le pays seront désactivées en attendant.");
            enabled = false;
            return;
        }

        try {
            reader = new DatabaseReader.Builder(dbFile).build();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de charger la base GeoIP", e);
            enabled = false;
        }
    }

    public String resolveCountry(String ip) {
        if (!enabled || reader == null) return "??";
        try {
            InetAddress address = InetAddress.getByName(ip);
            CountryResponse response = reader.country(address);
            return response.getCountry().getIsoCode() != null ? response.getCountry().getIsoCode() : "??";
        } catch (Exception e) {
            return "??";
        }
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}