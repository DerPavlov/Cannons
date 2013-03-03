package at.pavlov.Cannons.dao;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.Projectile;

public class CannonData
{
	// Database id - is 0 until stored in the database. Then it is the id is the database
	public int id;    
	public String name;
	// point where the snowball is fired
	public Location location;
	// direction the cannon is facing
	public BlockFace face;
	// length of the barrel
	public int barrel_length;
	// time the cannon was last time fired
	public long LastFired;
	public int gunpowder;
	public int projectileID;
	public int projectileData;
	public double horizontal_angle;
	public double vertical_angle;
	// player who has build this cannon
	public String owner;
	// designID of the cannon, for different types of cannons - not in use
	public int designId;
	public boolean isValid;
	public ArrayList<Location> CannonBlocks = new ArrayList<Location>();

	public CannonData()
	{
		CannonBlocks = new ArrayList<Location>();
		isValid = true;
	}

	public void addBlock(Location loc)
	{
		CannonBlocks.add(loc);
	}

	public Vector getFiringVector(Config config)
	{
		// get projectile
		Projectile proj = config.getProjectile(projectileID, projectileData);

		// set direction of the snowball
		Vector vect = new Vector(1f, 0f, 0f);
		Random r = new Random();
		// 1.0 for min_barrel_length and gets smaller
		double shrinkFactor = Math.exp(-2.0 * ((double) barrel_length - config.min_barrel_length) / (config.max_barrel_length - config.min_barrel_length));

		double deviation = r.nextGaussian() * config.angle_deviation * shrinkFactor;
		if (proj.canisterShot)
			deviation += r.nextGaussian() * proj.spreadCanisterShot;
		double horizontal = Math.sin((horizontal_angle + deviation) * Math.PI / 180);

		deviation = r.nextGaussian() * config.angle_deviation * shrinkFactor;
		if (proj.canisterShot)
			deviation += r.nextGaussian() * proj.spreadCanisterShot;
		double vertical = Math.sin((vertical_angle + deviation) * Math.PI / 180);

		if (face == BlockFace.WEST)
		{
			vect = new Vector(-1.0f, vertical, -horizontal);
		}
		else if (face == BlockFace.NORTH)
		{
			vect = new Vector(horizontal, vertical, -1.0f);
		}
		else if (face == BlockFace.EAST)
		{
			vect = new Vector(1.0f, vertical, horizontal);
		}
		else if (face == BlockFace.SOUTH)
		{
			vect = new Vector(-horizontal, vertical, 1.0f);
		}

		// old code for version below 1.4.5 R0.3
		/*
		 * if (face == BlockFace.NORTH) { vect = new Vector( -1.0f, vertical,
		 * -horizontal); } else if (face == BlockFace.EAST) { vect = new
		 * Vector(horizontal , vertical, -1.0f); } else if (face ==
		 * BlockFace.SOUTH) { vect = new Vector( 1.0f, vertical, horizontal); }
		 * else if (face == BlockFace.WEST) { vect = new Vector(-horizontal ,
		 * vertical,1.0f); }
		 */

		double multi = proj.max_speed * gunpowder / config.max_gunpowder;
		if (multi < 0.1)
			multi = 0.1;

		return vect.multiply(multi);
	}

	public boolean isLoaded()
	{
		return (projectileID == Material.AIR.getId()) ? false : true;
	}
	
	/**
	 * removes gunpowder and the projectile. Items are drop at the cannonball exit point
	 */
	private void dropCharge()
	{
		if (gunpowder > 0)
		{
			ItemStack powder = new ItemStack(Material.SULPHUR, gunpowder);
			location.getWorld().dropItemNaturally(location, powder);	
		}
		
		//can't drop Air
		if (projectileID > 0)
		{
			ItemStack projectile = new ItemStack(projectileID, 1, (short) projectileData);
			location.getWorld().dropItemNaturally(location, projectile);
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
		// goto the last first block of the cannon
		Block block = location.getBlock().getRelative(face.getOppositeFace(), barrel_length - 1);

		// left and right sign
		if (face == BlockFace.EAST || face == BlockFace.WEST)
		{
			updateSign(block.getRelative(BlockFace.NORTH));
			updateSign(block.getRelative(BlockFace.SOUTH));
		}
		else
		{
			updateSign(block.getRelative(BlockFace.WEST));
			updateSign(block.getRelative(BlockFace.EAST));	
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
				return "p: " + gunpowder + " c: " + projectileID + ":" + projectileData;	
			case 3:
				//angles
				return "" + horizontal_angle + "/" + vertical_angle;
		}
		return "missing";
	}
	
	/**
	 * returns false if the name of the cannon and the name on the sign are different
	 * @return
	 */
	public boolean isCannonEqualSign()
	{
		// goto the last first block of the cannon
		Block block = location.getBlock().getRelative(face.getOppositeFace(), barrel_length - 1);
		

		// left and right sign
		if (face == BlockFace.EAST || face == BlockFace.WEST)
		{
			// if one is false then cannon on the sign is different from the storage
			return isCannonEqualThisSign(block.getRelative(BlockFace.NORTH)) && isCannonEqualThisSign(block.getRelative(BlockFace.SOUTH));
		}
		else
		{
			return isCannonEqualThisSign(block.getRelative(BlockFace.EAST)) && isCannonEqualThisSign(block.getRelative(BlockFace.WEST));
		}
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
		
		if (sign.getLine(0) == null || sign.getLine(1) == null) return true;
		
		if (sign.getLine(0).equals(name) && sign.getLine(1).equals(owner)) return true;
			
		return false;
	}
	
	/**
	 * returns the name of the cannon written on the sign
	 * @return
	 */
	public String getLineOfCannonSigns(int line)
	{
		// goto the last first block of the cannon
		Block block = location.getBlock().getRelative(face.getOppositeFace(), barrel_length - 1);
		
		String lineStr = null;		
		
		// left and right sign
		if (face == BlockFace.EAST || face == BlockFace.WEST)
		{
			// if one is false then cannon on the sign is different from the storage
			lineStr = getLineOfThisSign(block.getRelative(BlockFace.NORTH),line);
			if (lineStr != null) return lineStr;
			lineStr = getLineOfThisSign(block.getRelative(BlockFace.SOUTH),line);
			if (lineStr != null) return lineStr;
		}
		else
		{
			lineStr = getLineOfThisSign(block.getRelative(BlockFace.EAST),line);
			if (lineStr != null) return lineStr;
			lineStr = getLineOfThisSign(block.getRelative(BlockFace.WEST),line);
			if (lineStr != null) return lineStr;
		}	
		return null;
	}

	
	/**
	 * returns line written on the sign
	 * @return
	 */
	private String getLineOfThisSign(Block block, int line)
	{
		if (block.getType() != Material.WALL_SIGN) return null;
		
		Sign sign = (Sign) block.getState();
		
		return sign.getLine(line);
	}
	
}
