package at.pavlov.cannons.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import at.pavlov.cannons.projectile.Projectile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import at.pavlov.cannons.cannon.Cannon;


public class InventoryManagement
{

	/**
	 * removes the given number of items from the players hand
	 * @param player
	 * @param numberOfItems
	 */
	public static void TakeFromPlayerInventory(Player player, int numberOfItems)
	{
		for (int i = 0; i < numberOfItems; i ++)
		{
			int amount = player.getInventory().getItemInHand().getAmount();
			if (amount == 1)
			{
				//last item removed
				player.getInventory().removeItem(player.getInventory().getItemInHand());
				break;
			}
			else
			{
				//remove one item
				player.getInventory().getItemInHand().setAmount(amount - 1);
			}
		}
	}


	// #################################### removeAmmoFromChest ##############
	public static ItemStack removeItemInChests(List<Inventory> invlist, ItemStack item)
	{
		if (item == null) return null;
		
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext() && item.getAmount() > 0)
		{
			Inventory next = iter.next();
			item = remove(next, item);
		}

        //the amount of remaining items
		return item;
	}

	// #################################### containsItemInChests ###############
	public static boolean containsItemInChests(List<Inventory> invlist, ItemStack item)
	{
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext())
		{
			if (contains(iter.next(), item)) { return true; }
		}
		return false;
	}

	/**
	 * returns true if the item is in the inventory. If datavalue < 0 it is not
	 * compared
	 * 
	 * @param inv
	 * @param item
	 * @return
	 */
	private static boolean contains(Inventory inv, ItemStack item)
	{
		if (inv == null)
			return false;

		for (ItemStack invItem : inv.getContents())
		{
			if (invItem != null && invItem.getTypeId() == item.getTypeId() && invItem.getData().getData() == item.getData().getData())
				return true;
		}

		return false;
	}

	/**
	 * removes item from in the inventory. If datavalue < 0 it is not compared
	 * 
	 * @param inv
	 * @param item
	 * @return the amount of not removed items
	 */
	private static ItemStack remove(Inventory inv, ItemStack item)
	{
		if (inv == null || item == null)
			return item;

		HashMap<Integer, ItemStack> itemMap = inv.removeItem(item);

		// all items have been removed
		if (itemMap.size() == 0)
		{
			item.setAmount(0);
			return item;
		}

		// not all items have been removed
		for (ItemStack newItem : itemMap.values())
		{
			// set new amount for item
			return newItem;
		}

		// return untouched item - no item removed
		return item;
	}

	/**
	 * puts an itemstack in the first empty space of the given inventories
	 * @param invlist
	 * @param item
	 * @return
	 */
	public static boolean addItemInChests(List<Inventory> invlist, ItemStack item)
	{
		// return if there should be nothing removed
		if (item == null || item.getAmount() == 0)
			return true;

		// return false if something is missing
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext())
		{
			Inventory next = iter.next();
			// add items and returned hashmap is zero
			int size = next.addItem(item).size();
			if (size == 0)
                return true;
		}
		return false;
	}

    /**
     * returns the inventory of this block if valid, else null
     * @param block
     * @param list
     * @return
     */
	public static List<Inventory> getInventories(Block block, List<Inventory> list)
	{
		if (list == null)
		{
			list = new ArrayList<Inventory>();
		}
		if (block.getType() == Material.CHEST)
		{
			Chest chest = (Chest) block.getState();
			list.add(chest.getInventory());

			Chest other = attached(block);
			if (other != null)
			{
				list.add(other.getInventory());
			}
		}
		return list;
	}

	// ############## attached ################################
	private static Chest attached(Block block)
	{
		// Find the first adjacent chest. Note: hacking of various sorts/degrees
		// and/or
		// other plugins might allow multiple chests to be adjacent. Deal with
		// that later
		// if it really becomes necessary (and at all possible to detect).
		BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
		for (BlockFace face : FACES)
		{
			Block other = block.getRelative(face);
			if (other.getType() == Material.CHEST) 
			{ 
				// Found it.
				return (Chest) other.getState();
			
			}
		}
		return null; // No other adjacent chest.
	}

}
