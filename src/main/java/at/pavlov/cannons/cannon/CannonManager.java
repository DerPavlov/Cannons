package at.pavlov.cannons.cannon;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.event.CannonDestroyedEvent;
import at.pavlov.cannons.utils.CannonsUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.container.SimpleBlock;
import at.pavlov.cannons.event.CannonAfterCreateEvent;
import at.pavlov.cannons.event.CannonBeforeCreateEvent;


public class CannonManager
{
	private final ConcurrentHashMap<UUID, Cannon> cannonList = new ConcurrentHashMap<UUID, Cannon>();
    private final ConcurrentHashMap<String, UUID> cannonNameMap = new ConcurrentHashMap<String, UUID>();


    private final Cannons plugin;
	private final UserMessages userMessages;
	private final Config config;



	public CannonManager(Cannons cannons, UserMessages userMessages, Config config)
	{
		this.userMessages = userMessages;
		this.config = config;
		this.plugin = cannons;
	}

	/**
	 * removes a cannons from the list that are not valid
     * @param cause the reason why the cannon is removed
	 */
	private void removeInvalidCannons(BreakCause cause)
	{
		Iterator<Cannon> iter = cannonList.values().iterator();

        while(iter.hasNext())
        {
            Cannon next = iter.next();
			if (!next.isValid())
			{
				removeCannon(next, false, cause, false, false);
                iter.remove();
			}
		}
	}

	/**
	 * removes a cannon from the list
	 * @param loc location of the cannon
     * @param breakCannon the cannon will explode and all cannon blocks will drop
     * @param cause the reason way the cannon was broken
	 */
	public void removeCannon(Location loc, boolean breakCannon, BreakCause cause)
	{
		Cannon cannon = getCannon(loc, null);
		removeCannon(cannon, breakCannon, cause);
	}

	/**
	 * removes a cannon from the list
	 * @param cannon cannon to remove
     * @param breakCannon the cannon will explode and all cannon blocks will drop
     * @param cause the reason way the cannon was broken
	 */
	public void removeCannon(Cannon cannon, boolean breakCannon, BreakCause cause)
	{
        removeCannon(cannon, breakCannon, cause, true, true);
	}

    /**
     * removes a cannon from the list
     * @param cannon cannon to remove
     * @param breakCannon the cannon will explode and all cannon blocks will drop
     * @param cause the reason way the cannon was broken
     * @param ignoreInvalid if true invalid cannons will be skipped and not removed
     */
    public void removeCannon(Cannon cannon, boolean breakCannon, BreakCause cause, boolean removeEntry, boolean ignoreInvalid)
    {
        //ignore invalid cannons
        if (cannon == null || (!cannon.isValid() && ignoreInvalid))
            return;

        // send message to the owner
        Player player = null;
        if (cannon.getOwner() != null)
        {
            player = Bukkit.getPlayer(cannon.getOwner());
        }

        //fire and an event that this cannon is destroyed
        CannonDestroyedEvent destroyedEvent = new CannonDestroyedEvent(cannon);
        Bukkit.getServer().getPluginManager().callEvent(destroyedEvent);

        // destroy cannon (drops items, edit sign)
        MessageEnum message = cannon.destroyCannon(breakCannon, cause);

        if (player != null)
            userMessages.displayMessage(player, cannon, message);

        //remove from database
        plugin.getPersistenceDatabase().deleteCannonAsync(cannon.getUID());
        //remove cannon name
        cannonNameMap.remove(cannon.getCannonName());
        //remove all entries for this cannon in the aiming class
        plugin.getAiming().removeCannon(cannon);

        //remove entry
        if (removeEntry)
            cannonList.remove(cannon.getUID());

    }

    /**
     * removes a cannon from the list
     * @param uid UID of the cannon
     * @param breakCannon the cannon will explode and all cannon blocks will drop
     * @param cause the reason way the cannon was broken
     */
    public void removeCannon(UUID uid, boolean breakCannon, BreakCause cause)
    {
        removeCannon(uid,breakCannon,cause,true);
    }

    /**
     * removes a cannon from the list
     * @param uid UID of the cannon
     * @param breakCannon the cannon will explode and all cannon blocks will drop
     * @param cause the reason way the cannon was broken
     * @param removeEntry should the cannon be removed from the list
     */
    public void removeCannon(UUID uid, boolean breakCannon, BreakCause cause, boolean removeEntry)
    {
        Cannon cannon = cannonList.get(uid);
        removeCannon(cannon,breakCannon,cause,removeEntry,true);
    }

