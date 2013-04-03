package at.pavlov.Cannons.sign;

import at.pavlov.Cannons.cannon.Cannon;


public class CannonSign
{

	public CannonSign()
	{
		
	}
	
	/**
	 * returns the name of the cannon written on the sign
	 * @return
	 */
	private String getLineOfCannonSigns(int line, Cannon Cannon)
	{
		// goto the last first block of the cannon
		//for (Location signLoc : design.getc)
		//Block block = 
		
		String lineStr = "";		
		
		/*
		// left and right sign
		if (cannonDa == BlockFace.EAST || face == BlockFace.WEST)
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
		*/
		return lineStr;
	}
	
	/**
	 * returns line written on the sign sign
	 * @return
	 */
	/*
	public String getLineOfThisSign(Block block, int line)
	{
		if (block.getType() != Material.WALL_SIGN) return null;
		
		Sign sign = (Sign) block.getState();
		
		return sign.getLine(line);
	}
	
	/**
	 * returns the cannon name that is written on a cannon sign
	 * @return
	 */
	/*
	public String getCannonNameFromSign()
	{
		return getLineOfCannonSigns(0);
	}
	
	/**
	 * returns the cannon owner that is written on a cannon sign
	 * @return
	 */
	/*
	public String getOwnerFromSign()
	{
		return getLineOfCannonSigns(1);
	}
	
	/**
	 * returns the amount of gunpowder that is written on a cannon sign
	 * @return
	 */
	/*
	public int getGunpowderFromSign()
	{
		String str[] = getLineOfCannonSigns(2).split(" ");
		// g: 2 c: 123:1
		if (str.length >= 4 )
		{
			return Integer.parseInt(str[1]);
		}
		return 0;
	}
	
	/**
	 * returns the projectileID that is written on a cannon sign
	 * @return
	 */
	/*
	public int getProjectileIDFromSign()
	{
		// g: 2 c: 123:1
		String str[] = getLineOfCannonSigns(2).split(" ");
		if (str.length >= 4 )
		{
			//123:1
			String str2[] = str[3].split(":");
			if (str.length >= 2)
			{
				return Integer.parseInt(str2[0]);
			}
		}
		return 0;
	}
	
	/**
	 * returns the projectileData that is written on a cannon sign
	 * @return
	 */
	/*
	public int getProjectileDataFromSign()
	{
		// g: 2 c: 123:1
		String str[] = getLineOfCannonSigns(2).split(" ");
		if (str.length >= 4 )
		{
			//123:1
			String str2[] = str[3].split(":");
			if (str.length >= 2)
			{
				return Integer.parseInt(str2[1]);
			}
		}
		return 0;
	}
	
	/**
	 * returns the horizontal angle that is written on a cannon sign
	 * @return
	 */
	/*
	public double getHorizontalAngleFromSign()
	{
		// 12/-2
		String str[] = getLineOfCannonSigns(2).split("/");
		if (str.length >= 2 )
		{
			return Double.parseDouble(str[0]);
		}
		return 0;
	}
	
	/**
	 * returns the vertical angle that is written on a cannon sign
	 * @return
	 */
	/*
	public double getVerticalAngleFromSign()
	{
		// 12/-2
		String str[] = getLineOfCannonSigns(2).split("/");
		if (str.length >= 2 )
		{
			return Double.parseDouble(str[1]);
		}
		return 0;
	}*/

}
