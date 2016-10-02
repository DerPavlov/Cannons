package at.pavlov.cannons.container;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class MovingObject {

    //location and speed
    private UUID world;
    private Vector loc;
    private Vector vel;
    private EntityType entityType;


    public MovingObject(Location loc, Vector vel, EntityType entityType)
    {
        world = loc.getWorld().getUID();
        this.loc = loc.toVector();
        this.vel = vel;
        this.entityType = entityType;
    }

    /**
     * calculates the new position for the projectile
     * @param inWater the projectile is in water
     */
    public void updateProjectileLocation(boolean inWater)
    {
        double f2 = 0.99F;
        if (inWater)
            f2 = 0.8F;
        double f3 = 0.03F;
        if (entityType.equals(EntityType.ARROW)){
            f3 = 0.05000000074505806D;
            if (inWater)
                f2 = 0.6F;
        }
        if (entityType.equals(EntityType.FIREBALL) || entityType.equals(EntityType.SMALL_FIREBALL)){
            f2 = 0.95F;
            f3 = 0.0;
        }
        //update location
        this.loc.add(this.vel);
        //slow down projectile
        this.vel.multiply(f2);
        //apply gravity
        this.vel.subtract(new Vector(0,f3,0));
    }

    /**
     * reverts and update of the projectile position
     * @param inWater the projectile is in water
     */
    public void revertProjectileLocation(boolean inWater)
    {
        double f2 = 0.99F;
        if (inWater)
            f2 = 0.8F;
        double f3 = 0.03F;
        //apply gravity
        this.vel.add(new Vector(0, f3, 0));
        //slow down projectile
        this.vel.multiply(1.0 / f2);
        //update location
        this.loc.subtract(this.vel);
    }

    /**
     * teleports the projectile to this location
     * @param loc the projectile will be teleported to this location
     * @param vel velocity of the object
     */
    public void teleport(Location loc, Vector vel)
    {
        this.loc = loc.toVector();
        this.vel = vel;
        this.world = loc.getWorld().getUID();
    }

    /**
     * returns the calculated location of the projectile
     * @return the location where the projectile should be
     */
    public Location getLocation()
    {
        return loc.toLocation(Bukkit.getWorld(world));
    }

    public void setLocation(Location loc)
    {
        this.loc = loc.toVector();
        this.world = loc.getWorld().getUID();
    }

    public UUID getWorld() {
        return world;
    }

    public void setWorld(UUID world) {
        this.world = world;
    }

    public Vector getLoc() {
        return loc;
    }

    public void setLoc(Vector loc) {
        this.loc = loc;
    }

    public Vector getVel() {
        return vel;
    }

    public void setVel(Vector vel) {
        this.vel = vel;
    }
}

