package at.pavlov.cannons.projectile;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.utils.DelayedTask;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public class ProjectileManager
{

    private final Cannons plugin;
    private final HashMap<UUID, FlyingProjectile> flyingProjectilesMap = new HashMap<UUID, FlyingProjectile>();

    /**
     * ProjectileManager
     * @param plugin - Cannons instance
     */
    public ProjectileManager(Cannons plugin)
    {
        this.plugin = plugin;
    }

    public org.bukkit.entity.Projectile spawnProjectile(Projectile projectile, UUID shooter, ProjectileSource source, Location playerLoc, Location spawnLoc, Vector velocity, UUID cannonId)
    {
        Validate.notNull(shooter, "shooter for the projectile can't be null");
        World world = spawnLoc.getWorld();

        //set yaw, pitch for fireballs
        double v = velocity.length();
        spawnLoc.setPitch((float) (Math.acos(velocity.getY()/v)*180.0/Math.PI - 90));
        spawnLoc.setYaw((float) (Math.atan2(velocity.getZ(),velocity.getX())*180.0/Math.PI - 90));

        Entity pEntity = world.spawnEntity(spawnLoc, projectile.getProjectileEntity());
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
        FlyingProjectile cannonball = new FlyingProjectile(projectile, projectileEntity, shooter, source, playerLoc, cannonId);


        flyingProjectilesMap.put(cannonball.getUID(), cannonball);

        //detonate timefused projectiles
        detonateTimefuse(cannonball);

        return projectileEntity;
    }



    /**
     * detonate a timefused projectile mid air
     * @param cannonball - the cannonball to detonate
     */
    private void detonateTimefuse(final FlyingProjectile cannonball)
    {
        if (cannonball.getProjectile().getTimefuse() > 0)
        {

            //Delayed Task
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,  new DelayedTask(cannonball.getUID())
            {
                public void run(Object object)
                {
                    //find given UID in list
                    FlyingProjectile fproj = flyingProjectilesMap.get((UUID) object);

                    if(fproj != null) {
                        //detonate timefuse
                        org.bukkit.entity.Projectile projectile_entity = fproj.getProjectileEntity();
                        plugin.getExplosion().detonate(cannonball, projectile_entity);
                        projectile_entity.remove();
                        flyingProjectilesMap.remove(cannonball.getUID());
                    }
                }}, (long) (cannonball.getProjectile().getTimefuse()*20));
        }
    }


    /**
     * detonates the given projectile entity
     * @param projectile - the projectile with this entity
     */
    public void detonateProjectile(Entity projectile)
    {
        if(projectile == null) return;

        FlyingProjectile fproj = flyingProjectilesMap.get(projectile.getUniqueId());
        if (fproj!=null)
        {
            org.bukkit.entity.Projectile projectile_entity = fproj.getProjectileEntity();
            plugin.getExplosion().detonate(fproj, projectile_entity);
            projectile_entity.remove();
            flyingProjectilesMap.remove(fproj.getUID());
        }
    }

    /**
     * detonates the given projectile entity
     * @param projectile - the projectile with this entity
     * @param target the entity hit by the projectile
     */
    public void directHitProjectile(Entity projectile, Entity target)
    {
        if(projectile == null || target == null) return;

        FlyingProjectile fproj = flyingProjectilesMap.get(projectile.getUniqueId());
        if (fproj != null)
        {
            org.bukkit.entity.Projectile projectile_entity = fproj.getProjectileEntity();
            plugin.getExplosion().directHit(fproj, projectile_entity, target);
            projectile_entity.remove();
            flyingProjectilesMap.remove(fproj.getUID());
        }
    }

    /**
     * returns true if the given entity is a cannonball projectile
     * @param projectile flying projectile
     * @return true if cannonball projectile
     */
    public boolean isFlyingProjectile(Entity projectile)
    {
        FlyingProjectile fproj = flyingProjectilesMap.get(projectile.getUniqueId());
        return fproj != null;
    }


    /**
     * returns the list of all flying projectiles
     * @return - the list of all flying projectiles
     */
    public HashMap<UUID, FlyingProjectile> getFlyingProjectiles()
    {
        return flyingProjectilesMap;
    }
}
