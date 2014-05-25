package at.pavlov.cannons.listener;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.container.MaterialHolder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;

public class EntityListener implements Listener
{
	private final Cannons plugin;
	
	public EntityListener(Cannons plugin)
	{
		this.plugin = plugin;
	}


    /**
     * The projectile has hit an entity
     * @param event
     */
	@EventHandler
	public void onProjectileHitEntity(EntityDamageByEntityEvent event)
	{
		Entity er = event.getDamager();
		if(er instanceof Projectile)
		{
			Projectile p = (Projectile) er;
			plugin.getProjectileManager().directHitProjectile(p, event.getEntity());
		}
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
		plugin.logDebug("Explode event listener called");

		//do nothing if it is cancelled
		if (event.isCancelled()) return;
		
		List<Block> blocks = event.blockList();
        HashSet<UUID> remove = new HashSet<UUID>();



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
                    remove.add(cannon.getUID());
				}			
			}
		}
		
		//iterate again and remove all block of intact cannons
		for (int i = 0; i < blocks.size(); i++)
		{
			Block block = blocks.get(i);
			
			Cannon cannon = plugin.getCannonManager().getCannon(block.getLocation(), null);

			// if it is a cannon block and the cannon is not destroyed (see above)
			if (cannon != null && !remove.contains(cannon.getUID()))
			{
				if (cannon.isCannonBlock(block))
				{
					blocks.remove(i--);
				}
			}
		}

        //cannons event - remove unbreakable blocks
        //this will also affect other plugins which spawn bukkit explosions
        if (event.getEntity() == null)
        {
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
        for (UUID id : remove)
		    plugin.getCannonManager().removeCannon(id, false, true, BreakCause.Explosion);
	}		

	
}
