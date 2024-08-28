package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * An event called after CannonFireEvent in the case that Linking cannons is enabled as
 * a feature, this might be useful if you want to change its behaviour, cancelling this
 * event will only prevent the linked cannons from firing
 */
public class CannonLinkFiringEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Cannon cannon;
    private List<Cannon> linkedCannons;
    private boolean cancelled;
    private final UUID player;

    public CannonLinkFiringEvent(Cannon cannon, List<Cannon> linkedCannons, UUID player) {
        this.cannon = cannon;
        this.linkedCannons = linkedCannons;
        this.player = player;
        this.cancelled = false;
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

    public List<Cannon> getLinkedCannons() {
        return linkedCannons;
    }

    /**
     * Change this list only if you know what you are doing
     * @param cannons new cannons of linked cannons to fire
     */
    public void setLinkedCannons(List<Cannon> cannons) {
        linkedCannons = cannons;
    }

    public UUID getPlayer() {
        return player;
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
