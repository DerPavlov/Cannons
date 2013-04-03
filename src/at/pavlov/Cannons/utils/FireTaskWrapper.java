package at.pavlov.Cannons.utils;

import org.bukkit.entity.Player;

import at.pavlov.Cannons.cannon.CannonData;


public class FireTaskWrapper{
	public CannonData cannon;
	public Player player;
	public boolean deleteCharge;
	
	public FireTaskWrapper(CannonData cannon, Player player, boolean deleteCharge)
	{
		this.cannon = cannon;
		this.player = player;
		this.deleteCharge = deleteCharge;
	}
	
}
