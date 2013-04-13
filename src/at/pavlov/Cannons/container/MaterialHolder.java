package at.pavlov.Cannons.container;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.blocks.BaseBlock;

//small class as container for item id and data
public class MaterialHolder
{
	private int id;
	private int data;
	
	public MaterialHolder(ItemStack item)
	{
		id = item.getTypeId();
		data = item.getData().getData();
	}
	
	public MaterialHolder(int _id, int _data)
	{
		id = _id;
		data = _data;
	}
	
	public MaterialHolder(String str)
	{
		//remove all spaces
		str = str.replace(" ", "");
		
		//split string at :
		String subStr[] = str.split(":");
	
		try
		{
			//get ID
			if (subStr.length>=1)
				id = Integer.parseInt(subStr[0]);
			else
				id = -1;		//invalid
			
			//get Data
			if (subStr.length >= 2)
				data = Integer.parseInt(subStr[1]);
			else
				data = -1;
		}
		catch (Exception e)
		{
			System.out.println("[Cannons] Can't convert " + str + " to Material");
			id = -1;
			data = -1;
		}		
	}
	
	public BaseBlock toBaseBlock()
	{
		return new BaseBlock(id, data);
	}
	
	public ItemStack toItemStack(int amount)
	{
		return new ItemStack(id, amount, (short) data);
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
	
	public boolean equals(Material material)
	{
		if (material == null) return false;
		return material.getId() == id;
	}
	
	public boolean equalsFuzzy(ItemStack item)
	{
		//System.out.println("id:" + item.getTypeId() + "-" + id + " data:" + item.getData().getData() + "-" + data);
		if (item != null)
		{
			if (item.getTypeId() == id)
			{
				return (item.getData().getData() == data || data == -1);
			}
		}	
		return false;
	}


	


}