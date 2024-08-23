package at.pavlov.cannons.utils;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

// http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

/**
 * Assist calculations for armor
 * @author Vaan1310/Intybyte
 */
public class ArmorCalculationUtil {
    private static final Map<Material, Double> ARMOR_REDUCTION_MAP = new HashMap<>();
    //looks like from minecraft wiki all armor values are multiplied for this number
    private static double MAGIC_VALUE = 0.04;

    static {
        ARMOR_REDUCTION_MAP.put(Material.TURTLE_HELMET, 2 * MAGIC_VALUE);

        ARMOR_REDUCTION_MAP.put(Material.LEATHER_HELMET, 1 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.GOLDEN_HELMET, 2 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.CHAINMAIL_HELMET, 2 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.IRON_HELMET, 2 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.DIAMOND_HELMET, 3 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.NETHERITE_HELMET, 3 * MAGIC_VALUE);

        ARMOR_REDUCTION_MAP.put(Material.LEATHER_BOOTS, 1 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.GOLDEN_BOOTS, 1 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.CHAINMAIL_BOOTS, 1 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.IRON_BOOTS, 2 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.DIAMOND_BOOTS, 3 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.NETHERITE_BOOTS, 3 * MAGIC_VALUE);

        ARMOR_REDUCTION_MAP.put(Material.LEATHER_LEGGINGS, 2 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.GOLDEN_LEGGINGS, 3 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.CHAINMAIL_LEGGINGS, 4 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.IRON_LEGGINGS, 5 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.DIAMOND_LEGGINGS, 6 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.NETHERITE_LEGGINGS, 6 * MAGIC_VALUE);

        ARMOR_REDUCTION_MAP.put(Material.LEATHER_CHESTPLATE, 3 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.GOLDEN_CHESTPLATE, 5 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.CHAINMAIL_CHESTPLATE, 5 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.IRON_CHESTPLATE, 6 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.DIAMOND_CHESTPLATE, 8 * MAGIC_VALUE);
        ARMOR_REDUCTION_MAP.put(Material.NETHERITE_CHESTPLATE, 8 * MAGIC_VALUE);
    }

    public static double getMagicValue() {
        return MAGIC_VALUE;
    }

    public static void setMagicValue(double magick) {
        MAGIC_VALUE = magick;
    }

    public static double getArmorDamageReduced(HumanEntity entity) {
        if (entity == null) return 0.0;

        org.bukkit.inventory.PlayerInventory inv = entity.getInventory();
        if (inv.isEmpty()) return 0.0;

        double totalReduction = 0.0;

        totalReduction += getArmorPieceReduction(inv.getHelmet());
        totalReduction += getArmorPieceReduction(inv.getBoots());
        totalReduction += getArmorPieceReduction(inv.getLeggings());
        totalReduction += getArmorPieceReduction(inv.getChestplate());

        // 100% protection, this would make you immune
        if(totalReduction > 1)
            totalReduction = 1;

        return totalReduction;
    }

    public static double getArmorPieceReduction(ItemStack armorPiece) {
        if (armorPiece == null) return 0.0;

        Material material = armorPiece.getType();
        return ARMOR_REDUCTION_MAP.getOrDefault(material, 0.0);
    }
}
