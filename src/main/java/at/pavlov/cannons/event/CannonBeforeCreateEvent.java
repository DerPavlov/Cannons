package at.pavlov.cannons.event;

import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class CannonBeforeCreateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Cannon cannon;
    private MessageEnum message;
	private final UUID player;
	private boolean cancelled;
	
	public CannonBeforeCreateEvent(Cannon cannon, MessageEnum message, UUID player) {

        this.cannon = cannon;
        this.message = message;
        this.player = player;
        this.cancelled = false;
    }
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
 
    public void setCancelled(boolean a) {
        this.cancelled = a;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public UUID getPlayer() {
        return player;
    }


    public MessageEnum getMessage() {
        return message;
    }

    public void setMessage(MessageEnum message) {
        this.message = message;
    }
}
