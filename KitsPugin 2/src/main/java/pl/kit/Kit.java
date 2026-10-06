package pl.kit;

import org.bukkit.inventory.ItemStack;

public class Kit {
    private final String name;
    private ItemStack displayItem;
    private final ItemStack[] inventory = new ItemStack[36];
    private ItemStack helmet, chestplate, leggings, boots, offhand;

    public Kit(String name) { this.name = name; }

    public String getName() { return name; }

    public ItemStack getDisplayItem() { return displayItem; }
    public void setDisplayItem(ItemStack displayItem) { this.displayItem = displayItem; }

    public ItemStack[] getInventory() { return inventory; }

    public ItemStack getHelmet() { return helmet; }
    public void setHelmet(ItemStack h) { this.helmet = h; }

    public ItemStack getChestplate() { return chestplate; }
    public void setChestplate(ItemStack c) { this.chestplate = c; }

    public ItemStack getLeggings() { return leggings; }
    public void setLeggings(ItemStack l) { this.leggings = l; }

    public ItemStack getBoots() { return boots; }
    public void setBoots(ItemStack b) { this.boots = b; }

    public ItemStack getOffhand() { return offhand; }
    public void setOffhand(ItemStack o) { this.offhand = o; }
}