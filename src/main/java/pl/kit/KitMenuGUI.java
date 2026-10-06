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
    public static final int MAX_SLOT = 53;

    public static void open(KitPlugin plugin, Player p) {
        Collection<Kit> kits = plugin.getKitManager().getKits();
        ConfigManager cfg = plugin.getConfigManager();

        if (kits.isEmpty()) {
            p.sendMessage(cfg.msg("no-kits"));
            return;
        }

        // 1) najwyzszy slot z jawnie ustawionych
        int highest = -1;
        for (Kit k : kits) {
            if (k.getMenuSlot() >= 0 && k.getMenuSlot() > highest)
                highest = k.getMenuSlot();
        }

        // 2) rozmiar menu: musi zmiescic najwyzszy slot, liczbe kitow i byc wielokrotnoscia 9
        int need = Math.max(highest + 1, kits.size());
        int size = Math.min(((need + 8) / 9) * 9, 54);
        if (size < 9) size = 9;

        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        // 3) najpierw kity z jawnie ustawionym slotem
        List<Kit> auto = new ArrayList<>();
        for (Kit k : kits) {
            if (k.getMenuSlot() >= 0 && k.getMenuSlot() < size) {
                inv.setItem(k.getMenuSlot(), buildItem(k));
            } else {
                auto.add(k);
            }
        }

        // 4) reszta (auto / poza zakresem) na pierwsze wolne sloty
        int s = 0;
        for (Kit k : auto) {
            while (s < size && inv.getItem(s) != null) s++;
            if (s >= size) break;
            inv.setItem(s, buildItem(k));
            s++;
        }

        p.openInventory(inv);
    }

    private static ItemStack buildItem(Kit kit) {
        ItemStack item = kit.getDisplayItem() != null
                ? kit.getDisplayItem().clone()
                : new ItemStack(Material.STONE);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l" + kit.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Kliknij aby zobaczyc kit");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
