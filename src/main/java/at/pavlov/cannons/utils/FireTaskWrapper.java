package at.pavlov.cannons.utils;

import at.pavlov.cannons.cannon.Cannon;
import java.util.UUID;


public class FireTaskWrapper{
	private Cannon cannon;
    private UUID player;
    private boolean removeCharge;

	public FireTaskWrapper(Cannon cannon, UUID player, boolean removeCharge)
    {
        this.cannon = cannon;
        this.player = player;
        this.removeCharge = removeCharge;
	}

    public Cannon getCannon() {
        return cannon;
    }

    public void setCannon(Cannon cannon) {
        this.cannon = cannon;
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public boolean isRemoveCharge() {
        return removeCharge;
    }

    public void setRemoveCharge(boolean removeCharge) {
        this.removeCharge = removeCharge;
    }
}
