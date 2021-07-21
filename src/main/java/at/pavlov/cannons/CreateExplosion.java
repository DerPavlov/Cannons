package at.pavlov.cannons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import at.pavlov.cannons.Enum.EntityDataType;
import at.pavlov.cannons.Enum.FakeBlockType;
import at.pavlov.cannons.Enum.ProjectileCause;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.container.DeathCause;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.container.SoundHolder;
import at.pavlov.cannons.container.SpawnEntityHolder;
import at.pavlov.cannons.container.SpawnMaterialHolder;
import at.pavlov.cannons.event.CannonsEntityDeathEvent;
import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import at.pavlov.cannons.utils.CannonsUtil;
import at.pavlov.cannons.utils.DelayedTask;

public class CreateExplosion {

    private final Cannons plugin;
    private final Config config;

    private FlyingProjectile currentCannonball;

    private final HashSet<Entity> affectedEntities = new HashSet<>();
    // the entity is used in 1 tick. There should be no garbage collector problem
    private final HashMap<Entity, Double> damageMap = new HashMap<>();
    // players killed by cannons <Player, Cannon>
    private final HashMap<UUID, DeathCause> killedPlayers = new HashMap<>();

    // ################### Constructor ############################################
    public CreateExplosion(Cannons plugin, Config config) {
	this.plugin = plugin;
	this.config = plugin.getMyConfig();
    }

    /**
     * Breaks a obsidian/water/lava blocks if the projectile has superbreaker
     * 
     * @param block
     * @param blocklist
     * @param superBreaker
     * @param blockDamage
     *            break blocks if true
     * @return true if the block can be destroyed
     */
    private boolean breakBlock(Block block, List<Block> blocklist, Boolean superBreaker, Boolean blockDamage) {
	BlockData destroyedBlock = block.getBlockData();

	// air is not an block to break, so ignore it
	if (!destroyedBlock.getMaterial().equals(Material.AIR)) {
	    // if it is unbreakable, ignore it
	    for (BlockData unbreakableBlock : this.config.getUnbreakableBlocks()) {
		if (unbreakableBlock.matches(destroyedBlock))
		    // this block is protected and impenetrable
		    return false;
	    }

	    // test if it needs superbreaker
	    for (BlockData superbreakerBlock : this.config.getSuperbreakerBlocks()) {
		if ((superbreakerBlock.matches(destroyedBlock))) {
		    if (superBreaker) {
			// this projectile has superbreaker and can destroy this block

			// don't do damage to blocks if false. But it will penetrate the blocks
			if (blockDamage) {
			    blocklist.add(block);
			}
			// break it
			return true;
		    } else
			// it has not the superbreaker ability and this block is therefore impenetrable
			return false;
		}
	    }

	    // so it is not protected and not a superbreaker block. So break it
	    if (blockDamage) {
		blocklist.add(block);
	    }
	    return true;

	}
	// air can be destroyed
	return true;
    }

    /**
     * breaks blocks that are on the trajectory of the projectile. The projectile is
     * stopped by impenetratable blocks (obsidian)
     * 
     * @param cannonball
     * @return the location after the piercing event
     */
    private Location blockBreaker(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity) {
	Projectile projectile = cannonball.getProjectile();


		// has this projectile the super breaker property and makes block damage
		Boolean superbreaker = projectile.hasProperty(ProjectileProperties.SUPERBREAKER);
		Boolean doesBlockDamage = projectile.getPenetrationDamage();

		// list of destroy blocks
		LinkedList<Block> blocklist = new LinkedList<>();

		Vector vel = projectile_entity.getVelocity();
		Location snowballLoc = projectile_entity.getLocation();
		World world = projectile_entity.getWorld();
		Location impactLoc = snowballLoc.clone();
		this.plugin.logDebug("Projectile impact: " + impactLoc.getBlockX() + ", " + impactLoc.getBlockY() + ", "
			+ impactLoc.getBlockZ() + " direction: " + impactLoc.getDirection());

		// find surface and set this as new impact location
		impactLoc = CannonsUtil.findSurface(impactLoc, vel);
		this.plugin.logDebug("Impact surface: " + impactLoc.getBlockX() + ", " + impactLoc.getBlockY() + ", "
			+ impactLoc.getBlockZ());

		// the cannonball will only break blocks if it has penetration.
		Random r = new Random();
		double randomness = (1 + r.nextGaussian() / 5.0);
		int penetration = (int) Math.round(
				randomness * cannonball.getProjectile().getPenetration() * Math.pow(vel.length() / projectile.getVelocity(),2));
		if (penetration < 0)
			penetration = 0;
		plugin.logDebug("velocity: " + vel.length() + " percent of max velocity: " + vel.length()/ projectile.getVelocity() + " penetration: " + penetration + " randomness: " + randomness);

		blocklist.clear();
		if (penetration > 0) {
			BlockIterator iter2 = new BlockIterator(world, impactLoc.toVector(), vel.normalize(), 0, penetration);
			while (iter2.hasNext()) {
				Block next = iter2.next();
				// if block can be destroyed the the iterator will check the next block. Else
				// the projectile will explode
				if (!this.breakBlock(next, blocklist, superbreaker, doesBlockDamage)) {
					// found indestructible block
					break;
				}
				impactLoc = next.getLocation();
			}

			this.plugin.logDebug("Penetration loc: " + impactLoc.getBlockX() + ", " + impactLoc.getBlockY() + ", "
					+ impactLoc.getBlockZ());

			if (superbreaker) {
				// small explosion on impact
				Block block = impactLoc.getBlock();
				this.breakBlock(block, blocklist, true, doesBlockDamage);
				this.breakBlock(block.getRelative(BlockFace.UP), blocklist, true, doesBlockDamage);
				this.breakBlock(block.getRelative(BlockFace.DOWN), blocklist, true, doesBlockDamage);
				this.breakBlock(block.getRelative(BlockFace.SOUTH), blocklist, true, doesBlockDamage);
				this.breakBlock(block.getRelative(BlockFace.WEST), blocklist, true, doesBlockDamage);
				this.breakBlock(block.getRelative(BlockFace.EAST), blocklist, true, doesBlockDamage);
				this.breakBlock(block.getRelative(BlockFace.NORTH), blocklist, true, doesBlockDamage);
			}

			// no eventhandling if the list is empty
			if (blocklist.size() > 0) {
				// fire custom piercing event to notify other plugins (blocks can be removed)
				ProjectilePiercingEvent piercingEvent = new ProjectilePiercingEvent(projectile, impactLoc, blocklist);
				this.plugin.getServer().getPluginManager().callEvent(piercingEvent);

				// create bukkit event
				EntityExplodeEvent event = new EntityExplodeEvent(projectile_entity, impactLoc, piercingEvent.getBlockList(), 1.0f);
				this.plugin.getServer().getPluginManager().callEvent(event);

				this.plugin.logDebug("was the cannons explode event canceled: " + event.isCancelled());
				// if not canceled break all given blocks
				if (!event.isCancelled()) {
					// break water, lava, obsidian if cannon projectile
					for (int i = 0; i < event.blockList().size(); i++) {
						Block pBlock = event.blockList().get(i);
						// break the block, no matter what it is
						this.BreakBreakNaturally(pBlock, event.getYield());
					}
				}

			}
		}

		// add the impact velocity as direction of the impactLoc, direction will be normalized
		impactLoc.setDirection(projectile_entity.getVelocity());

		return impactLoc;
    }

