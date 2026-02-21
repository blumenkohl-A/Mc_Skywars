package de.skywars.kit;

import de.skywars.team.SkyTeam;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class KitManager {

    /**
     * Gives the player the standard SkyWars starter kit.
     * Armor is dyed in the team color for visual identification.
     */
    public void giveKit(Player player, SkyTeam team) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        // --- Weapons ---
        inv.addItem(new ItemStack(Material.IRON_SWORD, 1));
        inv.addItem(new ItemStack(Material.BOW, 1));
        inv.addItem(new ItemStack(Material.ARROW, 16));

        // --- Food ---
        inv.addItem(new ItemStack(Material.BREAD, 16));

        // --- Utility ---
        inv.addItem(new ItemStack(Material.OAK_PLANKS, 32));
        inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
        inv.addItem(new ItemStack(Material.WATER_BUCKET, 1));

        // --- Team-colored leather armor ---
        Color armorColor = toColor(team);
        inv.setHelmet(coloredArmor(Material.LEATHER_HELMET, armorColor));
        inv.setChestplate(coloredArmor(Material.LEATHER_CHESTPLATE, armorColor));
        inv.setLeggings(coloredArmor(Material.LEATHER_LEGGINGS, armorColor));
        inv.setBoots(coloredArmor(Material.LEATHER_BOOTS, armorColor));

        // Full health & food
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
    }

    // ---- Internal ----------------------------------------------------------

    private ItemStack coloredArmor(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Color toColor(SkyTeam team) {
        return switch (team) {
            case RED    -> Color.RED;
            case BLUE   -> Color.BLUE;
            case GREEN  -> Color.LIME;
            case YELLOW -> Color.YELLOW;
        };
    }
}
