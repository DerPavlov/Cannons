package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class CannonUseEvent
{
    private Cannon cannon;
    private Player player;
    private Action action;
    private boolean cancelled;

    public CannonUseEvent(Cannon cannon, Player player, Action action)
    {
        this.cannon = cannon;
        this.player = player;
        this.action = action;
        this.cancelled = false;
    }

    Cannon getCannon() {
        return cannon;
    }

    Player getPlayer() {
        return player;
    }

    public Action getAction() {
        return action;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
