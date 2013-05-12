package at.pavlov.Cannons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.container.SimpleBlock;
import at.pavlov.Cannons.utils.CannonsUtil;

public class CannonManager
{
	private ArrayList<Cannon> cannonList = new ArrayList<Cannon>();

	private Cannons plugin;
	private UserMessages userMessages;
	private Config config;

	public CannonManager(Cannons cannons, UserMessages userMessages, Config config)
	{
		this.userMessages = userMessages;
		this.config = config;
		this.plugin = cannons;
	}

	/**
	 * removes a cannons from the list that are not valid
	 * @param cannon
	 */
	public void removeInvalidCannons()
	{
		for (int i=0; i < cannonList.size(); i++)
		{
			Cannon cannon = cannonList.get(i);
			if (!cannon.isValid())
			{
				removeCannon(cannon);
				i--;
			}
		}
	}

	/**
	 * removes a cannon from the list
	 * @param cannon
	 */
	public void removeCannon(Location loc)
	{
		Cannon cannon = getCannon(loc, null);
		removeCannon(cannon);
	}

	/**
	 * removes a cannon from the list
	 * @param cannon
	 */
	public void removeCannon(Cannon cannon)
	{
		if (cannon != null)
		{
			// send message to the owner
			Player player = null;
			if (cannon.getOwner() != null)
			{
				player = Bukkit.getPlayer(cannon.getOwner());
			}

			// destroy cannon (drops items, edit sign)
			MessageEnum message = cannon.destroyCannon();
			
			if (player != null) userMessages.displayMessage(player, message, cannon);

			//remove from database
			plugin.getPersistenceDatabase().deleteCannon(cannon);
			
			//remove from list
			cannonList.remove(cannon);
		}
	}