    /***
     * Breaks a block with a certain yield
     * 
     * @param block
     *            block to be break
     * @param yield
     *            chance to get the block item
     */
    private void BreakBreakNaturally(Block block, float yield) {
	Random r = new Random();
	if (r.nextFloat() > yield) {
	    block.breakNaturally();
	} else {
	    block.setType(Material.AIR);
	}
    }

    /**
     * places a entity on the given location and pushes it away from the impact
     * 
     * @param cannonball
     *            the involved projectile
     * @param loc
     *            location of the spawn
     * @param entityVelocity
     *            how fast the entity is push away
     * @param entityHolder
     *            type of entity to spawn
     */
    private void spawnEntity(FlyingProjectile cannonball, Location loc, double entityVelocity,
	    SpawnEntityHolder entityHolder) {
	Location impactLoc = cannonball.getImpactLocation();
	World world = impactLoc.getWorld();
	Random r = new Random();


	// move cloud to the ground
	if (entityHolder.getType() == EntityType.AREA_EFFECT_CLOUD) {
	    loc = this.moveToGround(loc, cannonball.getProjectile().getSpawnEntityRadius() * 2.);
	}

	// spawn entity
	Entity entity = world.spawnEntity(loc, entityHolder.getType());

	if (entity != null) {
	    this.plugin.logDebug("Spawned entity: " + entityHolder.getType().toString() + " at impact");
	    // get distance form the center + 1 to avoid division by zero
	    double dist = impactLoc.distance(loc) + 1;
	    // calculate veloctiy away from the impact (speed in y makes problems and entity
	    // sinks in ground)
	    Vector vect = loc.clone().subtract(impactLoc).toVector().normalize().multiply(entityVelocity / dist);// .multiply(new
														 // Vector(1.0,0.0,1.0));
	    // set the entity velocity
	    entity.setVelocity(vect);

	    // add some specific data values
	    // TNT
	    if (entity instanceof TNTPrimed) {
			TNTPrimed tnt = (TNTPrimed) entity;
			try {
				int fusetime = CannonsUtil.parseInt(entityHolder.getData().get(EntityDataType.FUSE_TIME),
					tnt.getFuseTicks());
				int fuseTicks = (int) (fusetime * (1 + r.nextGaussian() / 3.0));
				this.plugin.logDebug("reset TNT fuse ticks to: " + fuseTicks + " fusetime " + fusetime);
				tnt.setFuseTicks(fuseTicks);
			} catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
	    }
	    // AreaEffectCloud
	    if (entity instanceof AreaEffectCloud) {
			AreaEffectCloud cloud = (AreaEffectCloud) entity;
			try {
				// PARTICLE ("Particle"),
				// EFFECTS ("Effects"),
				cloud.setReapplicationDelay(
					CannonsUtil.parseInt(entityHolder.getData().get(EntityDataType.REAPPLICATION_DELAY),
						cloud.getReapplicationDelay()));
				cloud.setRadius(CannonsUtil.parseFloat(entityHolder.getData().get(EntityDataType.RADIUS),
					cloud.getRadius()));
				cloud.setRadiusPerTick(CannonsUtil.parseFloat(
					entityHolder.getData().get(EntityDataType.RADIUS_PER_TICK), cloud.getRadiusPerTick()));
				cloud.setRadiusOnUse(CannonsUtil.parseFloat(
					entityHolder.getData().get(EntityDataType.RADIUS_ON_USE), cloud.getRadiusOnUse()));
				cloud.setDuration(CannonsUtil.parseInt(entityHolder.getData().get(EntityDataType.DURATION),
					cloud.getDuration()));
				cloud.setDurationOnUse((int) CannonsUtil.parseFloat(
					entityHolder.getData().get(EntityDataType.RADIUS_ON_USE), cloud.getDurationOnUse()));
				cloud.setWaitTime(CannonsUtil.parseInt(entityHolder.getData().get(EntityDataType.WAIT_TIME),
					cloud.getWaitTime()));
				cloud.setColor(
					CannonsUtil.parseColor(entityHolder.getData().get(EntityDataType.COLOR), cloud.getColor()));
				cloud.setBasePotionData(CannonsUtil.parsePotionData(
					entityHolder.getData().get(EntityDataType.POTION_EFFECT), cloud.getBasePotionData()));
				cloud.setParticle(CannonsUtil.parseParticle(entityHolder.getData().get(EntityDataType.PARTICLE),
					cloud.getParticle()));
				cloud.setSource(cannonball.getSource());

				plugin.logDebug("spawn AREA_OF_EFFECT_CLOUD " + cloud);
				for (PotionEffect effect : entityHolder.getPotionEffects()){
					plugin.logDebug("add potion effect " + effect);
					cloud.addCustomEffect(effect, true);
				}
			}
			catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
		}
		// SpectralArrow
		if (entity instanceof SpectralArrow) {
			SpectralArrow arrow = (SpectralArrow) entity;
			try {
				arrow.setGlowingTicks(CannonsUtil.parseInt(entityHolder.getData().get(EntityDataType.DURATION),
					arrow.getGlowingTicks()));
			} catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
		}
		// TippedArrow
		if (entity instanceof Arrow) {
			Arrow arrow = (Arrow) entity;
			try {
				arrow.setBasePotionData(CannonsUtil.parsePotionData(
					entityHolder.getData().get(EntityDataType.POTION_EFFECT), arrow.getBasePotionData()));
			} catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
		}
		// LivingEntity
		if (entity instanceof LivingEntity) {
		LivingEntity living = (LivingEntity) entity;
			try {
				EntityEquipment equipment = living.getEquipment();
				if (equipment != null) {
				equipment.setBoots((CannonsUtil.parseItemstack(
					entityHolder.getData().get(EntityDataType.BOOTS_ARMOR_ITEM), equipment.getBoots())));
				equipment.setChestplate((CannonsUtil.parseItemstack(
					entityHolder.getData().get(EntityDataType.CHESTPLATE_ARMOR_ITEM),
					equipment.getChestplate())));
				equipment.setHelmet((CannonsUtil.parseItemstack(
					entityHolder.getData().get(EntityDataType.HELMET_ARMOR_ITEM), equipment.getHelmet())));
				equipment.setLeggings((CannonsUtil.parseItemstack(
					entityHolder.getData().get(EntityDataType.LEGGINGS_ARMOR_ITEM),
					equipment.getLeggings())));
				equipment.setItemInMainHand(
					(CannonsUtil.parseItemstack(entityHolder.getData().get(EntityDataType.MAIN_HAND_ITEM),
						equipment.getItemInMainHand())));
				equipment.setItemInOffHand(
					(CannonsUtil.parseItemstack(entityHolder.getData().get(EntityDataType.OFF_HAND_ITEM),
						equipment.getItemInOffHand())));
				}
			} catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
	    }
	    // ThrownPotion
	    if (entity instanceof SplashPotion) {
			SplashPotion pentity = (SplashPotion) entity;
			try {
				ItemStack potion = new ItemStack(Material.SPLASH_POTION);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				meta.setBasePotionData(CannonsUtil.parsePotionData(
					entityHolder.getData().get(EntityDataType.POTION_EFFECT), meta.getBasePotionData()));
				potion.setItemMeta(meta);
				pentity.setItem(potion);
			} catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
	    }
	    // LingeringPotion
	    if (entity instanceof LingeringPotion) {
			LingeringPotion pentity = (LingeringPotion) entity;
			try {
				ItemStack potion = new ItemStack(Material.LINGERING_POTION);
				PotionMeta meta = (PotionMeta) potion.getItemMeta();
				meta.setBasePotionData(CannonsUtil.parsePotionData(
					entityHolder.getData().get(EntityDataType.POTION_EFFECT), meta.getBasePotionData()));
				potion.setItemMeta(meta);
				pentity.setItem(potion);
			} catch (Exception e) {
				this.plugin.logSevere("error while converting entity data for "
					+ cannonball.getProjectile().getProjectileId() + " occurred: " + e);
			}
	    }
	}

	// //get distance form the center + 1 to avoid division by zero
	// double dist = impactLoc.distance(loc) + 1;
	// //calculate veloctiy away from the impact (speed in y makes problems and
	// entity sinks in ground)
	// Vector vect =
	// loc.clone().subtract(impactLoc).toVector().normalize().multiply(entityVelocity/dist).multiply(new
	// Vector(1.0,0.0,1.0));
	// String placestr = loc.getX() + " " + loc.getY() + " " + loc.getZ();
	// String data = entityHolder.getData().replace("IMPACT_VELOCITY",
	// "["+vect.getX()+"," +vect.getY()+","+vect.getZ()+"]");
	// plugin.logDebug("/summon " + entityHolder.getType() + " " + placestr + " " +
	// data);
	// try {
	// Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "summon " +
	// entityHolder.getType() + " " + placestr + " " + data);
	// }
	// catch (Exception e) {
	// plugin.logDebug("Dispatch error: " + e);
	// }

    }

