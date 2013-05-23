package at.pavlov.cannons.utils;

import org.bukkit.entity.Player;

import at.pavlov.cannons.cannon.Cannon;


public class FireTaskWrapper{
	public Cannon cannon;
	public Player player;
	public boolean deleteCharge;
	
	public FireTaskWrapper(Cannon cannon, Player player, boolean deleteCharge)
	{
		this.cannon = cannon;
		this.player = player;
		this.deleteCharge = deleteCharge;
	}
	
}
