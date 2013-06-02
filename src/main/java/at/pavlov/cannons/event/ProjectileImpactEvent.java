package at.pavlov.cannons.event;

import at.pavlov.cannons.projectile.Projectile;
import org.bukkit.Location;

public class ProjectileImpactEvent
{
    private Projectile projectile;
    private Location impactLocation;
    private boolean cancelled;

    public ProjectileImpactEvent(Projectile projectile, Location impactLocation)
    {
        this.projectile = projectile;
        this.impactLocation = impactLocation;
        this.cancelled = false;
    }


    public Location getImpactLocation() {
        return impactLocation;
    }

    public void setImpactLocation(Location impactLocation) {
        this.impactLocation = impactLocation;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public void setProjectile(Projectile projectile) {
        this.projectile = projectile;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
