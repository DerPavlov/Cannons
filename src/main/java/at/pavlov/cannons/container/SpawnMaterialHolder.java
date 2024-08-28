package at.pavlov.cannons.container;

import at.pavlov.cannons.Cannons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.MatchResult;

public class SpawnMaterialHolder {
    private BlockData material;
    private int minAmount;
    private int maxAmount;

    public SpawnMaterialHolder(String str)
    {
        //split string at space
        // id:data min-max
        // 10:0 1-2
        try
        {
            Scanner s = new Scanner(str);
            s.findInLine("(\\S+)\\s(\\d+)-(\\d+)");
            MatchResult result = s.match();
            material = Bukkit.createBlockData(result.group(1));
            setMinAmount(Integer.parseInt(result.group(2)));
            setMaxAmount(Integer.parseInt(result.group(3)));
            s.close();
        }
        catch(Exception e)
        {
            Cannons.logger().log(Level.SEVERE,"Error while converting " + str + ". Check formatting (minecraft:cobweb 1-2)");
            material =  Bukkit.createBlockData(Material.AIR);
            setMinAmount(0);
            setMaxAmount(0);
        }
    }

    public SpawnMaterialHolder(BlockData material, int minAmount, int maxAmount) {
        this.material = material;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BlockData getMaterial(){
        return this.material;
    }

    public void setMaterial(BlockData material){
        this.material = material;
    }
}
