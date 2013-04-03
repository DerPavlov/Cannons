package at.pavlov.Cannons.container;

import com.sk89q.worldedit.blocks.BaseBlock;

//small class as container for item id and data
public class MaterialHolder
{
	private int id;
	private int data;
	
	public MaterialHolder(int _id, int _data)
	{
		id = _id;
		data = _data;
	}
	
	public BaseBlock toBaseBlock()
	{
		return new BaseBlock(id, data);
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
	


}