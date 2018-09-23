package at.pavlov.cannons.container;


import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.Directional;
import org.bukkit.util.Vector;

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
	public boolean compareMaterial(World world, Vector offset)
	{		
		Block block = toLocation(world, offset).getBlock();
        return compareMaterial(block.getBlockData());
    }

	/**
	 * compare the location of the block and the id and data or data = -1
	 * @param block block to compare to
	 * @param offset the offset of the cannon
	 * @return true if both block match
	 */
	public boolean compareMaterialAndLoc(Block block, Vector offset)
	{		
		if (toVector().add(offset).equals(block.getLocation().toVector()))
		{
			if (compareMaterial(block.getBlockData()))
				return true;
		}
		return false;
	}

	/**
	 * return true if Materials match
	 * @param block block to compare to
	 * @return true if both block match
	 */
	public boolean compareMaterial(BlockData block)
	{
		return block.getMaterial().equals(this.blockData.getMaterial());
	}

	/**
	 * compares material and facing
	 * @param blockData block to compare to
	 * @return true if both block match
	 */
	public boolean compareMaterialAndFacing(BlockData blockData) {
		// different materials
		if (!compareMaterial(blockData))
			return false;
		// compare facing and face
		return blockData instanceof Directional && this instanceof Directional && ((Directional) this).getFacing().equals(((Directional) blockData).getFacing());
	}

	/**
	 * matches all entries in this SimpleBlock to the given block
	 * @param blockData block to compare to
	 * @return true if both block match
	 */
	public boolean compareBlockData(BlockData blockData)
	{
		return this.blockData.matches(blockData);
	}

	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc location to add
	 * @return new Simpleblock
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
	 */
	public SimpleBlock subtractInverted(Location loc)
	{
		return new SimpleBlock(loc.getBlockX() - locX, loc.getBlockY() - locY, loc.getBlockZ() - locZ, this.blockData);
	}
	

	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 */
	public SimpleBlock subtract(Location loc)
	{
		return new SimpleBlock(locX - loc.getBlockX() , locY - loc.getBlockY(), locZ - loc.getBlockZ(), this.blockData);
	}

	/**
	 * rotate the block 90° degree clockwise)
	 * @return
	 */
	public void rotate90(){
		this.blockData = CannonsUtil.roateBlockFacingClockwise(this.blockData);
		int newx = -this.locZ;
		this.locZ = this.locX;
		this.locX = newx;
	}
	
	/**
	 * SimpleBlock to Vector
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
