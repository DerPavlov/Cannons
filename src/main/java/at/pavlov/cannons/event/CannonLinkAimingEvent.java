package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CannonLinkAimingEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Cannon cannon;
    private final Player player;
    private List<Cannon> cannonList;
    //changing sameDesign to true can cause desync, but you can have fun ig
    private boolean sameDesign;

    public CannonLinkAimingEvent(Cannon cannon, Player player, List<Cannon> cannonList, boolean sameDesign) {
        this.cannon = cannon;
        this.player = player;
        this.cannonList = cannonList;
        this.sameDesign = sameDesign;
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

    public Player getPlayer() {
        return player;
    }

    public List<Cannon> getCannonList() {
        return cannonList;
    }

    public void setCannonList(List<Cannon> cannonList) {
        this.cannonList = cannonList;
    }

    public boolean isSameDesign() {
        return sameDesign;
    }

    public void setSameDesign(boolean sameDesign) {
        this.sameDesign = sameDesign;
    }
}
