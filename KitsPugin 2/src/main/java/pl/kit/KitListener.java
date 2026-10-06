package pl.kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class KitListener implements Listener {
    private final KitPlugin plugin;

    public KitListener(KitPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        String title = e.getView().getTitle();
        Inventory inv = e.getInventory();

        // ===== Edytor =====
        String editing = plugin.getEditingKit().get(p.getUniqueId());
        if (editing != null && plugin.getOpenEditor().get(p.getUniqueId()) == inv) {
            if (e.getClickedInventory() == inv) {
                int slot = e.getRawSlot();

                if (slot == KitEditorGUI.SLOT_SAVE) {
                    e.setCancelled(true);
                    saveEditor(inv, editing);
                    plugin.getSkipSave().add(p.getUniqueId());
                    plugin.getEditingKit().remove(p.getUniqueId());
                    plugin.getOpenEditor().remove(p.getUniqueId());
                    p.closeInventory();
                    p.sendMessage("§aKit §e" + editing + " §azostal zapisany!");
                    return;
                }

                if (slot == KitEditorGUI.SLOT_CANCEL) {
                    e.setCancelled(true);
                    plugin.getSkipSave().add(p.getUniqueId());
                    plugin.getEditingKit().remove(p.getUniqueId());
                    plugin.getOpenEditor().remove(p.getUniqueId());
                    p.closeInventory();
                    p.sendMessage("§cAnulowano edycje.");
                    return;
                }

                if (slot == KitEditorGUI.SLOT_INFO) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }

        // ===== Menu z kitami =====
        if (title.equals(KitMenuGUI.TITLE)) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            if (!clicked.getItemMeta().hasDisplayName()) return;

            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Kit kit = plugin.getKitManager().getKit(name);
            if (kit != null) {
                KitPreviewGUI.open(plugin, p, kit);
            }
            return;
        }

        // ===== Podgląd kitu =====
        if (title.startsWith("§8Kit: ")) {
            e.setCancelled(true);
            String name = title.substring("§8Kit: ".length());
            Kit kit = plugin.getKitManager().getKit(name);
            if (kit == null) return;

            int slot = e.getRawSlot();
            if (e.getClickedInventory() != inv) return;

            if (slot == KitPreviewGUI.SLOT_CONFIRM) {
                giveKit(p, kit);
                p.closeInventory();
                p.sendMessage("§aOtrzymales kit §e" + kit.getName() + "§a!");
            } else if (slot == KitPreviewGUI.SLOT_CANCEL) {
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;

        String editing = plugin.getEditingKit().get(p.getUniqueId());
        if (editing == null) return;

        Inventory inv = plugin.getOpenEditor().get(p.getUniqueId());
        if (inv == null || e.getInventory() != inv) return;

        if (plugin.getSkipSave().remove(p.getUniqueId())) {
            plugin.getEditingKit().remove(p.getUniqueId());
            plugin.getOpenEditor().remove(p.getUniqueId());
            return;
        }

        // zapis przy zamknieciu krzyzykiem
        saveEditor(inv, editing);
        plugin.getEditingKit().remove(p.getUniqueId());
        plugin.getOpenEditor().remove(p.getUniqueId());
        p.sendMessage("§aKit §e" + editing + " §azostal automatycznie zapisany.");
    }

    private void saveEditor(Inventory inv, String kitName) {
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) return;

        for (int i = 0; i < 36; i++) {
            kit.getInventory()[i] = inv.getItem(i);
        }
        kit.setHelmet(inv.getItem(KitEditorGUI.SLOT_HELMET));
        kit.setChestplate(inv.getItem(KitEditorGUI.SLOT_CHEST));
        kit.setLeggings(inv.getItem(KitEditorGUI.SLOT_LEGS));
        kit.setBoots(inv.getItem(KitEditorGUI.SLOT_BOOTS));
        kit.setOffhand(inv.getItem(KitEditorGUI.SLOT_OFFHAND));

        ItemStack display = inv.getItem(KitEditorGUI.SLOT_DISPLAY);
        if (display != null) kit.setDisplayItem(display);

        plugin.getKitManager().save();
    }

    private void giveKit(Player p, Kit kit) {
        for (ItemStack item : kit.getInventory()) {
            if (item == null) continue;
            Map<Integer, ItemStack> leftover = p.getInventory().addItem(item.clone());
            for (ItemStack drop : leftover.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), drop);
            }
        }
        if (kit.getHelmet() != null) p.getInventory().setHelmet(kit.getHelmet().clone());
        if (kit.getChestplate() != null) p.getInventory().setChestplate(kit.getChestplate().clone());
        if (kit.getLeggings() != null) p.getInventory().setLeggings(kit.getLeggings().clone());
        if (kit.getBoots() != null) p.getInventory().setBoots(kit.getBoots().clone());
        if (kit.getOffhand() != null) p.getInventory().setItemInOffHand(kit.getOffhand().clone());
    }
}