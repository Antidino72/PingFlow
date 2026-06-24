package fr.antidino.vertex.motd.motdAPI.manager;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ScheduleManager {

    private final MotdAPI motdAPI;
    private FileConfiguration config;

    public ScheduleManager(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }

    public void load() {
        this.config = motdAPI.clockConfig;
    }

    // ------------------------------------------
    // Public API - returns raw values from config
    // ------------------------------------------

    public String getMotd() {
        return resolve().motd;
    }

    public String getIconPath() {
        return resolve().icon;
    }

    // ------------------------------------------
    // Internal resolution
    // ------------------------------------------

    private ScheduleEntry resolve() {
        ZonedDateTime now =  ZonedDateTime.now(ZoneId.of(motdAPI.config.getString("timezone")));

        List<Map<?, ?>> schedule = config.getMapList("schedule");

        for (Map<?, ?> entry : schedule) {
            Object matchObj = entry.get("match");
            if (!(matchObj instanceof Map<?, ?> match)) continue;

            if (matches(match, now)) {
                String motd = (String) entry.get("motd");
                String icon = (String) entry.get("icon");
                return new ScheduleEntry(motd, icon);
            }
        }

        // Fallback to default
        return new ScheduleEntry(
                config.getString("default.motd", "&cAucun MOTD configuré"),
                config.getString("default.icon", "icon/default.png")
        );
    }

    private boolean matches(Map<?, ?> match, ZonedDateTime now) {
        // Exact fields
        if (match.containsKey("year")    && toInt(match.get("year"))   != now.getYear())           return false;
        if (match.containsKey("month")   && toInt(match.get("month"))  != now.getMonthValue())     return false;
        if (match.containsKey("day")     && toInt(match.get("day"))    != now.getDayOfMonth())     return false;
        if (match.containsKey("hour")    && toInt(match.get("hour"))   != now.getHour())           return false;
        if (match.containsKey("minute")  && toInt(match.get("minute")) != now.getMinute())         return false;

        // Weekday
        if (match.containsKey("weekday") &&
                !match.get("weekday").toString().equalsIgnoreCase(now.getDayOfWeek().name()))           return false;

        // Hour range (inclusive)
        if (match.containsKey("hour_from") && now.getHour() < toInt(match.get("hour_from")))       return false;
        if (match.containsKey("hour_to")   && now.getHour() > toInt(match.get("hour_to")))         return false;

        return true;
    }

    private int toInt(Object value) {
        if (value instanceof Integer i) return i;
        return Integer.parseInt(value.toString());
    }

    // ------------------------------------------
    // Internal record
    // ------------------------------------------

    private record ScheduleEntry(String motd, String icon) {}
}