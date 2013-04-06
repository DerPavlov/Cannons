package at.pavlov.Cannons.container;

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
		data = -1;
		System.out.println("split" + str);
		//remove all spaces
		str = str.replace(" ", "");
		
		System.out.println("split1");
		
		//split string at :
		String subStr[] = str.split(":");
	
		
		//get ID
		if (subStr.length>=1)
			id = Integer.parseInt(subStr[0]);
		
		//get Data
		if (subStr.length >= 2)
		{
			System.out.println("split2 " + subStr.length + " split1:" + subStr[0] + " split2:" + subStr[1]);
			data = Integer.parseInt(subStr[1]);
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
	
	public boolean equalsFuzzy(ItemStack item)
	{
		if (item.getTypeId() == id)
		{
			return (item.getData().getData() == data || data == -1);
		}
		return false;
	}


	


}