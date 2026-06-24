package fr.antidino.vertex.motd.motdAPI.resolvers;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import fr.antidino.vertex.motd.motdAPI.manager.ScheduleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MotdResolver {
    MotdAPI motdAPI;
    private final Random random = new Random();
    public MotdResolver(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }
    public String resolveMotd(String country) {
        FileConfiguration config = motdAPI.config;
        String motd = formatMotd(config.getStringList("motd"));
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
                            return formatMotd(rule.getStringList("text"));
                        }
                        break;

                    case "ping":
                        List<String> keys = rule.getStringList("text");
                        if (!keys.isEmpty()) {
                            return formatMotd(List.of(keys.get(random.nextInt(keys.size())))); // ✅ return direct
                        }
                        break;
                    case "clock":
                        return formatMotd(List.of(motdAPI.scheduleManager.getMotd()));
                    default:
                        break;
                }
            }
        }
        return motd;
    }
    private String formatMotd(List<String> lines) {
        return lines.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.joining("\n"));
    }
}
