package at.pavlov.cannons.container;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.util.*;

//small class as at.pavlov.cannons.container for item id and data
public class MaterialHolder
{
	private int id;
	private int data;
	private String displayName;
	private List<String> lore;


	public MaterialHolder(ItemStack item)
	{
		id = item.getTypeId();
		data = item.getData().getData();
		ItemMeta meta = item.getItemMeta();
		if (meta != null){
			if (meta.hasDisplayName())
				displayName = meta.getDisplayName();
			else
				displayName = "";
			if (meta.hasLore())
				lore = meta.getLore();
			else
				lore = new ArrayList<String>();
		}
	}

    public MaterialHolder(int id, int data)
    {
        this.id = id;
        this.data = data;
        this.displayName = "";
        this.lore = new ArrayList<String>();
    }

	public MaterialHolder(int id, int data, String description, List<String> lore)
	{
		this.id = id;
		this.data = data;
		if (description != null)
			this.displayName = ChatColor.translateAlternateColorCodes('&',description);
		else
			this.displayName = "";

		if (lore != null)
			this.lore = lore;
		else
			this.lore = new ArrayList<String>();
	}

	public MaterialHolder(String str)
	{
        // data structure:
        // data:id:DESCRIPTION:LORE1:LORE2
        // 10:0:COOL Item:Looks so cool:Fancy
        try
        {
            id = 0;
            data = -1;
            Scanner s = new Scanner(str).useDelimiter("\\s*:\\s*");
            if (s.hasNext())
                id = s.nextInt();
            else
                System.out.println("missing id value in: " + str);

            if (s.hasNext())
                data = s.nextInt();

			if (s.hasNext())
				displayName = ChatColor.translateAlternateColorCodes('&', s.next());
			else
				displayName = "";

			lore = new ArrayList<String>();
			while (s.hasNext()){
                String nextStr = s.next();
                if (!nextStr.equals(""))
				    lore.add(nextStr);
			}

            s.close();
        }
        catch(Exception e)
        {
            System.out.println("Error while converting " + str + ". Check formatting (10:0)");
        }
	}
	
	public BaseBlock toBaseBlock()
	{
		return new BaseBlock(id, data);
	}
	
	public ItemStack toItemStack(int amount)
	{
		ItemStack item = new ItemStack(id, amount, (short) data);
        ItemMeta meta = item.getItemMeta();
        if (!this.displayName.equals(""))
            meta.getDisplayName();
        meta.setLore(this.lore);
        item.setItemMeta(meta);
        return item;
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

    /**
     * compares the id of two Materials
     * @param material material to compare
     * @return true if both material are equal
     */
	public boolean equals(Material material)
	{
        return material != null && material.getId() == id;
    }
	
	/**
	 * compares id and data, but skips data comparison if one is -1
	 * @param item item to compare
	 * @return true if both items are equal in data and id or only the id if one data = -1
	 */
	public boolean equalsFuzzy(ItemStack item)
	{
		MaterialHolder materialHolder = new MaterialHolder(item);
		return equalsFuzzy(materialHolder);
	}
	
	
	/**
	 * compares id and data, but skips data comparison if one is -1
	 * @param item the item to compare
	 * @return true if both items are equal in data and id or only the id if one data = -1
	 */	
	public boolean equalsFuzzy(MaterialHolder item)
	{
		if (item != null)
		{
            //Item does not have the required display name
            if (this.hasDisplayName() && !item.hasDisplayName())
                return false;
            //Display name do not match
            if (item.hasDisplayName() && this.hasDisplayName() && !item.getDisplayName().equals(displayName))
                return false;


            if (this.hasLore()) {
                //does Item have a Lore
                if (!item.hasLore())
                    return false;

                Collection<String> similar = new HashSet<String>(this.lore);

                int size = similar.size();
                similar.retainAll(item.getLore());
                if (similar.size() < size)
                    return false;
            }
			if (item.getId() == id)
			{
				return (item.getData() == data || data == -1 || item.getData() == -1);
			}
		}	
		return false;
	}
	
	/**
	 * compares id and data, but skips data comparison if one is -1
	 * @param item item to compare
	 * @return true if both items are equal in data and id or only the id if one data = -1
	 */
	public boolean equalsFuzzy(Block item)
	{
		//System.out.println("id:" + item.getId() + "-" + id + " data:" + item.getData() + "-" + data);
		if (item != null)
		{
			if (item.getTypeId() == id)
			{
				return (item.getData() == data || data == -1 || item.getData() == -1);
			}
		}	
		return false;
	}
	
	public String toString()
	{
		return this.id + ":" + this.data + ":" + this.displayName + ":" + String.join(":", this.lore);
	}


	public String getDisplayName() {
		return displayName;
	}

    public boolean hasDisplayName(){
        return !this.displayName.equals("");
    }

	public List<String> getLore() {
		return lore;
	}

    public boolean hasLore(){
        return this.lore.size()>0;
    }
}