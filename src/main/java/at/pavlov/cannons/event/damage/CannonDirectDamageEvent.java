package at.pavlov.cannons.event.damage;

import at.pavlov.cannons.projectile.FlyingProjectile;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CannonDirectDamageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final FlyingProjectile cannonball;
    private final Entity target;
    private double damage;
    private double reduction;

    public CannonDirectDamageEvent(FlyingProjectile cannonball, Entity target, double damage, double reduction) {
        this.cannonball = cannonball;
        this.target = target;
        this.damage = damage;
        this.reduction = reduction;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public FlyingProjectile getCannonball() {
        return cannonball;
    }

    public Entity getTarget() {
        return target;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }
}
