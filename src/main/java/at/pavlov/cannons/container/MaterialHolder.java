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
	private Material material;
	private int data;
	private String displayName;
	private List<String> lore;


	public MaterialHolder(ItemStack item)
	{
        if (item == null){
            material=Material.AIR;
            data=0;
            displayName="";
            lore = new ArrayList<String>();
            return;
        }

		material = item.getType();
		data = item.getData().getData();

		if (item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
			if (meta.hasDisplayName() && meta.getDisplayName()!=null)
				displayName = meta.getDisplayName();
			else
				displayName = "";
			if (meta.hasLore() && meta.getLore()!=null)
				lore = meta.getLore();
			else
				lore = new ArrayList<String>();
		}
	}

    @Deprecated
    public MaterialHolder(int id, int data)
    {
        this(Material.getMaterial(id), data);
    }

    public MaterialHolder(Material material, int data)
    {
        this(material, data, null, null);
    }

	public MaterialHolder(Material material, int data, String description, List<String> lore)
	{
        if (material != null)
		    this.material = material;
        else
            this.material = Material.AIR;
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
            material = Material.AIR;
            data = -1;
            Scanner s = new Scanner(str).useDelimiter("\\s*:\\s*");
            if (s.hasNext()) {
                String next = s.next();
                if (next != null)
                    this.material = Material.matchMaterial(next);
                if (this.material == null) {
                    System.out.println("missing id value in: " + str);
                    this.material = Material.AIR;
                }
            }

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
		return new BaseBlock(material.getId(), data);
	}
	
	public ItemStack toItemStack(int amount)
	{
		ItemStack item = new ItemStack(material, amount, (short) data);
        ItemMeta meta = item.getItemMeta();
        if (this.hasDisplayName())
            meta.getDisplayName();
        if (this.hasLore())
            meta.setLore(this.lore);
        item.setItemMeta(meta);
        return item;
	}
	


    /**
     * compares the id of two Materials
     * @param material material to compare
     * @return true if both material are equal
     */
	public boolean equals(Material material)
	{
        return material != null && material.equals(this.material);
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
            if ((this.hasDisplayName() && !item.hasDisplayName()) || (!this.hasDisplayName() && item.hasDisplayName()))
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
			if (item.getType().equals(this.material))
			{
				return (item.getData() == data || data == -1 || item.getData() == -1);
			}
		}	
		return false;
	}
	
	/**
	 * compares id and data, but skips data comparison if one is -1
	 * @param block item to compare
	 * @return true if both items are equal in data and id or only the id if one data = -1
	 */
	public boolean equalsFuzzy(Block block)
	{
		//System.out.println("id:" + item.getId() + "-" + id + " data:" + item.getData() + "-" + data);
		if (block != null)
		{
			if (block.getType().equals(this.material))
			{
				return (block.getData() == data || data == -1 || block.getData() == -1);
			}
		}	
		return false;
	}
	
	public String toString()
	{
		return this.material + ":" + this.data + ":" + this.displayName + ":" + String.join(":", this.lore);
	}

	public Material getType()
	{
		return this.material;
	}
	public void setType(Material material)
	{
		this.material = material;
	}
	public int getData()
	{
		return data;
	}
	public void setData(int data)
	{
		this.data = data;
	}

	public String getDisplayName() {
		return displayName;
	}

    public boolean hasDisplayName(){
        return this.displayName!= null && !this.displayName.equals("");
    }

	public List<String> getLore() {
		return lore;
	}

    public boolean hasLore(){
        return this.lore!=null && this.lore.size()>0;
    }
}