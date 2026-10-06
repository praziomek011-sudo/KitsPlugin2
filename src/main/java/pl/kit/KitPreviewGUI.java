package pl.kit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class KitPreviewGUI {

    public static final int SLOT_CONFIRM = 48;
    public static final int SLOT_CANCEL = 50;

    public static void open(KitPlugin plugin, Player p, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Kit: " + kit.getName());

        // filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < 54; i++) inv.setItem(i, filler.clone());

        // itemy z kitu
        for (int i = 0; i < 36; i++) {
            if (kit.getInventory()[i] != null) {
                inv.setItem(i, kit.getInventory()[i].clone());
            }
        }

        // zbroja
        if (kit.getHelmet() != null) inv.setItem(37, kit.getHelmet().clone());
        if (kit.getChestplate() != null) inv.setItem(38, kit.getChestplate().clone());
        if (kit.getLeggings() != null) inv.setItem(39, kit.getLeggings().clone());
        if (kit.getBoots() != null) inv.setItem(40, kit.getBoots().clone());
        if (kit.getOffhand() != null) inv.setItem(42, kit.getOffhand().clone());

        // info
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("§e§lKit: §f" + kit.getName());
        im.setLore(Arrays.asList("§7Czy chcesz odebrac ten kit?"));
        info.setItemMeta(im);
        inv.setItem(45, info);

        // confirm
        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta cm = confirm.getItemMeta();
        cm.setDisplayName("§a§lPOTWIERDŹ ODBIÓR");
        cm.setLore(Arrays.asList("§7Kliknij aby odebrac kit"));
        confirm.setItemMeta(cm);
        inv.setItem(SLOT_CONFIRM, confirm);

        // cancel
        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta ccm = cancel.getItemMeta();
        ccm.setDisplayName("§c§lANULUJ");
        ccm.setLore(Arrays.asList("§7Kliknij aby anulowac"));
        cancel.setItemMeta(ccm);
        inv.setItem(SLOT_CANCEL, cancel);

        p.openInventory(inv);
    }
}