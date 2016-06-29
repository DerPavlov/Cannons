package at.pavlov.cannons.container;

import at.pavlov.cannons.Enum.EntityDataType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;

public class SpawnEntityHolder{
    private EntityType type;
    private int minAmount;
    private int maxAmount;
    private Map<EntityDataType, String> data;

    public SpawnEntityHolder(String str)
    {
        //split string at space
        // NAME min-max
        // ZOMBIE 1-2
        // EntityData string

        data = new HashMap<EntityDataType, String>();
        try
        {
            Scanner s = new Scanner(str);
            s.findInLine("(\\w+)\\s+(\\d+)-(\\d+)\\s*(.+)?");
            MatchResult result = s.match();
            setType(EntityType.valueOf(result.group(1)));
            setMinAmount(Integer.parseInt(result.group(2)));
            setMaxAmount(Integer.parseInt(result.group(3)));
            if (result.group(4) != null) {
                String strdata = StringUtils.substringBetween(result.group(4), "{", "}");
                //if there are no curly braces
                if (strdata == null)
                    strdata = result.group(4).trim();

                // convert entity data to map
                for (String s1 : strdata.split(",")){
                    String[] s2 = s1.split(":");
                    if (s2.length > 1){
                        boolean found = false;
                        for (EntityDataType dt : EntityDataType.values()) {
                            // add new entries
                            if (s2[0].trim().equals(dt.getString())) {
                                data.put(dt, s2[1]);
                                found = true;
                                System.out.println("[Cannons] " + dt.toString() + " found");
                            }

                        }
                        if (!found)
                            System.out.println("[Cannons] " + s2[0] + " is not supported by Cannons");
                    }
                }
            }
            s.close();
            //System.out.println("id: " + getId() + " data: " + getData() + " min: " + minAmount + " max: " + maxAmount + " from str: " + str);
        }
        catch(Exception e)
        {
            System.out.println("[Cannons] Error while converting " + str + ". Check formating (Zombie 1-2 EntityData)" + e);
            setType(null);
            setMinAmount(0);
            setMaxAmount(0);
        }
    }

    public SpawnEntityHolder(EntityType type, int minAmount, int maxAmount, Map<EntityDataType, String> data) {
        this.type = type;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.data = data;
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

    public Map<EntityDataType, String> getData() {
        return data;
    }

    public void setData(Map<EntityDataType, String> data) {
        this.data = data;
    }
}
