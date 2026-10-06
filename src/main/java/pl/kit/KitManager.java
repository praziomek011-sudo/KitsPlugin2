package pl.kit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {
    private final KitPlugin plugin;
    private final Map<String, Kit> kits = new LinkedHashMap<>();

    public KitManager(KitPlugin plugin) { this.plugin = plugin; }

    public void load() {
        kits.clear();
        File file = new File(plugin.getDataFolder(), "kits.yml");
        if (!file.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            ConfigurationSection sec = cfg.getConfigurationSection(key);
            if (sec == null) continue;
            Kit kit = new Kit(key);
            kit.setDisplayItem(sec.getItemStack("display"));
            kit.setMenuSlot(sec.getInt("slot", -1));
            for (int i = 0; i < 36; i++) {
                kit.getInventory()[i] = sec.getItemStack("inv." + i);
            }
            kit.setHelmet(sec.getItemStack("armor.helmet"));
            kit.setChestplate(sec.getItemStack("armor.chestplate"));
            kit.setLeggings(sec.getItemStack("armor.leggings"));
            kit.setBoots(sec.getItemStack("armor.boots"));
            kit.setOffhand(sec.getItemStack("offhand"));
            kits.put(key.toLowerCase(), kit);
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Kit kit : kits.values()) {
            String k = kit.getName();
            cfg.set(k + ".display", kit.getDisplayItem());
            cfg.set(k + ".slot", kit.getMenuSlot());
            for (int i = 0; i < 36; i++) {
                if (kit.getInventory()[i] != null)
                    cfg.set(k + ".inv." + i, kit.getInventory()[i]);
            }
            cfg.set(k + ".armor.helmet", kit.getHelmet());
            cfg.set(k + ".armor.chestplate", kit.getChestplate());
            cfg.set(k + ".armor.leggings", kit.getLeggings());
            cfg.set(k + ".armor.boots", kit.getBoots());
            cfg.set(k + ".offhand", kit.getOffhand());
        }
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            cfg.save(new File(plugin.getDataFolder(), "kits.yml"));
        } catch (IOException e) {
            plugin.getLogger().severe("Nie mozna zapisac kits.yml: " + e.getMessage());
        }
    }

    public Kit getKit(String name) {
        if (name == null) return null;
        return kits.get(name.toLowerCase());
    }

    public void addKit(Kit kit) { kits.put(kit.getName().toLowerCase(), kit); }

    public void removeKit(String name) { kits.remove(name.toLowerCase()); }

    public Collection<Kit> getKits() { return kits.values(); }

    // sprawdza czy dany slot jest zajety przez inny kit
    public Kit getKitBySlot(int slot, Kit except) {
        for (Kit k : kits.values()) {
            if (k == except) continue;
            if (k.getMenuSlot() == slot) return k;
        }
        return null;
    }
}
