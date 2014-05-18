package at.pavlov.cannons.container;

import org.bukkit.entity.EntityType;

import java.util.Scanner;
import java.util.regex.MatchResult;

public class SpawnEntityHolder{
    private EntityType type;
    private int minAmount;
    private int maxAmount;

    public SpawnEntityHolder(String str)
    {
        //split string at space
        // NAME min-max
        // ZOMBIE 1-2
        try
        {
            Scanner s = new Scanner(str);
            s.findInLine("(\\w+)\\s+(\\d+)-(\\d+)");
            MatchResult result = s.match();
            setType(EntityType.valueOf(result.group(1)));
            setMinAmount(Integer.parseInt(result.group(2)));
            setMaxAmount(Integer.parseInt(result.group(3)));
            s.close();
            //System.out.println("id: " + getId() + " data: " + getData() + " min: " + minAmount + " max: " + maxAmount + " from str: " + str);
        }
        catch(Exception e)
        {
            System.out.println("Error while converting " + str + ". Check formatting (ZOMBIE 1-2)");
            setType(null);
            setMinAmount(0);
            setMaxAmount(0);
        }
    }

    public SpawnEntityHolder(EntityType type, int minAmount, int maxAmount) {
        this.type = type;
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

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }
}
