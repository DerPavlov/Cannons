package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CannonFireEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Cannon cannon;
    private final UUID player;
    private boolean cancelled;

    public CannonFireEvent(Cannon cannon, UUID player)
    {
        this.cannon = cannon;
        this.player = player;
        this.cancelled = false;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public UUID getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
