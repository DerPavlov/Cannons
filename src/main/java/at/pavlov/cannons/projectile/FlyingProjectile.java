package at.pavlov.cannons.projectile;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.container.MovingObject;
import at.pavlov.cannons.utils.CannonsUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Flying;
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
    private Location impactLocation;
    //Important for visual splash effect when the cannonball hits the water surface
    private boolean inWater;
    private boolean wasInWater;
    private UUID cannonID;

    private MovingObject predictor;


	
	public FlyingProjectile(Projectile projectile, org.bukkit.entity.Projectile projectile_entity, Player shooter, UUID cannonId)
	{
        Validate.notNull(shooter, "shooter for the projectile can't be null");
        this.projectile_entity = projectile_entity;
        this.wasInWater = this.isInWater();
		this.projectile = projectile;
        this.cannonID = cannonId;
        if (shooter != null)
        {
            this.shooter = shooter.getName();
            this.firingLocation = shooter.getLocation();
            this.projectile_entity.setShooter(shooter);
        }
        else
        {
            this.firingLocation = projectile_entity.getLocation();
        }
		this.spawnTime = System.currentTimeMillis();

        //set location and speed
        Location new_loc = projectile_entity.getLocation();
        predictor = new MovingObject(new_loc, projectile_entity.getVelocity());
	}
	
	public Player getShooter()
	{
        if (shooter != null)
		    return Bukkit.getPlayer(shooter);
        else
            return null;
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
        predictor.updateProjectileLocation(isInWater());
    }

    /**
     * revert update of the location
     */
    public void revertUpdate()
    {
        predictor.revertProjectileLocation(isInWater());
    }

    /**
     * returns the calculated location of the projectile
     * @return the location where the projectile should be
     */
    public Location getExpectedLocation()
    {
        return predictor.getLocation();
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
        return projectile_entity.getLocation().toVector().distance(predictor.getLoc());
    }

    /**
     * teleports the projectile to the predicted location
     */
    public void teleportToPrediction()
    {
        projectile_entity.teleport(predictor.getLocation());
        projectile_entity.setVelocity(predictor.getVel());
    }

    /**
     * teleports the projectile to the given location
     * @param loc target location
     * @param vel velocity of the projectile
     */
    public void teleport(Location loc, Vector vel)
    {
        this.predictor.setLocation(loc);
        this.predictor.setVel(vel);
        teleportToPrediction();
    }

    @Override
    public int hashCode() {
        //compare projectile entities
        return projectile_entity.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        //equal if the projectile entities are equal
        FlyingProjectile obj2 = (FlyingProjectile) obj;
        return this.projectile_entity.equals(obj2.getProjectileEntity());
    }

    public UUID getUID()
    {
        return projectile_entity.getUniqueId();
    }

    public Location getImpactLocation() {
        return impactLocation;
    }

    public void setImpactLocation(Location impactLocation) {
        this.impactLocation = impactLocation;
    }

    public UUID getCannonID() {
        return cannonID;
    }

    public void setCannonID(UUID cannonID) {
        this.cannonID = cannonID;
    }
}
