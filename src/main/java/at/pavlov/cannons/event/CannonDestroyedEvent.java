package at.pavlov.cannons.event;

import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * If break cause is OTHER it might be because of
 * a command to delete all cannons
 */
public class CannonDestroyedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Cannon cannon;
    private final BreakCause breakCause;
    private final boolean breakAllCannonBlocks;
    private final boolean canExplode;

	public CannonDestroyedEvent(Cannon cannon, BreakCause breakCause, boolean breakBlocks, boolean canExplode) {
        this.breakCause = breakCause;
        this.cannon = cannon;
        this.breakAllCannonBlocks = breakBlocks;
        this.canExplode = canExplode;
    }
	
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public BreakCause getBreakCause() {
        return breakCause;
    }

    public boolean isBreakAllCannonBlocks() {
        return breakAllCannonBlocks;
    }

    public boolean isCanExplode() {
        return canExplode;
    }
}
