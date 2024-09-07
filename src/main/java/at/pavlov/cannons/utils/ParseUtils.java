package at.pavlov.cannons.utils;

import at.pavlov.cannons.Cannons;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class ParseUtils {
    /**
     * converts a string to float
     * @param str string to convert
     * @return returns parsed number or default
     */
    public static float parseFloat(String str, float default_value) {
        if (str == null) {
            return default_value;
        }
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            throw new NumberFormatException();
        }
    }

    /**
     * converts a string to int
     * @param str string to convert
     * @return returns parsed number or default
     */
    public static int parseInt(String str, int default_value) {
        if (str == null) {
            return default_value;
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            throw new NumberFormatException();
        }
    }

    /**
     * converts a string to color
     * @param str string to convert
     * @return returns parsed color or default
     */
    public static Color parseColor(String str, Color default_value) {
        if (str == null) {
            return default_value;
        }

        try {
            return Color.fromRGB(Integer.parseInt(str));
        } catch (Exception e) {
            throw new NumberFormatException();
        }
    }

    /**
     * converts a string to Potion effect
     * @param str string to convert
     * @return returns parsed number or default
     */
    public static PotionData parsePotionData(String str, PotionData default_value) {
        if (str == null) {
            return default_value;
        }
        str = str.toLowerCase();
        for (PotionType pt : PotionType.values()) {
            if (!str.contains(pt.toString().toLowerCase())) {
                continue;
            }

            boolean extended = str.contains("long");
            boolean upgraded = str.contains("strong");
            Cannons.logSDebug("Potion parsing: " + str);
            return new PotionData(pt, extended, upgraded);
        }
        return default_value;
    }

    /**
     * converts a string to float
     * @param str string to convert
     * @return returns parsed number or default
     */
    public static Particle parseParticle(String str, Particle default_value) {
        if (str == null) {
            return default_value;
        }

        for (Particle pt : Particle.values())
            if (str.equalsIgnoreCase(pt.toString())){
                return pt;
            }
        return default_value;
    }

    /**
     * converts a string to Itemstack
     * @param str string to convert
     * @return returns parsed number or default
     */
    public static ItemStack parseItemstack(String str, ItemStack default_value) {
        if (str == null) {
            return default_value;
        }

        for (Material mt : Material.values())
            if (str.equalsIgnoreCase(mt.toString())){
                return new ItemStack(mt);
            }
        return default_value;
    }
}
