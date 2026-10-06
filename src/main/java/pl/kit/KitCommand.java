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

        // ===== /kit create [nazwa] [item] =====
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
            Kit kit = new Kit(name);
            kit.setDisplayItem(new ItemStack(mat));
            plugin.getKitManager().addKit(kit);
            plugin.getKitManager().save();
            p.sendMessage(cfg.msg("kit-created", "%kit%", name));
            return true;
        }

        // ===== /kit edit [nazwa] =====
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
