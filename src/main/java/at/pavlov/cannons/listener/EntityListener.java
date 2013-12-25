package at.pavlov.cannons.listener;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.projectile.FlyingProjectile;

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
        plugin.getProjectileManager().detonateProjectile(event.getEntity());
	}
	
	/**
	 * handles the explosion event. Protects the buttons and torches of a cannon, because the break easily
	 * @param event
	 */
	@EventHandler
	public void EntityExplode(EntityExplodeEvent event)
	{
		plugin.logDebug("explode event");
		
		//do nothing if it is cancelled
		if (event.isCancelled()) return;
		
		List<Block> blocks = event.blockList();



		// first search if a barrel block was destroyed. 
		for (int i = 0; i < blocks.size(); i++)
		{
			Block block = blocks.get(i);
	
			Cannon cannon = plugin.getCannonManager().getCannon(block.getLocation(), null);
			
			// if it is a cannon block
			if (cannon != null)
			{
				if (cannon.isDestructibleBlock(block.getLocation()))
				{
					//this cannon is destroyed
					cannon.setValid(false);
				}			
			}
		}
		
		//iterate again and remove all block of intact cannons
		for (int i = 0; i < blocks.size(); i++)
		{
			Block block = blocks.get(i);
			
			Cannon cannon = plugin.getCannonManager().getCannon(block.getLocation(), null);

			// if it is a cannon block and the cannon is not destroyed (see above)
			if (cannon != null && cannon.isValid())
			{
				if (cannon.isCannonBlock(block))
				{
					blocks.remove(i--);
				}
			}
		}

        //cannons event - remove unbreakable blocks
        if (event.getEntity() == null)
        {
            plugin.logDebug("cannonball event");
            for (int i = 0; i < blocks.size(); i++)
            {
                Block block = blocks.get(i);

                for (MaterialHolder unbreakableBlock : plugin.getMyConfig().getUnbreakableBlocks())
                {
                    if (unbreakableBlock.equalsFuzzy(block))
                    {
                        blocks.remove(i--);
                    }
                }
            }
        }


		
		//now remove all invalid cannons
		plugin.getCannonManager().removeInvalidCannons();
	
	}		

	
}
