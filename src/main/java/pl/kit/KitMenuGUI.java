package pl.kit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KitMenuGUI {

    public static final String TITLE = "§8Kity";

    public static void open(KitPlugin plugin, Player p) {
        Collection<Kit> kits = plugin.getKitManager().getKits();
        ConfigManager cfg = plugin.getConfigManager();

        if (kits.isEmpty()) {
            p.sendMessage(cfg.msg("no-kits"));
            return;
        }

        int rows = Math.max(1, (int) Math.ceil(kits.size() / 9.0));
        int size = Math.min(rows * 9, 54);
        if (size < 9) size = 9;

        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        int slot = 0;
        for (Kit kit : kits) {
            if (slot >= size) break;
            ItemStack item = kit.getDisplayItem() != null
                    ? kit.getDisplayItem().clone()
                    : new ItemStack(Material.STONE);

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e§l" + kit.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Kliknij aby zobaczyc kit");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        p.openInventory(inv);
    }
}
