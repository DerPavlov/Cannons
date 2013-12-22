package at.pavlov.cannons.cannon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import at.pavlov.cannons.config.ProjectileStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.container.SimpleBlock;
import at.pavlov.cannons.inventory.InventoryManagement;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.sign.CannonSign;

public class Cannon
{
	// Database id - is -1 until stored in the database. Then it is the id in the
	// database
	private int databaseId = -1;
	private String designID;
	private String cannonName;

	// direction the cannon is facing
	private BlockFace cannonDirection;
	// the location is describe by the offset of the cannon and the design
	private Vector offset;
	// world of the cannon
	private String world;

	// time the cannon was last time fired
	private long lastFired;
	// time it was last aimed
	private long lastAimed;

	// amount of loaded gunpowder
	private int loadedGunpowder;
	// the loaded projectile - can be null
	private Projectile loadedProjectile;

	// angles
	private double horizontalAngle;
	private double verticalAngle;

	// player who has build this cannon
	private String owner;
	// designID of the cannon, for different types of cannons - not in use
	private boolean isValid;
    //the player which has used the cannon last, important for firing with redstone button
    private String lastUser;


	CannonDesign design;

	
	// not saved in the database
	// redstone handling event. Last player that pressed the firing button is saved in this list for the next redstone event
	private String firingButtonActivator;
	

	public Cannon(CannonDesign design, String world, Vector cannonOffset, BlockFace cannonDirection, String owner)
	{
		
		this.design = design;
		this.designID = design.getDesignID();
		this.world = world;
		this.offset = cannonOffset;
		this.cannonDirection = cannonDirection;
		this.owner = owner;
		this.isValid = true;

        this.horizontalAngle = (design.getMaxHorizontalAngle()+design.getMinHorizontalAngle())/2.0;
        this.verticalAngle = (design.getMaxVerticalAngle()+design.getMinVerticalAngle())/2.0;

		// reset
		this.loadedGunpowder = 0;
		this.loadedProjectile = null;
		
		this.databaseId = -1;
	}

	/**
	 * removes the loaded charge form the chest attached to the cannon, returns true if the ammo was found in the chest
	 *
	 * @return
	 */
	public boolean reloadFromChests(Player player)
	{
        List<Inventory> invlist = getInventoryList();


        //load the maximum gunpowder possible (maximum amount that fits in the cannon or is in the chest)
        int toLoad = design.getMaxLoadableGunpowder() - getLoadedGunpowder();
        ItemStack gunpowder = design.getGunpowderType().toItemStack(toLoad);
        gunpowder = InventoryManagement.removeItemInChests(invlist, gunpowder);
        if (gunpowder.getAmount() == 0)
        {
            //there was enough gunpowder in the chest
            loadedGunpowder = design.getMaxLoadableGunpowder();
        }
        else
        {
            //not enough gunpowder, put it back
            gunpowder.setAmount(toLoad-gunpowder.getAmount()) ;
            InventoryManagement.addItemInChests(invlist, gunpowder);
        }


        // find a loadable projectile in the chests
        for (Inventory inv : invlist)
        {
            for (ItemStack item : inv.getContents())
            {
                //try to load it and see what happens
                Projectile projectile = ProjectileStorage.getProjectile(this, item);
                if (projectile == null)
                    continue;

                MessageEnum message = CheckPermProjectile(projectile, player);
                if (message == MessageEnum.loadProjectile)
                {
                    // everything went fine, so remove it from the chest remove projectile
                    loadedProjectile = projectile;

                    if (item.getAmount() == 1)
                    {
                        //last item removed
                        inv.removeItem(item);
                        break;
                    }
                    else
                    {
                        //remove one item
                        item.setAmount(item.getAmount() - 1);
                    }
                    return true;
                }
            }
        }

		return false;
	}


