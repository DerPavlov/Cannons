package at.pavlov.Cannons;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.utils.DelayedFireTask;
import at.pavlov.Cannons.utils.FireTaskWrapper;

public class FireCannon {
	
	private Config config;
	private UserMessages userMessages;
	private InventoryManagement InvManage;
	private CannonPlugin plugin;
	private CreateExplosion explosion;
	
	public class FlyingProjectile
	{
		Snowball snowball;
		Projectile projectile;
	}
	public LinkedList<FlyingProjectile> flying_projectiles = new LinkedList<FlyingProjectile>();
	 
	
	
	
	public FireCannon(CannonPlugin plugin, Config config, UserMessages userMessages, InventoryManagement invManage, CreateExplosion explosion)
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
		if(cannon_loc.projectile == Material.AIR)
		{
			if (player != null) player.sendMessage(userMessages.NoProjectile);
			return false;
		}	
		if (cannon_loc.LastFired + config.fireDelay*20 >= cannon_loc.location.getWorld().getFullTime() )
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
				delayedFire(cannon_loc, player,deleteCharge);
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
	
	//####################################  FIRE  ##############################
    private void delayedFire(CannonData cannon_loc, Player player, Boolean deleteCharge)
    {
		//reset after firing
		cannon_loc.LastFired =  cannon_loc.location.getWorld().getFullTime();
		
		//Set up smoke effects on the torch
		Location torchLoc = cannon_loc.location.getBlock().getRelative(cannon_loc.face.getOppositeFace(),cannon_loc.barrel_length - 1).getRelative(BlockFace.UP, 1).getLocation().clone();
		torchLoc.setX(torchLoc.getX() + 0.5);
		torchLoc.setZ(torchLoc.getZ() + 0.5);
		torchLoc.getWorld().playEffect(torchLoc, Effect.SMOKE, BlockFace.UP);
		torchLoc.getWorld().playEffect(torchLoc, Effect.EXTINGUISH, 0);
		
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
    	Projectile projectile = config.getProjectile(cannon_locs.projectile);
    	
		Block Block = cannon_locs.location.getBlock();
		Location loc = Block.getRelative(cannon_locs.face).getLocation();
		World world = loc.getWorld();
		loc.setX(loc.getX()+0.5);
		loc.setY(loc.getY()+0.5);
		loc.setZ(loc.getZ()+0.5);
    	
		//Muzzle flash + Muzzle_displ
		if (config.Muzzle_flash == true){
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
    		
    		//confuse shooter if he wears no helmet
    		if ( i == 0 )
    		{
    			List<Entity> living = cannonball.snowball.getNearbyEntities(10, 10, 10);
    			Iterator<Entity> iter = living.iterator();
    			while (iter.hasNext())
    			{
    				Entity next = iter.next();
    				if (next instanceof Player)
    				{
    					Player player = (Player) next;
    					if (player.isOnline()){
    						if (!CheckHelmet(player) && player.getGameMode() != GameMode.CREATIVE && config.confusesShooter > 0 )
    						{
    							//no savefty gear
    							player.damage(1);
    							player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,(int) config.confusesShooter*20, 0));
    						}
    					}
    				}
    			}
    		}
    		
    		//set direction of the snowball
    		Vector vect = new Vector(1f,0f,0f);
    		Random r = new Random();
    		//1.0 for min_barrel_length and gets smaller
    		double shrinkFactor =  Math.exp(- 2.0 * ((double) cannon_locs.barrel_length - config.min_barrel_length)/(config.max_barrel_length - config.min_barrel_length));
    		
    		double deviation = r.nextGaussian() * config.angle_deviation * shrinkFactor;
    		if (projectile.canisterShot) deviation += r.nextGaussian() * projectile.spreadCanisterShot;
    		double horizontal =  Math.sin((cannon_locs.horizontal_angle + deviation) * Math.PI/180);
    		
    		deviation = r.nextGaussian() * config.angle_deviation *shrinkFactor;
    		if (projectile.canisterShot) deviation += r.nextGaussian() * projectile.spreadCanisterShot;
    		double vertical = Math.sin((cannon_locs.vertical_angle+ deviation)  * Math.PI/180);
    		
    		if (cannon_locs.face == BlockFace.NORTH) 
    		{
    			 vect = new Vector( -1.0f, vertical, -horizontal);
    		}
    		else if (cannon_locs.face == BlockFace.EAST) 
    		{
    			 vect = new Vector(horizontal , vertical, -1.0f);
    		}
    		else if (cannon_locs.face == BlockFace.SOUTH) 
    		{
    			vect = new Vector( 1.0f, vertical, horizontal);
    		}
    		else if (cannon_locs.face == BlockFace.WEST) 
    		{
    			 vect = new Vector(-horizontal , vertical,1.0f);
    		}
    		
    		double multi = cannonball.projectile.max_speed  * cannon_locs.gunpowder / config.max_gunpowder;
    		vect=vect.multiply(multi) ;
    		if (multi < 0.1) multi=0.1;
    		
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
		cannon_locs.LastFired =  loc.getWorld().getFullTime();

		if (deleteCharge)
		{
			//delete charge for human gunner
			cannon_locs.gunpowder = 0;
			cannon_locs.projectile = Material.AIR;
		}
		else
		{
			//redstone autoload
			if (config.redstone_consumption == true)
			{
				//ammo is removed form chest
				if (InvManage.removeAmmoFromChests(cannon_locs, cannon_locs.gunpowder, cannon_locs.projectile) == false)
				{
					//no more ammo in the chests - delete Charge
	    			cannon_locs.gunpowder = 0;
	    			cannon_locs.projectile = Material.AIR;
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
