package at.pavlov.cannons.event;

import at.pavlov.cannons.Enum.MessageEnum;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.pavlov.cannons.cannon.Cannon;

public class CannonBeforeCreateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final Cannon cannon;
    private MessageEnum message;
	private final Player player;
	private boolean cancelled;
	
	public CannonBeforeCreateEvent(Cannon cannon, MessageEnum message, Player player) {

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

    public Player getPlayer() {
        return player;
    }


    public MessageEnum getMessage() {
        return message;
    }

    public void setMessage(MessageEnum message) {
        this.message = message;
    }
}
