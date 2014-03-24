package at.pavlov.cannons.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;



public class FlyingProjectile
{
	private long spawnTime;
	
	private org.bukkit.entity.Projectile projectile_entity;
    private String shooter;
	private Projectile projectile;
    //location of the shooter before firing - important for teleporting the player back - observer property
    private Location firingLocation;
    //Important for visual splash effect when the cannonball hits the water surface
    private boolean wasInWater;
	
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
	public void setProjectileEntity(org.bukkit.entity.Projectile projectile_entity)
	{
		this.projectile_entity = projectile_entity;
	}
	public Projectile getProjectile()
	{
		return projectile;
	}
	public void setProjectile(Projectile projectile)
	{
		this.projectile = projectile;
	}
	public long getSpawnTime()
	{
		return spawnTime;
	}
	public void setSpawnTime(long spawnTime)
	{
		this.spawnTime = spawnTime;
	}

    public Location getFiringLocation() {
        return firingLocation;
    }

    public void setFiringLocation(Location firingLocation) {
        this.firingLocation = firingLocation;
    }

    public boolean isInWater()
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

    /**
     * if the projectile has entered the water surface
     * @return true if the projectile has entered the water surface
     */
    public boolean isWaterSurface(){
        return !wasInWater&&isInWater();
    }

    /**
     * returns if the projectile has entered the water surface and updates also inWater
     * @return true if the projectile has entered water
     */
    public boolean updateWaterSurfaceCheck()
    {
        boolean isSurface = isWaterSurface();
        wasInWater = isInWater();
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
}
