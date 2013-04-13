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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.container.MaterialHolder;
import at.pavlov.Cannons.projectile.FlyingProjectile;
import at.pavlov.Cannons.projectile.Projectile;
import at.pavlov.Cannons.projectile.ProjectileProperties;
import de.tyranus.minecraft.bukkit.guildawards.external.GunnerGuildConnector;

public class CreateExplosion {
	
	private Cannons plugin;
	@SuppressWarnings("unused")
	private Config config;
	
	LinkedList<UUID> transmittedEntities = new LinkedList<UUID>();
	
	//################### Constructor ############################################
	public CreateExplosion (Cannons plugin, Config config)
	{
		this.plugin = plugin;
		this.config = config;
		transmittedEntities = new LinkedList<UUID>();
	}


    /**
     * Breaks a obsidian/water/lava blocks if the projectile has superbreaker
     * @param block
     * @param blocklist
     * @param superBreaker
     * @param blockDamage break blocks if true
     * @return true if the block can be destroyed
     */
    private boolean breakBlock(Block block, List<Block> blocklist, Boolean superBreaker, Boolean blockDamage)
    {    	
    	Material material = block.getType();
				
		//register explosion event
		if (material != Material.AIR)
		{
			//test if obsidian
			if ((material == Material.OBSIDIAN))
			{
				if (superBreaker)
				{
					//don't do damage to blocks if false
					if (blockDamage)
						blocklist.add(block);
					// break obsidian/water/laver
					return true;
				}
				//can't break it
				return false;
			}
			//test if water/lava blocks
			else if (material == Material.WATER || material == Material.LAVA)
			{
				if (superBreaker)
				{
					//don't do damage to blocks if false
					if (blockDamage)
						blocklist.add(block);
					// break water/lava
					return true;
				}
				//can't break it but the projectile can pass this block
				return true;
			}
			else
			{
				//don't break bedrock
				if (material != Material.BEDROCK)
				{
					//default material, add block to explosion blocklist
					//don't do damage to blocks if false
					if (blockDamage)
						blocklist.add(block);
					return true;
				}
				//bedrock can't be destroyed
				return false;
			}
		}  
		// air can be destroyed
    	return true;
    }
    
