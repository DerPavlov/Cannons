package at.pavlov.Cannons;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.craftbukkit.entity.CraftSkeleton;
import org.bukkit.craftbukkit.entity.CraftSpider;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.FireCannon.FlyingProjectile;
import de.tyranus.minecraft.bukkit.guildawards.external.GunnerGuildConnector;

public class CreateExplosion {
	
	private CannonPlugin plugin;
	private Config config;
	
	LinkedList<UUID> transmittedEntities = new LinkedList<UUID>();
	
	//################### Constructor ############################################
	public CreateExplosion (CannonPlugin plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
		transmittedEntities = new LinkedList<UUID>();
	}


    //####################################  breakBreak ##############################
    private boolean breakBlock(Block block, List<Block> blocklist, Boolean superBreaker)
    {
    	boolean worldguardperm = plugin.checkPermission(block.getLocation());
    	if (!worldguardperm && config.allowBlockDamageInProtectedWorlds == false) return false;
    	
    	Material material = block.getType();
				
		//register explosion event
		if (material != Material.AIR)
		{
			//add obsidian/water/lava blocks
			if ((material == Material.OBSIDIAN || material == Material.WATER || material == Material.LAVA))
			{
				if (superBreaker)
				{
					blocklist.add(block);
				}
			}
			else
			{
				//don't break bedrock
				if (material != Material.BEDROCK)
				{
					//no obsidian, add block to explosion blocklist
					blocklist.add(block);
				}
			}
			return true;
		}  
    	return false;
    }
    
    //####################################  blockBreaker ##############################
    private Location blockBreaker(FlyingProjectile cannonball)
    {
    	LinkedList<Block> blocklist = new LinkedList<Block>();
    	
    	Boolean superbreaker = cannonball.projectile.superBreaker;
    	
    	int i=0;
    	Vector vel = cannonball.snowball.getVelocity();
    	Location loc = cannonball.snowball.getLocation();
    	Vector vect = vel.multiply((cannonball.projectile.penetration + 1) / cannonball.projectile.max_speed);
    	for (i = 0; i <= cannonball.projectile.penetration; i++)
    	{
    		vect = vel.clone().multiply((i + 1) / cannonball.projectile.max_speed);
    		breakBlock(loc.clone().add(vect).getBlock(), blocklist, superbreaker);
    	}
    		    	
		Location impactLoc = loc.add(vect).getBlock().getLocation();
    	if (cannonball.projectile.superBreaker == true)
    	{
    		//small explosion on impact
    		Block block = impactLoc.getBlock();
    		breakBlock(block,blocklist,superbreaker);
    		breakBlock(block.getRelative(BlockFace.UP), blocklist, superbreaker);
    		breakBlock(block.getRelative(BlockFace.DOWN), blocklist, superbreaker);
    		breakBlock(block.getRelative(BlockFace.SOUTH), blocklist, superbreaker);
    		breakBlock(block.getRelative(BlockFace.WEST), blocklist, superbreaker);
    		breakBlock(block.getRelative(BlockFace.EAST), blocklist, superbreaker);
    		breakBlock(block.getRelative(BlockFace.NORTH), blocklist, superbreaker);
    	}
    	
    	EntityExplodeEvent event = new EntityExplodeEvent(cannonball.snowball, impactLoc, blocklist, 0.3f);
	    
	    
	    //handle with bukkit
		plugin.getServer().getPluginManager().callEvent(event);
	    	
    	return impactLoc;
    }
    
    //####################################  PlaceMob ##############################
    private void PlaceRandomMob(Location Loc)
    {
    	World world = Loc.getWorld();
    	Random r = new Random();
    	
    	switch (r.nextInt(4))
    	{
    		case 0:
    			//spawn Zombie
    			world.spawn(Loc, CraftZombie.class);
    			break;
    		case 1:
    			//spawn Creeper
    			world.spawn(Loc, CraftCreeper.class);
    			break;
    		case 2:
    			//spawn Spider
    			world.spawn(Loc, CraftSpider.class);
    			break;
    		case 3:
    			//spawn Skeleton
    			world.spawn(Loc, CraftSkeleton.class);
    			break;
    	}
    }
    
    //####################################  makeBlockPlace ##############################
    private void makeBlockPlace(Location impactLoc, Location Loc, Projectile projectile)
    {
		Block block = Loc.getBlock();
		Boolean worldperm = plugin.checkPermission(block.getLocation());
		if (block.getType() == Material.AIR)
		{
			if (checkLineOfSight(impactLoc, Loc) == 0){
				if ((worldperm || config.allowBlockDamageInProtectedWorlds==true))
				{
					{
						//check if Material is no mob egg
						if (projectile == null) return;
						
						if (projectile.placeBlockMaterial != null)
						{
							if (projectile.placeBlockMaterial.equals(Material.MONSTER_EGG))
							{
								//else place mob
								PlaceRandomMob(Loc);
							}
							else
							{
								//replace block (air with blocktype)
								block.setType(projectile.placeBlockMaterial);
							}
						}
					}
				}
			}
	
		}
    }
    
