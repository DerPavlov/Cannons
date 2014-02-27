package at.pavlov.cannons.event;

import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CannonUseEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final Cannon cannon;
    private final Player player;
    private final InteractAction action;
    private boolean cancelled;

    public CannonUseEvent(Cannon cannon, Player player, InteractAction action)
    {
        this.cannon = cannon;
        this.player = player;
        this.action = action;
        this.cancelled = false;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Player getPlayer() {
        return player;
    }

    public InteractAction getAction() {
        return action;
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
