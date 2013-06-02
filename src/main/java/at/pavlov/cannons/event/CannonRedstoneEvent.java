package at.pavlov.cannons.event;

import at.pavlov.cannons.cannon.Cannon;

public class CannonRedstoneEvent
{
    private Cannon cannon;
    private boolean cancelled;

    public CannonRedstoneEvent(Cannon cannon)
    {
        this.cannon = cannon;
        this.cancelled = false;
    }

    Cannon getCannon()
    {
        return cannon;
    }


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
