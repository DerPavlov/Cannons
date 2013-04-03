package at.pavlov.Cannons.cannon;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.inventory.InventoryManagement;
import at.pavlov.Cannons.projectile.Projectile;
import at.pavlov.Cannons.utils.CannonsUtil;


public class Cannon
{
	// Database id - is 0 until stored in the database. Then it is the id in the database
	private int id;    
	private String name;
	// direction the cannon is facing
	private BlockFace cannonDirection;
	//world of the cannon
	private String world;
	// time the cannon was last time fired
	private long LastFired;
	private int loadedGunpowder;
	private int projectileID;
	private int projectileData;
	private double horizontalAngle;
	private double verticalAngle;
	//the location is describe by the offset of the cannon
	private Vector offset;
	// player who has build this cannon
	private String owner;
	// designID of the cannon, for different types of cannons - not in use
	private boolean isValid;
	private CannonDesign design;

	public Cannon()
	{
		
	}
	
	public Location getFiringTriggerLocation()
	{
		Vector vect = design.getFiringTriggerLocation(this.cannonDirection, this.offset);
		if (vect != null)
		{
			return new Location(getWorldBukkit(), vect.getBlockX(), vect.getBlockY(), vect.getZ());
		}
		//Muzzle location not found
		System.out.println("[Cannons] Firing trigger location not found");
		return new Location(getWorldBukkit(), 0, 100, 0);
	}
	
	/**
	 * returns the location of the muzzle
	 * @return
	 */
	public Location getMuzzleLocation()
	{
		Vector vect = design.getMuzzle(this.cannonDirection, this.offset);
		if (vect != null)
		{
			return new Location(getWorldBukkit(), vect.getBlockX(), vect.getBlockY(), vect.getZ());
		}
		//Muzzle location not found
		System.out.println("[Cannons] Muzzle location not found");
		return new Location(getWorldBukkit(), 0, 100, 0);
	}
	
	
	/**
	 * returns a list of firing indicators
	 * @return
	 */
	public List<Location> getFiringIndicator()
	{
		List<Vector> vectList = design.getFiringIndicatorLocations(cannonDirection);
		List<Location> locList = new ArrayList<Location>();
		
		for(Vector vect : vectList)
		{
			locList.add(toLocation(vect));
		}
		return locList;
	}

	
	public Vector getFiringVector(Config config)
	{
		// get projectile
		Projectile proj = config.getProjectile(projectileID, projectileData);

		// set direction of the snowball
		Vector vect = new Vector(1f, 0f, 0f);
		Random r = new Random();
		
		double deviation = r.nextGaussian() * design.getSpreadOfCannon();
		if (proj.canisterShot)
			deviation += r.nextGaussian() * proj.spreadCanisterShot;
		double horizontal = Math.sin((horizontalAngle + deviation) * Math.PI / 180);

		deviation = r.nextGaussian() * design.getSpreadOfCannon();
		if (proj.canisterShot)
			deviation += r.nextGaussian() * proj.spreadCanisterShot;
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
		
		double multi = proj.max_speed * design.getMultiplierVelocity() * loadedGunpowder / design.getMaxLoadableGunpowder();
		if (multi < 0.1)
			multi = 0.1;

		return vect.multiply(multi);
	}
	
	/**
	 * removes the loaded charge form the chest attached to the cannon
	 * @param cannon
	 * @return
	 */
	public boolean removeAmmoFromChests()
	{
		// create a new projectile stack with one projectile
		ItemStack projectile = CannonsUtil.newItemStack(projectileID, projectileData, 1);

		// gunpowder stack
		ItemStack powder =  design.getGunpowderType().toItemStack(loadedGunpowder);

		//For all possible chest locations
		// update all possible sign locations
		for (Vector chestVect : design.getSignsLocations(cannonDirection))
		{
			//shift by offset and make location
			Location signLoc = toLocation(chestVect);
			
			
			if (InventoryManagement.removeAmmoFromChest(signLoc.getBlock(), powder, projectile))
				return true;
		}
		return false;
	}
	
	/**
	 * is cannon loaded return true
	 * @return
	 */
	public boolean isLoaded()
	{
		return (projectileID == Material.AIR.getId()) ? false : true;
	}
	