	//####################################  spreadBlocks ##############################
    private void spreadBlocks(Location impactLoc, Projectile projectile)
    {
    	if (projectile.placeBlock == true)
    	{
    		double spread = projectile.placeBlockRadius;
    		int maxPlacement = projectile.placeBlockAmount;
    		
    		Random r = new Random();
    		Location loc;
    		
    		//iterate blocks around to get a good place
    		int i = 0;
			int iterations1 = 0;
    		do 
    		{
    			iterations1 ++;
    			
    			loc = impactLoc.clone();
    			//get new position
    			loc.setX(loc.getX() + r.nextGaussian()*spread/2);
    			loc.setZ(loc.getZ() + r.nextGaussian()*spread/2);
    			
    			//go up or down until it finds AIR and below no AIR
    			int iterations2 = 0;
    			Block block = loc.getBlock();
    			boolean finished = false;
    			do 
    			{
    				iterations2 ++;
    				if (block.getType() != Material.AIR)
    				{
    					//go up
    					block = block.getRelative(BlockFace.UP);
    				}
    				else if (block.getRelative(BlockFace.DOWN).getType() == Material.AIR)
    				{
    					//block below is AIR - go down
    					block = block.getRelative(BlockFace.DOWN);
    				}
    				else 
    				{
    					//block found
    					finished = true;
    				}
    			} while (finished == false && iterations2 <= spread);
    			//if no error place the block
    			if (finished == true)
    			{
    				i++;
    				makeBlockPlace(impactLoc, block.getLocation(), projectile);
    			}
    		} while (iterations1 < maxPlacement * 2 && i < maxPlacement);
    	}  	
    }
    
    //####################################  checkLineOfSight ##############################
    private int checkLineOfSight(Location impact, Location target)
    {    	
    	int blockingBlocks = 0;
    	
    	// vector pointing from impact to target
    	Vector vect = target.toVector().clone().subtract(impact.toVector());
    	int length = (int) Math.ceil(vect.length());
    	vect.normalize();
    	
    	
    	Location impactClone = impact.clone();
    	for (int i = 2; i <= length; i++)
    	{
    		// check if line of sight is blocked
    		if (impactClone.add(vect).getBlock().getType() != Material.AIR)
    		{
    			blockingBlocks ++;
    		}
    	}
    	return blockingBlocks;
    }
    
