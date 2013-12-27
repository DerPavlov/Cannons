package at.pavlov.cannons.config;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ProjectileManager
{

    Cannons plugin;
    private LinkedList<FlyingProjectile> flying_projectiles = new LinkedList<FlyingProjectile>();

    /**
     * ProjectileManager
     * @param plugin
     */
    public ProjectileManager(Cannons plugin)
    {
        this.plugin = plugin;
    }

    public Snowball spawnProjectile(Projectile projectile, Player shooter, Location spawnLoc, Vector velocity)
    {
        World world = spawnLoc.getWorld();

        //one snowball for each projectile
        Snowball snowball = world.spawn(spawnLoc, Snowball.class);
        snowball.setFireTicks(100);
        snowball.setTicksLived(2);


        //calculate firing vector
        snowball.setVelocity(velocity);

        //create a new flying projectile container
        FlyingProjectile cannonball = new FlyingProjectile(projectile, snowball, shooter);
        //set shooter to the cannonball
        if (shooter != null)
        {
            cannonball.setShooter(shooter);
        }

        flying_projectiles.add(cannonball);

        //detonate timefused projectiles
        detonateTimefuse(cannonball);

        return snowball;
    }



    /**
     * detonate a timefused projectile mid air
     * @param cannonball
     */
    private void detonateTimefuse(FlyingProjectile cannonball)
    {
        if (cannonball.getProjectile().getTimefuse() > 0)
        {

            //Delayed Task
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
            {
                public void run() {

                    //check is list is not empty
                    if (!flying_projectiles.isEmpty())
                    {
                        Iterator<FlyingProjectile> iterator = flying_projectiles.iterator();
                        while( iterator.hasNext())
                        {
                            FlyingProjectile flying = iterator.next();
                            Projectile proj = flying.getProjectile();
                            if (flying.getSnowball() != null)
                            {
                                if (flying.getSnowball().getTicksLived() > proj.getTimefuse()*20 - 5 && proj.getTimefuse() > 0)
                                {
                                    //detonate timefuse
                                    plugin.getExplosion().detonate(flying);
                                    flying.getSnowball().remove();
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }}, (long) (cannonball.getProjectile().getTimefuse()*20));
        }
    }


    /**
     * detonates the given projectile entity
     * @param entity
     */
    public void detonateProjectile(Entity entity)
    {
        if (entity == null) return;

        // iterate the list
        if (!flying_projectiles.isEmpty())
        {
            Iterator<FlyingProjectile> iterator = flying_projectiles.iterator();

            while (iterator.hasNext())
            {
                FlyingProjectile flying = iterator.next();
                if (entity.equals(flying.getSnowball()))
                {
                    plugin.getExplosion().detonate(flying);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * returns the list of all flying projectiles
     * @return
     */
    public List<FlyingProjectile> getFlyingProjectiles()
    {
        return flying_projectiles;
    }
}
