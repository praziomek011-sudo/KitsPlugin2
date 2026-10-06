package pl.kit;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommand implements CommandExecutor {
    private final KitPlugin plugin;

    public KitCommand(KitPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cTylko gracz moze uzywac tej komendy!");
            return true;
        }

        if (args.length == 0) {
            KitMenuGUI.open(plugin, p);
            return true;
        }

        // /kit create [nazwa] [item]
        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 3) {
                p.sendMessage("§cUzycie: /kit create [nazwa] [item]");
                return true;
            }
            String name = args[1];
            Material mat = Material.matchMaterial(args[2].toUpperCase());
            if (mat == null || mat.isAir()) {
                p.sendMessage("§cNieprawidlowy item: " + args[2]);
                return true;
            }
            if (plugin.getKitManager().getKit(name) != null) {
                p.sendMessage("§cKit o nazwie '" + name + "' juz istnieje!");
                return true;
            }
            Kit kit = new Kit(name);
            kit.setDisplayItem(new ItemStack(mat));
            plugin.getKitManager().addKit(kit);
            plugin.getKitManager().save();
            p.sendMessage("§aUtworzono kit §e" + name + "§a! Uzyj §e/kit edit " + name + "§a aby go edytowac.");
            return true;
        }

        // /kit edit [nazwa]
        if (args[0].equalsIgnoreCase("edit")) {
            if (args.length < 2) {
                p.sendMessage("§cUzycie: /kit edit [nazwa]");
                return true;
            }
            Kit kit = plugin.getKitManager().getKit(args[1]);
            if (kit == null) {
                p.sendMessage("§cNie znaleziono kitu: " + args[1]);
                return true;
            }
            KitEditorGUI.open(plugin, p, kit);
            return true;
        }

        // /kit [nazwa] - otwiera podglad
        Kit kit = plugin.getKitManager().getKit(args[0]);
        if (kit == null) {
            p.sendMessage("§cNie znaleziono kitu: " + args[0]);
            return true;
        }
        KitPreviewGUI.open(plugin, p, kit);
        return true;
    }
}