    /**
     *
     * @return returns the inventories of all attached chests
     */
    public List<Inventory> getInventoryList()
    {
        //get the inventories of all attached chests
        List<Inventory> invlist = new ArrayList<Inventory>();
        for (Location loc : getCannonDesign().getChestsAndSigns(this))
        {
            // check if block is a chest
            invlist = InventoryManagement.getInventories(loc.getBlock(), invlist);
        }
        return invlist;
    }

	
	/**
	 * loads Gunpowder in a cannon
	 * @param amountToLoad number of items which are loaded into the cannon
	 */
	public MessageEnum loadGunpowder(int amountToLoad)
	{
		// projectile already loaded
		if (isLoaded())
		{
			return MessageEnum.ErrorProjectileAlreadyLoaded;
		}
		// maximum gunpowder already loaded
		if (getLoadedGunpowder() >= design.getMaxLoadableGunpowder())
		{
			return MessageEnum.ErrorMaximumGunpowderLoaded;
		}
		
		//load the maximum gunpowder
		for (int i = 0; i < amountToLoad; i++)
		{
			if (getLoadedGunpowder() < design.getMaxLoadableGunpowder())
				loadedGunpowder += 1;
		}
		
		// update Signs
		updateCannonSigns();

		return MessageEnum.loadGunpowder;
	}
	
	
	/**
	 * checks the permission of a player before loading gunpowder in the cannon
	 * @param player
	 */
	public MessageEnum loadGunpowder(Player player)
	{
		//save the amount of gunpowder we loaded in the cannnon
		int gunpowder = 0;
		int maximumLoadable = design.getMaxLoadableGunpowder() - getLoadedGunpowder();
		
		//check if the player has permissions for this cannon
		MessageEnum returnVal = CheckPermGunpowder(player);
		
		//the player seems to have all rights to load the cannon.
		if (returnVal.equals(MessageEnum.loadGunpowder))
		{
			//if the player is sneaking the maximum gunpowder is loaded, but at least 1
			if (player.isSneaking())
			{
				//get the amount of gunpowder that can be maximal loaded
				gunpowder = player.getItemInHand().getAmount();
				if (gunpowder > maximumLoadable)
					gunpowder = maximumLoadable;
			}
			if (gunpowder <= 0)
				gunpowder = 1;
			
			//load the gunpowder
			returnVal = loadGunpowder(gunpowder);			
		}		

		// the cannon was loaded with gunpowder - lets get it form the player
		if (returnVal.equals(MessageEnum.loadGunpowder))
		{
			// take item from the player
			if (!design.isAmmoInfiniteForPlayer()) 
				InventoryManagement.TakeFromPlayerInventory(player, gunpowder);
		}
		return returnVal;

	}

	/**
	 * load the projectile in the cannon and checks permissions
	 * 
	 * @param player
	 * @return
	 */
	public MessageEnum loadProjectile(Projectile projectile, Player player)
	{
		if (projectile == null) return null;

		MessageEnum returnVal = CheckPermProjectile(projectile, player);

		// check if loading of projectile was successful
		if (returnVal.equals(MessageEnum.loadProjectile))
		{
			// load projectile
			loadedProjectile = projectile;

			// remove from player
			if (!design.isAmmoInfiniteForPlayer()) 
				InventoryManagement.TakeFromPlayerInventory(player,1);

			// update Signs
			updateCannonSigns();
		}
		return returnVal;
	}

	/**
	 * Check if cannons can be loaded with gunpowder by the player
	 * 
	 * @param player
	 *            check permissions of this player
	 * @return true if the cannon can be loaded
	 */
	private MessageEnum CheckPermGunpowder(Player player)
	{

		if (player != null)
		{
			//if the player is not the owner of this gun
			if (!this.getOwner().equals(player.getName()) && design.isAccessForOwnerOnly())
			{
				return MessageEnum.ErrorNotTheOwner;
			}
			// player can't load cannon
			if (player.hasPermission(design.getPermissionLoad()) == false)
			{
				return MessageEnum.PermissionErrorLoad;
			}
		}
		// loading successful
		return MessageEnum.loadGunpowder;
	}

	/**
	 * Check the if the cannons can be loaded
	 * 
	 * @param player
	 *            whose permissions are checked
	 * @return true if the player and cannons can load the projectile
	 */
	private MessageEnum CheckPermProjectile(Projectile projectile, Player player)
	{


		//if the player is not the owner of this gun
		if (player != null && !this.getOwner().equals(player.getName()) && design.isAccessForOwnerOnly())
		{
			return MessageEnum.ErrorNotTheOwner;
		}
		// already loaded with a projectile
		if (isLoaded())
		{
			return MessageEnum.ErrorProjectileAlreadyLoaded;
		}
		// no gunpowder loaded
		if (getLoadedGunpowder() == 0)
		{
			return MessageEnum.ErrorNoGunpowder;
		}
		if (player != null)
		{
			// no permission to load
			if (player.hasPermission(design.getPermissionLoad()) == false)
			{
				return MessageEnum.PermissionErrorLoad;
			}			
		}
		// no permission for this projectile
		if (!projectile.hasPermission(player))
		{
			return MessageEnum.PermissionErrorProjectile;
		}
		// loading successful
		return MessageEnum.loadProjectile;
	}

