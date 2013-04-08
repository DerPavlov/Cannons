package at.pavlov.Cannons.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;



public class Projectile {
	private String name;
	
	private int id;
	private int data;
	
	public String shooter;
	
	public boolean cannonball;
	public double explosion_power;
	public boolean blockDamage;
	public double player_damage;
	public double penetration;
	public double timefuse;
	public double max_speed;
	
	public boolean canisterShot;
	public double spreadCanisterShot;
	public int amountCanisterShot;

	public boolean placeBlock;
	public double placeBlockRadius;
	public int placeBlockAmount;
	public int placeBlockMaterialId;
	public int placeBlockMaterialData;
	
	public double effectDuration;
	public boolean superBreaker;
	public boolean incendiary;
	public boolean blindness;
	public boolean poison;
	public boolean slowness;
	public boolean slowDigging;
	public boolean weakness;
	public boolean confusion;
	public boolean hunger;
	public boolean teleport;
	
	private List<String> permissions = new ArrayList<String>();
	
	public Projectile(){
		setName("default projectile");
		
		setId(4);
		setData(0);
		
		cannonball = true;
		explosion_power = 4;
		player_damage = 0;
		blockDamage = true;
		penetration = 1;
		timefuse = 0;
		max_speed = 5;
		
		canisterShot = false;
		spreadCanisterShot = 5;
		amountCanisterShot = 30;
		
		placeBlock = false;
		placeBlockRadius = 2;
		placeBlockAmount = 10;
		placeBlockMaterialId = 0;
		placeBlockMaterialData = 0;

		effectDuration = 5;
		superBreaker = false;
		incendiary = false;
		blindness = false;
		poison = false;
		slowness = false;
		slowDigging = false;
		weakness = false;
		confusion = false;
		hunger = false;
		teleport = false;
	}
	
	public boolean isEqual(int _id, int _data)
	{
		if (_id == id)
		{
			// negative data values - allow all data values
			if (data < 0 || _data == data)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isMobEgg()
	{
		if (placeBlockMaterialId == 383)
		{
			return true;
		}
		return false;	
	}
	
	/**
	 * compares data and id. If projectile data is -1, comparision is skipped
	 * @param _id
	 * @param _data
	 * @return
	 */
	public boolean equalsFuzzy(int _id, int _data)
	{
		if (_id == id)
		{
			//compare data - if data is -1 skip
			return (data == _data || data == -1);
		}
		return false;
	}
	
	public String toString()
	{
		return id + ":" + data;
	}
	
	/**
	 * returns true if the player has permission to use that projectile
	 * @param player
	 * @return
	 */
	public boolean hasPermission(Player player)
	{
		for (String perm : permissions)
		{
			if(!player.hasPermission(perm))
			{
				//missing permission
				return false;
			}
		}
		//player has all permissions
		return true;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	
	

	

}
