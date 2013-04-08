package at.pavlov.Cannons.cannon;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.container.SimpleBlock;
import at.pavlov.Cannons.enums.MessageEnum;
import at.pavlov.Cannons.inventory.InventoryManagement;
import at.pavlov.Cannons.projectile.Projectile;
import at.pavlov.Cannons.sign.CannonSign;
import at.pavlov.Cannons.utils.CannonsUtil;

public class Cannon
{
	// Database id - is 0 until stored in the database. Then it is the id in the
	// database
	private int databaseId;
	private int designID;
	private String cannonName;
	// direction the cannon is facing
	private BlockFace cannonDirection;
	// the location is describe by the offset of the cannon and the design
	private Vector offset;
	// world of the cannon
	private String world;
	// time the cannon was last time fired
	private long LastFired;
	private int loadedGunpowder;
	//the loaded projectile - can be null
	private Projectile loadedProjectile;
	private int projectileID;
	private int projectileData;
	private double horizontalAngle;
	private double verticalAngle;
	// player who has build this cannon
	private String owner;
	// designID of the cannon, for different types of cannons - not in use
	private boolean isValid;

	CannonDesign design;

	public Cannon(CannonDesign design, String world, Vector cannonOffset, BlockFace cannonDirection, String owner)
	{
		this.design = design;
		this.designID = design.getDesignID();
		this.world = world;
		this.offset = cannonOffset;
		this.cannonDirection = cannonDirection;
		this.owner = owner;
		this.isValid = true;
	}

	/**
	 * removes the loaded charge form the chest attached to the cannon
	 * 
	 * @param cannon
	 * @return
	 */
	public boolean removeAmmoFromChests()
	{
		// create a new projectile stack with one projectile
		ItemStack projectile = CannonsUtil.newItemStack(projectileID, projectileData, 1);

		// gunpowder stack
		ItemStack powder = design.getGunpowderType().toItemStack(loadedGunpowder);

		// For all possible chest locations
		// update all possible sign locations
		for (Location chestLoc : design.getChestsAndSigns(this))
		{
			if (InventoryManagement.removeAmmoFromChest(chestLoc.getBlock(), powder, projectile)) return true;
		}
		return false;
	}

