package pl.kit;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class KitPlugin extends JavaPlugin {
    private static KitPlugin instance;
    private KitManager kitManager;
    private ConfigManager configManager;

    public static NamespacedKey PLACEHOLDER_KEY;

    private final Map<UUID, String> editingKit = new HashMap<>();
    private final Map<UUID, Inventory> openEditor = new HashMap<>();
    private final Set<UUID> skipSave = new HashSet<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        PLACEHOLDER_KEY = new NamespacedKey(this, "placeholder");

        configManager = new ConfigManager(this);
        kitManager = new KitManager(this);
        kitManager.load();

        KitCommand kitCmd = new KitCommand(this);
        getCommand("kit").setExecutor(kitCmd);
        getCommand("kit").setTabCompleter(kitCmd);

        getServer().getPluginManager().registerEvents(new KitListener(this), this);
        getLogger().info("KitPlugin wlaczony!");
    }

    @Override
    public void onDisable() {
        kitManager.save();
    }

    public static KitPlugin getInstance() { return instance; }
    public KitManager getKitManager() { return kitManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public Map<UUID, String> getEditingKit() { return editingKit; }
    public Map<UUID, Inventory> getOpenEditor() { return openEditor; }
    public Set<UUID> getSkipSave() { return skipSave; }
    public Map<UUID, Map<String, Long>> getCooldowns() { return cooldowns; }
}
