package at.pavlov.Cannons.utils;

import org.bukkit.block.Block;

public class BlockHelper
{
	public static boolean hasIdData(Block block, int id, int data)
	{
		if (block.getTypeId() == id && block.getData() == data)
		{
			return true;
		}
		return false;
	}
}
