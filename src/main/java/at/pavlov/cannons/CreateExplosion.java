package at.pavlov.cannons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;


import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;
import at.pavlov.cannons.utils.CannonsUtil;
import at.pavlov.cannons.utils.DelayedTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;

public class CreateExplosion {

    private final Cannons plugin;

    private LinkedList<UUID> transmittedEntities = new LinkedList<UUID>();

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
            for (MaterialHolder unbreakableBlock : plugin.getMyConfig().getUnbreakableBlocks())
            {
                if (unbreakableBlock.equalsFuzzy(destroyedBlock))
                {
                    //this block is protected and impenetrable
                    return false;
                }
            }

            //test if it needs superbreaker
            for (MaterialHolder superbreakerBlock : plugin.getMyConfig().getSuperbreakerBlocks())
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
        org.bukkit.entity.Projectile projectile_entity = cannonball.getProjectileEntity();

        //has this projectile the super breaker property and makes block damage
        Boolean superbreaker = projectile.hasProperty(ProjectileProperties.SUPERBREAKER);
        Boolean doesBlockDamage = projectile.getPenetrationDamage();

        //list of destroy blocks
        LinkedList<Block> blocklist = new LinkedList<Block>();

        Vector vel = projectile_entity.getVelocity();
        Location snowballLoc = projectile_entity.getLocation();
        World world = projectile_entity.getWorld();
        int penetration = (int) ((cannonball.getProjectile().getPenetration()) * vel.length() / projectile.getVelocity());
        Location impactLoc = snowballLoc.clone();

        plugin.logDebug("Projectile impact at: " + impactLoc.getBlockX() + ", "+ impactLoc.getBlockY() + ", " + impactLoc.getBlockZ());
        BlockIterator iter = new BlockIterator(world, snowballLoc.toVector(), vel.normalize(), 0, (int) (vel.length()*2));

        while (iter.hasNext())
        {
            Block next = iter.next();
            //if there is no block, go further until we hit the surface
            if (next.isEmpty())
            {
                impactLoc = next.getLocation();
            }
            else
            {
                plugin.logDebug("Found surface at: " + impactLoc.getBlockX() + ", " + impactLoc.getBlockY() + ", " + impactLoc.getBlockZ());
                break;
            }
        }

