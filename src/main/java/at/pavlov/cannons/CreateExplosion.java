package at.pavlov.cannons;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.UUID;


import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;

public class CreateExplosion {
	
	private Cannons plugin;

    LinkedList<UUID> transmittedEntities = new LinkedList<UUID>();
	
	//################### Constructor ############################################
	public CreateExplosion (Cannons plugin, Config config)
	{
		this.plugin = plugin;
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
        MaterialHolder destroyedBlock = new MaterialHolder(block.getTypeId(), block.getData());
				
		//air is not an block to break, so ignore it
		if (!destroyedBlock.equals(Material.AIR))
		{
            //if it is unbreakable, ignore it
            for (MaterialHolder unbreakableBlock : plugin.getmyConfig().getUnbreakableBlocks())
            {
                if (unbreakableBlock.equalsFuzzy(destroyedBlock))
                {
                    //this block is protected and impenetrable
                    return false;
                }
            }

			//test if it needs superbreaker
            for (MaterialHolder superbreakerBlock : plugin.getmyConfig().getSuperbreakerBlocks())
            {
                if ((superbreakerBlock.equalsFuzzy(destroyedBlock)))
                {
                    if (superBreaker)
                    {
                        //this projectile has superbreaker and can destroy this block

                        //don't do damage to blocks if false. But it will penetrate the blocks
                        if (blockDamage)
                            blocklist.add(block);
                        // break it
                        return true;
                    }
                    else
                    {
                        //it has not the superbreaker ability and this block is therefore impenetrable
                        return false;
                    }
                }
            }

            //so it is not protected and not a superbreaker block. So break it
            if (blockDamage)
                blocklist.add(block);
            return true;

		}  
		// air can be destroyed
    	return true;
    }
    
    /**
     * breaks blocks that are on the trajectory of the projectile. The projectile is stopped by impenetratable blocks (obsidian)
     * @param cannonball
     * @return
     */
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

        plugin.logDebug("piercing some blocks at: " + penetration);

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
    	
