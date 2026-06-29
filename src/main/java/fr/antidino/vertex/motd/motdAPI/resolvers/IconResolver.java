package fr.antidino.vertex.motd.motdAPI.resolvers;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class IconResolver {
    MotdAPI motdAPI;
    public final Map<String, CachedServerIcon> cache = new HashMap<String, CachedServerIcon>();
    private Random random = new Random();
    public IconResolver(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }
    public void init() {
        FileConfiguration config = motdAPI.config;

        // ✅ Icône default avec resize
        cache.put("default", loadIconFromPath(config.getString("icon")));

        // Parcourir les sections custom-motd
        ConfigurationSection customMotd = config.getConfigurationSection("custom-motd");
        if (customMotd == null) return;

        for (String key : customMotd.getKeys(false)) {
            String iconPath = config.getString("custom-motd." + key + ".icon");
            if (iconPath == null) continue;

            CachedServerIcon icon = loadIconFromPath(iconPath);
            if (icon != null) cache.put(key, icon);
        }
    }

    public CachedServerIcon loadIconFromPath(String path) {
        if (path == null) return null;

        File file = new File(motdAPI.getDataFolder(), path);
        if (!file.exists()) {
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return null;
            }

            if (image.getWidth() != 64 || image.getHeight() != 64) {
                BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = resized.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(image, 0, 0, 64, 64, null);
                g.dispose();
                image = resized;

            }

            return Bukkit.loadServerIcon(image);

        } catch (Exception e) {
            motdAPI.getLogger().warning("Erreur icône " + path + " : " + e.getMessage());
            return null;
        }
    }
    public String resolveIcon(String country) {
        FileConfiguration config = motdAPI.config;
        ConfigurationSection customMotd = config.getConfigurationSection("custom-motd");

        if (customMotd != null) {
            List<Map.Entry<String, ConfigurationSection>> rules = new ArrayList<>();

            for (String key : customMotd.getKeys(false)) {
                ConfigurationSection rule = customMotd.getConfigurationSection(key);
                if (rule != null) rules.add(Map.entry(key, rule));
            }

            rules.sort(Comparator.comparingInt(e -> e.getValue().getInt("priority", 99)));

            for (Map.Entry<String, ConfigurationSection> entry : rules) {
                ConfigurationSection rule = entry.getValue();
                String type = rule.getString("type");

                if (type == null) continue; // ✅ évite NullPointerException

                switch (type) {
                    case "country":
                        if (country == null) break; // ✅ pas de GeoIP = skip
                        String ruleCountry = rule.getString("country"); // ✅ minuscule
                        if (country.equalsIgnoreCase(ruleCountry)) {
                            return rule.getString("icon");// ✅ return direct
                        }
                        break;

                    case "ping":
                        List<String> icons = new ArrayList<>();
                        for (String path : rule.getStringList("icon")) {
                            icons.add(path);
                        }
                        if (!icons.isEmpty()) {
                            return icons.get(random.nextInt(icons.size()));
                        }
                        break;
                    case "clock":
                        return motdAPI.scheduleManager.getIconPath();
                    default:
                        break;
                }
            }
        }

        return config.getString("icon"); // ✅ fallback propre
    }
}
