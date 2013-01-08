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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.Projectile;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.CannonData;
import at.pavlov.Cannons.utils.DelayedFireTask;
import at.pavlov.Cannons.utils.FireTaskWrapper;
import at.pavlov.Cannons.utils.FlyingProjectile;
import at.pavlov.Cannons.utils.InventoryManagement;

public class FireCannon {
	
	private Config config;
	private UserMessages userMessages;
	private InventoryManagement InvManage;
	private Cannons plugin;
	private CreateExplosion explosion;
	
	public LinkedList<FlyingProjectile> flying_projectiles = new LinkedList<FlyingProjectile>();
	 
	
	
	
	public FireCannon(Cannons plugin, Config config, UserMessages userMessages, InventoryManagement invManage, CreateExplosion explosion)
	{
		this.plugin = plugin;
		this.config = config;
		this.userMessages = userMessages;
		this.InvManage = invManage;
		this.explosion = explosion;
	}
	
	public LinkedList<FlyingProjectile> getProjectiles ()
	{
		return flying_projectiles;
	}
	
	//################################### PREPARE_FIRE #################################
	public boolean displayPrepareFireMessage(CannonData cannon_loc, Player player)
	{
		if (player!= null)
		{
			if ( config.flint_and_steel==true && player.getItemInHand().getType() != Material.FLINT_AND_STEEL )
			{
				player.sendMessage(userMessages.NoFlintAndSteel);
				return false;
			}
		}
		if (cannon_loc.gunpowder <= 0)
		{
			if (player != null) player.sendMessage(userMessages.NoSulphur);
			return false;
		}
		if(cannon_loc.projectileID == Material.AIR.getId())
		{
			if (player != null) player.sendMessage(userMessages.NoProjectile);
			return false;
		}	
		if (cannon_loc.LastFired + config.fireDelay*1000 >= System.currentTimeMillis())
		{
			if (player != null) player.sendMessage(userMessages.BarreltoHot);
			return false;
		}	
		return true;
	}
	
	//################################### PREPARE_FIRE #################################
	public boolean prepare_fire(CannonData cannon_loc, Player player, Boolean deleteCharge)
	{		
		if (displayPrepareFireMessage(cannon_loc, player) == false) return false;
		
		//check if player or redstone has fired the gun
		if (player != null)
		{
			if (player.hasPermission("cannons.player.fire") )
			{
				//fire the gun
				player.sendMessage(userMessages.FireGun);
				
				//Player fires the gun
				delayedFire(cannon_loc, player, deleteCharge);

			}
			else
			{
				if (player != null) player.sendMessage(userMessages.ErrorPermFire);
			}
		}
		else
		{
			//redstone fire
			delayedFire(cannon_loc, player,deleteCharge);
		}	
		
		return true;
	}
	
	//####################################  DELAYED FIRE  ##############################
    private void delayedFire(CannonData cannon_loc, Player player, Boolean deleteCharge)
    {
		//reset after firing
		cannon_loc.LastFired =  System.currentTimeMillis();
		
		//Set up smoke effects on the torch
		Location torchLoc = cannon_loc.location.getBlock().getRelative(cannon_loc.face.getOppositeFace(),cannon_loc.barrel_length - 1).getRelative(BlockFace.UP, 1).getLocation().clone();
		torchLoc.setX(torchLoc.getX() + 0.5);
		torchLoc.setZ(torchLoc.getZ() + 0.5);
		torchLoc.getWorld().playEffect(torchLoc, Effect.SMOKE, BlockFace.UP);
		torchLoc.getWorld().playSound(torchLoc, Sound.FUSE , 1,1);
		
		//set up delayed task
		Object fireTask = new FireTaskWrapper(cannon_loc, player, deleteCharge);
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
    private void fire(CannonData cannon_locs, Player shooter, Boolean deleteCharge)
    {	
    	Projectile projectile = config.getProjectile(cannon_locs.projectileID, cannon_locs.projectileData);
    	
		Block Block = cannon_locs.location.getBlock();
		Location loc = Block.getRelative(cannon_locs.face).getLocation();
		World world = loc.getWorld();
		loc.setX(loc.getX()+0.5);
		loc.setY(loc.getY()+0.5);
		loc.setZ(loc.getZ()+0.5);
    	
		//Muzzle flash + Muzzle_displ
		if (config.Muzzle_flash == true)
		{
			cannon_locs.location.getWorld().createExplosion(Block.getRelative(cannon_locs.face, config.Muzzle_displ).getLocation(), 0F);
		}
		
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
    		cannonball.snowball = world.spawn(loc, Snowball.class);
    		cannonball.snowball.setFireTicks(100);
    		cannonball.snowball.setTicksLived(2);
    		
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
    		Vector vect = cannon_locs.getFiringVector(config);
    		
    		cannonball.snowball.setVelocity(vect);
    		if (shooter != null) cannonball.snowball.setShooter(shooter);
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
   	   			    			explosion.create_explosion(flying);
   	   			    			flying.snowball.remove();
   	   			    			iterator.remove();
   	   			    		}
   						}
   					}
   			    }}, (long) (cannonball.projectile.timefuse*20));
    			
    		}
		}
		
		
		//reset after firing
		cannon_locs.LastFired =  System.currentTimeMillis();

		if (deleteCharge)
		{
			//delete charge for human gunner
			cannon_locs.gunpowder = 0;
			cannon_locs.projectileID = Material.AIR.getId();
		}
		else
		{
			//redstone autoload
			if (config.redstone_consumption == true)
			{
				//ammo is removed form chest
				if (InvManage.removeAmmoFromChests(cannon_locs, cannon_locs.gunpowder, cannon_locs.projectileID, cannon_locs.projectileData) == false)
				{
					//no more ammo in the chests - delete Charge
	    			cannon_locs.gunpowder = 0;
	    			cannon_locs.projectileID = Material.AIR.getId();
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