	/**
	 * Checks if the name of a cannon is unique
	 * 
	 * @param name
	 * @return true if the name is unique
	 */
	private boolean isCannonNameUnique(String name, String owner)
	{
		for (Cannon cannon : cannonList)
		{
			if (cannon.getCannonName() != null && name != null)
			{
				if (cannon.getCannonName().equals(name))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * generates a new unique cannon name
	 * @return
	 */
	private String newCannonName(Cannon cannon)
	{		
		//check if this cannon has a owner
		if (cannon.getOwner() == null) return "missing Owner";
			
		String name;
		CannonDesign design = cannon.getCannonDesign();
		if (design != null)
			name = design.getDesignName();
		else
			name = "cannon";
	

		for (int i = 1; i < Integer.MAX_VALUE; i++)
		{
			String cannonName = name + " " + i;

			if (isCannonNameUnique(cannonName, cannon.getOwner()) == true)
			{
				return cannonName;
			}
		}

		return "no unique name";
	}

	/**
	 * adds a new cannon to the list of cannons
	 * @param cannon
	 * @param owner
	 */
	public void createCannon(Cannon cannon)
	{
		//the owner can't be null
		if (cannon.getOwner() == null) 
		{
			plugin.logInfo("can't save a cannon when the owner is null");
			return;
		}
		
		//if the cannonName is empty make a new one
		if (cannon.getCannonName() ==  null || cannon.getCannonName() == "")
			cannon.setCannonName(newCannonName(cannon));
		
		
		// add cannon to the list
		cannonList.add(cannon);
		plugin.getPersistenceDatabase().saveCannon(cannon);
		plugin.logDebug("added cannon to the list");
		
		cannon.updateCannonSigns();
		
		return ;
	}

	/**
	 * get cannon by cannonName and Owner - used for Signs
	 * @param cannonName
	 * @return
	 */
	public Cannon getCannonFromStorage(String cannonName, String owner)
	{
		if (cannonName == null || owner == null) return null;

		for (Cannon cannon : cannonList)
		{
			if (cannonName.equals(cannon.getCannonName()))
			{
				if (owner.equals(cannon.getOwner()))
				{
					return cannon;
				}
			}

		}
		return null;
	}

	/**
	 * Searches the storage if there is already a cannonblock on this location
	 * and returns the cannon
	 * @param loc
	 * @return
	 */
	private Cannon getCannonFromStorage(Location loc)
	{
		for (Cannon cannon : cannonList)
		{
			if (cannon.isCannonBlock(loc.getBlock()))
			{
				return cannon;
			}
		}
		return null;
	}
	
	/**
	 * searches for a cannon and creates a new entry if it does not exist
	 * 
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
	public Cannon getCannon(Location cannonBlock, String owner)
	{
		return getCannon(cannonBlock, owner, false);
	}
	
	/**
	 * searches for a cannon and creates a new entry if it does not exist
	 * 
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
	public Cannon getCannon(Location cannonBlock, String owner, boolean silent)
	{
		Cannon cannon = getCannonFromStorage(cannonBlock);

		// this block is a part of an existing cannon
		if (cannon != null)
		{
			plugin.logDebug("found cannon in the storage");
			// search cannon that is written on the sign
			// Cannon cannonFromSign =
			// getCannonFromStorage(cannon.getCannonNameFromSign(design),
			// cannon.getOwnerFromSign(design));

			// check if the name matches with the attached sign or if the name
			// is not valid
			// if (cannonFromSign == null || cannon.isCannonEqualSign(design) ==
			// true )
			{
				return cannon;
			}
			// else
			// {
			// different cannon, search the database for the right entry by the
			// cannon name from the sign
			// return cannonFromSign;
			// }
		}
		// else
		{
			// no existing cannon in storage -> check if there is a cannon
			return checkCannon(cannonBlock, owner, silent);
		}

	}

	/**
	 * searches if this block is part of a cannon and create a new one
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
	public Cannon checkCannon(Location cannonBlock, String owner, boolean silent)
	{
		// get world
		World world = cannonBlock.getWorld();

		plugin.logDebug("check cannon");

		// check all cannon design if this block is part of the design
		for (CannonDesign cannonDesign : plugin.getDesignStorage().getCannonDesignList())
		{
			// check of all directions
			BlockFace cannonDirection = BlockFace.NORTH;
			for (int i = 0; i < 4; i++)
			{
				// for all blocks for the design
				List<SimpleBlock> designBlockList = cannonDesign.getAllCannonBlocks(cannonDirection);
				for (SimpleBlock designBlock : designBlockList)
				{
					// compare blocks
					if (designBlock.compareBlockFuzzy(cannonBlock.getBlock()))
					{
						// this block is same as in the design, get the offset
						Vector offset = designBlock.substractInverted(cannonBlock).toVector();

						// check all other blocks of the cannon
						boolean isCannon = true;
						for (SimpleBlock checkBlocks : designBlockList)
						{
							if (!checkBlocks.compareBlockFuzzy(world, offset))
							{
								// if the block does not match this is not the
								// right one
								isCannon = false;
								break;
							}
						}

						// this is a cannon
						if (isCannon == true)
						{
							// make a new cannon if there is no sign on the
							// cannon
							Cannon cannon = new Cannon(cannonDesign, world.getName(), offset, cannonDirection, owner);
							
							//can this player can build one more cannon
							MessageEnum	message = canBuildCannon(cannon, owner);
							
							//check the permissions for redstone
							if (message == null || message == MessageEnum.CannonCreated)
								message = cannon.checkRedstonePermission(owner);
							
							//send messages
							if (!silent)
								userMessages.displayMessage(owner, message, cannon);
							
							//add cannon to the list if everything was fine and return the cannon
							if (message != null && message == MessageEnum.CannonCreated)
							{
								createCannon(cannon);
								return cannon;
							}
						}
					}
				}

				// rotate cannon direction
				cannonDirection = CannonsUtil.roatateFace(cannonDirection);
			}

		}

		return null;
	}

	/**
	 * returns the number of owned cannons of a player
	 * 
	 * @param player
	 * @return
	 */
	public int getNumberOfCannons(String player)
	{
		int i = 1;
		for (Cannon cannon : cannonList)
		{
			if (cannon.getOwner() == null)
			{
				plugin.logSevere("Cannon has no owner. Contact the plugin developer");
			}
			else if (cannon.getOwner().equalsIgnoreCase(player))
			{
				i++;
			}
		}
		return i;
	}

	/**
	 * 
	 * @return List of cannons
	 */
	public List<Cannon> getCannonList()
	{
		return cannonList;
	}
	
	/**
	 * 
	 * @return List of cannons
	 */
	public void clearCannonList()
	{
		cannonList.clear();
	}

	/**
	 * returns the number of cannons manged by the plugin
	 * 
	 * @return
	 */
	public int getcannonListSize()
	{
		return cannonList.size();
	}

	/**
	 * returns the amout of cannons a player can build
	 * 
	 * @param player
	 * @return
	 */
	public int getCannonBuiltLimit(Player player)
	{
		// the player is not valid - no limit check
		if (player == null) return Integer.MAX_VALUE;
		if (player.getName() == null) return Integer.MAX_VALUE;

		// both limitA/B and cannons.player.limit.5 work
		// if all notes are enabled, set limit to a high number. If no permission plugin is loaded, everything is enabled
		int newBuiltLimit = -1;
		if (player.hasPermission("cannons.player.limit." + Integer.MAX_VALUE))
		{
			newBuiltLimit = Integer.MAX_VALUE;
		}
		else
		{
			// else check all nodes for the player
			for (int i = 100; i >= 0; i--)
			{
				if (player.hasPermission("cannons.player.limit." + i))
				{
					newBuiltLimit = i;
					break;
				}
			}
		}
		plugin.logDebug("Build limit " + newBuiltLimit); 


		plugin.logDebug("Build limit neu" + newBuiltLimit); 
		


		// config implementation
		if (newBuiltLimit == -1 && config.isBuildLimitEnabled())
		{
			if (player.hasPermission("cannons.player.limitB"))
			{
				// return the
				return config.getBuildLimitB();
			}
			// limit B is stronger
			else if (player.hasPermission("cannons.player.limitA"))
			{
				return config.getBuildLimitA();
			}
		}
		// player implementation
		else
		{
			plugin.logDebug("limit return: " + newBuiltLimit);
			return newBuiltLimit;
		}
		return Integer.MAX_VALUE;
	}
	/**
	 * checks if the player can build a cannon (permission, builtLimit)
	 * 
	 * @param cannon
	 * @param player
	 * @return
	 */
	private MessageEnum canBuildCannon(Cannon cannon, String owner)
	{
		CannonDesign design = cannon.getCannonDesign();
		
		//get the player from the server
		if (owner == null) return null;
		Player player = Bukkit.getPlayer(owner);
		if (player == null) return null;
	
		// check if player has permission to build
		if (!player.hasPermission(design.getPermissionBuild()))
		{
			return MessageEnum.PermissionErrorBuild;
		}
		// player does not have too many guns
		if (getNumberOfCannons(owner) > getCannonBuiltLimit(player))
		{
			return MessageEnum.ErrorCannonBuiltLimit;
		}
		// player has sufficient permission to build a cannon
		return MessageEnum.CannonCreated;
	}


	/**
	 * Deletes all cannons of this player in the database to reset the cannon
	 * limit
	 * 
	 * @param player
	 */
	public void deleteCannons(String owner)
	{
		Iterator<Cannon> iter = cannonList.iterator();

		while (iter.hasNext())
		{
			Cannon next = iter.next();
			if (next.getOwner().equals(owner))
			{
				next.destroyCannon();
				iter.remove();
			}
		}
	}



}