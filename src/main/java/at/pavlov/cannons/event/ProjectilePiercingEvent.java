package at.pavlov.cannons.event;

import at.pavlov.cannons.projectile.Projectile;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ProjectilePiercingEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Projectile projectile;
    private Location impactLocation;
    private List<Block> blockList;
    private boolean cancelled;

    public ProjectilePiercingEvent(Projectile projectile, Location impactLocation, List<Block> blockList) {
        this.projectile = projectile;
        this.impactLocation = impactLocation;
        this.blockList = blockList;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
