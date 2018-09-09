package at.pavlov.cannons.container;


import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import sun.java2d.pipe.SpanShapeRenderer;

public class SimpleBlock
{
	private int locX;
	private int locY;
	private int locZ;
	
	private BlockData blockData;

	public SimpleBlock(int x, int y, int z, BlockData blockData)
	{
		locX = x;
		locY = y;
		locZ = z;

		this.blockData = blockData;
	}

	public SimpleBlock(Vector vect, BlockData blockData)
	{
		this(vect.getBlockX(), vect.getBlockY(), vect.getBlockZ(), blockData);
	}

	public SimpleBlock(int x, int y, int z, Material material)
	{
		this(x, y, z, material.createBlockData());
	}
	
	private SimpleBlock(Vector vect, Material material)
	{
		this(vect, material.createBlockData());
	}
	
	public SimpleBlock(Location loc, Material material)
	{
		locX = loc.getBlockX();
		locY = loc.getBlockY();
		locZ = loc.getBlockZ();
		
		this.blockData = material.createBlockData();
	}

	
	/**
	 * to location with offset
	 * @param world bukkit world
	 * @return location of the block
	 */
	public Location toLocation(World world, Vector offset)
	{
		return new Location(world, locX + offset.getBlockX(), locY + offset.getBlockY(), locZ + offset.getBlockZ());
	}

	
	/**
	 * compares the real world block by id and data (not data if data = -1)
	 * @param world the world of the block
	 * @param offset the locations in x,y,z
	 * @return true if both block are equal in data and id or only the id if one data = -1
	 */
	public boolean compareBlockFuzzy(World world, Vector offset)
	{		
		Block block = toLocation(world, offset).getBlock();
        return compareBlockFuzzy(block);
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
			if (compareBlockFuzzy(block)) 
				return true;
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
		return block.getType().equals(this.blockData.getMaterial());
	}
	
	
	/**
	 * return true if id and data are equal
	 * @param block
	 * @return
	 */
    boolean compareBlock(Block block)
	{
		System.out.println("compareBlock: " + block.toString() + " to blockdata: " + this.blockData.toString());
		if (block.getState().equals(this.blockData))
		{
			System.out.println("TRUE");
			return true;
		}
		System.out.println("FALSE");
		return false;
	}
	
	/**
	 * compares two Simpleblocks. If one data=-1 the data is not compared
	 * @param block
	 * @return
	 */
	public boolean equalsFuzzy(SimpleBlock block)
	{
		// compare the location
		if (this.getLocX() == block.getLocX() && this.getLocY() == block.getLocY() && this.getLocZ() == block.getLocZ())
		{
			System.out.println("compareBlock + Loc: " + block.toString() + " to blockdata: " + this.blockData.toString());
			//compare the id and data
			if (block.getBlockData().equals(this.blockData))
			{
				System.out.println("TRUE");
				return true;
			}
		}
		System.out.println("FALSE");
		return false;
	}
	
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 * @return
	 */
	public SimpleBlock add(Location loc)
	{
		return new SimpleBlock(locX + loc.getBlockX(), locY + loc.getBlockY(), locZ + loc.getBlockZ(), this.blockData);
	}
	
	/**
	 * shifts the location of the block without comparing the id
	 * @param vect offset vector
	 * @return a new block with a shifted location
	 */
	public SimpleBlock add(Vector vect)
	{
		return new SimpleBlock(toVector().add(vect), this.blockData);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param vect vector to subtract
	 * @return new block with new subtracted location
	 */
	public SimpleBlock subtract(Vector vect)
	{
		return new SimpleBlock(vect.getBlockX() - locX, vect.getBlockY() - locY, vect.getBlockZ() - locZ, this.blockData);
	}

    /**
     * shifts the location of the block without comparing the id
     * @param vect vector to subtract
     * @return new block with new subtracted location
     */
    public void subtract_noCopy(Vector vect)
    {
        locX -= vect.getBlockX();
        locY -= vect.getBlockY();
        locZ -= vect.getBlockZ();
    }
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 * @return
	 */
	public SimpleBlock subtractInverted(Location loc)
	{
		return new SimpleBlock(loc.getBlockX() - locX, loc.getBlockY() - locY, loc.getBlockZ() - locZ, this.blockData);
	}
	

	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 * @return
	 */
	public SimpleBlock subtract(Location loc)
	{
		return new SimpleBlock(locX - loc.getBlockX() , locY - loc.getBlockY(), locZ - loc.getBlockZ(), this.blockData);
	}
	
	/**
	 * SimpleBlock to Vector
	 * @return
	 */
	public Vector toVector()
	{
		return new Vector(locX, locY, locZ);
	}
	
	int getLocX()
	{
		return locX;
	}

	public void setLocX(int locX)
	{
		this.locX = locX;
	}

	int getLocY()
	{
		return locY;
	}

	public void setLocY(int locY)
	{
		this.locY = locY;
	}

	int getLocZ()
	{
		return locZ;
	}

	public void setLocZ(int locZ)
	{
		this.locZ = locZ;
	}

	public void setBlockData(BlockData blockData)
	{
		this.blockData = blockData;
	}

	public BlockData getBlockData()
	{
		return this.blockData;
	}

	public String toString()
	{
		return "x:" + locX + " y:" + locY + " z:" + locZ +" blockdata:" + this.getBlockData().toString();
	}


}
