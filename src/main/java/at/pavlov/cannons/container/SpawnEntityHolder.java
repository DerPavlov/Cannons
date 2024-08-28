package at.pavlov.cannons.container;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.EntityDataType;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpawnEntityHolder{
    private EntityType type;
    private int minAmount;
    private int maxAmount;
    private Map<EntityDataType, String> data;
    private List<PotionEffect> potionEffects;

    public SpawnEntityHolder(String str)
    {
        //split string at space
        // NAME min-max
        // ZOMBIE 1-2
        // EntityData string

        data = new HashMap<>();
        potionEffects = new ArrayList<>();
        Scanner s = new Scanner(str);
        s.findInLine("(\\w+)\\s+(\\d+)-(\\d+)\\s*(.+)?");
        MatchResult result = s.match();

        try {
            // 'AREA_EFFECT_CLOUD 1-1 {Particle:"entity_effect",Radius:5f,Duration:300,Color:16711680,Effects:[{Id:2b,Amplifier:3b,Duration:300,ShowParticles:0b},{Id:7b,Amplifier:1b,Duration:20,ShowParticles:0b},{Id:9b,Amplifier:2b,Duration:300,ShowParticles:0b},{Id:19b,Amplifier:2b,Duration:300,ShowParticles:0b}]}'

            setType(EntityType.valueOf(result.group(1)));
            setMinAmount(Integer.parseInt(result.group(2)));
            if (getMinAmount() < 0) {
                Cannons.getPlugin().logSevere("Min amount for spawned entity: " + getType() + " is <0");
                setMinAmount(0);
            }
            setMaxAmount(Integer.parseInt(result.group(3)));
            if (getMaxAmount() > 100) {
                Cannons.getPlugin().logSevere("Max amount for spawned entity: " + getType() + " is >1000 and was reset to 10");
                setMaxAmount(10);
            }
        } catch (Exception e) {
            s.close();
            Cannons.logger().log(Level.SEVERE, "[Cannons] Error: wrong entity type or min-max values");
            return;
        }

        if (result.group(4) == null) {
            s.close();
            Cannons.logger().log(Level.SEVERE, "[Cannons] type: " + getType() + " data: " + getData() + " min: " + minAmount + " max: " + maxAmount + " from str: " + str);
            return;
        }

        // search for curly braces in as parameter
        Pattern p = Pattern.compile("(?<=\\{)(.+)(?=\\})");
        Matcher m = p.matcher(result.group(4));
        String strdata = null;
        if(m.find())
            strdata = m.group(1);

        //if there are no curly braces set the string to complete argument
        if (strdata == null)
            strdata = result.group(4).trim();

        // convert entity data to map. Split the arguments by comma, but don't split inside parentheses []
        // {Particle:"entity_effect",Radius:5f,Duration:300,Color:16711680,Effects:[{Id:2b,Amplifier:3b,Duration:300,ShowParticles:0b},{Id:7b,Amplifier:1b,Duration:20,ShowParticles:0b},{Id:9b,Amplifier:2b,Duration:300,ShowParticles:0b},{Id:19b,Amplifier:2b,Duration:300,ShowParticles:0b}]}
        try {
            for (String s1 : strdata.split("(?![^)(]*\\([^)(]*?\\)\\)),(?![^\\[]*\\])")) {
                // separate in type and argument EFFECTS:1b
                String[] s2 = s1.split(":(?![^\\[]*\\])");
                // check if there are argument and value
                if (s2.length <= 1) {
                    Cannons.logger().log(Level.SEVERE, "[Cannons] " + s1 + " does not have an argument, use 'DURATION:10'");
                    continue;
                }

                boolean found = false;
                String com1 = s2[0].trim();
                // if the type is an effect it can have multiple effects in parentheses
                // effects:[{Id:2b,Amplifier:3b,Duration:300,ShowParticles:0b},{Id:7b,Amplifier:1b,Duration:20,ShowParticles:0b},{Id:9b,Amplifier:2b,Duration:300,ShowParticles:0b},{Id:19b,Amplifier:2b,Duration:300,ShowParticles:0b}]
                if (!com1.equalsIgnoreCase("effects")) {
                    for (EntityDataType dt : EntityDataType.values()) {
                        // add new entries
                        if (com1.equalsIgnoreCase(dt.getString())) {
                            data.put(dt, s2[1].trim());
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        Cannons.logger().log(Level.SEVERE, "[Cannons] '" + s2[0] + "' is not supported by Cannons");
                    continue;
                }

                String effects = s2[1].replaceAll("[\\[\\]]", "");

                // isolate every effect inside the parentheses for every potion effect
                // {Id:2b,Amplifier:3b,Duration:300,ShowParticles:0b},{Id:7b,Amplifier:1b,Duration:20,ShowParticles:0b},{Id:9b,Amplifier:2b,Duration:300,ShowParticles:0b},{Id:19b,Amplifier:2b,Duration:300,ShowParticles:0b}
                for (String effect : effects.split(",(?![^\\{]*\\})")) {
                    PotionEffectType type = null;
                    int duration = 0;
                    int amplifier = 0;
                    boolean ambient = false;
                    boolean particles = false;
                    boolean icon = true;
                    // remove the curly braces and split bei comma
                    // {Id:2b,Amplifier:3b,Duration:300,ShowParticles:0b}
                    for (String arg : effect.replaceAll("[\\{\\}]", "").split(",")) {
                        // split between argument and value
                        String[] s3 = arg.split(":");
                        if (s3.length <= 1) {
                            continue;
                        }

                        // check arguments type, duration, amplifier, ambient, particles, icon
                        String val = s3[1].replaceAll("b", "");
                        switch (s3[0].toLowerCase()) {
                            case "id":
                                type = PotionEffectType.getById(Integer.parseInt(val));
                                break;
                            case "duration":
                                duration = Integer.parseInt(val);
                                break;
                            case "amplifier":
                                amplifier = Integer.parseInt(val);
                                break;
                            case "ambient":
                                ambient = Boolean.parseBoolean(val);
                                break;
                            case "showparticles":
                                particles = Boolean.parseBoolean(val);
                                break;
                            case "icon":
                                icon = Boolean.parseBoolean(val);
                                break;
                            default:
                                Cannons.logger().log(Level.SEVERE, "[Cannons] '" + s3[0] + "' is not a correct potion effect argument. See Bukkit PotionType");
                        }
                    }
                    Cannons.getPlugin().logDebug("AREA OF EFFECT CLOUD potion type: " + type + " duration " + duration + " amplifier " + amplifier + " ambient " + ambient + " particles " + particles + " icon " + icon);
                    if (type != null && duration > 0 && amplifier > 0)
                        potionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
                }
            }
            s.close();
            Cannons.logger().info("[Cannons] type: " + getType() + " data: " + getData() + " min: " + minAmount + " max: " + maxAmount + " from str: " + str);
        } catch(Exception e) {
            Cannons.logger().log(Level.SEVERE, "[Cannons] Error while converting " + str + ". Check formating (Zombie 1-2 EntityData)" + e);
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

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }
}
