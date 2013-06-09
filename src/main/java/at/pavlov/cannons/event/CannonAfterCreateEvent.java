package at.pavlov.cannons.event;

import at.pavlov.cannons.config.MessageEnum;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import at.pavlov.cannons.cannon.Cannon;

public class CannonAfterCreateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Cannon cannon;
	private Player player;
	
	public CannonAfterCreateEvent(Cannon cannon, Player player) {
		
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

    public Player getPlayer() {
        return player;
    }

}
