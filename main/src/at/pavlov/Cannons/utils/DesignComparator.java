package at.pavlov.Cannons.utils;

import java.util.Comparator;

import org.bukkit.block.BlockFace;

import at.pavlov.Cannons.cannon.CannonDesign;

public class DesignComparator implements Comparator<CannonDesign>
{

	@Override
	public int compare(CannonDesign design1, CannonDesign design2)
	{
		int amount1 = getCannonBlockAmount(design1);
		int amount2 = getCannonBlockAmount(design2);
		
		return amount2 - amount1;
	}
	
	private Integer getCannonBlockAmount(CannonDesign design)
	{
		if (design == null) return 0;
		//if the design is invalid something goes wrong, message the user
		if (design.getAllCannonBlocks(BlockFace.NORTH) == null) 
		{
			System.out.println("[Cannons] invalid cannon design for " + design.getDesignName());
			return 0;
		}
		
		return design.getAllCannonBlocks(BlockFace.NORTH).size();
	}

}
