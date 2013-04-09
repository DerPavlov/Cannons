package at.pavlov.Cannons.projectile;

import org.bukkit.entity.Snowball;



public class FlyingProjectile
{
	private String shooter;
	
	private Snowball snowball;
	private Projectile projectile;
	
	
	public String getShooter()
	{
		return shooter;
	}
	public void setShooter(String shooter)
	{
		this.shooter = shooter;
	}
	public Snowball getSnowball()
	{
		return snowball;
	}
	public void setSnowball(Snowball snowball)
	{
		this.snowball = snowball;
	}
	public Projectile getProjectile()
	{
		return projectile;
	}
	public void setProjectile(Projectile projectile)
	{
		this.projectile = projectile;
	}
}
