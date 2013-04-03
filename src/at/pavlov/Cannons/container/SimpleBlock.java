package at.pavlov.Cannons.container;


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

	/**
	 * compare the location of the block and the id
	 * @param block
	 * @param offset
	 * @return
	 */
	public boolean compareBlockAndLoc(Block block, Vector offset)
	{		
		if (block.getX() == locX - offset.getBlockY() && block.getX() == locY - offset.getBlockY() && block.getZ() == locZ - offset.getBlockZ())
		{
			if (compareBlockFuzzy(block)) return true;
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
		return new SimpleBlock(locX + vect.getBlockX(), locY + vect.getBlockY(), locZ + vect.getBlockZ(), id, data);
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
}
