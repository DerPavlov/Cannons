package at.pavlov.cannons.container;

import org.bukkit.Material;

import java.util.Scanner;
import java.util.regex.MatchResult;

public class SpawnMaterialHolder {
    private ItemHolder material;
    private int minAmount;
    private int maxAmount;

    public SpawnMaterialHolder(String str)
    {
        //todo rework spawning type
        //split string at space
        // id:data min-max
        // 10:0 1-2
        try
        {
            Scanner s = new Scanner(str);
            s.findInLine("(\\w+):(\\d+)\\s+(\\d+)-(\\d+)");
            MatchResult result = s.match();
            material = new ItemHolder(result.group(1) + ":" + result.group(2));
            setMinAmount(Integer.parseInt(result.group(3)));
            setMaxAmount(Integer.parseInt(result.group(4)));
            s.close();
            //System.out.println("id: " + getId() + " data: " + getData() + " min: " + minAmount + " max: " + maxAmount + " from str: " + str);
        }
        catch(Exception e)
        {
            System.out.println("Error while converting " + str + ". Check formatting (10:0 1-2)");
            material = new ItemHolder(Material.AIR);
            setMinAmount(0);
            setMaxAmount(0);
        }
    }

    public SpawnMaterialHolder(ItemHolder material, int minAmount, int maxAmount) {
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

    public ItemHolder getMaterial(){
        return this.material;
    }

    public void setMaterial(ItemHolder material){
        this.material = material;
    }
}
