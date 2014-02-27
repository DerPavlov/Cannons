package at.pavlov.cannons.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;



public class FlyingProjectile
{
	private long spawnTime;
	
	private org.bukkit.entity.Projectile projectile_entity;
    private String shooter;
	private Projectile projectile;
    //location of the shooter before firing - important for teleporting the player back - observer property
    private Location firingLocation;
	
	public FlyingProjectile(Projectile projectile, org.bukkit.entity.Projectile projectile_entity, Player shooter)
	{
		this.projectile_entity = projectile_entity;
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
}
