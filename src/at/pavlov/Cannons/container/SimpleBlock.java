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
	
	public boolean compareBlockAndLoc(Block block, Vector offset)
	{		
		if (block.getX() == locX - offset.getBlockY() && block.getX() == locY - offset.getBlockY() && block.getZ() == locZ - offset.getBlockZ())
		{
			if (compareBlock(block)) return true;
		}
		return false;
	}
	
	public boolean compareBlock(Block block)
	{
		return (block.getTypeId() == id && block.getData() == data) ? true : false;
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
