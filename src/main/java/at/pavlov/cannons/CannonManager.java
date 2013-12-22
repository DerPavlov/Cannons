package at.pavlov.cannons;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.container.SimpleBlock;
import at.pavlov.cannons.event.CannonAfterCreateEvent;
import at.pavlov.cannons.event.CannonBeforeCreateEvent;

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
	 * @param loc
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
			plugin.getPersistenceDatabase().deleteCannonAsync(cannon);
			
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
			name = "at/pavlov/cannons/cannon";
	

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

        plugin.getPersistenceDatabase().saveCannonAsync(cannon);
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
        //long startTime = System.nanoTime();

        //check if there is a cannon at this location
        Cannon cannon = checkCannon(cannonBlock, owner);

        //if there is no cannon, exit
        if (cannon == null)
            return null;

        // search cannon that is written on the sign
        Cannon cannonFromSign = getCannonFromStorage(cannon.getCannonNameFromSign(), cannon.getOwnerFromSign());

        // if there is a different name on the cannon sign we use that one
        if (cannonFromSign != null)
        {
            plugin.logDebug("use entry from cannon sign");
            //update the position of the cannon
            cannonFromSign.setCannonDirection(cannon.getCannonDirection());
            cannonFromSign.setOffset(cannon.getOffset());
            //use the updated object from the storage
            cannon = cannonFromSign;
        }
        else
        {
            // this cannon has no sign, so look in the database if there is something
            Cannon storageCannon =  getCannonFromStorage(cannonBlock);
            if (storageCannon != null)
            {
                //try to find something in the storage
                plugin.logDebug("cannon found in storage");
                cannon = storageCannon;
            }
            //nothing in the storage, so we make a new entry
            else
            {
                //search for a player, because owner == null is not valid
                if (owner == null) return null;
                Player player = Bukkit.getPlayer(owner);

                //can this player can build one more cannon
                MessageEnum	message = canBuildCannon(cannon, owner);

                //check the permissions for redstone
                if (message == null || message == MessageEnum.CannonCreated)
                    message = cannon.checkRedstonePermission(owner);

                //if a sign is required to operate the cannon, there must be at least one sign
                if (message == MessageEnum.CannonCreated && (cannon.getCannonDesign().isSignRequired() && !cannon.hasCannonSign()))
                    message = MessageEnum.ErrorMissingSign;

                //send messages
                if (!silent)
                    userMessages.displayMessage(owner, message, cannon);

                CannonBeforeCreateEvent cbceEvent = new CannonBeforeCreateEvent(cannon, message, player);
                Bukkit.getServer().getPluginManager().callEvent(cbceEvent);


                //add cannon to the list if everything was fine and return the cannon
                if (!cbceEvent.isCancelled() && cbceEvent.getMessage() != null && cbceEvent.getMessage() == MessageEnum.CannonCreated)
                {

                    plugin.logDebug("a new cannon was create by " + cannon.getOwner());
                    createCannon(cannon);

                    CannonAfterCreateEvent caceEvent = new CannonAfterCreateEvent(cannon, player);
                	Bukkit.getServer().getPluginManager().callEvent(caceEvent);
               }
                else
                {
                    plugin.logDebug("missing permission while creating a cannon: " + message);
                    return null;
                }
            }
        }

        //plugin.logDebug("Time to find cannon: " + new DecimalFormat("0.00").format((System.nanoTime() - startTime)/1000000.0) + "ms");

        return cannon;
	}

	/**
	 * searches if this block is part of a cannon and create a new one
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
	public Cannon checkCannon(Location cannonBlock, String owner)
	{
		// get world
		World world = cannonBlock.getWorld();


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
						Vector offset = designBlock.subtractInverted(cannonBlock).toVector();

						// check all other blocks of the cannon
						boolean isCannon = true;
                        //check for empty entries
                        if (designBlockList.size() == 0)
                        {
                            isCannon = false;
                            plugin.logSevere("There are empty cannon design schematics in your design folder. Please check it.");
                        }
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
                           // cannon
							return new Cannon(cannonDesign, world.getName(), offset, cannonDirection, owner);
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
	 * returns the amount of cannons a player can build
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


		// config implementation
		if (config.isBuildLimitEnabled())
		{
			if (player.hasPermission("cannons.player.limitB") && (newBuiltLimit > config.getBuildLimitB()))
			{
				// return the
                plugin.logDebug("build limitB sets the number of cannons to: " + config.getBuildLimitB());
				return config.getBuildLimitB();
			}
			// limit B is stronger
			else if (player.hasPermission("cannons.player.limitA") && (newBuiltLimit > config.getBuildLimitA()))
			{
                plugin.logDebug("build limitA sets the number of cannons to: " + config.getBuildLimitA());
				return config.getBuildLimitA();
			}
		}
		// player implementation
		if (newBuiltLimit >= 0)
        {
            plugin.logDebug("permission build limit sets the maximum number of cannons to: " + newBuiltLimit);
            return newBuiltLimit;
		}
        plugin.logDebug("no build limit found. Setting to max value.");
        return Integer.MAX_VALUE;
	}
	/**
	 * checks if the player can build a cannon (permission, builtLimit)
	 * 
	 * @param cannon
	 * @param owner
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
	 * @param owner
     * @return returns true if there was an entry of this player in the list
	 */
	public boolean deleteCannons(String owner)
	{
		Iterator<Cannon> iter = cannonList.iterator();
        boolean inList = false;

		while (iter.hasNext())
		{
			Cannon next = iter.next();
			if (next.getOwner().equals(owner))
			{
                inList = true;
				next.destroyCannon();
				iter.remove();
			}
		}
        return inList;
	}



}