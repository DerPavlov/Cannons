package at.pavlov.Cannons;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.DesignStorage;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.projectile.FlyingProjectile;
import at.pavlov.Cannons.projectile.Projectile;
import at.pavlov.Cannons.utils.DelayedFireTask;
import at.pavlov.Cannons.utils.FireTaskWrapper;

public class FireCannon {
	
	private final Config config;
	private final DesignStorage designStorage;
	private final UserMessages userMessages;
	private final Cannons plugin;
	private final CreateExplosion explosion;
	
	public LinkedList<FlyingProjectile> flying_projectiles = new LinkedList<FlyingProjectile>();
	 
	
	
	
	public FireCannon(Cannons plugin, Config config, UserMessages userMessages, CreateExplosion explosion)
	{
		this.plugin = plugin;
		this.config = config;
		this.designStorage = plugin.getDesignStorage();
		this.userMessages = userMessages;
		this.explosion = explosion;
	}
	
	public LinkedList<FlyingProjectile> getProjectiles ()
	{
		return flying_projectiles;
	}
	
	//################################### PREPARE_FIRE #################################
	public MessageEnum displayPrepareFireMessage(Cannon cannon, Player player)
	{
		CannonDesign design = cannon.getCannonDesign();
		
		
		//check for flint and steel
		if (player!= null)
		{
			if ( config.flint_and_steel==true && player.getItemInHand().getType() != Material.FLINT_AND_STEEL )
			{
				return MessageEnum.ErrorNoFlintAndSteel;
			}
		}
		//check if there is some gunpowder in the barrel
		if (cannon.getLoadedGunpowder() <= 0)
		{
			return MessageEnum.ErrorNoGunpowder;
		}
		//is there a projectile
		if(cannon.getProjectileID() == Material.AIR.getId())
		{
			return MessageEnum.ErrorNoProjectile;
		}	
		//if the player has permission to fire
		if (player.hasPermission(design.getPermissionFire()))
		{
			return MessageEnum.PermissionErrorFire;
		}
		//Barrel too hot
		if (cannon.getLastFired() + design.getBarrelCooldownTime()*1000 >= System.currentTimeMillis())
		{
			return MessageEnum.ErrorBarrelTooHot;
		}	
		//everything fine fire the damn cannon
		return MessageEnum.CannonFire;
	}
	
	//################################### PREPARE_FIRE #################################
	public boolean prepare_fire(Cannon cannon, Player player, Boolean deleteCharge)
	{		
		//check for all permissions
		MessageEnum message = displayPrepareFireMessage(cannon, player);
		
		//return if there are permission missing
		if (message != MessageEnum.CannonFire) return false;
		
		//ignite the cannon
		delayedFire(cannon, player, deleteCharge);	
		
		return true;
	}
	
	//####################################  DELAYED FIRE  ##############################
    private void delayedFire(Cannon cannon, Player player, Boolean deleteCharge)
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
		Object fireTask = new FireTaskWrapper(cannon, player, deleteCharge);
		Long delay = (long) config.ignitionDelay * 20;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedFireTask(fireTask) 
		{
			public void run(Object object) 
			    {			
					FireTaskWrapper fireTask = (FireTaskWrapper) object;
			    	fire(fireTask.cannon, fireTask.player, fireTask.deleteCharge);
			    }
		}, delay);	
    }
	
	//####################################  FIRE  ##############################
    private void fire(Cannon cannon, Player shooter, Boolean deleteCharge)
    {	
    	CannonDesign design = designStorage.getDesign(cannon);
    	Projectile projectile = config.getProjectile(cannon.getProjectileID(), cannon.getProjectileData());
    	
		Location firingLoc = design.getMuzzle(cannon);
		World world = cannon.getWorldBukkit();
    	
		//Muzzle flash + Muzzle_displ
		world.createExplosion(firingLoc, 0F);
		world.playEffect(firingLoc, Effect.SMOKE, cannon.getCannonDirection());
		
		int max_projectiles = 1;
		if (projectile.canisterShot == true && projectile.cannonball == false)
		{
			// get more snowball for canisterShot
			max_projectiles = projectile.amountCanisterShot;
		}
		if (projectile.canisterShot == false && projectile.cannonball == false)
		{
			//fire nothing
			max_projectiles = 0;
		}
		
		for (int i=0; i < max_projectiles; i++)
		{
    		FlyingProjectile cannonball = new FlyingProjectile();
    		cannonball.projectile = projectile;
    		cannonball.snowball = world.spawn(firingLoc, Snowball.class);
    		cannonball.snowball.setFireTicks(100);
    		cannonball.snowball.setTicksLived(2);
    		if (cannonball.projectile.teleport)
    			cannonball.snowball.setPassenger(shooter);
    
    		
    		//confuse shooter if he wears no helmet (only for one projectile and if its configured)
    		if ( i == 0 && config.confusesShooter > 0)
    		{
    			List<Entity> living = cannonball.snowball.getNearbyEntities(10, 10, 10);
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
						livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,(int) config.confusesShooter*20, 0));
	
    				}
    			}
    		}
    		
    		//calculate firing vector
    		Vector vect = cannon.getFiringVector(config);
    		    		
    		cannonball.snowball.setVelocity(vect);
    		if (shooter != null) 
    		{
    			cannonball.snowball.setShooter(shooter);
    			cannonball.projectile.shooter = shooter.getName();
    		}
			flying_projectiles.add(cannonball);
			
    		//detonate if timefuse is used
    		if (cannonball.projectile.timefuse > 0 && !cannonball.projectile.canisterShot)
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
   							if (flying.snowball.getTicksLived() > flying.projectile.timefuse*20 && flying.projectile.timefuse > 0)
   	   			    		{
   	   			    			//detonate timefuse
   	   			    			explosion.detonate(flying);
   	   			    			flying.snowball.remove();
   	   			    			iterator.remove();
   	   			    		}
   						}
   					}
   			    }}, (long) (cannonball.projectile.timefuse*20));
    			
    		}
		}
		
		
		//reset after firing
		cannon.setLastFired(System.currentTimeMillis());

		if (deleteCharge)
		{
			//delete charge for human gunner
			cannon.setLoadedGunpowder(0);
			cannon.setProjectileID(Material.AIR.getId());
			
			//update Sign
			cannon.updateCannonSigns();
		}
		else
		{
			//redstone autoload
			if (config.redstone_consumption == true)
			{
				//ammo is removed form chest
				if (cannon.removeAmmoFromChests() == false)
				{
					//no more ammo in the chests - delete Charge
	    			cannon.setLoadedGunpowder(0);
	    			cannon.setProjectileID(Material.AIR.getId());
	    			//update Sign
	    			cannon.updateCannonSigns();
				}
			}
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
			   if(next.snowball.getTicksLived() > 10000)
			   {
				   next.snowball.remove();
				   flying.remove();
			   }
		   }
	}
	   
}
