package pl.kit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KitEditorGUI {

    public static final int SLOT_HELMET = 36;
    public static final int SLOT_CHEST = 37;
    public static final int SLOT_LEGS = 38;
    public static final int SLOT_BOOTS = 39;
    public static final int SLOT_OFFHAND = 40;
    public static final int SLOT_DISPLAY = 45;
    public static final int SLOT_INFO = 49;
    public static final int SLOT_CANCEL = 52;
    public static final int SLOT_SAVE = 53;

    public static void open(KitPlugin plugin, Player p, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Edycja: " + kit.getName());

        // wypełniacz dolnego paska
        ItemStack filler = makeFiller();
        for (int i = 36; i < 54; i++) inv.setItem(i, filler.clone());

        // itemy kitu
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, kit.getInventory()[i]);
        }

        // zbroja + offhand
        inv.setItem(SLOT_HELMET, kit.getHelmet());
        inv.setItem(SLOT_CHEST, kit.getChestplate());
        inv.setItem(SLOT_LEGS, kit.getLeggings());
        inv.setItem(SLOT_BOOTS, kit.getBoots());
        inv.setItem(SLOT_OFFHAND, kit.getOffhand());

        // item reprezentujacy
        inv.setItem(SLOT_DISPLAY, kit.getDisplayItem() != null
                ? kit.getDisplayItem().clone()
                : new ItemStack(Material.STONE));

        // info
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("§e§lInformacje");
        im.setLore(Arrays.asList(
                "§7Slot 36: §fHełm",
                "§7Slot 37: §fNapierśnik",
                "§7Slot 38: §fSpodnie",
                "§7Slot 39: §fButy",
                "§7Slot 40: §fOffhand",
                "§7Slot 45: §fItem reprezentujacy kit"
        ));
        info.setItemMeta(im);
        inv.setItem(SLOT_INFO, info);

        // cancel
        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cm = cancel.getItemMeta();
        cm.setDisplayName("§c§lANULUJ");
        cm.setLore(Arrays.asList("§7Zamknij bez zapisywania"));
        cancel.setItemMeta(cm);
        inv.setItem(SLOT_CANCEL, cancel);

        // save
        ItemStack save = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta sm = save.getItemMeta();
        sm.setDisplayName("§a§lZAPISZ");
        sm.setLore(Arrays.asList("§7Zapisz zmiany w kicie"));
        save.setItemMeta(sm);
        inv.setItem(SLOT_SAVE, save);

        plugin.getEditingKit().put(p.getUniqueId(), kit.getName());
        plugin.getOpenEditor().put(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    private static ItemStack makeFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        return filler;
    }
}