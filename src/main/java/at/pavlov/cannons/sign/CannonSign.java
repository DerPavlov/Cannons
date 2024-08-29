package at.pavlov.cannons.sign;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;


public class CannonSign {

	
	/**
	 * returns line written on the sign sign
	 * @return
	 */
	public static String getLineOfThisSign(Block block, int line)
	{
        if (block == null) return null;
		if (!(block.getBlockData() instanceof WallSign)) return null;

		Sign sign = (Sign) block.getState();
		
		return sign.getLine(line);
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
