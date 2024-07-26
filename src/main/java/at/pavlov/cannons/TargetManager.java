package at.pavlov.cannons;

import at.pavlov.cannons.container.Target;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TargetManager {
    private static HashMap<UUID, Target> targets = new HashMap<>();

    public static void addTarget(Target target){
        targets.put(target.getUniqueId(), target);
    }

    public static Target getTarget(UUID uid){
        return targets.get(uid);
    }

    public static HashSet<Target> getTargetsInSphere(Location center, double radius){
        HashSet<Target> newTargetList = new HashSet<>();

        for (Target target : targets.values()) {
            if (target.getCenterLocation().distanceSquared(center) < radius * radius)
                newTargetList.add(target);
        }
        return newTargetList;
    }

    public static HashSet<Target> getTargetsInBox(Location center, double lengthX, double lengthY, double lengthZ){
        HashSet<Target> newTargetList = new HashSet<>();

        for (Target target : targets.values()) {
            Location newLoc = target.getCenterLocation();
            Vector box = newLoc.subtract(center).toVector();

            if (newLoc.getWorld().equals(center.getWorld().getUID()) && Math.abs(box.getX())<lengthX/2 && Math.abs(box.getY())<lengthY/2 && Math.abs(box.getZ())<lengthZ/2)
                newTargetList.add(target);
        }
        return newTargetList;
    }
}
