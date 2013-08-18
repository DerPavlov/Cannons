package at.pavlov.cannons;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.pavlov.cannons.event.CannonAfterCreateEvent;
import at.pavlov.cannons.event.CannonFireEvent;
import at.pavlov.cannons.utils.DelayedFireTask;
import at.pavlov.cannons.utils.FireTaskWrapper;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.DesignStorage;
import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;

public class FireCannon {
	
	private final Config config;
	private final DesignStorage designStorage;
	private final Cannons plugin;
	private final CreateExplosion explosion;
	
	public LinkedList<FlyingProjectile> flying_projectiles = new LinkedList<FlyingProjectile>();
	 
	
	
	
	public FireCannon(Cannons plugin, Config config, CreateExplosion explosion)
	{
		this.plugin = plugin;
		this.config = config;
		this.designStorage = plugin.getDesignStorage();
		this.explosion = explosion;
	}
	
	public LinkedList<FlyingProjectile> getProjectiles ()
	{
		return flying_projectiles;
	}
	
	/**
	 * checks all condition but does not fire the cannon
	 * @param cannon
	 * @param player
	 * @return
	 */
	public MessageEnum getPrepareFireMessage(Cannon cannon, Player player)
	{
		CannonDesign design = cannon.getCannonDesign();
		if (design == null) return null;
		//if the player is not the owner of this gun
		if (player != null && !cannon.getOwner().equals(player.getName())  && design.isAccessForOwnerOnly())
		{
			return MessageEnum.ErrorNotTheOwner;
		}
		//check if there is some gunpowder in the barrel
		if (cannon.getLoadedGunpowder() <= 0)
		{
			return MessageEnum.ErrorNoGunpowder;
		}
		//is there a projectile
		if(!cannon.isLoaded())
		{
			return MessageEnum.ErrorNoProjectile;
		}
		//Barrel too hot
		if (cannon.getLastFired() + design.getBarrelCooldownTime()*1000 >= System.currentTimeMillis())
		{
			return MessageEnum.ErrorBarrelTooHot;
		}	
		if (player!= null)
		{
		
			//if the player has permission to fire
			if (!player.hasPermission(design.getPermissionFire()))
			{
				return MessageEnum.PermissionErrorFire;
			}
			//check if the player has the permission for this projectile
			Projectile projectile = cannon.getLoadedProjectile();
			if(projectile != null && !projectile.hasPermission(player))
			{
				return MessageEnum.PermissionErrorProjectile;
			}
			//check for flint and steel
			if ( design.isFiringItemRequired() && !config.getToolFiring().equalsFuzzy(player.getItemInHand()) )
			{
				return MessageEnum.ErrorNoFlintAndSteel;
			}
		}
		//everything fine fire the damn cannon
		return MessageEnum.CannonFire;
	}
	
	/**
	 * checks if all preconditions for firing are fulfilled and fires the cannon
	 * @param cannon
	 * @param player
	 * @param autoload
	 * @return
	 */
	public MessageEnum prepareFire(Cannon cannon, Player player, boolean autoload)
	{
        Projectile projectile = cannon.getLoadedProjectile();
        //no charge no firing
        if (cannon.getLoadedGunpowder() == 0 || projectile == null)
        {
            //this cannon will try to find some gunpowder and projectile in the chest
            if (autoload)
            {
                boolean hasReloaded =  cannon.reloadFromChests(player);
                if (!hasReloaded)
                {
                    //there is not enough gunpowder or no projectile in the chest
                    plugin.logDebug("Can't reload cannon, because there is no valid charge in the chests");
                    if (projectile == null)
                        return MessageEnum.ErrorNoProjectile;
                    return MessageEnum.ErrorNoGunpowder;
                }
                else
                {
                    plugin.logDebug("Charge loaded from chest");
                }

            }
            else
            {
                //can't fire without charge
                plugin.logDebug("Can't fire without a charge");
                if (projectile == null)
                    return MessageEnum.ErrorNoProjectile;
                return MessageEnum.ErrorNoGunpowder;
            }
        }

		//check for all permissions
		MessageEnum message = getPrepareFireMessage(cannon, player);
		
		//return if there are permission missing
		if (message != MessageEnum.CannonFire)
            return message;

        CannonFireEvent fireEvent = new CannonFireEvent(cannon, player);
        Bukkit.getServer().getPluginManager().callEvent(fireEvent);

        if (fireEvent.isCancelled())
            return null;

		//ignite the cannon
		delayedFire(cannon, player);
		
		return message;
	}
	