	/**
	 * removes gunpowder and the projectile. Items are drop at the cannonball exit point
	 */
	private void dropCharge()
	{
		if (loadedGunpowder > 0)
		{
			ItemStack powder = new ItemStack(Material.SULPHUR, loadedGunpowder);
			getWorldBukkit().dropItemNaturally(getMuzzleLocation(), powder);	
		}
		
		//can't drop Air
		if (projectileID > 0)
		{
			ItemStack projectile = new ItemStack(projectileID, 1, (short) projectileData);
			getWorldBukkit().dropItemNaturally(getMuzzleLocation(), projectile);	
		}
		
	}
	
	
	/**
	 * removes the sign text and charge of the cannon after destruction 
	 */
	public void destroyCannon()
	{
		//update cannon signs the last time
		isValid = false;
		updateCannonSigns();
		
		//drop charge
		dropCharge();
	}
	
	/**
	 * updates all signs that are attached to a cannon
	 */
	public void updateCannonSigns()
	{
		// update all possible sign locations
		for (Vector signVect : design.getSignsLocations(cannonDirection))
		{
			//shift by offset and make location
			Location signLoc = toLocation(signVect);
			
			updateSign(signLoc.getBlock());
		}
	}
	
	/**
	 * updates the selected sign
	 * @param block
	 */
	private void updateSign(Block block)
	{
		if (block.getType() != Material.WALL_SIGN) return;
		
		Sign sign = (Sign) block.getState();
		
		if (isValid == true)
		{
			//Cannon name in the first line
			sign.setLine(0, getSignString(0));
			//Cannon owner in the second
			sign.setLine(1, getSignString(1));
			//loaded Gunpowder/Projectile
			sign.setLine(2, getSignString(2));		
			//angles
			sign.setLine(3, getSignString(3));
		}
		else
		{
			//Cannon name in the first line
			sign.setLine(0, "cannon");
			//Cannon owner in the second
			sign.setLine(1, "damaged");
			//loaded Gunpowder/Projectile
			sign.setLine(2, "");		
			//angles
			sign.setLine(3, "");
		}
		
		sign.update(true);
	}
	
	/**
	 * returns the strings for the sign
	 * @param index
	 * @return
	 */
	public String getSignString(int index)
	{

		switch (index)
		{

			case 0:
				//Cannon name in the first line
				if (name == null)  name = "missing Name";
				return name;
			case 1:
				//Cannon owner in the second
				if (owner == null) owner = "missing Owner";
				return owner;
			case 2:
				//loaded Gunpowder/Projectile
				return "p: " + loadedGunpowder + " c: " + projectileID + ":" + projectileData;	
			case 3:
				//angles
				return  horizontalAngle + "/" + verticalAngle;
		}
		return "missing";
	}
	
	/**
	 * returns false if the name of the cannon and the name on the sign are different
	 * @return
	 */
	public boolean isCannonEqualSign()
	{
		// update all possible sign locations
		for (Vector signVect : design.getSignsLocations(cannonDirection))
		{
			//shift by offset and make location
			Location signLoc = toLocation(signVect);
			
			//if one sign is not equal, the there is a problem
			if (isCannonEqualThisSign(signLoc.getBlock()))
				return false;
		}
		
		return true;
	}
	
	/**
	 * extracts the cannon name and owner from the sign and comperes to the cannon
	 * @param block
	 * @return
	 */
	private boolean isCannonEqualThisSign(Block block)
	{
		if (block.getType() != Material.WALL_SIGN) return true;
		
		Sign sign = (Sign) block.getState();
		
		//sign is empty
		if (sign.getLine(0) == null || sign.getLine(1) == null) return true;
		
		//sign name and owner are the same
		if (sign.getLine(0).equals(name) && sign.getLine(1).equals(owner)) return true;
			
		return false;
	}
	
	/**
	 * transforms a location and shifts it by the offset
	 * @param vect
	 * @return
	 */
	private Location toLocation(Vector vect)
	{
		Vector newVect = vect.add(offset);
		return new Location(getWorldBukkit(), newVect.getBlockX(), newVect.getBlockY(), newVect.getBlockZ());
	}
	


	/**
	 * get bukkit world
	 * @return
	 */
	public World getWorldBukkit()
	{
		if (this.world != null)
		{
			return Bukkit.getWorld(this.world);
			//return new Location(bukkitWorld, )
		}
		return null;
	}
	
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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
	
	public int getDesignID()
	{
		return design.getUniqueID();
	}
	
	public void setDesignID(int designID)
	{
		this.design.setUniqueID(designID);
	}
	
	public int getMaxLoadableGunpowder()
	{
		return design.getMaxLoadableGunpowder();
	}
	
	public CannonDesign getCannonDesign()
	{
		return design;
	}
	


	
}