    //####################################  blockBreaker ##############################
    private Location blockBreaker(FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();
    	Snowball snowball = cannonball.getSnowball();
   
    	//has this projectile the super breaker property and makes block damage
    	Boolean superbreaker = projectile.hasProperty(ProjectileProperties.SUPERBREAKER);
    	Boolean doesBlockDamage = projectile.getPenetrationDamage();
   
    	//list of destroy blocks
    	LinkedList<Block> blocklist = new LinkedList<Block>();
    	
    	Vector vel = snowball.getVelocity();
    	Location snowballLoc = snowball.getLocation();
    	World world = snowball.getWorld();
    	int penetration = (int) ((cannonball.getProjectile().getPenetration()) * vel.length() / projectile.getVelocity());
    	Location impactLoc = snowballLoc.clone();
    	
    	// the cannonball will only break blocks if it has penetration. 
    	if (cannonball.getProjectile().getPenetration() > 0)
    	{
    		BlockIterator iter = new BlockIterator(world, snowballLoc.toVector(), vel.normalize(), 0, penetration + 1);
    		
    		int i=0;
    		while (iter.hasNext() && i <= penetration + 1)
    		{
    			i++;
    			Block next = iter.next();
    			//Break block on ray
    			if (i <= penetration)
    			{
    				// if block can be destroyed the the iterator will check the next block. Else the projectile will explode
    				if (breakBlock(next, blocklist, superbreaker, doesBlockDamage) == false)
    				{
    					//found undestroyable block - set impactloc
    					impactLoc = next.getLocation();
    					break;
    				}
    			}
    			//set impact location
    			else
    			{
        			impactLoc = next.getLocation();
    			}
    		}
    	}
    		    	
    	if (superbreaker)
    	{
    		//small explosion on impact
    		Block block = impactLoc.getBlock();
    		breakBlock(block, blocklist, superbreaker, doesBlockDamage);
    		breakBlock(block.getRelative(BlockFace.UP), blocklist, superbreaker, doesBlockDamage);
    		breakBlock(block.getRelative(BlockFace.DOWN), blocklist, superbreaker, doesBlockDamage);
    		breakBlock(block.getRelative(BlockFace.SOUTH), blocklist, superbreaker, doesBlockDamage);
    		breakBlock(block.getRelative(BlockFace.WEST), blocklist, superbreaker, doesBlockDamage);
    		breakBlock(block.getRelative(BlockFace.EAST), blocklist, superbreaker, doesBlockDamage);
    		breakBlock(block.getRelative(BlockFace.NORTH), blocklist, superbreaker, doesBlockDamage);
    	}
    	
    	//no eventhandling if the list is empty
    	if (blocklist.size() > 0) 
    	{
    	
    		//create tnt event
    		EntityExplodeEvent event = new EntityExplodeEvent(cannonball.getSnowball(), impactLoc, blocklist, 1.0f);
    		
    		//handle with bukkit
    		plugin.getServer().getPluginManager().callEvent(event);
		
    		//if not canceled 
    		if(!event.isCancelled() && plugin.BlockBreakPluginLoaded() == false)
    		{
    			// break water, lava, obsidian if cannon projectile
    			for (int i = 0; i < event.blockList().size(); i++)
    			{
    				Block block =  event.blockList().get(i);
    				if (event.getEntity() != null)
    				{
    					block =  event.blockList().get(i);
					
    					// break the block, no matter what it is
    					BlockBreak(block,event.getYield());
    				}
    			}
    		}
    	}
    	return impactLoc;
    }
    
    /***
     * Breaks a block with a certain yield
     * @param block
     * @param yield
     */
	private void BlockBreak(Block block, float yield)
	{
		Random r = new Random();
		if (r.nextFloat() > yield) 
		{
			block.breakNaturally();
		}
		else
		{
			block.setTypeId(0);
		}
	}
    
    /**
     * places a mob on the given location and pushes it away from the impact
     * @param impactLoc
     * @param Loc
     * @param data
     */
    private void PlaceMob(Location impactLoc, Location loc, int data)
    {
    	//set spawnpoint to the middle
    	loc.add(0.5, 0, 0.5);
    	
    	World world = loc.getWorld();
     	Random r = new Random();
     	
     	Integer mobList[] = {50,51,52,54,55,56,57,58,59,60,61,62,65,66,90,91,92,93,94,95,96,98,120};
    	
    	if (data < 0) 
    	{
    		//if all datavalues are allowed create a random spawn
    		data = mobList[r.nextInt(mobList.length)];
    	}
    	
    	Entity entity;
    	EntityType entityType = EntityType.fromId(data);
    	if (entityType != null)
    	{
    		//spawn mob
    		entity = world.spawnEntity(loc, entityType);
    	}
    	else
    	{
    		plugin.logSevere("MonsterEgg ID " + data + " does not exist");
    		return;
    	}
    	
    	if (entity != null)
    	{
    		//get distance form the center
    		double dist = impactLoc.distance(loc);
    		//calculate veloctiy away from the impact
    		Vector vect = loc.clone().subtract(impactLoc).toVector().multiply(1/dist);
    		//set the entity velocity
    		entity.setVelocity(vect);
    	}
    }
    
