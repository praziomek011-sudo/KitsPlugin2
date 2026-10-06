package pl.kit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class KitListener implements Listener {
    private final KitPlugin plugin;
    private final ConfigManager cfg;

    public KitListener(KitPlugin plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
    }

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
                    p.sendMessage(cfg.msg("kit-saved", "%kit%", editing));
                    return;
                }

                if (slot == KitEditorGUI.SLOT_CANCEL) {
                    e.setCancelled(true);
                    plugin.getSkipSave().add(p.getUniqueId());
                    plugin.getEditingKit().remove(p.getUniqueId());
                    plugin.getOpenEditor().remove(p.getUniqueId());
                    p.closeInventory();
                    p.sendMessage(cfg.msg("edit-cancelled"));
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

            if (e.getClickedInventory() != inv) return;
            int slot = e.getRawSlot();

            if (slot == KitPreviewGUI.SLOT_CONFIRM) {
                // sprawdz permisje per-kit
                if (cfg.enforcePermissions() && !p.isOp()) {
                    String perKit = "kitplugin.kit." + kit.getName().toLowerCase();
                    boolean hasGlobal = p.hasPermission(cfg.perm("receive"));
                    boolean hasKit = p.hasPermission(perKit);
                    if (!hasGlobal && !hasKit) {
                        p.sendMessage(cfg.msg("no-permission-kit"));
                        p.closeInventory();
                        return;
                    }
                }

                // sprawdz cooldown
                long remaining = getRemainingCooldown(p, kit.getName());
                if (remaining > 0) {
                    String msg = cfg.cooldownMessage()
                            .replace("%time%", String.valueOf(remaining))
                            .replace("%kit%", kit.getName());
                    p.sendMessage(msg);
                    p.closeInventory();
                    return;
                }

                giveKit(p, kit);
                setCooldown(p, kit.getName());
                p.closeInventory();
                p.sendMessage(cfg.msg("kit-received", "%kit%", kit.getName()));
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
        if (cfg.autoSaveOnClose()) {
            saveEditor(inv, editing);
            p.sendMessage(cfg.msg("kit-auto-saved", "%kit%", editing));
        }
        plugin.getEditingKit().remove(p.getUniqueId());
        plugin.getOpenEditor().remove(p.getUniqueId());
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
            if (cfg.dropOnFullInventory()) {
                for (ItemStack drop : leftover.values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), drop);
                }
            }
        }
        if (kit.getHelmet() != null) p.getInventory().setHelmet(kit.getHelmet().clone());
        if (kit.getChestplate() != null) p.getInventory().setChestplate(kit.getChestplate().clone());
        if (kit.getLeggings() != null) p.getInventory().setLeggings(kit.getLeggings().clone());
        if (kit.getBoots() != null) p.getInventory().setBoots(kit.getBoots().clone());
        if (kit.getOffhand() != null) p.getInventory().setItemInOffHand(kit.getOffhand().clone());
    }

    // ===== Cooldown =====
    private long getRemainingCooldown(Player p, String kitName) {
        int sec = cfg.cooldownSeconds();
        if (sec <= 0) return 0;

        Map<String, Long> map = plugin.getCooldowns().get(p.getUniqueId());
        if (map == null) return 0;

        Long end = map.get(kitName.toLowerCase());
        if (end == null) return 0;

        long diff = end - System.currentTimeMillis();
        if (diff <= 0) return 0;
        return (diff / 1000) + 1;
    }

    private void setCooldown(Player p, String kitName) {
        int sec = cfg.cooldownSeconds();
        if (sec <= 0) return;

        plugin.getCooldowns()
                .computeIfAbsent(p.getUniqueId(), k -> new HashMap<>())
                .put(kitName.toLowerCase(), System.currentTimeMillis() + (sec * 1000L));
    }
}