	/**
	 * Checks if the name of a cannon is unique
	 * @param name name of the cannon
	 * @return true if the name is unique
	 */
	private boolean isCannonNameUnique(String name)
	{
        if (name == null)
            return false;

		// try to find this in the map
        //there is no such cannon name
        if (cannonNameMap.get(name)==null)
            return true;
        //there is such a cannon name
        else
            return false;
	}

	/**
	 * generates a new unique cannon name
	 * @return
	 */
	private String newCannonName(Cannon cannon)
	{		
		//check if this cannon has a owner
		if (cannon.getOwner() == null)
            return "missing Owner";
			
		String name;
		CannonDesign design = cannon.getCannonDesign();
		if (design != null)
			name = design.getDesignName();
		else
			name = "cannon";
	

		for (int i = 1; i < Integer.MAX_VALUE; i++)
		{
			String cannonName = name + " " + i;

			if (isCannonNameUnique(cannonName))
			{
				return cannonName;
			}
		}

		return "no unique name";
	}

    public MessageEnum renameCannon(Player player, Cannon cannon, String newCannonName)
    {
        Validate.notNull(player, "player must not be null");
        Validate.notNull(cannon, "cannon must not be null");

        //check some permissions
        if (!player.getName().equals(cannon.getOwner()))
            return MessageEnum.ErrorNotTheOwner;
        if (!player.hasPermission(cannon.getCannonDesign().getPermissionRename()))
            return MessageEnum.PermissionErrorRename;
        if (newCannonName == null || !isCannonNameUnique(newCannonName))
            return MessageEnum.CannonRenameFail;

        //put the new name
        cannon.setCannonName(newCannonName);
        cannon.updateCannonSigns();

        return MessageEnum.CannonRenameSuccess;

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
		if (cannon.getCannonName() ==  null || cannon.getCannonName().equals(""))
			cannon.setCannonName(newCannonName(cannon));
		
		
		// add cannon to the list
		cannonList.put(cannon.getUID(), cannon);
        //add cannon name to the list
        cannonNameMap.put(cannon.getCannonName(), cannon.getUID());

        plugin.getPersistenceDatabase().saveCannonAsync(cannon);
        plugin.logDebug("added cannon to the list");
		
		cannon.updateCannonSigns();

        //make some sounds
        Player player = Bukkit.getPlayer(cannon.getOwner());
        if (player != null)
            player.getWorld().playSound(cannon.getMuzzle(), Sound.ANVIL_LAND, 1F, 0.5F);

        return ;
	}

    /**
     * returns all known cannons in a sphere around the given location
     * @param center - center of the box
     * @param sphereRadius - radius of the sphere in blocks
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannonsInSphere(Location center, double sphereRadius)
    {
        HashSet<Cannon> newCannonList = new HashSet<Cannon>();

        for (Cannon cannon : getCannonList().values())
        {
            Location newLoc = cannon.getCannonDesign().getBarrelBlocks(cannon).get(0);
            if (newLoc.distance(center) < sphereRadius)
                newCannonList.add(cannon);
        }
        return newCannonList;
    }

    /**
     * returns all known cannons in a box around the given location
     * @param center - center of the box
     * @param lengthX - box length in X
     * @param lengthY - box length in Y
     * @param lengthZ - box length in Z
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannonsInBox(Location center, double lengthX, double lengthY, double lengthZ)
    {
        HashSet<Cannon> newCannonList = new HashSet<Cannon>();

        for (Cannon cannon : getCannonList().values())
        {
            Location newLoc = cannon.getCannonDesign().getBarrelBlocks(cannon).get(0);
            Vector box = newLoc.subtract(center).toVector();
            if (cannon.getWorld().equals(center.getWorld().getName()) && Math.abs(box.getX())<lengthX/2 && Math.abs(box.getY())<lengthY/2 && Math.abs(box.getZ())<lengthZ/2)
                newCannonList.add(cannon);
        }
        return newCannonList;
    }

    /**
     * returns all cannons for a list of locations
     * @param locations - a list of location to search for cannons
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannonsByLocations(List<Location> locations)
    {
        HashSet<Cannon> newCannonList = new HashSet<Cannon>();
        for (Cannon cannon : getCannonList().values())
        {
            for (Location loc : locations)
            {
                if (cannon.isCannonBlock(loc.getBlock()))
                    newCannonList.add(cannon);
            }

        }
        return newCannonList;
    }

    /**
     * returns all cannons for a list of locations - this will update all locations
     * @param locations - a list of location to search for cannons
     * @param player - player which operates the cannon
     * @param silent - no messages will be displayed if silent is true
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannons(List<Location> locations, String player, boolean silent)
    {
        HashSet<Cannon> newCannonList = new HashSet<Cannon>();
        for (Location loc : locations)
        {
            Cannon newCannon = getCannon(loc, player, silent);
            if (newCannon != null)
            {
                newCannonList.add(newCannon);
            }
        }

        return newCannonList;
    }

	/**
	 * get cannon by cannonName and Owner - used for Signs
	 * @param cannonName
	 * @return
	 */
	public Cannon getCannon(String cannonName)
	{
		if (cannonName == null) return null;

        UUID uid = cannonNameMap.get(cannonName);
        if (uid != null)
		    return cannonList.get(uid);

        //cannon not found
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
		for (Cannon cannon : cannonList.values())
		{
			if (/*:*/loc.toVector().distance(cannon.getOffset()) <= 32 /*To make code faster on servers with a lot of cannons */ && cannon.isCannonBlock(loc.getBlock()))
			{
				return cannon;
			}
		}
		return null;
	}

