package at.pavlov.cannons.container;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;


import com.sk89q.worldedit.blocks.BaseBlock;

public class SimpleBlock
{
	private int locX;
	private int locY;
	private int locZ;
	
	private Material material;
	private int data;
	
	public SimpleBlock(int x, int y, int z, BaseBlock block)
	{
		locX = x;
		locY = y;
		locZ = z;
		
		material = Material.getMaterial(block.getId());
		data = block.getData();
	}
	
	public SimpleBlock(int x, int y, int z, MaterialHolder material)
	{
		locX = x;
		locY = y;
		locZ = z;
		
		this.material = material.getType();
		data = material.getData();
	}
	
	public SimpleBlock(int x, int y, int z, Material material, int data)
	{
		locX = x;
		locY = y;
		locZ = z;
		
		this.material = material;
		this.data = data;
	}
	
	private SimpleBlock(Vector vect, Material material, int data)
	{
		locX = vect.getBlockX();
		locY = vect.getBlockY();
		locZ = vect.getBlockZ();
		
		this.material = material;
		this.data = data;
	}
	
	public SimpleBlock(Location loc, Material material, int data)
	{
		locX = loc.getBlockX();
		locY = loc.getBlockY();
		locZ = loc.getBlockZ();
		
		this.material = material;
		this.data = data;
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
	 * compare the location of the block and the id
	 * @param block
	 * @param offset
	 * @return
	 */
	public boolean compareBlockAndLoc(Block block, Vector offset)
	{		
		if (toVector().add(offset).equals(block.getLocation().toVector()))
		{
			if (compareBlock(block))
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
		if (block.getType().equals(this.material))
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
    boolean compareBlock(Block block)
	{
		if (block.getType().equals(this.material))
		{
			if (block.getData() == data)
			{
				return true;
			}
		}
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
			//compare the id and data
			if (block.getType().equals(this.material))
			{
				if (this.getData() == block.getData() || this.getData()==-1 || block.getData()==-1)
					return true;
			}
		}
		return false;
	}
	
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 * @return
	 */
	public SimpleBlock add(Location loc)
	{
		return new SimpleBlock(locX + loc.getBlockX(), locY + loc.getBlockY(), locZ + loc.getBlockZ(), this.material, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param block
	 * @return
	 */
	public SimpleBlock add(SimpleBlock block)
	{
		return new SimpleBlock(locX + block.getLocX(), locY + block.getLocY(), locZ + block.getLocZ(), this.material, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param vect offset vector
	 * @return a new block with a shifted location
	 */
	public SimpleBlock add(Vector vect)
	{
		return new SimpleBlock(toVector().add(vect), this.material, data);
	}
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param vect vector to subtract
	 * @return new block with new subtracted location
	 */
	public SimpleBlock subtract(Vector vect)
	{
		return new SimpleBlock(vect.getBlockX() - locX, vect.getBlockY() - locY, vect.getBlockZ() - locZ, this.material, data);
	}

    /**
     * shifts the location of the block without comparing the id
     * @param vect vector to subtract
     * @return new block with new subtracted location
     */
    public SimpleBlock subtract_noCopy(Vector vect)
    {
        locX -= vect.getBlockX();
        locY -= vect.getBlockY();
        locZ -= vect.getBlockZ();
        return this;
    }
	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 * @return
	 */
	public SimpleBlock subtractInverted(Location loc)
	{
		return new SimpleBlock(loc.getBlockX() - locX, loc.getBlockY() - locY, loc.getBlockZ() - locZ, this.material, data);
	}
	

	
	/** 
	 * shifts the location of the block without comparing the id
	 * @param loc
	 * @return
	 */
	public SimpleBlock subtract(Location loc)
	{
		return new SimpleBlock(locX - loc.getBlockX() , locY - loc.getBlockY(), locZ - loc.getBlockZ(), material, data);
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

	public void setType(Material material)
	{
		this.material = material;
	}

	public Material getType()
	{
		return this.material;
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
		return "x:" + locX + " y:" + locY + " z:" + locZ +" id:" + this.getType() + " data:" + data;
	}


}
