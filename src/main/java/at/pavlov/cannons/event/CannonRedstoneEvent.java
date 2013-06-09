package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CannonRedstoneEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private Cannon cannon;
    private boolean cancelled;

    public CannonRedstoneEvent(Cannon cannon)
    {
        this.cannon = cannon;
        this.cancelled = false;
    }

    public Cannon getCannon()
    {
        return cannon;
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
