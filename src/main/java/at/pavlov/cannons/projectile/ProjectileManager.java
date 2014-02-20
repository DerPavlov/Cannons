package at.pavlov.cannons.projectile;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
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

    public org.bukkit.entity.Projectile spawnProjectile(Projectile projectile, Player shooter, Location spawnLoc, Vector velocity)
    {
        World world = spawnLoc.getWorld();

        //set yaw, pitch for fireballs
        double v = velocity.length();
        spawnLoc.setPitch((float) (Math.acos(velocity.getY()/v)*180.0/Math.PI - 90));
        spawnLoc.setYaw((float) (Math.atan2(velocity.getZ(),velocity.getX())*180.0/Math.PI - 90));

        Entity pEntity = world.spawnEntity(spawnLoc, projectile.getProjectileEntity());;
        org.bukkit.entity.Projectile projectileEntity;
        try
        {
            projectileEntity = (org.bukkit.entity.Projectile) pEntity;
        }
        catch(Exception e)
        {
            plugin.logSevere("Can't convert EntityType " + pEntity.getType() + " to projectile. Using Snowball");
            projectileEntity = (org.bukkit.entity.Projectile) world.spawnEntity(spawnLoc, EntityType.SNOWBALL);
        }

        if (projectile.isProjectileOnFire())
            projectileEntity.setFireTicks(100);
        //projectileEntity.setTicksLived(2);

        //calculate firing vector
        projectileEntity.setVelocity(velocity);

        //create a new flying projectile container
        FlyingProjectile cannonball = new FlyingProjectile(projectile, projectileEntity, shooter);
        //set shooter to the cannonball
        if (shooter != null)
        {
            cannonball.setShooter(shooter);
        }

        flying_projectiles.add(cannonball);

        //detonate timefused projectiles
        detonateTimefuse(cannonball);

        return projectileEntity;
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
                            if (flying.getProjectileEntity() != null)
                            {
                                if (flying.getProjectileEntity().getTicksLived() > proj.getTimefuse()*20 - 5 && proj.getTimefuse() > 0)
                                {
                                    //detonate timefuse
                                    plugin.getExplosion().detonate(flying);
                                    flying.getProjectileEntity().remove();
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
                if (entity.equals(flying.getProjectileEntity()))
                {
                    plugin.getExplosion().detonate(flying);
                    flying.getProjectileEntity().remove();
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