	/**
	 * is cannon loaded return true
	 * 
	 * @return
	 */
	public boolean isLoaded()
	{
		return (loadedProjectile != null);
	}

	/**
	 * removes gunpowder and the projectile. Items are drop at the cannonball
	 * exit point
	 */
	private void dropCharge()
	{
		removeCharge();
		if (loadedGunpowder > 0)
		{
			ItemStack powder = design.getGunpowderType().toItemStack(loadedGunpowder);
			getWorldBukkit().dropItemNaturally(design.getMuzzle(this), powder);
		}

		// can't drop Air
		if (isLoaded())
		{
			getWorldBukkit().dropItemNaturally(design.getMuzzle(this), loadedProjectile.getLoadingItem().toItemStack(1));
		}

	}
	
	/**
	 * removes the gunpowder and projectile loaded in the cannon
	 */
	public void removeCharge()
	{
		//delete charge for human gunner
		this.setLoadedGunpowder(0);
		this.setLoadedProjectile(null);
		
		//update Sign
		this.updateCannonSigns();
	}

	/**
	 * removes the sign text and charge of the cannon after destruction
	 */
	public MessageEnum destroyCannon()
	{
		// update cannon signs the last time
		isValid = false;
		updateCannonSigns();

		// drop charge
		dropCharge();

		// return message
		return MessageEnum.CannonDestroyed;
	}