    //####################################  makeBlockPlace ##############################
    private void makeBlockPlace(Location impactLoc, Location loc, FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();

    	
		Block block = loc.getBlock();
		if (block.getType() == Material.AIR)
		{
			if (checkLineOfSight(impactLoc, loc) == 0)
			{
				if (projectile == null) return;
				
				for (MaterialHolder placeBlock : projectile.getBlockPlaceList())
				{
					//check if Material is a mob egg
					if (placeBlock.equals(Material.MONSTER_EGG))
					{
						//else place mob
						PlaceMob(impactLoc, loc, placeBlock.getData());
					}
					else
					{
						if (cannonball.getSnowball().getShooter() instanceof Player)
						{
							Player player = (Player) cannonball.getSnowball().getShooter();
							//replace air
							block.setTypeId(placeBlock.getId());
							block.setData((byte) placeBlock.getData());
							BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), null, player, true);
							if (event.isCancelled())
							{
								//place air again
								block.setTypeId(0);
							}
						}
					}
				}
				
				
			}
	
		}
    }
    
	//####################################  spreadBlocks ##############################
    private void spreadBlocks(Location impactLoc, FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();
    	
    	if (projectile.doesBlockPlace() == true)
    	{
    		double spread = projectile.getBlockPlaceRadius();
    		int maxPlacement = projectile.getBlockPlaceAmount();
    		
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
    				makeBlockPlace(impactLoc, block.getLocation(), cannonball);
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
    private void applyPotionEffect(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();
    
    	double dist = impactLoc.distanceSquared(next.getLocation());
    	//if the entity is too far away, return
    	if (dist > projectile.getPotionRange()) return;
		
    	// duration of the potion effect
    	double duration = projectile.getPotionDuration()*20;
    	
		if (next instanceof LivingEntity)
		{				
			//calc damage
			LivingEntity living = (LivingEntity) next;

			
			//check line of sight and reduce damage if the way is blocked
			int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
			duration = duration / (blockingBlocks + 1);
			
			//critical
			Random r = new Random();
			int crit = r.nextInt(10);
			if (crit == 0) duration *= 3; 
	
		
		
			// apply potion effect if the duration is not small then 1 tick
			if (duration >= 1)
			{
				int intDuration = (int) Math.floor(duration);
				
				for (PotionEffectType potionEffect : projectile.getPotionsEffectList())
				{
					// apply to entity
					potionEffect.createEffect(intDuration, projectile.getPotionAmplifier()).apply(living);
				}
			}
		}
    }
    
    //####################################  CREATE_EXPLOSION ##############################
    public void detonate(FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();
    	Snowball snowball = cannonball.getSnowball();
    	
    	//blocks form the impact to the impactloc
    	Location impactLoc = blockBreaker(cannonball);
    	
    	//get world
    	World world = impactLoc.getWorld();      	
   
    	
    	//teleport snowball to impact
    	snowball.teleport(impactLoc);
    	
    	float explosion_power = (float) projectile.getExplosionPower();
    	//find living entities
		List<Entity> entity;
		

			
		//explosion event
		boolean incendiary = projectile.hasProperty(ProjectileProperties.INCENDIARY);
		boolean blockDamage = projectile.getExplosionDamage();
	    world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), explosion_power, incendiary, blockDamage);
		
		
		//place blocks around the impact like webs, lava, water
		spreadBlocks(impactLoc, cannonball);
		
		//do potion effects
		int effectRange = (int) projectile.getPotionRange()/2;
		entity = snowball.getNearbyEntities(effectRange, effectRange, effectRange);
		
		Iterator<Entity> it = entity.iterator();
		while (it.hasNext())
		{
			Entity next = it.next();
			applyPotionEffect(impactLoc, next, cannonball);
		}
		
		
		//teleport to impact
		if (cannonball.getProjectile().hasProperty(ProjectileProperties.TELEPORT) == true)
		{
			//teleport shooter to impact
			LivingEntity shooter = snowball.getShooter();
			if (shooter != null) shooter.teleport(impactLoc);
		}
		
		//check which entities are affected by the event
		List<Entity> EntitiesAfterExplosion = snowball.getNearbyEntities(effectRange, effectRange, effectRange);
		transmittingEntities(EntitiesAfterExplosion, snowball.getShooter());
		
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