	/**
	 * loads Gunpowder in a cannon
	 * 
	 * @param userMessages
	 * @param player
	 */
	public MessageEnum loadGunpowder(Player player)
	{
		MessageEnum returnVal = CheckPermGunpowder(player);

		// player has permission and the gunpowder is loaded successfully
		if (returnVal.equals(MessageEnum.loadGunpowder))
		{
			loadedGunpowder += 1;

			// take item from the player
			if (!design.isAmmoInfiniteForPlayer()) InventoryManagement.TakeFromPlayerInventory(player);

			// update Signs
			updateCannonSigns();
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
		MessageEnum returnVal = CheckPermProjectile(projectile, player);

		// check if loading of projectile was successful
		if (returnVal.equals(MessageEnum.loadProjectile))
		{
			ItemStack itemInHand = player.getItemInHand();
			// load projectile
			projectileID = itemInHand.getTypeId();
			projectileData = itemInHand.getData().getData();

			// remove from player
			if (!design.isAmmoInfiniteForPlayer()) InventoryManagement.TakeFromPlayerInventory(player);

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
	 * @param cannon
	 *            check if this cannon can be loaded
	 * @return true if the cannon can be loaded
	 */
	private MessageEnum CheckPermGunpowder(Player player)
	{

		// already loaded
		if (isLoaded())
		{
			return MessageEnum.ErrorProjectileAlreadyLoaded;
		}
		// maximum loaded gunpowder
		if (getLoadedGunpowder() >= design.getMaxLoadableGunpowder())
		{
			return MessageEnum.ErrorMaximumGunpowderLoaded;
		}
		// player can't load cannon
		if (player.hasPermission(design.getPermissionLoad()) == false)
		{
			return MessageEnum.PermissionErrorLoad;
		}
		// loading successful
		return MessageEnum.loadGunpowder;
	}

	/**
	 * Check the if the cannons can be loaded
	 * 
	 * @param player
	 *            whose permissions are checked
	 * @param cannon
	 *            cannon to check
	 * @return true if the player and cannons can load the projectile
	 */
	private MessageEnum CheckPermProjectile(Projectile projectile, Player player)
	{
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
		// no permission to load
		if (player.hasPermission(design.getPermissionLoad()) == false)
		{
			return MessageEnum.PermissionErrorLoad;
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
		return (projectileID == Material.AIR.getId()) ? false : true;
	}

	/**
	 * removes gunpowder and the projectile. Items are drop at the cannonball
	 * exit point
	 */
	private void dropCharge()
	{
		if (loadedGunpowder > 0)
		{
			ItemStack powder = design.getGunpowderType().toItemStack(loadedGunpowder);
			getWorldBukkit().dropItemNaturally(design.getMuzzle(this), powder);
		}

		// can't drop Air
		if (projectileID > 0)
		{
			ItemStack projectile = new ItemStack(projectileID, 1, (short) projectileData);
			getWorldBukkit().dropItemNaturally(design.getMuzzle(this), projectile);
		}

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
		
		//return message
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
	 * return true if this block is a part of the loading interface - default is
	 * the barrel
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
	 * checks if the player has permission to use the cannon with redstone
	 * @return
	 */
	public MessageEnum checkRedstonePermission(Player player)
	{
		//the player is null means he is offline -> automatic handling like database check
		if (player == null) return MessageEnum.CannonCreated;
		//if the player has the permission to use redstone return 
		if (player.hasPermission(design.getPermissionRedstone())) return MessageEnum.CannonCreated;
		
		// torch
		for (Location loc : design.getRedstoneTorches(this))
		{
			Material b = loc.getBlock().getType();
			if (b == Material.REDSTONE_TORCH_ON || b == Material.REDSTONE_TORCH_OFF)
			{
				return MessageEnum.PermissionErrorRedstone;
			}
		}

		// wire
		for (Location loc : design.getRedstoneWireAndRepeater(this))
		{
			Material b = loc.getBlock().getType();
			if (b == Material.REDSTONE_WIRE || b == Material.DIODE || b == Material.DIODE_BLOCK_ON || b == Material.DIODE_BLOCK_OFF)
			{
				return MessageEnum.PermissionErrorRedstone;
			}
		}

		// no redstone wiring found
		return MessageEnum.CannonCreated;
	}

	/**
	 * return the firing vector of the cannon
	 * 
	 * @param plugin
	 * @return
	 */
	public Vector getFiringVector(Config config)
	{
		// CannonDesign design = plugin.getDesignStorage().getDesign(this);
		// Config config =

		// get projectile
		Projectile proj = config.getProjectile(projectileID, projectileData);

		// set direction of the snowball
		Vector vect = new Vector(1f, 0f, 0f);
		Random r = new Random();

		double deviation = r.nextGaussian() * design.getSpreadOfCannon();
		if (proj.canisterShot) deviation += r.nextGaussian() * proj.spreadCanisterShot;
		double horizontal = Math.sin((horizontalAngle + deviation) * Math.PI / 180);

		deviation = r.nextGaussian() * design.getSpreadOfCannon();
		if (proj.canisterShot) deviation += r.nextGaussian() * proj.spreadCanisterShot;
		double vertical = Math.sin((verticalAngle + deviation) * Math.PI / 180);

		if (cannonDirection.equals(BlockFace.WEST))
		{
			vect = new Vector(-1.0f, vertical, -horizontal);
		}
		else if (cannonDirection.equals(BlockFace.NORTH))
		{
			vect = new Vector(horizontal, vertical, -1.0f);
		}
		else if (cannonDirection.equals(BlockFace.EAST))
		{
			vect = new Vector(1.0f, vertical, horizontal);
		}
		else if (cannonDirection.equals(BlockFace.SOUTH))
		{
			vect = new Vector(-horizontal, vertical, 1.0f);
		}

		double multi = proj.max_speed * design.getMultiplierVelocity() * (loadedGunpowder / design.getMaxLoadableGunpowder());
		if (multi < 0.1) multi = 0.1;

		return vect.multiply(multi);
	}

	/**
	 * updates all signs that are attached to a cannon
	 */
	public void updateCannonSigns()
	{
		// update all possible sign locations
		for (Location signLoc : design.getChestsAndSigns(this))
		{
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
		if (block.getType() != Material.WALL_SIGN) return;

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
			sign.setLine(0, "cannon");
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
				return "p: " + loadedGunpowder + " c: " + projectileID + ":" + projectileData;
			case 3 :
				// angles
				return horizontalAngle + "/" + verticalAngle;
		}
		return "missing";
	}

	/**
	 * returns false if the name of the cannon and the name on the sign are
	 * different
	 * 
	 * @return
	 */
	public boolean isCannonEqualSign()
	{
		// update all possible sign locations
		for (Location signLoc : design.getChestsAndSigns(this))
		{
			// if one sign is not equal, the there is a problem
			if (isCannonEqualThisSign(signLoc.getBlock())) return false;
		}

		return true;
	}

	/**
	 * extracts the cannon name and owner from the sign and comperes to the
	 * cannon
	 * 
	 * @param block
	 * @return
	 */
	private boolean isCannonEqualThisSign(Block block)
	{
		if (block.getType() != Material.WALL_SIGN) return true;

		Sign sign = (Sign) block.getState();

		// sign is empty
		if (sign.getLine(0) == null || sign.getLine(1) == null) return true;

		// sign name and owner are the same
		if (sign.getLine(0).equals(cannonName) && sign.getLine(1).equals(owner)) return true;

		return false;
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
			if (lineStr != "") return lineStr;
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

	public int getDesignID()
	{
		return designID;
	}

	public void setDesignID(int designID)
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
		return LastFired;
	}

	public void setLastFired(long lastFired)
	{
		LastFired = lastFired;
	}

	public int getLoadedGunpowder()
	{
		return loadedGunpowder;
	}

	public void setLoadedGunpowder(int loadedGunpowder)
	{
		this.loadedGunpowder = loadedGunpowder;
	}

	public int getProjectileID()
	{
		return projectileID;
	}

	public void setProjectileID(int projectileID)
	{
		this.projectileID = projectileID;
	}

	public int getProjectileData()
	{
		return projectileData;
	}

	public void setProjectileData(int projectileData)
	{
		this.projectileData = projectileData;
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
}
