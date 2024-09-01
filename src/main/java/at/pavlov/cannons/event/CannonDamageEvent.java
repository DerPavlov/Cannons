package at.pavlov.cannons.event;

import at.pavlov.cannons.Enum.DamageType;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.projectile.FlyingProjectile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CannonDamageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final FlyingProjectile cannonball;
    private final LivingEntity target;
    private double damage;
    private double reduction;
    private Double distance;
    private final DamageType type;

    public CannonDamageEvent(FlyingProjectile cannonball, LivingEntity target, double damage, double reduction, @Nullable Double distance, DamageType type) {
        this.cannonball = cannonball;
        this.target = target;
        this.damage = damage;
        this.reduction = reduction;
        this.distance = distance; //this will be Location of the target if DamageType.DIRECT
        this.type = type;
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

    public LivingEntity getTarget() {
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

    public DamageType getType() {
        return type;
    }

    public double getDistance() {
        if (distance == null && type == DamageType.DIRECT)
            distance = cannonball.getImpactLocation().distance(target.getLocation());

        return distance;
    }
}