	/**
	 * returns true if this block is a block of the cannon
	 * 
	 * @param block
	 * @return
	 */
	public boolean isCannonBlock(Block block)
	{
		for (SimpleBlock designBlock : design.getAllCannonBlocks(cannonDirection))
		{
			if (designBlock.compareBlockAndLocFuzzy(block, offset))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * return true if this block can be destroyed, false if it is protected
	 * the barrel the barrel
	 * 
	 * @param block
	 * @return
	 */
	public boolean isDestructibleBlock(Location block)
	{
		for (Location loc : design.getDestructibleBlocks(this))
		{
			if (loc.equals(block))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * return true if this block is a part of the loading interface - default is
	 * the barrel the barrel
	 * 
	 * @param block
	 * @return
	 */
	public boolean isLoadingBlock(Location block)
	{
		for (Location loc : design.getLoadingInterface(this))
		{
			if (loc.equals(block))
			{
				return true;
			}
		}
		return false;
	}


    /**
     * return true if this location where the torch interacts with the cannon
     *
     * @param block
     * @return
     */
    public boolean isChestInterface(Location block)
    {
        for (Location loc : design.getChestsAndSigns(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this location where the torch interacts with the cannon
     * does not check the ID
     *
     * @param loc
     * @return
     */
    public boolean isCannonSign(Location loc)
    {
        if (loc.getBlock().getType() != Material.WALL_SIGN) return false;

        CannonBlocks cannonBlocks  = this.getCannonDesign().getCannonBlockMap().get(this.getCannonDirection());
        if (cannonBlocks != null)
        {
            for (SimpleBlock cannonblock : cannonBlocks.getChestsAndSigns())
            {
                // compare location
                if (cannonblock.toLocation(this.getWorldBukkit(),this.offset).equals(loc))
                {
                    Block block = loc.getBlock();
                    //compare and data
                    //only the two lower bits of the bytes are important for the direction (delays are not interessting here)
                    if (cannonblock.getData() == block.getData() || block.getData() == -1 || cannonblock.getData() == -1 )
                        return true;
                }
            }
        }
        return false;
    }

	/**
	 * return true if this is a right click trigger block
	 * 
	 * @param block
	 * @return
	 */
	public boolean isRightClickTrigger(Location block)
	{
		for (Location loc : design.getRightClickTrigger(this))
		{
			if (loc.equals(block))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * return true if this is a redstone trigger block
	 * 
	 * @param block
	 * @return
	 */
	public boolean isRestoneTrigger(Location block)
	{
		for (Location loc : design.getRedstoneTrigger(this))
		{
			if (loc.equals(block))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * return true if this location where the torch interacts with the cannon
	 * 
	 * @param block
	 * @return
	 */
	public boolean isRedstoneTorchInterface(Location block)
	{
		for (Location loc : design.getRedstoneTorches(this))
		{
			if (loc.equals(block))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * return true if this location where the torch interacts with the cannon
	 * 
	 * @param block
	 * @return
	 */
	public boolean isRedstoneWireInterface(Location block)
	{
		for (Location loc : design.getRedstoneWireAndRepeater(this))
		{
			if (loc.equals(block))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * return true if this location where the torch interacts with the cannon
	 * does not check the ID
	 * 
	 * @param loc
	 * @return
	 */
	public boolean isRedstoneRepeaterInterface(Location loc)
	{		
		CannonBlocks cannonBlocks  = this.getCannonDesign().getCannonBlockMap().get(this.getCannonDirection());
    	if (cannonBlocks != null)
    	{
    		for (SimpleBlock cannonblock : cannonBlocks.getRedstoneWiresAndRepeater())
    		{
    			// compare location
    			if (cannonblock.toLocation(this.getWorldBukkit(),this.offset).equals(loc))
    			{
    				Block block = loc.getBlock();
    				//compare and data    					
    				//only the two lower bits of the bytes are important for the direction (delays are not interessting here)
    				if (cannonblock.getData() == block.getData() %4 || block.getData() == -1 || cannonblock.getData() == -1 )
    					return true;
    			}
    		}
    	}
    	return false;
	}

	/**
	 * returns true if the player has the permission to place redstone near a cannon.
	 * player = null will also return true
	 * @param player
	 * @return
	 */
	public MessageEnum checkRedstonePermission(String player)
	{
		Player playerBukkit = null;
		if (player != null) playerBukkit = Bukkit.getPlayer(player);
		return checkRedstonePermission(playerBukkit);
	}

	/**
	 * checks if the player has permission to use the cannon with redstone
	 * 
	 * @return
	 */
	public MessageEnum checkRedstonePermission(Player player)
	{
		// the player is null means he is offline -> automatic handling like
		// database check
		if (player == null) return MessageEnum.CannonCreated;
		// if the player has the permission to use redstone return
		if (player.hasPermission(design.getPermissionRedstone())) return MessageEnum.CannonCreated;

		// torch
		for (Location loc : design.getRedstoneTorches(this))
		{
			Material b = loc.getBlock().getType();
			if (b == Material.REDSTONE_TORCH_ON || b == Material.REDSTONE_TORCH_OFF)
			{
				removeRedstone();
				return MessageEnum.PermissionErrorRedstone;
			}
		}

		// wire
		for (Location loc : design.getRedstoneWireAndRepeater(this))
		{
			Material b = loc.getBlock().getType();
			if (b == Material.REDSTONE_WIRE || b == Material.DIODE || b == Material.DIODE_BLOCK_ON || b == Material.DIODE_BLOCK_OFF)
			{
				removeRedstone();
				return MessageEnum.PermissionErrorRedstone;
			}
		}

		// no redstone wiring found
		return MessageEnum.CannonCreated;
	}

	/**
	 * break all redstone connections to this cannon
	 */
	private void removeRedstone()
	{
		// torches
		for (Location loc : design.getRedstoneTorches(this))
		{
			Block block = loc.getBlock();
			if (block.getType() == Material.REDSTONE_TORCH_ON || block.getType() == Material.REDSTONE_TORCH_OFF)
			{
				block.breakNaturally();
			}
		}

		// wires and repeater
		for (Location loc : design.getRedstoneWireAndRepeater(this))
		{
			Block block = loc.getBlock();
			if (block.getType() == Material.REDSTONE_WIRE || block.getType() == Material.DIODE_BLOCK_ON || block.getType() == Material.DIODE_BLOCK_OFF)
			{
				block.breakNaturally();
			}
		}
	}

	/**
	 * return the firing vector of the cannon. The spread depends on the cannon, the projectile and the player
	 * 
	 * @param player
	 * @return
	 */
	public Vector getFiringVector(Player player)
	{
		// get projectile
		// set direction of the snowball
		Vector vect = new Vector(1f, 0f, 0f);
		Random r = new Random();
		
		double playerSpreadMultiplier = getPlayerSpreadMultiplier(player);

        double deviation = r.nextGaussian() * design.getSpreadOfCannon() * loadedProjectile.getSpreadMultiplier()*playerSpreadMultiplier;
        double azi = (horizontalAngle + deviation) * Math.PI / 180;

        deviation = r.nextGaussian() * design.getSpreadOfCannon() * loadedProjectile.getSpreadMultiplier()*playerSpreadMultiplier;
        double polar = (-design.getDefaultVerticalAngle() - verticalAngle + 90.0 + deviation)* Math.PI / 180;

        double hx = Math.sin(polar)*Math.sin(azi);
        double hy = Math.sin(polar)*Math.cos(azi);
        double hz = Math.cos(polar);

        System.out.println("azi " + horizontalAngle + " polar " + (-design.getDefaultVerticalAngle() - verticalAngle + 90.0) + " hx " + hx +  " hy " + hy +" hz " + hz);

		if (cannonDirection.equals(BlockFace.WEST))
		{
			vect = new Vector(-hy, hz, -hx);
		}
		else if (cannonDirection.equals(BlockFace.NORTH))
		{
			vect = new Vector(hx, hz, -hy);
		}
		else if (cannonDirection.equals(BlockFace.EAST))
		{
			vect = new Vector(hy, hz, hx);
		}
		else if (cannonDirection.equals(BlockFace.SOUTH))
		{
			vect = new Vector(-hx, hz, hy);
		}

		double multi = getCannonballVelocity();
		if (multi < 0.1) multi = 0.1;

		return vect.multiply(multi);
	}
	
	/**
	 * etracts the spreadMultiplier from the permissions
	 * @param player
	 * @return
	 */
	private double getPlayerSpreadMultiplier(Player player)
	{
		if (player == null) return 1.0;
		
		
		// only if the permissions system is enabled. If there are no permissions, everything is true.
		if (!player.hasPermission( this.getCannonDesign().getPermissionSpreadMultiplier() + "." + Integer.MAX_VALUE))
		{
			// search if there is a valid entry 
			for (int i = 1; i <= 10; i++)
			{
				if (player.hasPermission( this.getCannonDesign().getPermissionSpreadMultiplier() + "." + i)) 
				{
					return i/10.0;
				}
			}
			
		}
		
		//using default value
		return 1.0;
	}
	
	

	/**
	 * returns the speed of the cannonball depending on the cannon, projectile,
	 * loaded gunpowder the dependency on the gunpowder is (1-2^(-4*loaded/max))
	 * 
	 * @return
	 */
	public double getCannonballVelocity()
	{
		if (loadedProjectile == null || design == null) return 0.0;
		return loadedProjectile.getVelocity() * design.getMultiplierVelocity() * (1 - Math.pow(2, -4 * loadedGunpowder / design.getMaxLoadableGunpowder()));
	}

    /**
     * @return true if the cannons has a sign
     */
    public boolean hasCannonSign()
    {
        // search all possible sign locations
        for (Location signLoc : design.getChestsAndSigns(this))
        {
            if (signLoc.getBlock().getTypeId() == Material.WALL_SIGN.getId())
                return true;
        }
        return false;
    }

	/**
	 * updates all signs that are attached to a cannon
	 */
	public void updateCannonSigns()
	{
		// update all possible sign locations
		for (Location signLoc : design.getChestsAndSigns(this))
		{
            //check blocktype and orientation before updating sign.
            if (isCannonSign(signLoc))
			    updateSign(signLoc.getBlock());
		}
	}

	/**
	 * updates the selected sign
	 * 
	 * @param block
	 */
	private void updateSign(Block block)
	{


		Sign sign = (Sign) block.getState();

		if (isValid == true)
		{
			// Cannon name in the first line
			sign.setLine(0, getSignString(0));
			// Cannon owner in the second
			sign.setLine(1, getSignString(1));
			// loaded Gunpowder/Projectile
			sign.setLine(2, getSignString(2));
			// angles
			sign.setLine(3, getSignString(3));
		}
		else
		{
			// Cannon name in the first line
			sign.setLine(0, "this cannon is");
			// Cannon owner in the second
			sign.setLine(1, "damaged");
			// loaded Gunpowder/Projectile
			sign.setLine(2, "");
			// angles
			sign.setLine(3, "");
		}

		sign.update(true);
	}

	/**
	 * returns the strings for the sign
	 * 
	 * @param index
	 * @return
	 */
	public String getSignString(int index)
	{

		switch (index)
		{

			case 0 :
				// Cannon name in the first line
				if (cannonName == null) cannonName = "missing Name";
				return cannonName;
			case 1 :
				// Cannon owner in the second
				if (owner == null) owner = "missing Owner";
				return owner;
			case 2 :
				// loaded Gunpowder/Projectile
				if (loadedProjectile != null) return "p: " + loadedGunpowder + " c: " + loadedProjectile.toString();
				else return "p: " + loadedGunpowder + " c: " + "0:0";
			case 3 :
				// angles
				return horizontalAngle + "/" + verticalAngle;
		}
		return "missing";
	}




	/**
	 * returns the name of the cannon written on the sign
	 * 
	 * @return
	 */
	private String getLineOfCannonSigns(int line)
	{
		String lineStr = "";
		// goto the first cannon sign
		for (Location signLoc : design.getChestsAndSigns(this))
		{
			lineStr = CannonSign.getLineOfThisSign(signLoc.getBlock(), line);
			// if something is found return it
			if (lineStr != null && lineStr != "")
            {
                return lineStr;
            }
		}

		return lineStr;
	}

	/**
	 * returns the cannon name that is written on a cannon sign
	 * 
	 * @return
	 */
	public String getCannonNameFromSign()
	{
		return getLineOfCannonSigns(0);
	}

	/**
	 * returns the cannon owner that is written on a cannon sign
	 * 
	 * @return
	 */
	public String getOwnerFromSign()
	{
		return getLineOfCannonSigns(1);
	}

	/**
	 * returns true if cannon design for this cannon is found
	 * 
	 * @param cannonDesign
	 * @return
	 */
	public boolean equals(CannonDesign cannonDesign)
	{
		if (designID == cannonDesign.getDesignID()) return true;
		return false;
	}

    /**
     *
     * @param cannon
     * @return true if both cannons are equal
     */
    public boolean equals(Cannon cannon)
    {
        return (cannon.getCannonName().equals(this.cannonName) && cannon.getOwner().equals(this.owner)) ? true : false;
    }

	/**
	 * get bukkit world
	 * 
	 * @return
	 */
	public World getWorldBukkit()
	{
		if (this.world != null)
		{
			return Bukkit.getWorld(this.world);
			// return new Location(bukkitWorld, )
		}
		return null;
	}

	public int getDatabaseId()
	{
		return databaseId;
	}

	public void setDatabaseId(int databaseId)
	{
		this.databaseId = databaseId;
	}

	public String getDesignID()
	{
		return designID;
	}

	public void setDesignID(String designID)
	{
		this.designID = designID;
	}

	public String getCannonName()
	{
		return cannonName;
	}

	public void setCannonName(String name)
	{
		this.cannonName = name;
	}

	public BlockFace getCannonDirection()
	{
		return cannonDirection;
	}

	public void setCannonDirection(BlockFace cannonDirection)
	{
		this.cannonDirection = cannonDirection;
	}

	public String getWorld()
	{
		return world;
	}

	public void setWorld(String world)
	{
		this.world = world;
	}

	public long getLastFired()
	{
		return lastFired;
	}

	public void setLastFired(long lastFired)
	{
		this.lastFired = lastFired;
	}

	public int getLoadedGunpowder()
	{
		return loadedGunpowder;
	}

	public void setLoadedGunpowder(int loadedGunpowder)
	{
		this.loadedGunpowder = loadedGunpowder;
	}

	public double getHorizontalAngle()
	{
		return horizontalAngle;
	}

	public void setHorizontalAngle(double horizontalAngle)
	{
		this.horizontalAngle = horizontalAngle;
	}

	public double getVerticalAngle()
	{
		return verticalAngle;
	}

	public void setVerticalAngle(double verticalAngle)
	{
		this.verticalAngle = verticalAngle;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public boolean isValid()
	{
		return isValid;
	}

	public void setValid(boolean isValid)
	{
		this.isValid = isValid;
	}

	public Vector getOffset()
	{
		return offset;
	}

	public void setOffset(Vector offset)
	{
		this.offset = offset;
	}

	public void setCannonDesign(CannonDesign design)
	{
		this.design = design;
	}

	public CannonDesign getCannonDesign()
	{
		return this.design;
	}

	public Projectile getLoadedProjectile()
	{
		return loadedProjectile;
	}

	public void setLoadedProjectile(Projectile loadedProjectile)
	{
		this.loadedProjectile = loadedProjectile;
	}

	public String getFiringButtonActivator()
	{
		return firingButtonActivator;
	}

	public void setFiringButtonActivator(String firingButtonActivator)
	{
		this.firingButtonActivator = firingButtonActivator;
	}

	public long getLastAimed()
	{
		return lastAimed;
	}

	public void setLastAimed(long lastAimed)
	{
		this.lastAimed = lastAimed;
	}

    public String getLastUser() {
        return lastUser;
    }

    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }
}