	/**
	 * searches for a cannon and creates a new entry if it does not exist
	 * 
	 * @param cannonBlock - one block of the cannon
	 * @param owner - the owner of the cannon (important for message notification). Can't be null
	 * @return
	 */
	public Cannon getCannon(Location cannonBlock, String owner)
	{
		return getCannon(cannonBlock, owner, false);
	}
	
	/**
	 * searches for a cannon and creates a new entry if it does not exist
	 * 
	 * @param cannonBlock - one block of the cannon
	 * @param owner - the owner of the cannon (important for message notification). Can't be null
	 * @return the cannon at this location
	 */
	public Cannon getCannon(Location cannonBlock, String owner, boolean silent)
	{
        long startTime = System.nanoTime();

        //check if there is a cannon at this location
        Cannon cannon = checkCannon(cannonBlock, owner);

        //if there is no cannon, exit
        if (cannon == null)
            return null;

        // search cannon that is written on the sign
        Cannon cannonFromSign = getCannon(cannon.getCannonNameFromSign());

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


                CannonBeforeCreateEvent cbceEvent = new CannonBeforeCreateEvent(cannon, message, player);
                Bukkit.getServer().getPluginManager().callEvent(cbceEvent);


                //add cannon to the list if everything was fine and return the cannon
                if (!cbceEvent.isCancelled() && cbceEvent.getMessage() != null && cbceEvent.getMessage() == MessageEnum.CannonCreated)
                {

                    plugin.logDebug("a new cannon was created by " + cannon.getOwner());
                    createCannon(cannon);

                    //send messages
                    if (!silent)
                        userMessages.displayMessage(owner, message, cannon);

                    CannonAfterCreateEvent caceEvent = new CannonAfterCreateEvent(cannon, player);
                	Bukkit.getServer().getPluginManager().callEvent(caceEvent);
               }
                else
                {
                    plugin.logDebug("Creating a cannon event was canceled by a plugin: " + message);
                    return null;
                }
            }
        }

        plugin.logDebug("Time to find cannon: " + new DecimalFormat("0.00").format((System.nanoTime() - startTime)/1000000.0) + "ms");

        return cannon;
	}

    /**
     * returns the cannon from the storage
     * @param uid UUID of the cannon
     * @return the cannon from the storage
     */
    public Cannon getCannon(UUID uid)
    {
        if (uid == null)
            return null;

        return cannonList.get(uid);
    }

	/**
	 * searches if this block is part of a cannon and create a new one
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
    private Cannon checkCannon(Location cannonBlock, String owner)
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
						if (isCannon)
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
		for (Cannon cannon : cannonList.values())
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
	public ConcurrentHashMap<UUID,Cannon> getCannonList()
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
    int getCannonBuiltLimit(Player player)
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
     * removes all cannon
     */
    public void deleteAllCannons()
    {
        Iterator<Cannon> iter = cannonList.values().iterator();

        while (iter.hasNext())
        {
            Cannon next = iter.next();
            next.destroyCannon(false, BreakCause.Other);
            iter.remove();
        }
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
		Iterator<Cannon> iter = cannonList.values().iterator();
        boolean inList = false;

		while (iter.hasNext())
		{
			Cannon next = iter.next();
			if (next.getOwner().equals(owner))
			{
                inList = true;
				next.destroyCannon(false, BreakCause.Other);
				iter.remove();
			}
		}
        return inList;
	}

    /**
     * reloads designs from the design list and updates all entries in the cannon
     */
    public void updateCannonDesigns()
    {
        for (Cannon cannon : cannonList.values())
        {
            cannon.setCannonDesign(plugin.getCannonDesign(cannon));
        }
    }



}