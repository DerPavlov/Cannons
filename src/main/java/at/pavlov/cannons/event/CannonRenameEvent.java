package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CannonRenameEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Cannon cannon;
    private final String proposedNewName;
    private boolean cancelled;

    public CannonRenameEvent(Player player, Cannon cannon, String proposedNewName) {
        this.player = player;
        this.cannon = cannon;
        this.proposedNewName = proposedNewName;
        this.cancelled = false;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    public Player getPlayer() {
        return player;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public String getProposedNewName() {
        return proposedNewName;
    }
}