    /**
     * moves down until it finds the ground
     * 
     * @param loc
     *            start location
     * @return new location on the ground or in the air if nothing was found
     */
    private Location moveToGround(Location loc, double max_iterations) {
	Location tloc = loc.clone();
	tloc = tloc.subtract(0, 1, 0);
	for (int i = 0; i < max_iterations; i++) {
	    tloc = tloc.subtract(0, 1, 0);
	    if (tloc.getBlock().getType() != Material.AIR)
		return tloc.add(0, 1, 0);
	}
	return tloc;
    }

    /**
     * performs the block spawning for the given projectile
     * 
     * @param cannonball
     *            involved projectile
     */
    private void spreadEntities(FlyingProjectile cannonball) {

		if (!cannonball.getProjectile().isSpawnEnabled())
			return;

		Projectile projectile = cannonball.getProjectile();
		Location impactLoc = cannonball.getImpactLocation();

		Random r = new Random();
		Location placeLoc;

		double spread = projectile.getSpawnEntityRadius();

		for (SpawnEntityHolder spawn : projectile.getSpawnEntities()) {
			// add some randomness to the amount of spawned blocks
			int maxPlacement = CannonsUtil.getRandomInt(spawn.getMinAmount(), spawn.getMaxAmount());

			// iterate blocks around to get a good spot
			int placedEntities = 0;
			int iterations1 = 0;
			do {
				iterations1++;

				// get new position
				placeLoc = CannonsUtil.randomPointInSphere(impactLoc, spread);

				// check a entity can spawn on this block if it is a living entity
				if (this.canPlaceEntity(placeLoc.getBlock()) || !spawn.getType().isAlive()) {
					placedEntities++;
					// place the entity
					this.spawnEntity(cannonball, placeLoc, projectile.getSpawnVelocity(), spawn);
				}
			} while (iterations1 < maxPlacement * 10 && placedEntities < maxPlacement);

			if (placedEntities < maxPlacement) {
			this.plugin.logDebug("Could only place " + placedEntities + " entities instead of " + maxPlacement);
			}
		}
    }

