package at.pavlov.Cannons.listener;


import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.projectile.FlyingProjectile;

public class EntityListener implements Listener
{
	Cannons plugin;
	
	public EntityListener(Cannons plugin)
	{
		this.plugin = plugin;
	}
	

	/**
	 * Cannon snowball hits the ground
	 * 
	 * @param event
	 */
	@EventHandler
	public void ProjectileHit(ProjectileHitEvent event)
	{

		// get FlyingProjectiles
		LinkedList<FlyingProjectile> flying_projectiles = plugin.getFireCannon().getProjectiles();

		// iterate the list
		if (!flying_projectiles.isEmpty())
		{
			Iterator<FlyingProjectile> iterator = flying_projectiles.iterator();

			while (iterator.hasNext())
			{
				FlyingProjectile flying = iterator.next();
				if (event.getEntity().equals(flying.getSnowball()))
				{
					//flying.setSnowball((Snowball) event.getEntity());
					plugin.getExplosion().detonate(flying);
					iterator.remove();
				}
			}
		}
	}
	

	
}
