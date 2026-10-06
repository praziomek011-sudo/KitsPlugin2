package pl.kit;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements CommandExecutor, TabCompleter {
    private final KitPlugin plugin;
    private final ConfigManager cfg;

    public KitCommand(KitPlugin plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfigManager();
    }

    private boolean hasPerm(Player p, String key) {
        if (!cfg.enforcePermissions()) return true;
        if (p.isOp()) return true;
        return p.hasPermission(cfg.perm(key));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(cfg.rawMsg("player-only"));
            return true;
        }

        // ===== /kit =====
        if (args.length == 0) {
            if (!hasPerm(p, "use")) {
                p.sendMessage(cfg.msg("no-permission"));
                return true;
            }
            KitMenuGUI.open(plugin, p);
            return true;
        }

        // ===== /kit create [nazwa] [item] [slot] =====
        if (args[0].equalsIgnoreCase("create")) {
            if (!hasPerm(p, "create")) {
                p.sendMessage(cfg.msg("no-permission"));
                return true;
            }
            if (args.length < 3) {
                p.sendMessage(cfg.msg("usage-create"));
                return true;
            }
            String name = args[1];
            Material mat = Material.matchMaterial(args[2].toUpperCase());
            if (mat == null || mat.isAir()) {
                p.sendMessage(cfg.msg("invalid-item", "%item%", args[2]));
                return true;
            }
            if (plugin.getKitManager().getKit(name) != null) {
                p.sendMessage(cfg.msg("kit-exists", "%kit%", name));
                return true;
            }

            // slot (opcjonalny)
            int slot = -1;
            if (args.length >= 4) {
                try {
                    slot = Integer.parseInt(args[3]);
                } catch (NumberFormatException ex) {
                    p.sendMessage("§cSlot musi byc liczba (0-53)!");
                    return true;
                }
                if (slot < 0 || slot > 53) {
                    p.sendMessage("§cSlot musi byc w zakresie 0-53!");
                    return true;
                }
                Kit occupied = plugin.getKitManager().getKitBySlot(slot, null);
                if (occupied != null) {
                    p.sendMessage("§cSlot " + slot + " jest zajety przez kit §e" + occupied.getName() + "§c!");
                    return true;
                }
            }

            Kit kit = new Kit(name);
            kit.setDisplayItem(new ItemStack(mat));
            kit.setMenuSlot(slot);
            plugin.getKitManager().addKit(kit);
            plugin.getKitManager().save();

            p.sendMessage(cfg.msg("kit-created", "%kit%", name));
            if (slot >= 0) {
                p.sendMessage("§7Kit bedzie na slocie §e" + slot + " §7w menu.");
            } else {
                p.sendMessage("§7Kit bedzie na pierwszym wolnym slocie w menu.");
            }
            return true;
        }

        // ===== /kit edit [nazwa] [slot] =====
        if (args[0].equalsIgnoreCase("edit")) {
            if (!hasPerm(p, "edit")) {
                p.sendMessage(cfg.msg("no-permission"));
                return true;
            }
            if (args.length < 2) {
                p.sendMessage(cfg.msg("usage-edit"));
                return true;
            }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                p.sendMessage(cfg.msg("kit-not-found", "%kit%", args[1]));
                return true;
            }

            // jesli podano slot - zmien go
            if (args.length >= 3) {
                int slot;
                try {
                    slot = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    p.sendMessage("§cSlot musi byc liczba (0-53)!");
                    return true;
                }
                if (slot < 0 || slot > 53) {
                    p.sendMessage("§cSlot musi byc w zakresie 0-53!");
                    return true;
                }
                Kit occupied = plugin.getKitManager().getKitBySlot(slot, kit);
                if (occupied != null) {
                    p.sendMessage("§cSlot " + slot + " jest zajety przez kit §e" + occupied.getName() + "§c!");
                    return true;
                }
                kit.setMenuSlot(slot);
                plugin.getKitManager().save();
                p.sendMessage("§aKit §e" + kit.getName() + " §abedzie teraz na slocie §e" + slot + "§a.");
                return true;
            }

            // bez slotu - otworz edytor
            KitEditorGUI.open(plugin, p, kit);
            return true;
        }

        // ===== /kit delete [nazwa] =====
        if (args[0].equalsIgnoreCase("delete")) {
            if (!hasPerm(p, "delete")) {
                p.sendMessage(cfg.msg("no-permission"));
                return true;
            }
            if (args.length < 2) {
                p.sendMessage(cfg.msg("usage-delete"));
                return true;
            }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                p.sendMessage(cfg.msg("kit-not-found", "%kit%", args[1]));
                return true;
            }
            plugin.getKitManager().removeKit(args[1]);
            plugin.getKitManager().save();
            p.sendMessage(cfg.msg("kit-deleted", "%kit%", args[1]));
            return true;
        }

        // ===== /kit [nazwa] - podglad =====
        if (!hasPerm(p, "use")) {
            p.sendMessage(cfg.msg("no-permission"));
            return true;
        }
        Kit kit = plugin.getKitManager().getKit(args[0]);
        if (kit == null) {
            p.sendMessage(cfg.msg("kit-not-found", "%kit%", args[0]));
            return true;
        }
        KitPreviewGUI.open(plugin, p, kit);
        return true;
    }

    // ==================== TAB COMPLETION ====================
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("create");
            options.add("edit");
            options.add("delete");
            for (Kit k : plugin.getKitManager().getKits()) {
                options.add(k.getName());
            }
            return filter(options, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("edit") || sub.equals("delete")) {
                List<String> names = plugin.getKitManager().getKits().stream()
                        .map(Kit::getName)
                        .collect(Collectors.toList());
                return filter(names, args[1]);
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("edit")) {
                // podpowiedz sloty dla /kit edit [nazwa] [slot]
                List<String> slots = new ArrayList<>();
                for (int i = 0; i <= 53; i++) slots.add(String.valueOf(i));
                return filter(slots, args[2]);
            }
        }

        if (args.length == 4) {
            String sub = args[0].toLowerCase();
            if (sub.equals("create")) {
                List<String> slots = new ArrayList<>();
                for (int i = 0; i <= 53; i++) slots.add(String.valueOf(i));
                return filter(slots, args[3]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        String p = prefix.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(p))
                .sorted()
                .collect(Collectors.toList());
    }
}
