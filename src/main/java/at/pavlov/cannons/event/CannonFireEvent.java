package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CannonFireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Cannon cannon;
    private Player player;
    private boolean cancelled;

    public CannonFireEvent(Cannon cannon, Player player)
    {
        this.cannon = cannon;
        this.player = player;
        this.cancelled = false;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Player getPlayer() {
        return player;
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
