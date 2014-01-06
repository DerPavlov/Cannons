package at.pavlov.cannons.scheduler;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedList;

public class Teleporter {
    Cannons plugin;


    /**
     * Constructor
     * @param plugin
     */
    public Teleporter(Cannons plugin)
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
                updateTeleporter();
            }
        }, 1L, 1L);
    }

    /**
     * teleports the player to new position
     */
    public void updateTeleporter()
    {
        //get projectiles
        for (FlyingProjectile fproj : plugin.getProjectileManager().getFlyingProjectiles())
        {
            //if projectile has teleporter - update player position
            Projectile projectile = fproj.getProjectile();
            if (projectile.hasProperty(ProjectileProperties.TELEPORT) || projectile.hasProperty(ProjectileProperties.OBSERVER))
            {
                LivingEntity shooter = fproj.getShooter();
                if(shooter == null)
                    continue;

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

}