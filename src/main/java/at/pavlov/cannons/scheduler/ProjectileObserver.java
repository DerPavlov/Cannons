package at.pavlov.cannons.scheduler;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.FakeBlockType;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.container.SoundHolder;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class ProjectileObserver {
    private final Cannons plugin;


    /**
     * Constructor
     * @param plugin - Cannons instance
     */
    public ProjectileObserver(Cannons plugin)
    {
        this.plugin = plugin;
    }

    /**
     * starts the scheduler of the teleporter
     */
    public void setupScheduler()
    {
        //changing angles for aiming mode
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            public void run()
            {
                //get projectiles
                Iterator<Map.Entry<UUID,FlyingProjectile>> iter = plugin.getProjectileManager().getFlyingProjectiles().entrySet().iterator();
                while(iter.hasNext())
                {
                    FlyingProjectile cannonball = iter.next().getValue();
                    org.bukkit.entity.Projectile projectile_entity = cannonball.getProjectileEntity();
                    //remove an not valid projectile
                    if (!cannonball.isValid(projectile_entity))
                    {
                        //teleport the observer back to its start position
                        CannonsUtil.teleportBack(cannonball);
                        if (projectile_entity != null)
                        {
                            Location l = projectile_entity.getLocation();
                            projectile_entity.remove();
                            plugin.logDebug("removed Projectile at " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + " because it was not valid.");
                        }
                        else
                            plugin.logDebug("removed Projectile at because the entity was missing");
                        //remove entry in hashmap
                        iter.remove();
                        continue;
                    }

                    //update the cannonball
                    checkWaterImpact(cannonball, projectile_entity);
                    updateTeleporter(cannonball, projectile_entity);
                    updateSmokeTrail(cannonball, projectile_entity);
                    if (updateProjectileLocation(cannonball, projectile_entity)) {
                        iter.remove();
                        continue;
                    }
                }

            }
        }, 1L, 1L);
    }

    /**
     * if cannonball enters water it will spawn a splash effect
     * @param cannonball the projectile to check
     */
    private void checkWaterImpact(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity)
    {

        //the projectile has passed the water surface, make a splash
        if (cannonball.updateWaterSurfaceCheck(projectile_entity))
        {
            //go up until there is air and place the same liquid
            Location startLoc = projectile_entity.getLocation().clone();
            Vector vel = projectile_entity.getVelocity().clone();
            ItemHolder liquid = new ItemHolder(startLoc.getBlock().getType());

            for (int i = 0; i<5; i++)
            {
                Block block = startLoc.subtract(vel.clone().multiply(i)).getBlock();
                if (block != null && block.isEmpty())
                {
                    //found a free block - make the splash
                    sendSplashToPlayers(block.getLocation(), liquid, cannonball.getProjectile().getSoundImpactWater());
                    break;
                }
            }
        }
    }

    /**
     * creates a sphere of fake blocks on the impact for all player in the vicinity
     * @param loc - location of the impact
     * @param liquid - material of the fake blocks
     */
    public void sendSplashToPlayers(Location loc, ItemHolder liquid, SoundHolder sound)
    {
        int maxDist = (int) plugin.getMyConfig().getImitatedBlockMaximumDistance();
        int maxSoundDist = plugin.getMyConfig().getImitatedSoundMaximumDistance();
        float maxVol = plugin.getMyConfig().getImitatedSoundMaximumVolume();

        for(Player p : loc.getWorld().getPlayers())
        {
            Location pl = p.getLocation();
            double distance = pl.distance(loc);

            if(distance <= maxDist)
                plugin.getFakeBlockHandler().imitatedSphere(p, loc, 1, Bukkit.createBlockData(liquid.getType()), FakeBlockType.WATER_SPLASH, 1.0);

        }
        CannonsUtil.imitateSound(loc, sound, maxSoundDist, maxVol);
    }

    /**
     * teleports the player to new position of the cannonball
     * @param cannonball the FlyingProjectile to check
     */
    private void updateTeleporter(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity)
    {
        //do nothing if the teleport was already performed
        if (cannonball.isTeleported())
            return;

        //if projectile has HUMAN_CANNONBALL or OBSERVER - update player position
        Projectile projectile = cannonball.getProjectile();
        if (projectile.hasProperty(ProjectileProperties.HUMAN_CANNONBALL) || projectile.hasProperty(ProjectileProperties.OBSERVER))
        {
            Player shooter = Bukkit.getPlayer(cannonball.getShooterUID());
            if(shooter == null)
                return;

            //set some distance to the snowball to prevent a collision
            Location optiLoc = projectile_entity.getLocation().clone().subtract(projectile_entity.getVelocity().normalize().multiply(20.0));

            Vector distToOptimum = optiLoc.toVector().subtract(shooter.getLocation().toVector());
            Vector playerVel = projectile_entity.getVelocity().add(distToOptimum.multiply(0.1));
            //cap for maximum speed
            if (playerVel.getX() > 5.0)
                playerVel.setX(5.0);
            if (playerVel.getY() > 5.0)
                playerVel.setY(5.0);
            if (playerVel.getZ() > 5.0)
                playerVel.setZ(5.0);
            shooter.setVelocity(playerVel);
            shooter.setFallDistance(0.0f);


            //teleport if the player is behind
            if (distToOptimum.length() > 30)
            {
                optiLoc.setYaw(shooter.getLocation().getYaw());
                optiLoc.setPitch(shooter.getLocation().getPitch());
                shooter.teleport(optiLoc);
            }
        }
    }

    /**
     * calculates the location where the projectile should be an teleports the projectile to this location
     * @param cannonball projectile to update
     * @return true if the projectile must be removed
     */
    private boolean updateProjectileLocation(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity)
    {
        if (!plugin.getMyConfig().isKeepAliveEnabled())
            return false;

        if (cannonball.distanceToProjectile(projectile_entity) > plugin.getMyConfig().getKeepAliveTeleportDistance())
        {
            Location toLoc = cannonball.getExpectedLocation();
            plugin.logDebug("teleported projectile to: " +  toLoc.getBlockX() + "," + toLoc.getBlockY() + "," + toLoc.getBlockZ());
            cannonball.teleportToPrediction(projectile_entity);
        }


        //see if we hit something
        Block block = cannonball.getExpectedLocation().getBlock();
        if (!block.isEmpty() && !block.isLiquid())
        {
            cannonball.revertUpdate();
            cannonball.teleportToPrediction(projectile_entity);
            plugin.getExplosion().detonate(cannonball, projectile_entity);
            projectile_entity.remove();
            return true;
        }
        //todo proximity fuse
        cannonball.update();
        return false;
    }


    /**
     * spawn smoke clouds behind the projectile to improve the visibility
     * @param cannonball the cannonball entity entry of cannons
     * @param projectile_entity the entity of the projectile
     */
    private void updateSmokeTrail(FlyingProjectile cannonball, org.bukkit.entity.Projectile projectile_entity)
    {
        Random r = new Random();
        Projectile proj = cannonball.getProjectile();
        int maxDist = (int) plugin.getMyConfig().getImitatedBlockMaximumDistance();
        double smokeDist = proj.getSmokeTrailDistance()*(0.5 + r.nextDouble());
        double smokeDuration = proj.getSmokeTrailDuration()*(0.5 + r.nextGaussian());

        if (proj.isSmokeTrailEnabled() && cannonball.getExpectedLocation().distance(cannonball.getLastSmokeTrailLocation()) > smokeDist)
        {
            //create a new smoke trail cloud
            Location newLoc = cannonball.getExpectedLocation();
            cannonball.setLastSmokeTrailLocation(newLoc);
            plugin.logDebug("smoke trail at: " +  newLoc.getBlockX() + "," + newLoc.getBlockY() + "," + newLoc.getBlockZ());

            if (proj.isSmokeTrailParticleEnabled()) {
                cannonball.getWorld().spawnParticle(proj.getSmokeTrailParticleType(), newLoc, proj.getSmokeTrailParticleCount(), proj.getSmokeTrailParticleOffsetX(), proj.getSmokeTrailParticleOffsetY(), proj.getSmokeTrailParticleOffsetZ(), proj.getSmokeTrailParticleSpeed(), null, true);
            }
            else {
                for (Player p : newLoc.getWorld().getPlayers()) {
                    Location pl = p.getLocation();
                    double distance = pl.distance(newLoc);

                    if (distance <= maxDist)
                        plugin.getFakeBlockHandler().imitatedSphere(p, newLoc, 0, proj.getSmokeTrailMaterial(), FakeBlockType.SMOKE_TRAIL, smokeDuration);

                }
            }
        }

    }

}