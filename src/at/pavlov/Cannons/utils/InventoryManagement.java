package at.pavlov.Cannons.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import at.pavlov.Cannons.dao.CannonData;

public class InventoryManagement 
{
	
	//############## TakeFromPlayerInventory   ################################
	public void TakeFromPlayerInventory(Player player, boolean inventory_take)
	{ 
		//take item from the player
		if (inventory_take == true)
		{
			int amount = player.getInventory().getItemInHand().getAmount();
			if (amount == 1) 
			{
				player.getInventory().removeItem(player.getInventory().getItemInHand());
			}
			else 
			{
				player.getInventory().getItemInHand().setAmount(amount-1);
			}
		}
	}
	
	//####################################  removeAmmoFromChest ##############################
	public boolean removeAmmoFromChests(CannonData cannon, int gunpowder, int projectile)
	{   
	    BlockFace face = cannon.face;
	    // goto the last first block of the cannon
	    Block block = cannon.location.getBlock().getRelative(face.getOppositeFace(), cannon.barrel_length-1);
	    	
	    //left and right chest
	    if (face == BlockFace.EAST || face == BlockFace.WEST)
	    {
	    	if (removeAmmoFromChest(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), gunpowder, projectile)) return true;		
	    }
	    else
	    {
	    	if (removeAmmoFromChest(block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST), gunpowder, projectile)) return true;
	    }
	    		
	    return false;
	}
	    
	//####################################  removeAmmoFromChest ##############################
	private boolean removeAmmoFromChest(Block block1, Block block2, int gunpowder, int projectile)
	{
		ArrayList<Inventory> invlist = new ArrayList<Inventory>();
	    //check if block is a chest
	    invlist = getInventories(block1, invlist);
	    invlist = getInventories(block2, invlist);
	    
	    //check if chests contain the projectile
	    if (containsItemInChests(invlist, projectile) == false) return false;
	    
	    //remove gunpowder
	    int removed_gunpowder = 0;
	    boolean notEnoughGunpowder = false;
	    do
	    {
	    	notEnoughGunpowder = !removeItemInChests(invlist, Material.SULPHUR.getId());
	    	removed_gunpowder++;
	    } while(removed_gunpowder < gunpowder && notEnoughGunpowder == false);
	    	
	    removed_gunpowder--;
	    if (notEnoughGunpowder == true)
	    {   
	    	//restore items
	    	addItemInChests(invlist,  Material.SULPHUR.getId(), removed_gunpowder);
	    	return false;
	    }
	    	
	    //remove projectile
   		if (removeItemInChests(invlist, projectile) == false)
   		{
   			//this case should not be called because we check if a projectile exist
	    	//restore items
	    	addItemInChests(invlist,  Material.SULPHUR.getId(), removed_gunpowder);
	    	//no projectile found 
	    	return false;
   		}
	    return true;
	}
	    
	//####################################  removeAmmoFromChest ##############################
	private boolean removeItemInChests(ArrayList<Inventory> invlist, int item)
	{
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext())
		{
			Inventory next = iter.next();
			// if inventory contains item
			if (next.contains(item) == true)
			{
				next.removeItem(new ItemStack(item,1));
				return true;
			}
		}
		return false;
	}
	
	//####################################  containsItemInChests ##############################
	private boolean containsItemInChests(ArrayList<Inventory> invlist, int item)
	{
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext())
		{
			if (iter.next().contains(item) == true)
			{
				return true;
			}
		}
		return false;
	}
	    
	//####################################  addItemInChests ##############################
	private boolean addItemInChests(ArrayList<Inventory> invlist, int item, int amount)
	{
		//return if there should be nothing removed
		if (amount <= 0) return true;
		
   		//return false if something is missing
		Iterator<Inventory> iter = invlist.iterator();
		while (iter.hasNext())
		{
			Inventory next = iter.next();
			//add items and returned hashmap is zero
			int size = next.addItem(new ItemStack(item, amount)).size();
			if (size == 0)
			{
				return true;
			}
		}
		return false;
	}
	      

   //############## getInventories   ################################
   private ArrayList<Inventory> getInventories(Block block, ArrayList<Inventory> list) 
   {
	   if (list == null) {
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
   
   //############## attached ################################
   private Chest attached(Block block) 
   {
	    // Find the first adjacent chest. Note: hacking of various sorts/degrees and/or
	    // other plugins might allow multiple chests to be adjacent. Deal with that later
	    // if it really becomes necessary (and at all possible to detect).
	   BlockFace[] FACES = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
	   for (BlockFace face : FACES) 
	   {
	        Block other = block.getRelative(face);
	        if (other.getType() == Material.CHEST) 
	        {
	            return (Chest) other.getState();    // Found it.
	        }
	   }
	   return null;    // No other adjacent chest.
	   }
}