	/**
	 * delays the firing by the fuse burn time
	 * @param cannon
	 * @param player
	 */
    private void delayedFire(Cannon cannon, Player player)
    {
    	CannonDesign design = designStorage.getDesign(cannon);
    	
		//reset after firing
		cannon.setLastFired(System.currentTimeMillis());
		
		//Set up smoke effects on the torch
		for (Location torchLoc : design.getFiringIndicator(cannon))
		{
			torchLoc.setX(torchLoc.getX() + 0.5);
			torchLoc.setY(torchLoc.getY() + 1);
			torchLoc.setZ(torchLoc.getZ() + 0.5);
			torchLoc.getWorld().playEffect(torchLoc, Effect.SMOKE, BlockFace.UP);
			torchLoc.getWorld().playSound(torchLoc, Sound.FUSE , 1,1);
		}

		
		//set up delayed task
		Object fireTask = new FireTaskWrapper(cannon, player);
		Long fuseBurnTime = (long) design.getFuseBurnTime() * 20;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedFireTask(fireTask)
		{
			public void run(Object object) 
			    {			
					FireTaskWrapper fireTask = (FireTaskWrapper) object;
			    	fire(fireTask.getCannon(), fireTask.getPlayer());
			    }
		}, fuseBurnTime);	
    }
	
	/**
	 * fires a cannon and removes the charge from the player
	 * @param cannon
	 * @param shooter
	 */
    private void fire(Cannon cannon, Player shooter)
    {
    	CannonDesign design = designStorage.getDesign(cannon);
    	Projectile projectile = cannon.getLoadedProjectile();

        // no projectile no cannon firing
        if (projectile == null) return;
        // no gunpowder no cannon firing
        if (cannon.getLoadedGunpowder() <= 0) return;
    	
    	
		Location firingLoc = design.getMuzzle(cannon);
		World world = cannon.getWorldBukkit();
    	
		//Muzzle flash + Muzzle_displ
		world.createExplosion(firingLoc, 0F);
		world.playEffect(firingLoc, Effect.SMOKE, cannon.getCannonDirection());
		
		//for each bullet, but at least once		
		for (int i=0; i < Math.max(projectile.getNumberOfBullets(), 1); i++)
		{
			//one snowball for each projectile
    		Snowball snowball = world.spawn(firingLoc, Snowball.class);
    		snowball.setFireTicks(100);
    		snowball.setTicksLived(2);
        	if (i == 0 && projectile.hasProperty(ProjectileProperties.SHOOTER_AS_PASSENGER))
            	snowball.setPassenger(shooter);
	
       		//calculate firing vector
    		Vector vect = cannon.getFiringVector(shooter);    		
    		snowball.setVelocity(vect);
    		
    		//create a new flying projectile container
    		FlyingProjectile cannonball = new FlyingProjectile(projectile, snowball);   
    		//set shooter to the cannonball
    		if (shooter != null) 
    		{
    			cannonball.setShooter(shooter);
    		}
    		
 
			flying_projectiles.add(cannonball);
			
    		//detonate timefused projectiles
			detonateTimefuse(cannonball);	

			//confuse all entity which wear no helmets due to the blast of the cannon
			List<Entity> living = snowball.getNearbyEntities(5, 5, 5);
			//do only once
			if (snowball != null && i==0)
			{
				confuseShooter(living, design.getBlastConfusion());
			}
		}
	
		
		//reset after firing
		cannon.setLastFired(System.currentTimeMillis());


		//redstone or player infinite ammo will not remove the charge
		if ((shooter == null && !design.isAmmoInfiniteForRedstone()) || (shooter != null && !design.isAmmoInfiniteForPlayer()))
		{
			plugin.logDebug("fire event complete, charge removed from the cannon");
			//ammo is removed form chest, if false there is not enough ammo to reload the cannon
            cannon.removeCharge();
		}
    }	 
    
    /**
     * confuses an entity to simulate the blast of a cannon
     * @param living
     * @param confuseTime
     */
    private void confuseShooter(List<Entity> living, double confuseTime)
    {
    	 //confuse shooter if he wears no helmet (only for one projectile and if its configured)
    	if (confuseTime > 0)
    	{
    		Iterator<Entity> iter = living.iterator();
    		while (iter.hasNext())
    		{
    			boolean harmEntity = false;
    			Entity next = iter.next();
    			if (next instanceof Player)
    			{
    				
    				Player player = (Player) next;
    				if ( player.isOnline() && !CheckHelmet(player) && player.getGameMode() != GameMode.CREATIVE  )
    				{
    					//if player has no helmet and is not in creative and there are confusion effects - harm him
    					harmEntity = true;		
    				}
    			}
    			else if (next instanceof LivingEntity)
    			{
    				//damage entity
    				harmEntity = true;
    			}
    			if (harmEntity == true)
    			{
    				//damage living entities and unprotected players
    				LivingEntity livingEntity = (LivingEntity) next;
    				livingEntity.damage(1);
    				livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,(int) confuseTime*20, 0));

    			}
    		}
    	}
    }
    
	/**
	 * detonate a timefused projectile mid air
	 * @param cannonball
	 */
    private void detonateTimefuse(FlyingProjectile cannonball)
    {
		if (cannonball.getProjectile().getTimefuse() > 0)
		{
			
			//Delayed Task
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() 
			{
			    public void run() {
			    	
			    	//check is list is not empty
			    	if (!flying_projectiles.isEmpty())
			    	{
						Iterator<FlyingProjectile> iterator = flying_projectiles.iterator();	       			    		  
						while( iterator.hasNext())
						{
							FlyingProjectile flying = iterator.next();	
							Projectile proj = flying.getProjectile();
							Snowball snow = flying.getSnowball();
							if (flying.getSnowball() != null)
							{
	   							if (flying.getSnowball().getTicksLived() > proj.getTimefuse()*20 - 5 && proj.getTimefuse() > 0)
	   	   			    		{
	   	   			    			//detonate timefuse
	   	   			    			explosion.detonate(flying);
	   	   			    			snow.remove();
	   	   			    			iterator.remove();
	   	   			    		}	
							}
						}
					}
			    }}, (long) (cannonball.getProjectile().getTimefuse()*20));
			
		}
	}
		
		
    //############## CheckHelmet   ################################
	private boolean CheckHelmet(Player player)
	{
		ItemStack helmet = player.getInventory().getHelmet();
		if (helmet != null)
		{
			return true;
		}
		return false;
	}
	

	
	//############## deleteOldSnowballs  ################################
	public void deleteOldSnowballs()
	{
		   //delete really old snowballs
		   Iterator<FlyingProjectile> flying = flying_projectiles.iterator();
		   while (flying.hasNext())
		   {
			   FlyingProjectile next = flying.next();
			   if(next.getSnowball().getTicksLived() > 10000)
			   {
				   next.getSnowball().remove();
				   flying.remove();
			   }
		   }
	}
	   
}
