package at.pavlov.cannons.event;

import at.pavlov.cannons.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ProjectilePiercingEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private Projectile projectile;
    private Location impactLocation;
    private List<Block> blockList;

    public ProjectilePiercingEvent(Projectile projectile, Location impactLocation, List<Block> blockList)
    {
        this.projectile = projectile;
        this.impactLocation = impactLocation;
        this.blockList = blockList;
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

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }
}