    /**
     * spawns a falling block with the id and data that is slinged away from the
     * impact
     * 
     * @param impactLoc
     *            location of the impact
     * @param placeLoc
     *            spawn location of the falling block
     * @param entityVelocity
     *            velocity of the falling block
     * @param item
     *            type of the falling block
     */
    private void spawnFallingBlock(Location impactLoc, Location placeLoc, double entityVelocity, BlockData item) {
		//FallingBlock entity = impactLoc.getWorld().spawnFallingBlock(placeLoc, item.getType(), (byte) 0);
		FallingBlock entity = impactLoc.getWorld().spawnFallingBlock(placeLoc, item);

		// give the blocks some velocity
		if (entity != null) {
			// get distance form the center + 1, to avoid division by zero
			double dist = impactLoc.distance(placeLoc) + 1;
			// calculate veloctiy away from the impact
			Vector vect = placeLoc.clone().subtract(impactLoc).toVector().normalize().multiply(entityVelocity / dist);
			// set the entity velocity
			entity.setVelocity(vect);
			// set some other properties
			entity.setDropItem(false);
			this.plugin.logDebug("Spawned block: " + item.toString() + " at impact");
		} else {
			this.plugin.logSevere(
				"Item id:" + item.toString() + " can't be spawned as falling block.");
		}
    }

    /**
     * performs the block spawning for the given projectile
     * 
     * @param cannonball
     *            the fired cannonball
     */
    private void spreadBlocks(FlyingProjectile cannonball) {
	if (!cannonball.getProjectile().isSpawnEnabled())
	    return;

	Projectile projectile = cannonball.getProjectile();
	Location impactLoc = cannonball.getImpactLocation();

	Random r = new Random();
	Location placeLoc;

	double spread = projectile.getSpawnBlockRadius();

	for (SpawnMaterialHolder spawn : projectile.getSpawnBlocks()) {

	    // add some randomness to the amount of spawned blocks
	    int maxPlacement = CannonsUtil.getRandomInt(spawn.getMinAmount(), spawn.getMaxAmount());

	    // iterate blocks around to get a good spot
	    int placedBlocks = 0;
	    int iterations1 = 0;
	    do {
		iterations1++;

		// get location to place block
		placeLoc = CannonsUtil.randomPointInSphere(impactLoc, spread);

		// check a entity can spawn on this block
		if (this.canPlaceBlock(placeLoc.getBlock())) {
		    placedBlocks++;
		    // place the block
		    this.spawnFallingBlock(impactLoc, placeLoc, projectile.getSpawnVelocity(), spawn.getMaterial());
		}
	    } while (iterations1 < maxPlacement * 5 && placedBlocks < maxPlacement);

	    if (placedBlocks < maxPlacement) {
		this.plugin.logDebug("Could only place " + placedBlocks + " blocks instead of " + maxPlacement);
	    }
	}
    }

    /**
     * returns true if an falling block can be place on this block
     * 
     * @param block
     *            location to spawn the entity
     * @return true if the block is empty or liquid
     */
    private boolean canPlaceBlock(Block block) {
	return block.isEmpty() || block.isLiquid();
    }

    /**
     * returns true if an entity can be place on this block
     * 
     * @param block
     *            location to spawn the entity
     * @return true if there can be an entity spawned
     */
    private boolean canPlaceEntity(Block block) {
	// this block an the block underneath should be empty
	return this.canPlaceBlock(block) && this.canPlaceBlock(block.getRelative(BlockFace.DOWN));
    }

    /**
     * counts the number of blocks which are between the given locations
     * 
     * @param impact
     *            starting point
     * @param target
     *            end point
     * @return number of non AIR block between the locations
     */
    private int checkLineOfSight(Location impact, Location target) {
	int blockingBlocks = 0;

	// vector pointing from impact to target
	Vector vect = target.toVector().clone().subtract(impact.toVector());
	int length = (int) Math.ceil(vect.length());
	vect.normalize();

	Location impactClone = impact.clone();
	for (int i = 2; i <= length; i++) {
	    // check if line of sight is blocked
	    if (impactClone.add(vect).getBlock().getType() != Material.AIR) {
		blockingBlocks++;
	    }
	}
	return blockingBlocks;
    }

    /**
     * Gives a player next to an explosion an entity effect
     * 
     * @param impactLoc
     * @param next
     * @param cannonball
     */
    private void applyPotionEffect(Location impactLoc, Entity next, FlyingProjectile cannonball) {
	Projectile projectile = cannonball.getProjectile();

	if (next instanceof LivingEntity) {
	    LivingEntity living = (LivingEntity) next;

	    double dist = impactLoc.distance(living.getEyeLocation());
	    // if the entity is too far away, return
	    if (dist > projectile.getPotionRange())
		return;

	    // duration of the potion effect
	    double duration = projectile.getPotionDuration() * 20;

	    // check line of sight and reduce damage if the way is blocked
	    int blockingBlocks = this.checkLineOfSight(impactLoc, living.getEyeLocation());
	    duration = duration / (blockingBlocks + 1);

	    // randomizer
	    Random r = new Random();
	    float rand = r.nextFloat();
	    duration *= rand / 2 + 0.5;

	    // apply potion effect if the duration is not small then 1 tick
	    if (duration >= 1) {
		int intDuration = (int) Math.floor(duration);

		for (PotionEffectType potionEffect : projectile.getPotionsEffectList()) {
		    // apply to entity
		    potionEffect.createEffect(intDuration, projectile.getPotionAmplifier()).apply(living);
		}
	    }
	}
    }

