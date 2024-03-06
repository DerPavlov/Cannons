package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class CannonAfterCreateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Cannon cannon;
	private final UUID player;
	
	public CannonAfterCreateEvent(Cannon cannon, UUID player) {
		
		this.cannon = cannon;
		this.player = player;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public UUID getPlayer() {
        return player;
    }

}
