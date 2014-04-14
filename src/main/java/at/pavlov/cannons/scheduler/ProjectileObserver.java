package at.pavlov.cannons.scheduler;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import at.pavlov.cannons.utils.CannonsUtil;
import at.pavlov.cannons.utils.DelayedTask;
import at.pavlov.cannons.utils.FireTaskWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;


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
                Iterator<FlyingProjectile> iter = plugin.getProjectileManager().getFlyingProjectiles().iterator();
                while(iter.hasNext())
                {
                    FlyingProjectile fproj = iter.next();
                    //remove an not valid projectile
                    if (fproj.getProjectileEntity() == null)
                    {
                        iter.remove();
                        continue;
                    }

                    //update the cannonball
                    checkWaterImpact(fproj);
                    updateTeleporter(fproj);

                }

            }
        }, 1L, 1L);
    }

    /**
     * if cannonball enters water it will spawn a splash effect
     * @param fproj the projectile to check
     */
    private void checkWaterImpact(FlyingProjectile fproj)
    {

        //the projectile has passed the water surface, make a splash
        if (fproj.updateWaterSurfaceCheck())
        {
            //go up until there is air and place the same liquid
            Location startLoc = fproj.getProjectileEntity().getLocation().clone();
            Vector vel = fproj.getProjectileEntity().getVelocity().clone();
            MaterialHolder liquid = new MaterialHolder(startLoc.getBlock().getTypeId(), startLoc.getBlock().getData());

            for (int i = 0; i<5; i++)
            {
                Block block = startLoc.subtract(vel.clone().multiply(i)).getBlock();
                if (block != null && block.isEmpty())
                {
                    //found a free block - make the splash
                    sendSplashToPlayers(block.getLocation(), (float) fproj.getProjectileEntity().getVelocity().length(), liquid);
                    break;
                }
            }
        }
    }

    /**
     * creates a sphere of fake blocks on the impact for all player in the vicinity
     * @param loc - location of the impact
     * @param power - power of splash
     * @param liquid - material of the fake blocks
     */
    public void sendSplashToPlayers(Location loc, float power, MaterialHolder liquid)
    {
        int maxDist = (int) plugin.getMyConfig().getImitatedBlockMaximumDistance();

        for(Player p : loc.getWorld().getPlayers())
        {
            Location pl = p.getLocation();
            double distance = pl.distance(loc);

            if(distance <= maxDist)
            {
                CannonsUtil.imitateSound(loc, Sound.SPLASH, power, 0, maxDist);
                plugin.getFakeBlockHandler().imitatedSphere(p, loc, 1, new MaterialHolder(liquid.getId(), 0), 40);
            }
        }
    }

    /**
     * teleports the player to new position
     * @param fproj the FlyingProjectile to check
     */
    private void updateTeleporter(FlyingProjectile fproj)
    {
        //if projectile has teleporter - update player position
        Projectile projectile = fproj.getProjectile();
        if (projectile.hasProperty(ProjectileProperties.TELEPORT) || projectile.hasProperty(ProjectileProperties.OBSERVER))
        {
            LivingEntity shooter = fproj.getShooter();
            if(shooter == null)
                return;

            org.bukkit.entity.Projectile ball = fproj.getProjectileEntity();
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

}