    		//create bukkit event
    		EntityExplodeEvent event = new EntityExplodeEvent(null, impactLoc, blocklist, 1.0f);
    		//handle with bukkit
    		plugin.getServer().getPluginManager().callEvent(event);

		
    		//if not canceled
    		if(!event.isCancelled());// && plugin.BlockBreakPluginLoaded() == false)
    		{
                //fire custom piercing event to notify other plugins
                ProjectilePiercingEvent impactEvent = new ProjectilePiercingEvent(projectile, impactLoc, blocklist);
                Bukkit.getServer().getPluginManager().callEvent(impactEvent);

    			// break water, lava, obsidian if cannon projectile
    			for (int i = 0; i < event.blockList().size(); i++)
    			{
    				Block block =  event.blockList().get(i);
    				if (event.getEntity() != null)
    				{
    					block =  event.blockList().get(i);
					
    					// break the block, no matter what it is
                        BreakBreakNaturally(block,event.getYield());
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
	private void BreakBreakNaturally(Block block, float yield)
	{
		Random r = new Random();
		if (r.nextFloat() > yield) 
		{
			block.breakNaturally();
		}
		else
		{
			block.setType(Material.AIR);
		}
	}
    
    /**
     * places a mob on the given location and pushes it away from the impact
     * @param impactLoc
     * @param loc
     * @param data
     */
    private void PlaceMob(Location impactLoc, Location loc, double entityVelocity, int data, double tntFuse)
    {    	
    	World world = impactLoc.getWorld();
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
    		//get distance form the center + 1 to avoid division by zero
    		double dist = impactLoc.distance(loc) + 1;
    		//calculate veloctiy away from the impact
    		Vector vect = loc.clone().subtract(impactLoc).toVector().multiply(entityVelocity/dist);
    		//set the entity velocity
    		entity.setVelocity(vect);
            //for TNT only
            if (entity instanceof TNTPrimed)
            {
                TNTPrimed tnt = (TNTPrimed) entity;
                tnt.setFuseTicks((int) tntFuse*20);
            }
    	}
    }
    
    /**
     * spawns a falling block with the id and data that is slinged away from the impact
     * @param impactLoc
     * @param loc
     * @param entityVelocity
     * @param item
     */
    private void spawnFallingBlock(Location impactLoc, Location loc, double entityVelocity, MaterialHolder item)
    {
    	FallingBlock entity = impactLoc.getWorld().spawnFallingBlock(loc, item.getId(), (byte) item.getData());
    	
    	//give the blocks some velocity
    	if (entity != null)
    	{
    		//get distance form the center + 1, to avoid division by zero
    		double dist = impactLoc.distance(loc) + 1;
    		//calculate veloctiy away from the impact
    		Vector vect = loc.clone().subtract(impactLoc).toVector().multiply(entityVelocity/dist);
    		//set the entity velocity
    		entity.setVelocity(vect);
    		//set some other properties
    		entity.setDropItem(false);
    	}
    	else
    	{
    		plugin.logSevere("Item id:" + item.getId() + " data:" + item.getData() + " can't be spawned as falling block.");
    	}
    }
    
    /**
     * performs the entity placing on the given location
     * @param impactLoc
     * @param loc
     * @param cannonball
     */
    private void makeBlockPlace(Location impactLoc, Location loc, FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();

		if (canPlaceBlock(loc.getBlock()))
		{
			if (checkLineOfSight(impactLoc, loc) == 0)
			{
		    	if (projectile == null)
		    	{
		    		plugin.logSevere("no projectile data in flyingprojectile for makeBlockPlace");
		    		return;
		    	}
				
				for (MaterialHolder placeBlock : projectile.getBlockPlaceList())
				{
					//check if Material is a mob egg
					if (placeBlock.equals(Material.MONSTER_EGG))
					{
						//else place mob
						PlaceMob(impactLoc, loc, projectile.getBlockPlaceVelocity(), placeBlock.getData(),projectile.getTntFuseTime());
					}
					else
					{
						spawnFallingBlock(impactLoc, loc, projectile.getBlockPlaceVelocity(), placeBlock);
					}
				}			
			}
		}
    }
    
	/**
	 * performs the block spawning for the given projectile
	 * @param impactLoc
	 * @param cannonball
	 */
    private void spreadBlocks(Location impactLoc, FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();
    	
    	if (projectile.doesBlockPlace() == true)
    	{
    		Random r = new Random();
    		Location loc;
    		
    		double spread = projectile.getBlockPlaceRadius();
    		//add some randomness to the amount of spawned blocks
    		int maxPlacement = (int) (projectile.getBlockPlaceAmount() * (1+r.nextGaussian()));
    		
    		
    		//iterate blocks around to get a good place
    		int placedBlocks = 0;
			int iterations1 = 0;
    		do 
    		{
    			iterations1 ++;
    			
    			loc = impactLoc.clone();
    			//get new position
    			loc.setX(loc.getX() + r.nextGaussian()*spread/2);
    			loc.setZ(loc.getZ() + r.nextGaussian()*spread/2);
    			
    			//check a entity can spawn on this block
    			if (canPlaceBlock(loc.getBlock()))
    			{
    				placedBlocks++;
    				//place the block
    				makeBlockPlace(impactLoc, loc, cannonball);
    			}
    		} while (iterations1 < maxPlacement && placedBlocks < maxPlacement);
    	}  	
    }
    
    /**
     * returns true if an entity can be place on this block
     * @param block
     * @return
     */
    private boolean canPlaceBlock(Block block)
    {
    	return block.getType() == Material.AIR || block.getType() == Material.FIRE || block.isLiquid();
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

    /**
     * Gives a player next to an explosion an entity effect
     * @param impactLoc
     * @param next
     * @param cannonball
     */
    private void applyPotionEffect(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
    	Projectile projectile = cannonball.getProjectile();

        if (next instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) next;

    	    double dist = impactLoc.distance(living.getEyeLocation());
    	    //if the entity is too far away, return
    	    if (dist > projectile.getPotionRange()) return;
		
    	    // duration of the potion effect
    	    double duration = projectile.getPotionDuration()*20;

			//check line of sight and reduce damage if the way is blocked
			int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
			duration = duration / (blockingBlocks + 1);
			
			//randomizer
			Random r = new Random();
			float rand = r.nextFloat();
			duration *= rand/2 + 0.5;
		
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

    /**
     * Hurts a player next to an explosion
     * @param impactLoc
     * @param next
     * @param cannonball
     */
    private void doPlayerDamage(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
        Projectile projectile = cannonball.getProjectile();

        if (next instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) next;

            double dist = impactLoc.distance((living).getEyeLocation());
            plugin.logDebug("Distance to impact: " + String.format("%.2f", dist));
            //if the entity is too far away, return
            if (dist > projectile.getPlayerDamageRange()) return;

            //given damage is in half hearts
            double damage = projectile.getPlayerDamage();

            //check line of sight and reduce damage if the way is blocked
            int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
            damage = damage / (blockingBlocks + 1);

            //randomizer
            Random r = new Random();
            float rand = r.nextFloat();
            damage *= rand/2 + 0.5;

            //calculate the armor reduction
            double reduction = 1.0;
            if (living instanceof Player)
            {
                Player player = (Player) living;
                reduction *= (1-CannonsUtil.getArmorDamageReduced(player)) * (1-CannonsUtil.getBlastProtection(player));
            }

            plugin.logDebug("PlayerDamage done to " + living.getType() + " is: " + String.format("%.2f", damage) + " armor reduction factor: " + String.format("%.2f", reduction));

            damage = damage * reduction;

            // apply damage to the entity.
            if (damage >= 1)
            {
                //damage entity
                living.damage((int) Math.floor(damage));

                //if player wears armor reduce damage
                if (living instanceof Player)
                {
                    Player player = (Player) living;
                    CannonsUtil.reduceArmorDurability(player);
                }
            }
        }
    }


    /**
     * Hurts a player hit by a projectile
     * @param impactLoc
     * @param next
     * @param cannonball
     */
    private void doDirectHitDamage(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
        Projectile projectile = cannonball.getProjectile();


        if (next instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) next;

            double dist = impactLoc.distance((living).getEyeLocation());
            //if the entity is too far away, return
            if (dist > 2) return;

            //given damage is in half hearts
            double damage = projectile.getDirectHitDamage();

            //check line of sight and reduce damage if the way is blocked
            int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
            damage = damage / (blockingBlocks + 1);

            //randomizer
            Random r = new Random();
            float rand = r.nextFloat();
            damage *= rand/2 + 0.5;

            //calculate the armor reduction
            double reduction = 1.0;
            if (living instanceof Player)
            {
                Player player = (Player) living;
                reduction *= (1-CannonsUtil.getArmorDamageReduced(player)) * (1-CannonsUtil.getProjectileProtection(player));
            }

            plugin.logDebug("DirectHitDamage done to " + living.getType() + " is: " + String.format("%.2f", damage) + " armor reduction factor: " + String.format("%.2f", reduction));

            damage = damage * reduction;
            // apply damage to the entity.
            if (damage >= 1)
            {
                //damage entity
                living.damage((int) Math.floor(damage));

                //if player wears armor reduce damage
                if (living instanceof Player)
                {
                    Player player = (Player) living;
                    CannonsUtil.reduceArmorDurability(player);
                }
            }
        }
    }
    
    //####################################  CREATE_EXPLOSION ##############################
    public void detonate(FlyingProjectile cannonball)
    {
        plugin.logDebug("detonate cannonball");

    	Projectile projectile = cannonball.getProjectile().clone();
    	Snowball snowball = cannonball.getSnowball();

        LivingEntity shooter = snowball.getShooter();
        Player player = null;
        if (shooter instanceof Player)
            player = (Player) shooter;

        //do no underwater damage if disabled (explosion but no damage)
        boolean isUnderwater = false;
        if ( snowball.getLocation().clone().subtract(snowball.getVelocity().normalize().multiply(2)).getBlock().isLiquid())
        {
            isUnderwater = true;
        }
        plugin.logDebug("Explosion is underwater: " + isUnderwater);
    	
    	//breaks blocks from the impact of the projectile to the location of the explosion
    	Location impactLoc = blockBreaker(cannonball);
    	
    	//get world
    	World world = impactLoc.getWorld();      	

    	//teleport snowball to impact
    	snowball.teleport(impactLoc);
    	
    	float explosion_power = (float) projectile.getExplosionPower();

        //reset explosion power if it is underwater and not allowed
        if (projectile.isUnderwaterDamage() == false && isUnderwater)
        {
            plugin.logDebug("Underwater explosion power set to zero");
            explosion_power = 0;
        }

    	//find living entities
		List<Entity> entity;

        //fire impact event
        ProjectileImpactEvent impactEvent = new ProjectileImpactEvent(projectile, impactLoc);
        Bukkit.getServer().getPluginManager().callEvent(impactEvent);

        //if canceled then exit
        if (impactEvent.isCancelled())
        {
            //event canceled, make a save fake explosion
            world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), 0);
            return;
        }
			
		//explosion event
		boolean incendiary = projectile.hasProperty(ProjectileProperties.INCENDIARY);
		boolean blockDamage = projectile.getExplosionDamage();
	    boolean notCanceled = world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), explosion_power, incendiary, blockDamage);


        //send a message about the impact (only if the projectile has enabled this feature)
        if (projectile.isImpactMessage())
            plugin.displayImpactMessage(player, impactLoc, notCanceled);

		if (notCanceled == true)
        {
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
                doPlayerDamage(impactLoc, next, cannonball);
                doDirectHitDamage(impactLoc, next, cannonball);
            }

            //check which entities are affected by the event
            List<Entity> EntitiesAfterExplosion = snowball.getNearbyEntities(effectRange, effectRange, effectRange);
            transmittingEntities(EntitiesAfterExplosion, snowball.getShooter());//place blocks around the impact like webs, lava, water
		    spreadBlocks(impactLoc, cannonball);

        }
    }
    
	//####################################  transmittingEntities  ##############################
	private void transmittingEntities(List<Entity> after, Entity shooter)
	{
        //exit now
        shooter = null;
		
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
				//handler.updateGunnerReputation((Player) shooter, killedEntities, distance);
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
