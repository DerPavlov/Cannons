package at.pavlov.cannons.scheduler;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;
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
                    //remove an not valid projectile
                    if (cannonball.getProjectileEntity() == null)
                    {
                        iter.remove();
                        continue;
                    }

                    //update the cannonball
                    checkWaterImpact(cannonball);
                    updateTeleporter(cannonball);
                    if (updateProjectileLocation(cannonball)) {
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
    private void checkWaterImpact(FlyingProjectile cannonball)
    {

        //the projectile has passed the water surface, make a splash
        if (cannonball.updateWaterSurfaceCheck())
        {
            //go up until there is air and place the same liquid
            Location startLoc = cannonball.getProjectileEntity().getLocation().clone();
            Vector vel = cannonball.getProjectileEntity().getVelocity().clone();
            MaterialHolder liquid = new MaterialHolder(startLoc.getBlock().getTypeId(), startLoc.getBlock().getData());

            for (int i = 0; i<5; i++)
            {
                Block block = startLoc.subtract(vel.clone().multiply(i)).getBlock();
                if (block != null && block.isEmpty())
                {
                    //found a free block - make the splash
                    sendSplashToPlayers(block.getLocation(), liquid);
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
    public void sendSplashToPlayers(Location loc, MaterialHolder liquid)
    {
        int maxDist = (int) plugin.getMyConfig().getImitatedBlockMaximumDistance();
        int maxSoundDist = plugin.getMyConfig().getImitatedSoundMaximumDistance();

        for(Player p : loc.getWorld().getPlayers())
        {
            Location pl = p.getLocation();
            double distance = pl.distance(loc);

            if(distance <= maxDist) plugin.getFakeBlockHandler().imitatedSphere(p, loc, 1, new MaterialHolder(liquid.getId(), 0), 40);
            
        }
        CannonsUtil.imitateSound(loc, Sound.SPLASH, maxSoundDist, 0.3F);//Too many errors in code, Peter!
    }

    /**
     * teleports the player to new position of the cannonball
     * @param cannonball the FlyingProjectile to check
     */
    private void updateTeleporter(FlyingProjectile cannonball)
    {
        //if projectile has HUMAN_CANNONBALL or OBSERVER - update player position
        Projectile projectile = cannonball.getProjectile();
        if (projectile.hasProperty(ProjectileProperties.HUMAN_CANNONBALL) || projectile.hasProperty(ProjectileProperties.OBSERVER))
        {
            LivingEntity shooter = cannonball.getShooter();
            if(shooter == null)
                return;

            org.bukkit.entity.Projectile ball = cannonball.getProjectileEntity();
            //set some distance to the snowball to prevent a collision
            Location optiLoc = ball.getLocation().clone().subtract(ball.getVelocity().normalize().multiply(20.0));

            Vector distToOptimum = optiLoc.toVector().subtract(shooter.getLocation().toVector());
            Vector playerVel = ball.getVelocity().add(distToOptimum.multiply(0.1));
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
    private boolean updateProjectileLocation(FlyingProjectile cannonball)
    {
        if (!plugin.getMyConfig().isKeepAliveEnabled())
            return false;

        if (cannonball.distanceToProjectile() > plugin.getMyConfig().getKeepAliveTeleportDistance())
        {
            Location toLoc = cannonball.getExpectedLocation();
            plugin.logDebug("teleported projectile to: " +  toLoc.getBlockX() + "," + toLoc.getBlockY() + "," + toLoc.getBlockZ());
            cannonball.teleportToPrediction();
        }


        //see if we hit something
        Block block = cannonball.getExpectedLocation().getBlock();
        if (!block.isEmpty() && !block.isLiquid())
        {
            cannonball.revertUpdate();
            cannonball.teleportToPrediction();
            plugin.getExplosion().detonate(cannonball);
            cannonball.getProjectileEntity().remove();
            return true;
        }
        cannonball.update();
        return false;
    }

}