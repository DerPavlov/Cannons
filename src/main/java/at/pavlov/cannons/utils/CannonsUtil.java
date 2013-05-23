package at.pavlov.cannons.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import at.pavlov.cannons.container.MaterialHolder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;
import org.bukkit.material.Torch;



public class CannonsUtil
{
	public static boolean hasIdData(Block block, int id, int data)
	{
		if (block.getTypeId() == id && block.getData() == data) { return true; }
		return false;
	}

	// ################# CheckAttachedButton ###########################
	public static boolean CheckAttachedButton(Block block, BlockFace face)
	{
		Block attachedBlock = block.getRelative(face);
		if (attachedBlock.getType() == Material.STONE_BUTTON)
		{
			Button button = (Button) attachedBlock.getState().getData();
			if (button.getAttachedFace() != null)
			{
				if (attachedBlock.getRelative(button.getAttachedFace()).equals(block)) { return true; }
			}
			// attached face not available
			else
			{
				return true;
			}
		}
		return false;
	}

	// ################# CheckAttachedTorch ###########################
	@Deprecated
    public static boolean CheckAttachedTorch(Block block)
	{
		Block attachedBlock = block.getRelative(BlockFace.UP);
		if (attachedBlock.getType() == Material.TORCH)
		{
			Torch torch = (Torch) attachedBlock.getState().getData();
			if (torch.getAttachedFace() != null)
			{
				if (attachedBlock.getRelative(torch.getAttachedFace()).equals(block)) { return true; }
			}
			// attached face not available
			else
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * changes the extension of the a string (e.g. classic.yml to
	 * classic.schematic)
	 * 
	 * @param originalName
	 * @param newExtension
	 * @return
	 */
	public static String changeExtension(String originalName, String newExtension)
	{
		int lastDot = originalName.lastIndexOf(".");
		if (lastDot != -1)
		{
			return originalName.substring(0, lastDot) + newExtension;
		}
		else
		{
			return originalName + newExtension;
		}
	}
	
	/**
	 * removes the extrions of a filename like classic.yml
	 * @param str
	 * @return
	 */
	public static String removeExtension(String str)
	{
		return str.substring(0, str.lastIndexOf('.'));
	}

	/**
	 * return true if the folder is empty
	 * @param folderPath
	 * @return
	 */
	public static boolean isFolderEmpty(String folderPath)
	{
		File file = new File(folderPath);
		if (file.isDirectory())
		{
			if (file.list().length > 0)
			{
				//folder is not empty
				return false;
			}
		}
		return true;
	}
	
	/**
	 * copies a file form the .jar to the disk
	 * @param in
	 * @param file
	 */
	public static void copyFile(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	/**
	 * rotates the direction by 90�	
	 * @param face
	 * @return
	 */
	public static BlockFace roatateFace(BlockFace face)
	{
		if (face.equals(BlockFace.NORTH)) return BlockFace.EAST;
		if (face.equals(BlockFace.EAST)) return BlockFace.SOUTH;
		if (face.equals(BlockFace.SOUTH)) return BlockFace.WEST;
		if (face.equals(BlockFace.WEST)) return BlockFace.NORTH;
		return BlockFace.UP;
	}
	
	/**
	 * returns a new Itemstack
	 * @param id
	 * @param data
	 * @param amount
	 * @return
	 */
	public static ItemStack newItemStack(int id, int data, int amount)
	{
		ItemStack item = new ItemStack(id, amount, (short) data);
		return item;
	}
	
	/**
	 * returns a list of MaterialHolder
	 * @param stringList
	 * @return
	 */
	public static List<MaterialHolder> toMaterialHolderList(List<String> stringList)
	{
		List<MaterialHolder> materialList = new ArrayList<MaterialHolder>();
		
		for (String str : stringList)
		{
			MaterialHolder material = new MaterialHolder(str); 
			//if id == -1 the str was invalid
			if (material.getId() >= 0)
				materialList.add(material);
		}
		
		return materialList;
	}
	
	
	/**
	 * get all block next to this block (UP, DOWN, SOUT, WEST, NORTH, EAST)
	 * @param block
	 * @return
	 */
	public static ArrayList<Block> SurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.UP));
		Blocks.add(block.getRelative(BlockFace.DOWN));
		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}

	/**
	 * get all block in the horizontal plane next to this block (SOUTH, WEST, NORTH, EAST)
	 * @param block
	 * @return
	 */
	public static ArrayList<Block> HorizontalSurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}
	
	
	/**
	 * returns the yaw of a given blockface
	 * @param direction
	 * @return
	 */
    public static int directionToYaw(BlockFace direction) {
        switch (direction) {
            case NORTH: return 180;
            case EAST: return 270;
            case SOUTH: return 0;
            case WEST: return 90;
            case NORTH_EAST: return 135;
            case NORTH_WEST: return 45;
            case SOUTH_EAST: return -135;
            case SOUTH_WEST: return -45;
            default: return 0;
        }
    }

}
