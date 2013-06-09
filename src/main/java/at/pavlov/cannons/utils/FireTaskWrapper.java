package at.pavlov.cannons.utils;

import org.bukkit.entity.Player;

import at.pavlov.cannons.cannon.Cannon;


public class FireTaskWrapper{
	private Cannon cannon;
    private Player player;
	
	public FireTaskWrapper(Cannon cannon, Player player)
    {
        this.cannon = cannon;
        this.player = player;
	}

    public Cannon getCannon() {
        return cannon;
    }

    public void setCannon(Cannon cannon) {
        this.cannon = cannon;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
