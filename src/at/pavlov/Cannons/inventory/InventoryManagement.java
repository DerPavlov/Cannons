package at.pavlov.Cannons.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class InventoryManagement
{

	// ############## TakeFromPlayerInventory ################################
	public void TakeFromPlayerInventory(Player player, boolean inventory_take)
	{
		// take item from the player
		if (inventory_take == true)
		{
			int amount = player.getInventory().getItemInHand().getAmount();
			if (amount == 1)
			{
				player.getInventory().removeItem(player.getInventory().getItemInHand());
			}
			else
			{
				player.getInventory().getItemInHand().setAmount(amount - 1);
			}
		}
	}

	// #################################### removeAmmoFromChest
	public static boolean removeAmmoFromChest(Block block1, ItemStack gunpowder, ItemStack projectile)
	{
		ArrayList<Inventory> invlist = new ArrayList<Inventory>();
		// check if block is a chest
		invlist = getInventories(block1, invlist);

		// check if one of the chests contains the projectile - if not no
		// gunpowder is removed

		if (containsItemInChests(invlist, projectile) == false) {

		return false; }

		// remove gunpowder
		int startingAmount = gunpowder.getAmount();
		gunpowder = removeItemInChests(invlist, gunpowder);

		// there was not enough gunpowder in the chests, but the used gunpowder
		// back
		if (gunpowder.getAmount() > 0)
		{
			// not enough gunpowder - reset amount
			gunpowder.setAmount(startingAmount - gunpowder.getAmount());
			addItemInChests(invlist, gunpowder);
			return false;
		}

		// remove projectile
		removeItemInChests(invlist, projectile);

		return true;
	}

	// #################################### removeAmmoFromChest ##############
	private static ItemStack removeItemInChests(ArrayList<Inventory> invlist, ItemStack item)
	{
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext() && item.getAmount() > 0)
		{
			Inventory next = iter.next();
			item = remove(next, item);
		}

		// true if all item have been removed
		return item;
	}

	// #################################### containsItemInChests ###############
	private static boolean containsItemInChests(ArrayList<Inventory> invlist, ItemStack item)
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
	 * 
	 * @param invlist
	 * @param item
	 * @return
	 */
	private static boolean addItemInChests(ArrayList<Inventory> invlist, ItemStack item)
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
			if (size == 0) { return true; }
		}
		return false;
	}

	// ############## getInventories ################################
	private static ArrayList<Inventory> getInventories(Block block, ArrayList<Inventory> list)
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
