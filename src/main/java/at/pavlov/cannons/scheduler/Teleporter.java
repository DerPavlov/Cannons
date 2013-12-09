package at.pavlov.cannons.scheduler;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import java.util.HashMap;
import java.util.LinkedList;

public class Teleporter {
    Cannons plugin;


    //##################### Constructor ##############################
    public Teleporter(Cannons plugin)
    {
        this.plugin = plugin;
    }

    //##################### InitAimingMode ##############################
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

    public void updateTeleporter()
    {
        //get projectiles
        for (FlyingProjectile fproj : plugin.getFireCannon().flying_projectiles)
        {
            //if projectile has teleporter - update player position
            Projectile projectile = fproj.getProjectile();
            if (projectile.hasProperty(ProjectileProperties.TELEPORT))
            {
                LivingEntity shooter = fproj.getShooter();
                if(shooter == null)
                    continue;

                Snowball ball = fproj.getSnowball();
                shooter.teleport(ball.getLocation().subtract(ball.getVelocity().multiply(-3)));

                shooter.setVelocity(ball.getVelocity());
            }

        }
    }

}