  //####################################  doPlayerDamage ##############################
    private void doPlayerDamage(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
    	double dist = impactLoc.distanceSquared(next.getLocation());
		if (next instanceof LivingEntity)
		{				
			//calc damage
			LivingEntity living = (LivingEntity) next;
			double damage;
			if (dist <= 16)
			{
				//set the damage to max if player is near to explosion (distance is squared)
				damage = cannonball.projectile.player_damage;
			}
			else
			{
				//player is farer away
				damage = Math.floor(cannonball.projectile.player_damage/(dist-15));	
			}
			
			//check line of sight and reduce damage if the way is blocked
			int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
			damage = damage / (blockingBlocks + 1);
			
			//critical
			Random r = new Random();
			int crit = r.nextInt(10);
			if (crit == 0) damage *= 3;  
			if (damage >= 1)
			{
						
				//to obtain half heart damage
				int intDamage = (int) (damage * 2.0);
				
				if (config.usePlayerName == true)
				{
					// transmit the player for player damage. Kill can be displayed.
					living.damage(intDamage,  cannonball.snowball.getShooter());
				}
				else
				{
					//some plugins seems to interfere if the player is used
					living.damage(intDamage,  null);
				}
			}
			
			//only use effects if serious damage is done
			int amplifer = 0;
			if (damage >= 2)
			{
				amplifer = 1;
			}
			
			if (cannonball.projectile.blindness && damage >= 1)
			{
				living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (cannonball.projectile.effectDuration*20), amplifer));
			}
			if (cannonball.projectile.confusion && damage >= 1)
			{
				living.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) (cannonball.projectile.effectDuration*20), amplifer));
			}
			if (cannonball.projectile.hunger && damage >= 1)
			{
				living.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, (int) (cannonball.projectile.effectDuration*20), amplifer));
			}
			if (cannonball.projectile.poison && damage >= 1)
			{					
				living.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (cannonball.projectile.effectDuration*5), 0));
			}
			if (cannonball.projectile.slowDigging && damage >= 1)
			{
				living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, (int) (cannonball.projectile.effectDuration*20), amplifer));
			}
			if (cannonball.projectile.slowness && damage >= 1)
			{
				living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (cannonball.projectile.effectDuration*20), amplifer));
			}
			if (cannonball.projectile.weakness && damage >= 1)
			{
				living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) (cannonball.projectile.effectDuration*20), amplifer));
			}
		}
    }
    
    //####################################  CREATE_EXPLOSION ##############################
    public void create_explosion(FlyingProjectile cannonball)
    {
    	//blocks form the impact to the impactloc
    	Location impactLoc = blockBreaker(cannonball);	
    	
    	
    	//check worldguard
    	boolean worldperm = plugin.checkPermission(impactLoc);
    	float explosion_power = (float) cannonball.projectile.explosion_power;
    	if (!worldperm && config.allowBlockDamageInProtectedWorlds == false) explosion_power = 0;
    	//find living entities
		List<Entity> entity;
		if (cannonball.projectile.canisterShot)
		{
			//canister shot - no explosion
			entity = cannonball.snowball.getNearbyEntities(2, 2, 2);
		}
		else 
		{
			//normal shot + explosion
			entity = cannonball.snowball.getNearbyEntities(20, 20, 20);
	    	cannonball.snowball.getLocation().getWorld().createExplosion(impactLoc, explosion_power, cannonball.projectile.incendiary);
		}
		
		
		//place blocks around the impact
		spreadBlocks(impactLoc, cannonball.projectile);
		
		//do damage to player if worldguard allows it
		if (worldperm == true || config.allowPlayerDamageInProtectedWorlds == true) 
		{
			Iterator<Entity> it = entity.iterator();
			while (it.hasNext())
			{
				Entity next = it.next();
				doPlayerDamage(impactLoc, next, cannonball);
			}
		}
		
		
		//teleport to impact
		if (cannonball.projectile.teleport == true)
		{
			//teleport shooter to impact
			LivingEntity shooter = cannonball.snowball.getShooter();
			if (shooter != null) shooter.teleport(impactLoc);
		}
		
		List<Entity> EntitiesAfterExplosion = cannonball.snowball.getNearbyEntities(20, 20, 20);
		transmittingEntities(EntitiesAfterExplosion, cannonball.snowball.getShooter());
		
    }
    
	//####################################  transmittingEntities  ##############################
	private void transmittingEntities(List<Entity> after, Entity shooter)
	{
		//check if GuildAwards is loaded
		GunnerGuildConnector handler = plugin.getCannonGuildHandler();
		if (handler == null) return;
		
		//check if there is a shooter, redstone cannons are not counted
		if (shooter == null) return;
		if (!(shooter instanceof Player)) return;
		
		//return if the list before is empty
		if (after.size() == 0) return;
		
		//calculate distance form the shooter location to the first monster
		double distance = 0.0;
		
		//check which entities have been killed
		List<LivingEntity> killedEntities = new LinkedList<LivingEntity>();
		Iterator<Entity> iter = after.iterator();
		while (iter.hasNext())
		{
			Entity entity = iter.next();
			if (entity instanceof LivingEntity)
			{
				// killed by the explosion
				if (entity.isDead() == true)
				{	
					LivingEntity LivEntity = (LivingEntity) entity;
					//check if the entity has not been transmitted
					if(hasBeenTransmitted(LivEntity.getUniqueId()) == false)
					{
						//calculate distance form the shooter location to the first monster
						distance = shooter.getLocation().distance(LivEntity.getLocation());	
						killedEntities.add(LivEntity);
						transmittedEntities.add(LivEntity.getUniqueId());
					}

				}
			
			}
		}
			
		// list should not be empty
		if (killedEntities.size() > 0)
		{
			try {
				handler.updateGunnerReputation((Player) shooter, killedEntities, distance);
			} catch (Exception e) {
				plugin.logSevere("Error adding reputation to player");
			}	
		}
	}
	
	
	//############### hasBeenTransmitted ########################
	private boolean hasBeenTransmitted(UUID id)
	{
		ListIterator<UUID> iter = transmittedEntities.listIterator(transmittedEntities.size());
		while (iter.hasPrevious())
		{
			if (iter.previous() == id) return true;
		}
		//has not been transmitted
		return false;
	}
	
	//############### deleteTransmittedEntities ##################
	public void deleteTransmittedEntities()
	{
		transmittedEntities = new LinkedList<UUID>();
	}
}