        // the cannonball will only break blocks if it has penetration.
        if (cannonball.getProjectile().getPenetration() > 0)
        {
            iter = new BlockIterator(world, snowballLoc.toVector(), vel.normalize(), 0, penetration + 1);

            int i=0;
            while (iter.hasNext() && i <= penetration + 1)
            {
                i++;
                Block next = iter.next();
                //Break block on ray
                if (i <= penetration)
                {
                    // if block can be destroyed the the iterator will check the next block. Else the projectile will explode
                    if (!breakBlock(next, blocklist, superbreaker, doesBlockDamage))
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
            //fire custom piercing event to notify other plugins (blocks can be removed)
            ProjectilePiercingEvent piercingEvent = new ProjectilePiercingEvent(projectile, impactLoc, blocklist);
            plugin.getServer().getPluginManager().callEvent(piercingEvent);

            //create bukkit event
            EntityExplodeEvent event = new EntityExplodeEvent(null, impactLoc, piercingEvent.getBlockList(), 1.0f);
            //handle with bukkit
            plugin.getServer().getPluginManager().callEvent(event);

            plugin.logDebug("explode event canceled: " + event.isCancelled());
            //if not canceled break all given blocks
            if(!event.isCancelled())
            {
                plugin.logDebug("breaking block for penetration event");
                // break water, lava, obsidian if cannon projectile
                for (int i = 0; i < event.blockList().size(); i++)
                {
                    Block pBlock =  event.blockList().get(i);
                    // break the block, no matter what it is
                    BreakBreakNaturally(pBlock,event.getYield());
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
                plugin.logDebug("set TNT fuse ticks to: " + (int)(tntFuse*20.0));
                tnt.setFuseTicks((int)(tntFuse*20.0));
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

        if (projectile.doesBlockPlace())
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
     * Returns the amount of damage the livingEntity receives due to explosion of the projectile
     * @param impactLoc
     * @param next
     * @param cannonball
     * @return - damage done to the entity
     */
    private double getPlayerDamage(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
        Projectile projectile = cannonball.getProjectile();

        if (next instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) next;

            double dist = impactLoc.distance((living).getEyeLocation());
            plugin.logDebug("Distance of " + living.getType() + " to impact: " + String.format("%.2f", dist));
            //if the entity is too far away, return
            if (dist > projectile.getPlayerDamageRange()) return 0.0;

            //given damage is in half hearts
            double damage = projectile.getPlayerDamage();

            //check line of sight and reduce damage if the way is blocked
            int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
            damage = damage / (blockingBlocks + 1);

            //randomizer
            Random r = new Random();
            float rand = r.nextFloat();
            damage *= (rand + 0.5);

            //calculate the armor reduction
            double reduction = 1.0;
            if (living instanceof Player)
            {
                Player player = (Player) living;
                double armorPiercing = Math.max(projectile.getPenetration(),0);
                reduction *= (1-CannonsUtil.getArmorDamageReduced(player)/(armorPiercing+1)) * (1-CannonsUtil.getBlastProtection(player));
            }

            plugin.logDebug("PlayerDamage " + living.getType() + ": " + String.format("%.2f", damage) + ", reduction: " + String.format("%.2f", reduction));

            damage = damage * reduction;

            return damage;
        }
        //if the entity is not alive
        return 0.0;
    }


    /**
     * Returns the amount of damage dealt to an entity by the projectile
     * @param impactLoc
     * @param next
     * @param cannonball
     * @return return the amount of damage done to the living entity
     */
    private double getDirectHitDamage(Location impactLoc, Entity next, FlyingProjectile cannonball)
    {
        Projectile projectile = cannonball.getProjectile();


        if (next instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) next;

            double dist = impactLoc.distance((living).getEyeLocation());
            //if the entity is too far away, return
            if (dist > 3) return 0.0;

            //given damage is in half hearts
            double damage = projectile.getDirectHitDamage();

            //check line of sight and reduce damage if the way is blocked
            int blockingBlocks = checkLineOfSight(impactLoc, living.getEyeLocation());
            damage = damage / (blockingBlocks + 1);

            //randomizer
            Random r = new Random();
            float rand = r.nextFloat();
            damage *= (rand + 0.5);

            //calculate the armor reduction
            double reduction = 1.0;
            if (living instanceof Player)
            {
                Player player = (Player) living;
                double armorPiercing = Math.max(projectile.getPenetration(),0);
                reduction *= (1-CannonsUtil.getArmorDamageReduced(player)/(armorPiercing+1)) * (1-CannonsUtil.getProjectileProtection(player)/(armorPiercing+1));
            }

            plugin.logDebug("DirectHitDamage " + living.getType() + ": " + String.format("%.2f", damage) + ", reduction: " + String.format("%.2f", reduction));
            return damage * reduction;
        }
        //if the entity is not living
        return 0.0;
    }

    //####################################  CREATE_EXPLOSION ##############################
    public void detonate(FlyingProjectile cannonball)//TODO
    {
        plugin.logDebug("detonate cannonball");

        Projectile projectile = cannonball.getProjectile().clone();
        org.bukkit.entity.Projectile projectile_entity = cannonball.getProjectileEntity();

        LivingEntity shooter = cannonball.getShooter();
        Player player = null;
        if (shooter instanceof Player)
            player = (Player) shooter;

        //do no underwater damage if disabled (explosion but no damage)
        boolean isUnderwater = false;
        if ( projectile_entity.getLocation().clone().subtract(projectile_entity.getVelocity().normalize().multiply(2)).getBlock().isLiquid())
        {
            isUnderwater = true;
        }
        plugin.logDebug("Explosion is underwater: " + isUnderwater);

        //breaks blocks from the impact of the projectile to the location of the explosion
        Location impactLoc = blockBreaker(cannonball);

        //get world
        World world = impactLoc.getWorld();

        //teleport snowball to impact
        projectile_entity.teleport(impactLoc);

        float explosion_power = projectile.getExplosionPower();

        //reset explosion power if it is underwater and not allowed
        if (!projectile.isUnderwaterDamage() && isUnderwater)
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
            //event canceled, make a save imitated explosion
            //world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), 0);//CHANGED
            return;
        }

        //explosion event
        boolean incendiary = projectile.hasProperty(ProjectileProperties.INCENDIARY);
        boolean blockDamage = projectile.getExplosionDamage();
        boolean notCanceled = world.createExplosion(impactLoc.getX(), impactLoc.getY(), impactLoc.getZ(), explosion_power, incendiary, blockDamage);//FIXME sound underwater mustn't to be if !doesUnderwaterExplosion!

        //send a message about the impact (only if the projectile has enabled this feature)
        if (projectile.isImpactMessage())
            plugin.displayImpactMessage(player, impactLoc, notCanceled);

        if (notCanceled)
        {
            //if the player is too far away, there will be a imitated explosion made of fake blocks
            if(cannonball.getProjectile().isUnderwaterDamage() || !cannonball.wasInWater()) sendExplosionToPlayers(impactLoc, projectile.getExplosionPower());

            //place blocks around the impact like webs, lava, water
            spreadBlocks(impactLoc, cannonball);

            //spawns additional projectiles after the explosion
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedTask(cannonball)
            {
                public void run(Object object) {
                    FlyingProjectile flying = (FlyingProjectile) object;
                    spawnProjectiles(flying);
                }
            }, 1L);

            //spawn fireworks
            spawnFireworks(cannonball);


            //do potion effects
            int effectRange = (int) projectile.getPotionRange()/2;
            entity = projectile_entity.getNearbyEntities(effectRange, effectRange, effectRange);

            Iterator<Entity> it = entity.iterator();
            while (it.hasNext())
            {
                Entity next = it.next();
                applyPotionEffect(impactLoc, next, cannonball);

                double damage = 0.0;
                damage += getPlayerDamage(impactLoc, next, cannonball);
                damage += getDirectHitDamage(impactLoc, next, cannonball);
                // apply damage to the entity.
                if (damage >= 1 && next instanceof LivingEntity)
                {
                    LivingEntity living = (LivingEntity) next;
                    living.damage(damage);
                    //if player wears armor reduce damage
                    if (living instanceof Player)
                        CannonsUtil.reduceArmorDurability((Player) living);
                }
            }

            Location teleLoc = null;
            //teleport to impact and reset speed - make a soft landing
            if (projectile.hasProperty(ProjectileProperties.TELEPORT))
            {
                teleLoc = impactLoc.clone();
            }
            //teleport the player back to the location before firing
            else if(projectile.hasProperty(ProjectileProperties.OBSERVER))
            {
                teleLoc = cannonball.getFiringLocation();
            }
            //teleport to this location
            if (teleLoc != null)
            {
                teleLoc.setYaw(player.getLocation().getYaw());
                teleLoc.setPitch(player.getLocation().getPitch());
                player.teleport(teleLoc);
                player.setVelocity(new Vector(0,0,0));
            }

            //check which entities are affected by the event
            List<Entity> EntitiesAfterExplosion = projectile_entity.getNearbyEntities(effectRange, effectRange, effectRange);
            transmittingEntities(EntitiesAfterExplosion, cannonball.getShooter());//place blocks around the impact like webs, lava, water
            spreadBlocks(impactLoc, cannonball);

        }
    }

    /**
     * spawns fireworks after the explosion
     * @param cannonball
     */
    private void spawnFireworks(FlyingProjectile cannonball)
    {
        World world = cannonball.getProjectileEntity().getWorld();
        Projectile projectile = cannonball.getProjectile();

        //a fireworks needs some colors
        if (projectile.getFireworksColors().size() == 0) return;

        //building the fireworks effect
        FireworkEffect.Builder fwb = FireworkEffect.builder().flicker(projectile.isFireworksFlicker()).trail(projectile.isFireworksTrail()).with(projectile.getFireworksType());
        //setting colors
        for (Integer color : projectile.getFireworksColors())
        {
            fwb.withColor(Color.fromRGB(color));
        }
        for (Integer color : projectile.getFireworksFadeColors())
        {
            fwb.withFade(Color.fromRGB(color));
        }


        //apply to rocket
        final Firework fw = (Firework) world.spawnEntity(cannonball.getProjectileEntity().getLocation(), EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();

        meta.addEffect(fwb.build());
        meta.setPower(0);
        fw.setFireworkMeta(meta);

        //detonate firework after 1tick. This seems to works much better than detonating instantaneously
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedTask(fw)
        {
            public void run(Object object) {
                Firework fw = (Firework) object;
                //fw.detonate();
            }
        }, 1L);
    }

    /**
     * spawns Projectiles given in the spawnProjectile property
     * @param cannonball
     */
    private void spawnProjectiles(FlyingProjectile cannonball)
    {
        Projectile projectile = cannonball.getProjectile();
        LivingEntity shooter = cannonball.getShooter();
        Player player = (Player) shooter;
        Location impactLoc = cannonball.getProjectileEntity().getLocation();


        Random r = new Random();

        for (String strProj : projectile.getSpawnProjectiles())
        {
            Projectile newProjectiles = plugin.getProjectileStorage().getByName(strProj);
            if (newProjectiles == null)
            {
                plugin.logSevere("Can't use spawnProjectile " + strProj + " because Projectile does not exist");
                continue;
            }

            for (int i=0; i<newProjectiles.getNumberOfBullets(); i++)
            {
                Vector vect = new Vector(r.nextDouble()-0.5, r.nextDouble()-0.5, r.nextDouble()-0.5);
                vect = vect.normalize().multiply(newProjectiles.getVelocity());

                //don't spawn the projectile in the center
                Location spawnLoc = impactLoc.clone().add(vect.clone().normalize().multiply(3.0));
                plugin.getProjectileManager().spawnProjectile(newProjectiles, player, spawnLoc, vect);
            }
        }
    }

    /**
     * event for all entities which died in the explosion
     */
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
                if (entity.isDead())
                {
                    LivingEntity LivEntity = (LivingEntity) entity;
                    //check if the entity has not been transmitted
                    if(!hasBeenTransmitted(LivEntity.getUniqueId()))
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



    /**
     * creates a imitated explosion made of blocks which is transmitted to player in a give distance
     * @param l location of the explosion
     */
    public void sendExplosionToPlayers(Location l, float power)
    {
        double minDist = plugin.getMyConfig().getImitatedExplosionMinimumDistance();
        double maxDist = plugin.getMyConfig().getImitatedExplosionMaximumDistance();
        int r = plugin.getMyConfig().getImitatedExplosionSphereSize()/2;
        MaterialHolder mat = plugin.getMyConfig().getImitatedExplosionMaterial();
        int delay = (int) plugin.getMyConfig().getImitatedExplosionTime()*20;

        //ToConfig///////////
        int minExplodeSoundDistance = 40;
        int maxExplodeSoundDistance = 256;
        /////////////////////
        imitateSound(l, Sound.EXPLODE, power, minExplodeSoundDistance, maxExplodeSoundDistance);//TODO

        List<String> players = new ArrayList<String>();//IMPROVED
        for(Player p : l.getWorld().getPlayers())
        {
            Location pl = p.getLocation();
            double distance = pl.distance(l);

            if(distance >= minDist  && distance <= maxDist)
            {
                //p.playSound(l, Sound.EXPLODE, (float) (0.1*distance*distance/maxDist), 0.5f); TODO deleted
                players.add(p.getName());
            }
        }
        createImitatedSphere(players, l, r, mat, delay);
    }

    /**
     * creates a imitated explosion sound
     * @param l location of the explosion
     * @param s sound
     * @param power power of the explosion
     * @param minDist minimum distance
     * @param maxDist maximum distance
     */
    public static void imitateSound(Location l, Sound s, float power, int minDist, int maxDist)//TODO
    {
        World w = l.getWorld();
        if(s.equals(Sound.EXPLODE)) w.createExplosion(l, 0F, false);

        //To config///////////
        float soundPower = 5F;
        float additionVolume = 3F;
        //////////////////
        TreeMap<String, Integer> lp = new TreeMap<String, Integer>();
        for(Player p : w.getPlayers())
        {
            Location pl = p.getLocation();
            int x = Math.abs(pl.getBlockX() - l.getBlockX());
            int y = Math.abs(pl.getBlockY() - l.getBlockY());
            int z = Math.abs(pl.getBlockZ() - l.getBlockZ());
            int d = (int) Math.hypot(Math.hypot(x, y), z);
            if(minDist<=d&&d<=maxDist)
            {
                lp.put(p.getName(), d);
            }
        }
        for(String name : lp.keySet())
        {
            Player p = Bukkit.getPlayer(name);
            Location pl = p.getLocation();
            int x = l.getBlockX() - pl.getBlockX();
            int y = l.getBlockY() - pl.getBlockY();
            int z = l.getBlockZ() - pl.getBlockZ();
            Vector v = new Vector(x,y,z).normalize().multiply(20);
            float volume = soundPower*(power+additionVolume)/(float) Math.sqrt(lp.get(name));
            p.playSound(p.getEyeLocation().add(v), s, volume, 0F);
        }
    }

    /**
     * creates a sphere of fake block and sends it to the given player
     * @param player the player how will be notified
     * @param l center of the sphere
     * @param r radius of the sphere
     * @param mat material of the fake block
     * @param delay delay until the block disappears again
     */
    public void createImitatedSphere(List<String> players, Location l, int r, MaterialHolder mat, int delay)//IMPROVED
    {
        for(String name : players)
        {
            Player player = Bukkit.getPlayer(name);
            if(player!=null)
            {
                for(int x = -r; x <=r; x++)
                {
                    for(int y = -r; y<=r; y++)
                    {
                        for(int z = -r; z<=r; z++)
                        {
                            Location newL = l.clone().add(x, y, z);
                            if(newL.distance(l)<=r)
                            {
                                sendBlockChangeToPlayer(player, newL, mat, delay);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * sends fake block to the given player
     * @param p player to display the blocks
     * @param l location of the block
     * @param material type of the block
     * @param delay how long to remove the block
     */
    public static void sendBlockChangeToPlayer(final Player p, final Location l, MaterialHolder material, int delay)//TODO
    {
        if(l.getBlock().isEmpty())
        {
            p.sendBlockChange(l, material.getId(), (byte) material.getData());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Cannons"), new Runnable()
            {
                @Override
                public void run()
                {
                    p.sendBlockChange(l, l.getBlock().getType(), l.getBlock().getData());
                }
            }, delay);
        }
    }


}
