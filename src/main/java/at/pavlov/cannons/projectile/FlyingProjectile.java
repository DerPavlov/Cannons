package at.pavlov.cannons.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.rmi.server.UID;
import java.util.UUID;


public class FlyingProjectile
{
	private final long spawnTime;
	
	private final org.bukkit.entity.Projectile projectile_entity;
    private String shooter;
	private final Projectile projectile;
    //location of the shooter before firing - important for teleporting the player back - observer property
    private final Location firingLocation;
    //Important for visual splash effect when the cannonball hits the water surface
    private boolean inWater;
    private boolean wasInWater;

    //location and speed
    private UUID world;
    private Vector loc;
    private Vector vel;
	
	public FlyingProjectile(Projectile projectile, org.bukkit.entity.Projectile projectile_entity, Player shooter)
	{
		this.projectile_entity = projectile_entity;
        this.wasInWater = this.isInWater();
		this.projectile = projectile;
        if (shooter != null)
        {
            this.shooter = shooter.getName();
            this.firingLocation = shooter.getLocation();
        }
        else
        {
            this.firingLocation = projectile_entity.getLocation();
        }
		this.spawnTime = System.currentTimeMillis();

        //set location and speed
        Location new_loc = projectile_entity.getLocation();
        world = new_loc.getWorld().getUID();
        loc = new_loc.toVector();
        vel = projectile_entity.getVelocity();
	}
	
	public Player getShooter()
	{
        if (shooter != null)
		    return Bukkit.getPlayer(shooter);
        else
            return null;
	}
	public void setShooter(Player shooter)
	{
		this.projectile_entity.setShooter(shooter);
	}

	public org.bukkit.entity.Projectile getProjectileEntity()
	{
		return projectile_entity;
	}

	public Projectile getProjectile()
	{
		return projectile;
	}

	public long getSpawnTime()
	{
		return spawnTime;
	}

    public Location getFiringLocation() {
        return firingLocation;
    }

    /**
     * check if the projectile in in a liquid
     * @return true if the projectile is in a liquid
     */
    private boolean isInWaterCheck()
    {
        if(projectile_entity!=null)
        {
            Block block = projectile_entity.getLocation().getBlock();
            if (block != null)
            {
                return block.isLiquid();
            }
        }
        return false;
    }

    public boolean isInWater() {
        return inWater;
    }

    /**
     * if the projectile has entered the water surface
     * @return true if the projectile has entered the water surface
     */
    public boolean isWaterSurface(){
        return !wasInWater&&isInWaterCheck();
    }

    /**
     * returns if the projectile has entered the water surface and updates also inWater
     * @return true if the projectile has entered water
     */
    public boolean updateWaterSurfaceCheck()
    {
        boolean isSurface = isWaterSurface();
        inWater = isInWaterCheck();
        wasInWater = inWater;
        return isSurface;
    }

    public boolean wasInWater() {
        return wasInWater;
    }

    public void setWasInWater(boolean wasInWater) {
        this.wasInWater = wasInWater;
    }

    /**
     * if the projectile is still alive and valid
     * @return returns false if the projectile entity is null
     */
    public boolean isValid()
    {
        return projectile_entity!=null;
    }

    /**
     * updated the location and speed of the projectile
     */
    public void update()
    {
        double f2 = 0.99F;
        if (isInWater())
            f2 = 0.8F;
        double f3 = 0.03F;
        //update location
        this.loc.add(this.vel);
        //slow down projectile
        this.vel.multiply(f2);
        //apply gravity
        this.vel.subtract(new Vector(0,f3,0));
    }

    /**
     * revert update of the location
     */
    public void revertUpdate()
    {
        double f2 = 0.99F;
        if (isInWater())
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
     * returns the calculated location of the projectile
     * @return the location where the projectile should be
     */
    public Location getExpectedLocation()
    {
        return loc.toLocation(Bukkit.getWorld(world));
    }

    /**
     * returns actual location of the projectile
     * @return momentary position of the projectile
     */
    public Location getActualLocation()
    {
        return this.projectile_entity.getLocation();
    }

    /**
     * returns the distance of the projectile location to the calculated location
     * @return distance of the projectile location to the calculated location
     */
    public double distanceToProjectile()
    {
        return projectile_entity.getLocation().toVector().distance(loc);
    }

    /**
     * teleports the projectile to this location
     * @param loc the projectile will be teleported to this location
     */
    public void teleport(Location loc)
    {
        projectile_entity.teleport(loc);
        //projectile_entity.setVelocity(vel);
        this.loc = loc.toVector();
        this.world = loc.getWorld().getUID();
    }
}
