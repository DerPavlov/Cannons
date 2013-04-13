package at.pavlov.Cannons.container;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;


import com.sk89q.worldedit.blocks.BaseBlock;

public class SimpleBlock
{
	private int locX;
	private int locY;
	private int locZ;
	
	private int id;
	private int data;
	
	public SimpleBlock(int x, int y, int z, BaseBlock block)
	{
		locX = x;
		locY = y;
		locZ = z;
		
		id = block.getId();
		data = block.getData();
	}
	
	public SimpleBlock(int x, int y, int z, MaterialHolder material)
	{
		locX = x;
		locY = y;
		locZ = z;
		
		id = material.getId();
		data = material.getData();
	}
	
	public SimpleBlock(int x, int y, int z, int id, int data)
	{
		locX = x;
		locY = y;
		locZ = z;
		
		this.id = id;
		this.data = data;
	}
	
	public SimpleBlock(Vector vect, int id, int data)
	{
		locX = vect.getBlockX();
		locY = vect.getBlockY();
		locZ = vect.getBlockZ();
		
		this.id = id;
		this.data = data;
	}
	
	public SimpleBlock(Location loc, int id, int data)
	{
		locX = loc.getBlockX();
		locY = loc.getBlockY();
		locZ = loc.getBlockZ();
		
		this.id = id;
		this.data = data;
	}
	
	/**
	 * to location
	 * @param world
	 * @return
	 */
	public Location toLocation(World world)
	{
		return new Location(world, locX, locY, locZ);
	}
	
	/**
	 * to location with offset
	 * @param world
	 * @return
	 */
	public Location toLocation(World world, Vector offset)
	{
		return new Location(world, locX + offset.getBlockX(), locY + offset.getBlockY(), locZ + offset.getBlockZ());
	}

	
	/**
	 * compares the real world block by id and data (not data if data = -1)
	 * @param block
	 * @param offset
	 * @return
	 */
	public boolean compareBlockFuzzy(World world, Vector offset)
	{		
		Block block = toLocation(world, offset).getBlock();
		if (compareBlockFuzzy(block)) 
			return true;
		return false;
	}

	/**
	 * compare the location of the block and the id and data or data = -1
	 * @param block
	 * @param offset
	 * @return
	 */
	public boolean compareBlockAndLocFuzzy(Block block, Vector offset)
	{		
		if (toVector().add(offset).equals(block.getLocation().toVector()))
		{
			if (compareBlockFuzzy(block)) return true;
		}
		return false;
	}
	
	/**
	 * compare the location of the block and the id
	 * @param block
	 * @param offset
	 * @return
	 */
	public boolean compareBlockAndLoc(Block block, Vector offset)
	{		
		if (toVector().add(offset).equals(block.getLocation().toVector()))
		{
			if (compareBlock(block)) return true;
		}
		return false;
	}
	
	/**
	 * return true if id and data are equal or data is -1
	 * @param block
	 * @return
	 */
	public boolean compareBlockFuzzy(Block block)
	{
		if (block.getTypeId() == id)
		{
			if (block.getData() == data || data == -1 || block.getData() == -1)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * return true if id and data are equal
	 * @param block
	 * @return
	 */
	public boolean compareBlock(Block block)
	{
		if (block.getTypeId() == id)
		{
			if (block.getData() == data)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock add(Location loc)
	{
		return new SimpleBlock(locX + loc.getBlockX(), locY + loc.getBlockY(), locZ + loc.getBlockZ(), id, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock add(SimpleBlock block)
	{
		return new SimpleBlock(locX + block.getLocX(), locY + block.getLocY(), locZ + block.getLocZ(), id, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock add(Vector vect)
	{
		return new SimpleBlock(toVector().add(vect), id, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock substract(Vector vect)
	{
		return new SimpleBlock(vect.getBlockX() - locX, vect.getBlockY() - locY, vect.getBlockZ() - locZ, id, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock substractInverted(Location loc)
	{
		return new SimpleBlock(loc.getBlockX() - locX, loc.getBlockY() - locY, loc.getBlockZ() - locZ, id, data);
	}
	

	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock substract(Location loc)
	{
		return new SimpleBlock(locX - loc.getBlockX() , locY - loc.getBlockY(), locZ - loc.getBlockZ(), id, data);
	}
	
	/**
	 * SimpleBlock to Vector
	 * @return
	 */
	public Vector toVector()
	{
		return new Vector(locX, locY, locZ);
	}
	
	public int getLocX()
	{
		return locX;
	}

	public void setLocX(int locX)
	{
		this.locX = locX;
	}

	public int getLocY()
	{
		return locY;
	}

	public void setLocY(int locY)
	{
		this.locY = locY;
	}

	public int getLocZ()
	{
		return locZ;
	}

	public void setLocZ(int locZ)
	{
		this.locZ = locZ;
	}

	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public int getData()
	{
		return data;
	}

	public void setData(int data)
	{
		this.data = data;
	}
	
	public String toString()
	{
		return "x:" + locX + " y:" + locY + " z:" + locZ +" id:" + id + " data:" + data;
	}


}
