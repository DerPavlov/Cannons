package at.pavlov.cannons.container;

import at.pavlov.cannons.Enum.TargetType;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Target {

    private String name;
    private TargetType targetType;
    private EntityType type;
    private UUID uid;
    private Location location;
    private Vector velocity;

    public Target(String name, TargetType targetType, EntityType type, UUID uid, Location location, Vector velocity) {
        this.name = name;
        this.targetType = targetType;
        this.type = type;
        this.uid = uid;
        this.location = location;
        this.velocity = velocity;
    }

    public Target(Entity entity) {
        this.name = entity.getName();
        if (entity instanceof Player)
            this.targetType = TargetType.PLAYER;
        else if (entity instanceof Monster)
            this.targetType = TargetType.MONSTER;
        else if (entity instanceof Animals)
            this.targetType = TargetType.ANIMAL;
        else
            this.targetType = TargetType.OTHER;
        this.type = entity.getType();
        this.uid = entity.getUniqueId();
        // aim for center of mass
        this.location = entity.getLocation().clone().add(0, 0, 1);
        this.velocity = entity.getVelocity();
    }
    public Target(Cannon cannon) {
        this.name = cannon.getCannonName();
        this.targetType = TargetType.CANNON;
        this.type = null;
        this.uid = cannon.getUID();
        this.location = cannon.getLocation();
        this.velocity = cannon.getVelocity();
    }

    public String toString() {
        return "name: " + this.name + "UID: " + this.uid + "location: " + this.location.toString() + " velocity: " + velocity.toString();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public UUID getUniqueId() {
        return uid;
    }

    public EntityType getType() {
        return type;
    }

    public TargetType getTargetType() {
        return targetType;
    }
}