    /**
     * Returns the amount of damage the livingEntity receives due to explosion of
     * the projectile
     * 
     * @param impactLoc
     * @param next
     * @param cannonball
     * @return - damage done to the entity
     */
    private double getPlayerDamage(Location impactLoc, Entity next, FlyingProjectile cannonball) {
	Projectile projectile = cannonball.getProjectile();

	if (next instanceof LivingEntity) {
	    LivingEntity living = (LivingEntity) next;

	    double dist = impactLoc.distance((living).getEyeLocation());
	    // if the entity is too far away, return
	    if (dist > projectile.getPlayerDamageRange())
		return 0.0;

	    // given damage is in half hearts
	    double damage = projectile.getPlayerDamage();

	    // check line of sight and reduce damage if the way is blocked
	    int blockingBlocks = this.checkLineOfSight(impactLoc, living.getEyeLocation());
	    damage = damage / (blockingBlocks + 1);

	    // randomizer
	    Random r = new Random();
	    float rand = r.nextFloat();
	    damage *= (rand + 0.5);

	    // calculate the armor reduction
	    double reduction = 1.0;
	    if (living instanceof HumanEntity) {
		HumanEntity human = (HumanEntity) living;
		double armorPiercing = Math.max(projectile.getPenetration(), 0);
		reduction *= (1 - CannonsUtil.getArmorDamageReduced(human) / (armorPiercing + 1))
			* (1 - CannonsUtil.getBlastProtection(human));
	    }

	    this.plugin.logDebug("PlayerDamage " + living.getType() + ":" + String.format("%.2f", damage) + ",reduct:"
		    + String.format("%.2f", reduction) + ",dist:" + String.format("%.2f", dist));

	    damage = damage * reduction;

	    return damage;
	}
	// if the entity is not alive
	return 0.0;
    }

    /**
     * Returns the amount of damage dealt to an entity by the projectile
     * 
     * @param cannonball
     * @param target
     * @return return the amount of damage done to the living entity
     */
    private double getDirectHitDamage(FlyingProjectile cannonball, Entity target) {
	Projectile projectile = cannonball.getProjectile();

	// if (cannonball.getProjectileEntity()==null)
	// return 0.0;

	if (target instanceof LivingEntity) {
	    LivingEntity living = (LivingEntity) target;

	    // given damage is in half hearts
	    double damage = projectile.getDirectHitDamage();

	    // randomizer
	    Random r = new Random();
	    float rand = r.nextFloat();
	    damage *= (rand + 0.5);

	    // calculate the armor reduction
	    double reduction = 1.0;
	    if (living instanceof HumanEntity) {
		HumanEntity human = (HumanEntity) living;
		double armorPiercing = Math.max(projectile.getPenetration(), 0);
		reduction *= (1 - CannonsUtil.getArmorDamageReduced(human) / (armorPiercing + 1))
			* (1 - CannonsUtil.getProjectileProtection(human) / (armorPiercing + 1));
	    }

	    this.plugin.logDebug("DirectHitDamage " + living.getType() + ": " + String.format("%.2f", damage)
		    + ", reduction: " + String.format("%.2f", reduction));
	    return damage * reduction;
	}
	// if the entity is not living
	return 0.0;
    }

    /**
     * adds the affected living entities to the list
     * 
     * @param entity
     *            only alive and living entities will be added
     */
    private void addAffectedEntity(Entity entity) {
	if (!entity.isDead() && entity instanceof LivingEntity) {
	    this.affectedEntities.add(entity);
	}
    }

    /**
     * the given entity was hit by a cannonball
     * 
     * @param cannonball
     *            cannonball which hit the entity
     * @param entity
     *            entity hit
     */
    public void directHit(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity, Entity entity) {
	// add damage to map - it will be applied later to the player
	double directHit = this.getDirectHitDamage(cannonball, entity);
	this.damageMap.put(entity, directHit);
	this.addAffectedEntity(entity);
	// explode the cannonball
	this.detonate(cannonball, projectile_entity);

    }

