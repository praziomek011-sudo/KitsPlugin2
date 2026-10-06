package pl.kit;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final KitPlugin plugin;

    public ConfigManager(KitPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String prefix() {
        return color(cfg().getString("prefix", "&8[&eKity&8] &r"));
    }

    public String msg(String key) {
        String raw = cfg().getString("messages." + key, "&cBrak wiadomosci: " + key);
        return prefix() + color(raw);
    }

    public String msg(String key, String placeholder, String value) {
        return msg(key).replace(placeholder, value);
    }

    public String rawMsg(String key) {
        return color(cfg().getString("messages." + key, ""));
    }

    public int cooldownSeconds() {
        return cfg().getInt("cooldown-seconds", 0);
    }

    public String cooldownMessage() {
        return prefix() + color(cfg().getString("cooldown-message",
                "&cPoczekaj jeszcze &e%time%s &cprzed kolejnym odbiorem!"));
    }

    public String perm(String key) {
        return cfg().getString("permissions." + key, "kitplugin." + key);
    }

    public boolean enforcePermissions() {
        return cfg().getBoolean("permissions.enforce", false);
    }

    public boolean dropOnFullInventory() {
        return cfg().getBoolean("settings.drop-on-full-inventory", true);
    }

    public boolean autoSaveOnClose() {
        return cfg().getBoolean("settings.auto-save-on-close", true);
    }

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
