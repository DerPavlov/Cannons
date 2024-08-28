package at.pavlov.cannons.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

/**
 * Assist calculations for armor
 * @author Vaan1310/Intybyte
 */
public class ArmorCalculationUtil {
    private static final Map<Material, Double> ARMOR_REDUCTION_MAP = new HashMap<>();
    private static final Random random = new Random();
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

    public static double getPlayerEnchantProtection(HumanEntity entity, Enchantment enchantment) {
        //http://www.minecraftwiki.net/wiki/Armor#Armor_enchantment_effect_calculation

        if (entity == null) return 0.0;

        org.bukkit.inventory.PlayerInventory inv = entity.getInventory();
        if (inv == null) return 0.0;

        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();

        double reduction = 0.0;
        reduction += getItemEnchantProtection(boots, enchantment);
        reduction += getItemEnchantProtection(helmet, enchantment);
        reduction += getItemEnchantProtection(chest, enchantment);
        reduction += getItemEnchantProtection(pants, enchantment);
        //cap it to 25
        if (reduction > 25) reduction = 25;

        //give it some randomness
        reduction = reduction * (random.nextFloat()/2 + 0.5);

        //cap it to 20
        if (reduction > 20) reduction = 20;

        //1 point is 4%
        return reduction*4/100;
    }

    public static double getItemEnchantProtection(ItemStack item, Enchantment special) {
        int reduction = 0;

        if (item == null) {
            return reduction;
        }

        int lvl = item.getEnchantmentLevel(special);
        if (lvl > 0)
            reduction += (int) Math.floor((6 + lvl * lvl) * 1.5 / 3);

        lvl = item.getEnchantmentLevel(Enchantment.PROTECTION);
        if (lvl > 0)
            reduction += (int) Math.floor((6 + lvl * lvl) * 0.75 / 3);

        return reduction;
    }

    public static double getDirectHitReduction(HumanEntity human, double armorPiercing) {
        double overallPiercing = armorPiercing + 1;
        return (1 - getArmorDamageReduced(human) / overallPiercing) * (1 - getPlayerEnchantProtection(human, Enchantment.PROJECTILE_PROTECTION) / overallPiercing);
    }

    public static double getExplosionHitReduction(HumanEntity human, double armorPiercing) {
        double overallPiercing = armorPiercing + 1;
        return (1 - getArmorDamageReduced(human) / overallPiercing) * (1 - getPlayerEnchantProtection(human, Enchantment.BLAST_PROTECTION));
    }

    /**
     * reduces the durability of the player's armor
     * @param entity - the affected human player
     */
    public static void reduceArmorDurability(HumanEntity entity) {
        PlayerInventory inv = entity.getInventory();
        if (inv == null) return;


        for(ItemStack item : inv.getArmorContents()) {
            if (item == null) {
                continue;
            }

            int lvl = item.getEnchantmentLevel(Enchantment.UNBREAKING);
            //chance of breaking in 0-1
            double breakingChance = 0.6+0.4/(lvl+1);

            if (random.nextDouble() < breakingChance)
            {
                Damageable itemMeta = (Damageable) item.getItemMeta();
                itemMeta.setDamage(itemMeta.getDamage() + 1);
            }
        }
    }
}