    /**
     * detonated the cannonball
     * 
     * @param cannonball
     *            cannonball which will explode
     */
    public void detonate(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity) {
	this.plugin.logDebug("detonate cannonball");

	Projectile projectile = cannonball.getProjectile().clone();
	Player player = Bukkit.getPlayer(cannonball.getShooterUID());

	boolean canceled = false;
	// breaks blocks from the impact of the projectile to the location of the
	// explosion
	Location impactLoc = this.blockBreaker(cannonball, projectile_entity);
	// impactLoc = projectile_entity.getLocation();
	cannonball.setImpactLocation(impactLoc);
	World world = impactLoc.getWorld();


	// find block which caused the shell impact
	Location impactBlock = CannonsUtil.findFirstBlock(impactLoc, cannonball.getVelocity());
	if (impactBlock != null) {
	    cannonball.setImpactBlock(impactBlock);
	    //this.plugin.logDebug("todo: impact block: " + impactBlock.getBlock());
	}

	// teleport snowball to impact
	projectile_entity.teleport(impactLoc);

	float explosion_power = projectile.getExplosionPower();
	if (projectile.isExplosionPowerDependsOnVelocity()) {
	    double vel = projectile_entity.getVelocity().length();
	    double maxVel = projectile.getVelocity();
	    double maxEnergy = Math.pow(maxVel, 2);
	    double energy = Math.pow(vel, 2);
	    explosion_power *= energy / maxEnergy;
	}

	// reset explosion power if it is underwater and not allowed
	this.plugin.logDebug("Explosion is underwater: " + cannonball.wasInWater());
	if (!projectile.isUnderwaterDamage() && cannonball.wasInWater()) {
	    this.plugin.logDebug("Underwater explosion not allowed. Event cancelled");
	    return;
	}

	// deflect cannonball
	if (this.deflectProjectile(cannonball)) {
	    // cannonball was deflected - no explosion
	    this.plugin.logDebug("Cannonball was deflected");
	    return;
	}

	boolean incendiary = projectile.hasProperty(ProjectileProperties.INCENDIARY);
	boolean blockDamage = projectile.getExplosionDamage();

	this.plugin.logDebug("Projectile impact event: " + impactLoc.getBlockX() + ", " + impactLoc.getBlockY() + ", "
				+ impactLoc.getBlockZ() + " direction: " + impactLoc.getYaw() + " Pitch: " + impactLoc.getPitch());

	// fire impact event
	ProjectileImpactEvent impactEvent = new ProjectileImpactEvent(projectile, impactLoc,
		cannonball.getShooterUID());
	Bukkit.getServer().getPluginManager().callEvent(impactEvent);
	canceled = impactEvent.isCancelled();

	this.currentCannonball = cannonball;
	// if canceled then exit
	if (impactEvent.isCancelled()) {
	    // event cancelled, make some effects - even if the area is protected by a
	    // plugin
	    world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), 0, false, false, cannonball.getProjectileEntity());
	    this.sendExplosionToPlayers(projectile, impactLoc, projectile.getSoundImpactProtected());
	} else {
	    // if the explosion power is negative there will be only a arrow impact sound
	    if (explosion_power >= 0) {
			// get affected entities
			for (Entity cEntity : projectile_entity.getNearbyEntities(explosion_power, explosion_power,
				explosion_power)) {
				this.addAffectedEntity(cEntity);
		}
		// make the explosion
		canceled = !world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), explosion_power,
			incendiary, blockDamage, cannonball.getProjectileEntity());
	    }
	}

	// send a message about the impact (only if the projectile has enabled this
	// feature and a player fired this projectile)
	if (projectile.isImpactMessage() && cannonball.getProjectileCause() == ProjectileCause.PlayerFired) {
	    this.plugin.sendImpactMessage(player, impactLoc, canceled);
	}

	// do nothing if the projectile impact was canceled or it is underwater with
	// deactivated
	if (!canceled) {
	    // if the player is too far away, there will be a imitated explosion made of
	    // fake blocks
	    this.sendExplosionToPlayers(projectile, impactLoc, projectile.getSoundImpact());
	    // place blocks around the impact like webs, lava, water
	    this.spreadBlocks(cannonball);
	    // spawns additional projectiles after the explosion
	    this.spawnProjectiles(cannonball);
	    // spawn fireworks
	    this.spawnFireworks(cannonball, projectile_entity);
	    // do potion effects
	    this.damageEntity(cannonball, projectile_entity);
	    // teleport the player to the impact or to the start point
	    this.teleportPlayer(cannonball, player);
	    // make some additional explosion around the impact
	    this.clusterExplosions(cannonball);
	    // fire event for all kill entities
	    this.fireEntityDeathEvent(cannonball);
	    // place blocks around the impact like webs, lava, water
	    this.spreadEntities(cannonball);
	}
    }

    private void fireEntityDeathEvent(FlyingProjectile cannonball) {
	LinkedList<LivingEntity> lEntities = new LinkedList<>();
	// check which entities are affected by the event
	for (Entity entity : this.affectedEntities) {
	    if (entity != null) {
			// entity has died
			if (entity.isDead() && entity instanceof LivingEntity) {
				lEntities.add((LivingEntity) entity);
				if (entity instanceof Player) {
					this.killedPlayers.put(entity.getUniqueId(), new DeathCause(cannonball.getProjectile(),
					cannonball.getCannonUID(), cannonball.getShooterUID()));
				}
			}
	    }
	}
	this.affectedEntities.clear();

	// fire entityDeathEvent
	for (LivingEntity entity : lEntities) {
	    CannonsEntityDeathEvent entityDeathEvent = new CannonsEntityDeathEvent(entity, cannonball.getProjectile(),
		    cannonball.getCannonUID(), cannonball.getShooterUID());
	    Bukkit.getServer().getPluginManager().callEvent(entityDeathEvent);
	}
    }

	/**
	 * was this player affected by cannons, it might have been the death cause
	 * @param player player to check
	 * @return true if the player was affected by cannons
	 */
	public boolean wasAffectedByCannons(Player player){
		return this.affectedEntities.contains(player);
	}


    /**
     * makes a sphere with additional explosions around the impact
     * 
     * @param cannonball
     *            exploding cannonball
     */
    private void clusterExplosions(FlyingProjectile cannonball) {
	final Projectile projectile = cannonball.getProjectile();
	if (projectile.isClusterExplosionsEnabled()) {
	    for (int i = 0; i < projectile.getClusterExplosionsAmount(); i++) {
		double delay = projectile.getClusterExplosionsMinDelay() + Math.random()
			* (projectile.getClusterExplosionsMaxDelay() - projectile.getClusterExplosionsMinDelay());
		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin,
			new DelayedTask(cannonball) {
			    @Override
			    public void run(Object object) {
				FlyingProjectile cannonball = (FlyingProjectile) object;
				Projectile proj = cannonball.getProjectile();

				Location expLoc = CannonsUtil.randomPointInSphere(cannonball.getImpactLocation(),
					proj.getClusterExplosionsRadius());
				// only do if explosion in blocks are allowed
				if (proj.isClusterExplosionsInBlocks() || expLoc.getBlock().isEmpty()
					|| (expLoc.getBlock().isLiquid() && proj.isUnderwaterDamage())) {
				    expLoc.getWorld().createExplosion(expLoc, (float) proj.getClusterExplosionsPower(), projectile.hasProperty(ProjectileProperties.INCENDIARY), true, cannonball.getProjectileEntity());
				    CreateExplosion.this.sendExplosionToPlayers(null, expLoc,
					    projectile.getSoundImpact());
				}
			    }
			}, (long) (delay * 20.0));
	    }
	}
    }

    /**
     * teleports the player to the impact, depending on the given projectile
     * properties
     * 
     * @param cannonball
     *            the flying projectile
     * @param player
     *            the one to teleport
     */
    private void teleportPlayer(FlyingProjectile cannonball, Player player) {
	if (player == null || cannonball == null)
	    return;

	Projectile projectile = cannonball.getProjectile();
	Location impactLoc = cannonball.getImpactLocation();

	Location teleLoc = null;
	// teleport to impact and reset speed - make a soft landing
	if (projectile.hasProperty(ProjectileProperties.TELEPORT)
		|| projectile.hasProperty(ProjectileProperties.HUMAN_CANNONBALL)) {
	    teleLoc = impactLoc.clone();
	}
	// teleport the player back to the location before firing
	else if (projectile.hasProperty(ProjectileProperties.OBSERVER)) {
	    teleLoc = cannonball.getPlayerlocation();
	}
	// teleport to this location
	if (teleLoc != null) {
	    teleLoc.setYaw(player.getLocation().getYaw());
	    teleLoc.setPitch(player.getLocation().getPitch());
	    player.teleport(teleLoc);
	    player.setVelocity(new Vector(0, 0, 0));
	    cannonball.setTeleported(true);
	}
    }

    /**
     * does additional damage effects to player (directHit, explosion and potion
     * effects)
     * 
     * @param cannonball
     *            the flying projectile
     */
    private void damageEntity(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity) {
	Projectile projectile = cannonball.getProjectile();
	Location impactLoc = cannonball.getImpactLocation();

	// explosion effect
	double effectRange = projectile.getPlayerDamageRange();
	List<Entity> entities = projectile_entity.getNearbyEntities(effectRange, effectRange, effectRange);

	// search all entities to damage
	Iterator<Entity> it = entities.iterator();
	while (it.hasNext()) {
	    Entity next = it.next();

	    if (next instanceof LivingEntity) {
		// get previous damage
		double damage = 0.0;
		if (this.damageMap.containsKey(next)) {
		    damage = this.damageMap.get(next);
		}

		// add explosion damage
		damage += this.getPlayerDamage(impactLoc, next, cannonball);
		this.damageMap.put(next, damage);
	    }
	}

	// apply sum of all damages
	for (Map.Entry<Entity, Double> entry : this.damageMap.entrySet()) {
	    double damage = entry.getValue();
	    Entity entity = entry.getKey();

	    if (damage >= 1 && entity instanceof LivingEntity) {
		LivingEntity living = (LivingEntity) entity;
		this.plugin.logDebug(
			"apply damage to entity " + living.getType() + " by " + String.format("%.2f", damage));
		double health = living.getHealth();
		living.setNoDamageTicks(0);// It will do damage by each projectile without noDamageTime
		living.damage(damage);

		// if player wears armor reduce damage if the player has take damage
		if (living instanceof HumanEntity && health > living.getHealth()) {
		    CannonsUtil.reduceArmorDurability((HumanEntity) living);
		}
	    }
	}

	// potion effects
	effectRange = projectile.getPotionRange();
	entities = projectile_entity.getNearbyEntities(effectRange, effectRange, effectRange);

	// apply potion effect
	it = entities.iterator();
	while (it.hasNext()) {
	    Entity next = it.next();
	    this.applyPotionEffect(impactLoc, next, cannonball);
	}

	// remove all entries in damageMap
	this.damageMap.clear();
    }

    /**
     * deflect cannonball on unbreakable surface
     * 
     * @param cannonball
     *            the flying projectile
     */
    private boolean deflectProjectile(FlyingProjectile cannonball) {
	// todo deflect projectiles
	// if (!cannonball.getProjectile().isSpawnEnabled())
	// return;

	Random r = new Random();
	Location impactLoc = cannonball.getImpactLocation();
	Location impactBlock = cannonball.getImpactBlock();
	if (impactBlock == null)
	    return false;

	Vector vectdeflect = cannonball.getVelocity().clone().multiply(.5);
	// vectdeflect.add(new
	// Vector(vectdeflect.length()*r.nextGaussian()*0.2,vectdeflect.length()*r.nextGaussian()*0.2,vectdeflect.length()*r.nextGaussian()*0.2));

	this.plugin.logDebug("Deflection calculating");

	// ignore too slow cannonballs
	if (vectdeflect.length() < 0.3)
	    return false;

	// no deflection if the projectile pierces the block
	if (cannonball.getProjectile().hasProperty(ProjectileProperties.SUPERBREAKER))
	    return false;

	if (impactBlock.getBlock().getType() != Material.OBSIDIAN
		&& impactBlock.getBlock().getType() != Material.BEDROCK)
	    return false;

	this.plugin.logDebug("Deflection valid");
	// spawn a new deflected cannnonball
	this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DelayedTask(cannonball) {
	    @Override
	    public void run(Object object) {
		FlyingProjectile cannonball = (FlyingProjectile) object;

		Projectile projectile = cannonball.getProjectile();

		Random r = new Random();

		Location impactBlock = cannonball.getImpactBlock();
		Vector vnormal = CannonsUtil.detectImpactSurfaceNormal(cannonball.getImpactLocation().toVector(),
			cannonball.getVelocity().clone());

		Vector vectdeflect = cannonball.getVelocity().multiply(.5);
		Location impactLoc = cannonball.getImpactLocation()
			.subtract(cannonball.getVelocity().normalize().multiply(0.3));
		// vectdeflect.add(new
		// Vector(vectdeflect.length()*r.nextGaussian()*0.2,vectdeflect.length()*r.nextGaussian()*0.2,vectdeflect.length()*r.nextGaussian()*0.2));
		vectdeflect.setY(-vectdeflect.getY());
		CreateExplosion.this.plugin.logDebug("Deflect projectile: " + vectdeflect);

		CreateExplosion.this.plugin.getProjectileManager().spawnProjectile(projectile,
			cannonball.getShooterUID(), cannonball.getSource(), cannonball.getPlayerlocation(),
			impactLoc.clone(), vectdeflect, cannonball.getCannonUID(), ProjectileCause.DeflectedProjectile);
	    }
	}, 1L);

	return true;
    }

    /**
     * spawns Projectiles given in the spawnProjectile property
     * 
     * @param cannonball
     *            the flying projectile
     */
    private void spawnProjectiles(FlyingProjectile cannonball) {
	if (!cannonball.getProjectile().isSpawnEnabled())
	    return;

	this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DelayedTask(cannonball) {
	    @Override
	    public void run(Object object) {
		FlyingProjectile cannonball = (FlyingProjectile) object;

		Projectile projectile = cannonball.getProjectile();
		Location impactLoc = cannonball.getImpactLocation();

		Random r = new Random();

		for (String strProj : projectile.getSpawnProjectiles()) {
		    Projectile newProjectiles = CreateExplosion.this.plugin.getProjectileStorage().getByName(strProj);
		    if (newProjectiles == null) {
			CreateExplosion.this.plugin.logSevere(
				"Can't use spawnProjectile " + strProj + " because Projectile does not exist");
			continue;
		    }

		    for (int i = 0; i < newProjectiles.getNumberOfBullets(); i++) {
			Vector vect = new Vector(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5);
			vect = vect.normalize().multiply(newProjectiles.getVelocity());

			// don't spawn the projectile in the center
			Location spawnLoc = impactLoc.clone().add(vect.clone().normalize().multiply(3.0));

			CreateExplosion.this.plugin.getProjectileManager().spawnProjectile(newProjectiles,
				cannonball.getShooterUID(), cannonball.getSource(), null, spawnLoc, vect,
				cannonball.getCannonUID(), ProjectileCause.SpawnedProjectile);
		    }
		}
	    }
	}, 1L);
    }

    /**
     * spawns fireworks after the explosion
     * 
     * @param cannonball
     *            the flying projectile
     */
    private void spawnFireworks(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity) {
	World world = cannonball.getWorld();
	Projectile projectile = cannonball.getProjectile();

	// a fireworks needs some colors
	if (!projectile.isFireworksEnabled() || projectile.getFireworksColors().size() == 0)
	    return;

	// building the fireworks effect
	FireworkEffect.Builder fwb = FireworkEffect.builder().flicker(projectile.isFireworksFlicker())
		.trail(projectile.isFireworksTrail()).with(projectile.getFireworksType());
	// setting colors
	for (Integer color : projectile.getFireworksColors()) {
	    fwb.withColor(Color.fromRGB(color));
	}
	for (Integer color : projectile.getFireworksFadeColors()) {
	    fwb.withFade(Color.fromRGB(color));
	}

	// apply to rocket
	final Firework fw = (Firework) world.spawnEntity(projectile_entity.getLocation(), EntityType.FIREWORK);
	FireworkMeta meta = fw.getFireworkMeta();

	meta.addEffect(fwb.build());
	meta.setPower(0);
	fw.setFireworkMeta(meta);

	// detonate firework after 1tick. This seems to works much better than
	// detonating instantaneously
	this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new DelayedTask(fw) {
	    @Override
	    public void run(Object object) {
		Firework fw = (Firework) object;
		fw.detonate();
	    }
	}, 1L);
    }

    /**
     * Broadcasts an explosion with higher volume to the player. Also adds an impact
     * indicator
     * 
     * @param projectile
     *            Which type of projectile exploded. Can be null to suppress the
     *            impact indicator
     * @param loc
     *            Location of the explosion
     * @param sound
     *            Which sound is broadcasted
     */
    public void sendExplosionToPlayers(Projectile projectile, Location loc, SoundHolder sound) {
	CannonsUtil.imitateSound(loc, sound, this.config.getImitatedSoundMaximumDistance(),
		this.config.getImitatedSoundMaximumVolume());

	if (!this.config.isImitatedExplosionEnabled())
	    return;

	double minDist = this.config.getImitatedBlockMinimumDistance();
	double maxDist = this.config.getImitatedBlockMaximumDistance();
	int r = this.config.getImitatedExplosionSphereSize() / 2;
	BlockData mat = this.config.getImitatedExplosionMaterial();
	double delay = this.config.getImitatedExplosionTime();

	for (Player p : loc.getWorld().getPlayers()) {
	    Location pl = p.getLocation();
	    double distance = pl.distance(loc);

	    if (projectile != null && projectile.isImpactIndicator() && distance >= minDist && distance <= maxDist) {
		this.plugin.getFakeBlockHandler().imitatedSphere(p, loc, r, mat, FakeBlockType.EXPLOSION, delay);
	    }
	}
    }

    /**
     * returns true if the player was killed by a cannon explosion
     * 
     * @param playerUID
     *            killed player
     * @return true if player was killed by a cannonball
     */
    public boolean isKilledByCannons(UUID playerUID) {
		return this.killedPlayers.containsKey(playerUID);
    }

    /**
     * returns death cause when the player was killed by a cannon explosion
     * 
     * @param playerUID
     *            killed player
     * @return death cause
     */
    public DeathCause getDeathCause(UUID playerUID) {
	return this.killedPlayers.get(playerUID);
    }

    /**
     * removes player form the list of cannons killed players
     * 
     * @param playerUID
     *            killed player
     */
    public void removeKilledPlayer(UUID playerUID) {
	this.killedPlayers.remove(playerUID);
    }

	public FlyingProjectile getCurrentCannonball() {
		return currentCannonball;
	}
}
