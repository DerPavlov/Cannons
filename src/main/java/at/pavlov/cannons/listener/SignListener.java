package at.pavlov.cannons.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import at.pavlov.cannons.CannonManager;
import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.UserMessages;

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
			
			//get block which is the sign attached to
			BlockFace signFace = ((org.bukkit.material.Sign) s.getData()).getFacing();
			Block cannonBlock = block.getRelative(signFace.getOppositeFace());
			

			//get cannon from location and creates a cannon if not existing
	        Cannon cannon = cannonManager.getCannon(cannonBlock.getLocation(), event.getPlayer().getName());
			
	        //get cannon from the sign
			Cannon cannonFromSign = cannonManager.getCannonFromStorage(event.getLine(0), event.getLine(1));
			
			//if the sign is placed against a cannon - no problem
			//if the sign has the name of other cannon - change it
			if(cannon == null && cannonFromSign  != null)
			{
				//this sign is in conflict with cannons
				event.getPlayer().sendMessage(ChatColor.RED + "This sign is in conflict with cannons");
				event.setLine(0, "[Cannons]");
				event.setLine(1, "Player");
			}

            //if there is a cannon and the sign is mounted on the sign interface
			if (cannon != null && cannon.isCannonSign(block.getLocation()))
			{
				event.setLine(0, cannon.getSignString(0));
				event.setLine(1, cannon.getSignString(1));
				event.setLine(2, cannon.getSignString(2));
				event.setLine(3, cannon.getSignString(3));
			}
		}
	}
}

