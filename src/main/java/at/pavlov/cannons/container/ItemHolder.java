package at.pavlov.cannons.container;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.*;


//small class as at.pavlov.cannons.container for item id and data
public class ItemHolder
{
	private Material material;
	private String displayName;
	private List<String> lore;
	private boolean useTypeName;

	private static Class localeClass = null;
	private static Class craftItemStackClass = null, nmsItemStackClass = null, nmsItemClass = null;
	private static String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
	private static String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

	public ItemHolder(ItemStack item)
	{
		useTypeName = false;
        if (item == null){
            material=Material.AIR;
            displayName="";
            lore = new ArrayList<String>();
            return;
        }

		material = item.getType();

		if (item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
			if (meta.hasDisplayName() && meta.getDisplayName()!=null)
				displayName = meta.getDisplayName();
			else if (!meta.hasDisplayName()){
				useTypeName = true;
				displayName = getFriendlyName(item, true);
			}
			else
				displayName = "";
			if (meta.hasLore() && meta.getLore()!=null)
				lore = meta.getLore();
			else
				lore = new ArrayList<String>();
		}
	}

    @Deprecated
    public ItemHolder(int id)
    {
    	//not working
        this(Material.AIR);
    }

    public ItemHolder(Material material)
    {
        this(material, null, null);
    }

	public ItemHolder(Material material, String description, List<String> lore)
	{
        if (material != null)
		    this.material = material;
        else
            this.material = Material.AIR;
		if (description != null)
			this.displayName = ChatColor.translateAlternateColorCodes('&',description);
		else
			this.displayName = "";

		if (lore != null)
			this.lore = lore;
		else
			this.lore = new ArrayList<String>();
	}

	public ItemHolder(String str)
	{
        // data structure:
        // id;DESCRIPTION;LORE1;LORE2
        // HOE;COOL Item;Looks so cool;Fancy
        try
        {
        	material = Material.AIR;
            Scanner s = new Scanner(str).useDelimiter("\\s*;\\s*");
            if (s.hasNext()) {
                String next = s.next();
                if (next != null)
                    this.material = Material.matchMaterial(next);
                if (this.material == null) {
                    this.material = Material.AIR;
                }
            }

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
            System.out.println("[CANNONS] Error while converting " + str + ". Check formatting (minecraft:clock)");
        }
	}
	
	public SimpleBlock toSimpleBlock()
	{
		return new SimpleBlock(0, 0, 0, material);
	}
	
	public ItemStack toItemStack(int amount)
	{
		ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (this.hasDisplayName())
            meta.setDisplayName(this.displayName);
        if (this.hasLore())
            meta.setLore(this.lore);
        item.setItemMeta(meta);
        return item;
	}

	/**
	 * Creates a new BlockData instance for this Material, with all properties initialized to unspecified defaults.
	 * @return BlockData instance
	 */
	public BlockData toBlockData()
	{
		return this.material.createBlockData();
	}

	/**
	 * Creates a new BlockData instance for this Material, with all properties initialized to unspecified defaults, except for those provided in data.
	 * @return BlockData instance
	 */
	public BlockData toBlockData(String string)
	{
		return this.material.createBlockData(string);
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
		ItemHolder itemHolder = new ItemHolder(item);
		return equalsFuzzy(itemHolder);
	}
	
	
	/**
	 * compares id and data, but skips data comparison if one is -1
	 * @param item the item to compare
	 * @return true if both items are equal in data and id or only the id if one data = -1
	 */	
	public boolean equalsFuzzy(ItemHolder item)
	{
		if (item != null)
		{
			//System.out.println("item: " + item.getDisplayName() + " cannons " + this.getDisplayName());
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
			return item.getType().equals(this.material);
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
				return true;
			}
		}	
		return false;
	}
	
	public String toString()
	{
		return this.material + ":" + this.displayName + ":" + StringUtils.join(this.lore, ":");
	}

	public Material getType()
	{
		return this.material;
	}
	public void setType(Material material)
	{
		this.material = material;
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

	private static String capitalizeFully(String name) {
		if (name != null) {
			if (name.length() > 1) {
				if (name.contains("_")) {
					StringBuilder sbName = new StringBuilder();
					for (String subName : name.split("_"))
						sbName.append(subName.substring(0, 1).toUpperCase() + subName.substring(1).toLowerCase()).append(" ");
					return sbName.toString().substring(0, sbName.length() - 1);
				} else {
					return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
				}
			} else {
				return name.toUpperCase();
			}
		} else {
			return "";
		}
	}

	private static String getFriendlyName(Material material) {
		return material == null ? "Air" : getFriendlyName(new ItemStack(material), false);
	}

	private static String getFriendlyName(ItemStack itemStack, boolean checkDisplayName) {
		if (itemStack == null || itemStack.getType() == Material.AIR) return "Air";
		try {
			if (craftItemStackClass == null)
				craftItemStackClass = Class.forName(OBC_PREFIX + ".inventory.CraftItemStack");
			Method nmsCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);

			if (nmsItemStackClass == null) nmsItemStackClass = Class.forName(NMS_PREFIX + ".ItemStack");
			Object nmsItemStack = nmsCopyMethod.invoke(null, itemStack);

			Object itemName = null;
			if (checkDisplayName) {
				Method getNameMethod = nmsItemStackClass.getMethod("getName");
				itemName = getNameMethod.invoke(nmsItemStack);
			} else {
				Method getItemMethod = nmsItemStackClass.getMethod("getItem");
				Object nmsItem = getItemMethod.invoke(nmsItemStack);

				if (nmsItemClass == null) nmsItemClass = Class.forName(NMS_PREFIX + ".Item");

				Method getNameMethod = nmsItemClass.getMethod("getName");
				Object localItemName = getNameMethod.invoke(nmsItem);

				if (localeClass == null) localeClass = Class.forName(NMS_PREFIX + ".LocaleI18n");
				Method getLocaleMethod = localeClass.getMethod("get", String.class);

				Object localeString = localItemName == null ? "" : getLocaleMethod.invoke(null, localItemName);
				itemName = ("" + getLocaleMethod.invoke(null, localeString.toString() + ".name")).trim();
			}
			return itemName != null ? itemName.toString() : capitalizeFully(itemStack.getType().name().replace("_", " ").toLowerCase());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return capitalizeFully(itemStack.getType().name().replace("_", " ").toLowerCase());
	}
}