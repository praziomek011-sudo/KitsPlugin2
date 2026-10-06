package pl.kit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class KitEditorGUI {

    public static final int SLOT_HELMET = 36;
    public static final int SLOT_CHEST  = 37;
    public static final int SLOT_LEGS   = 38;
    public static final int SLOT_BOOTS  = 39;
    public static final int SLOT_OFFHAND = 40;
    public static final int SLOT_DISPLAY = 45;
    public static final int SLOT_INFO   = 49;
    public static final int SLOT_CANCEL = 52;
    public static final int SLOT_SAVE   = 53;

    public static void open(KitPlugin plugin, Player p, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Edycja: " + kit.getName());

        // ===== sloty 0-35 (itemy) =====
        for (int i = 0; i < 36; i++) {
            ItemStack existing = kit.getInventory()[i];
            inv.setItem(i, existing != null
                    ? existing.clone()
                    : makeGlassPlaceholder("§7Slot na item", "§7Wrzuc tutaj item z kitu"));
        }

        // ===== zbroja + offhand =====
        inv.setItem(SLOT_HELMET, kit.getHelmet() != null
                ? kit.getHelmet().clone()
                : makeArmorHint(Material.LEATHER_HELMET, "§7Hełm"));
        inv.setItem(SLOT_CHEST, kit.getChestplate() != null
                ? kit.getChestplate().clone()
                : makeArmorHint(Material.LEATHER_CHESTPLATE, "§7Napierśnik"));
        inv.setItem(SLOT_LEGS, kit.getLeggings() != null
                ? kit.getLeggings().clone()
                : makeArmorHint(Material.LEATHER_LEGGINGS, "§7Spodnie"));
        inv.setItem(SLOT_BOOTS, kit.getBoots() != null
                ? kit.getBoots().clone()
                : makeArmorHint(Material.LEATHER_BOOTS, "§7Buty"));
        inv.setItem(SLOT_OFFHAND, kit.getOffhand() != null
                ? kit.getOffhand().clone()
                : makeGlassPlaceholder("§7Offhand", "§7Wrzuc tutaj item do drugiej reki"));

        // ===== sloty dekoracyjne (nie mozna wkladac) =====
        ItemStack darkFiller = makeDarkFiller();
        int[] darkSlots = {41, 42, 43, 44, 46, 47, 48, 50, 51};
        for (int s : darkSlots) inv.setItem(s, darkFiller.clone());

        // ===== slot 45 - item reprezentujacy =====
        inv.setItem(SLOT_DISPLAY, kit.getDisplayItem() != null
                ? kit.getDisplayItem().clone()
                : makeDisplayHint());

        // ===== przyciski =====
        inv.setItem(SLOT_INFO,   makeInfo());
        inv.setItem(SLOT_CANCEL, makeCancel());
        inv.setItem(SLOT_SAVE,   makeSave());

        plugin.getEditingKit().put(p.getUniqueId(), kit.getName());
        plugin.getOpenEditor().put(p.getUniqueId(), inv);
        p.openInventory(inv);
    }

    // ==================== PLACEHOLDERY ====================

    private static void mark(ItemMeta meta) {
        meta.getPersistentDataContainer().set(
                KitPlugin.PLACEHOLDER_KEY, PersistentDataType.BYTE, (byte) 1);
    }

    public static boolean isPlaceholder(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(KitPlugin.PLACEHOLDER_KEY, PersistentDataType.BYTE);
    }

    private static ItemStack makeGlassPlaceholder(String name, String lore) {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        m.setLore(Arrays.asList(lore));
        mark(m);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack makeDarkFiller() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(" ");
        mark(m);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack makeArmorHint(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        m.setLore(Arrays.asList("§7Wrzuc tutaj " + name.substring(2).toLowerCase()));
        mark(m);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack makeDisplayHint() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName("§e§lItem reprezentujacy kit");
        m.setLore(Arrays.asList(
                "§7Wrzuc tutaj item ktory ma sie",
                "§7pokazywac w menu z kitami"));
        mark(m);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack makeInfo() {
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("§e§lInformacje");
        im.setLore(Arrays.asList(
                "§7Slot 36: §fHełm",
                "§7Slot 37: §fNapierśnik",
                "§7Slot 38: §fSpodnie",
                "§7Slot 39: §fButy",
                "§7Slot 40: §fOffhand",
                "§7Slot 45: §fItem reprezentujacy kit"));
        info.setItemMeta(im);
        return info;
    }

    private static ItemStack makeCancel() {
        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cm = cancel.getItemMeta();
        cm.setDisplayName("§c§lANULUJ");
        cm.setLore(Arrays.asList("§7Zamknij bez zapisywania"));
        cancel.setItemMeta(cm);
        return cancel;
    }

    private static ItemStack makeSave() {
        ItemStack save = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta sm = save.getItemMeta();
        sm.setDisplayName("§a§lZAPISZ");
        sm.setLore(Arrays.asList("§7Zapisz zmiany w kicie"));
        save.setItemMeta(sm);
        return save;
    }
}
