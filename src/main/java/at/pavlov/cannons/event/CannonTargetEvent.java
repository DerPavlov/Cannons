package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.container.Target;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Fired when a Sentry cannon trys to target an entity
 */
public class CannonTargetEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Cannon cannon;
    private final Target target;
    private boolean cancelled;

    public CannonTargetEvent(Cannon cannon, Target player)
    {
        this.cannon = cannon;
        this.target = player;
        this.cancelled = false;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Target getPlayer() {
        return target;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
