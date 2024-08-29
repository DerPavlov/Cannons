package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.projectile.Projectile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CannonPreLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Cannon cannon;
    private Projectile projectile;
    private final Player player;

    public CannonPreLoadEvent(Cannon cannon, Projectile projectile, Player player) {
        this.cannon = cannon;
        this.projectile = projectile;
        this.player = player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public void setProjectile(Projectile projectile) {
        this.projectile = projectile;
    }

    public Player getPlayer() {
        return player;
    }
}
