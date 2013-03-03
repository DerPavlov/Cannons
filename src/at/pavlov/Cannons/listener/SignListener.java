package at.pavlov.Cannons.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import at.pavlov.Cannons.CannonManager;
import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.CannonData;

public class SignListener implements Listener
{
	@SuppressWarnings("unused")
	private Config config;
	@SuppressWarnings("unused")
	private UserMessages userMessages;
	private Cannons plugin;
	private CannonManager cannonManager;

	
	public SignListener(Cannons plugin)
	{
		this.plugin = plugin;
		this.config = this.plugin.getmyConfig();
		this.userMessages = this.plugin.getmyConfig().getUserMessages();
		this.cannonManager = this.plugin.getCannonManager();
	}
	
	/**
	 * Sign place event
	 * @param event
	 */
	@EventHandler
	public void signChange(SignChangeEvent event)
	{
		if (event.getBlock().getType() == Material.WALL_SIGN)
		{
			Block block = event.getBlock();
			Sign s = (Sign) event.getBlock().getState();

			BlockFace signFace = ((org.bukkit.material.Sign) s.getData()).getFacing();
			Block cannonBlock = block.getRelative(signFace.getOppositeFace());
			

	        CannonData cannon = cannonManager.getCannon(cannonBlock.getLocation());
			if (cannon != null)
			{
				event.setLine(0, cannon.getSignString(0));
				event.setLine(1, cannon.getSignString(1));
				event.setLine(2, cannon.getSignString(2));
				event.setLine(3, cannon.getSignString(3));
			}
		}
	}
}
