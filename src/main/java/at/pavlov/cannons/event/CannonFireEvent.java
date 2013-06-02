package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.entity.Player;

class CannonFireEvent {
    private Cannon cannon;
    private Player player;
    private boolean cancelled;

    public CannonFireEvent(Cannon cannon, Player player)
    {
        this.cannon = cannon;
        this.player = player;
        this.cancelled = false;
    }

    Cannon getCannon() {
        return cannon;
    }

    Player getPlayer() {
        return player;
    }

    boolean isCancelled() {
        return cancelled;
    }

    void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
