package at.pavlov.cannons.container;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Target {

    private String name;
    private EntityType type;
    private boolean isCannon;
    private UUID uid;
    private Location location;
    private Vector velocity;

    public Target(String name, EntityType type, boolean isCannon, UUID uid, Location location, Vector velocity) {
        this.name = name;
        this.type = type;
        this.isCannon = false;
        this.uid = uid;
        this.location = location;
        this.velocity = velocity;
    }

    public Target(Entity entity) {
        this.name = entity.getName();
        this.type = entity.getType();
        this.isCannon = false;
        this.uid = entity.getUniqueId();
        this.location = entity.getLocation();
        this.velocity = entity.getVelocity();
    }
    public Target(Cannon cannon) {
        this.name = cannon.getCannonName();
        this.type = null;
        this.isCannon = true;
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

    public boolean isCannon() {
        return isCannon;
    }
}
