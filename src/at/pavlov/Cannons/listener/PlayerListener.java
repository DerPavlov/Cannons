package at.pavlov.Cannons.listener;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import at.pavlov.Cannons.CalcAngle;
import at.pavlov.Cannons.CannonManager;
import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.FireCannon;
import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.projectile.Projectile;
import at.pavlov.Cannons.utils.CannonsUtil;

public class PlayerListener implements Listener
{
	@SuppressWarnings("unused")
	private Config config;
	private UserMessages userMessages;
	private Cannons plugin;
	private CannonManager cannonManager;
	private FireCannon fireCannon;
	private CalcAngle calcAngle;

	public PlayerListener(Cannons plugin)
	{
		this.plugin = plugin;
		this.config = this.plugin.getmyConfig();
		this.userMessages = this.plugin.getmyConfig().getUserMessages();
		this.cannonManager = this.plugin.getCannonManager();
		this.fireCannon = this.plugin.getFireCannon();
		this.calcAngle = this.plugin.getCalcAngle();
	}

	// ########### PlayerMove #######################################
	@EventHandler
	public void PlayerMove(PlayerMoveEvent event)
	{
		// only active if the player is in aiming mode
		calcAngle.PlayerMove(event.getPlayer());
	}

	// ########### BlockFromTo #######################################
	@EventHandler
	public void BlockFromTo(BlockFromToEvent event)
	{
		Block block = event.getToBlock();
		if (block.getType() == Material.STONE_BUTTON || block.getType() == Material.TORCH)
		{
			if (cannonManager.getCannon(block.getLocation(), null) != null)
			{
				event.setCancelled(true);
			}
		}
	}

	// ########### EntityExplode #######################################
	public void EntityExplode(EntityExplodeEvent event)
	{
		List<Block> blocks = event.blockList();

		// protect the cannon from explosion
		for (int i = 0; i < blocks.size(); i++)
		{
			Block block = blocks.get(i);

			// if it is a cannon block
			if (cannonManager.getCannon(block.getLocation(), null) != null)
			{
				// get distance to the impact
				double distance = event.getLocation().distance(block.getLocation());
				if (distance < 2)
				{
					// closer impact will break the cannon
					cannonManager.removeCannon(block.getLocation());
				}
				else
				{
					// impact farther away will not destroy the cannon
					blocks.remove(i--);
				}

			}
		}
	}

	// ########### BlockPistonRetract #######################################
	@EventHandler
	public void BlockPistonRetract(BlockPistonRetractEvent event)
	{
		// when piston is sticky and has a cannon block attached delete the
		// cannon
		if (event.isSticky() == true)
		{
			Location loc = event.getBlock().getRelative(event.getDirection(), 2).getLocation();
			Cannon cannon = cannonManager.getCannon(loc, null);
			if (cannon != null)
			{
				cannonManager.removeCannon(cannon);
			}
		}
	}

	// ########### BlockPistonExtend #######################################
	@EventHandler
	public void BlockPistonExtend(BlockPistonExtendEvent event)
	{
		// when the moved block is a cannonblock
		Iterator<Block> iter = event.getBlocks().iterator();
		while (iter.hasNext())
		{
			// if moved block is cannonBlock delete cannon
			Cannon cannon = cannonManager.getCannon(iter.next().getLocation(), null);
			if (cannon != null)
			{
				cannonManager.removeCannon(cannon);
			}
		}
	}

	// ########### BlockBurn #######################################
	@EventHandler
	public void BlockBurn(BlockBurnEvent event)
	{
		// the cannon will not burn down
		if (cannonManager.getCannon(event.getBlock().getLocation(), null) != null)
		{
			event.setCancelled(true);
		}
	}

	/**
	 * if one block of the cannon is destroyed, it is removed from the list of cannons
	 * @param event
	 */
	@EventHandler
	public void BlockBreak(BlockBreakEvent event)
	{
		// if deleted block is cannonBlock delete cannon
		Cannon cannon = cannonManager.getCannon(event.getBlock().getLocation(), null);
		if (cannon != null)
		{
			cannonManager.removeCannon(cannon);
		}
	}

	/**
	 * cancels the event if the player click a cannon with water
	 * 
	 * @param event
	 */
	@EventHandler
	public void PlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		// if player loads a lava/water bucket in the cannon
		Location blockLoc = event.getBlockClicked().getLocation();

		Cannon cannon = cannonManager.getCannon(blockLoc, event.getPlayer().getName());

		// check if it is a cannon
		if (cannon != null)
		{
			// data =-1 means no data check, all buckets are allowed
			Projectile projectile = plugin.getProjectile(event.getBucket().getId(), -1);
			if (projectile != null) event.setCancelled(true);
		}
	}

	/**
	 * Create a cannon if the building process is finished Deletes a projectile
	 * if loaded Checks for redstone torches if built
	 * 
	 * @param event
	 */
	@EventHandler
	public void BlockPlace(BlockPlaceEvent event)
	{
		Block block = event.getBlockPlaced();
		Location blockLoc = block.getLocation();

		// setup a new cannon
		cannonManager.getCannon(blockLoc, event.getPlayer().getName());

		// Checks if the cannon building is complete - Checks also for
		// permissions redstonetorches
		/*
		 * if (cannon != null) { // no block place for firing the cannon if
		 * (block.getType() == Material.TORCH || block.getType() ==
		 * Material.STONE_BUTTON) { // change location to barrel Block
		 * barrelBlock = event.getBlockAgainst();
		 * 
		 * // if placed on snow the torch must be moved one down if
		 * (barrelBlock.
		 * getLocation().equals(event.getBlockPlaced().getLocation())) {
		 * barrelBlock = barrelBlock.getRelative(BlockFace.DOWN); } blockLoc =
		 * barrelBlock.getLocation(); } //set up a new cannon
		 * cannonManager.getCannon(blockLoc); }
		 */

		// delete placed projectile if clicked against the barrel
		if (event.getBlockAgainst() != null)
		{
			Location barrel = event.getBlockAgainst().getLocation();

			// check if block is cannonblock
			Cannon cannon = cannonManager.getCannon(barrel, event.getPlayer().getName());
			if (cannon != null)
			{
				// delete projectile
				Projectile projectile = plugin.getProjectile(block.getTypeId(), block.getData());
				if (projectile != null && cannon.getCannonDesign().canLoad(projectile))
				{
					// check if the placed block is not part of the cannon
					if (!cannon.isCannonBlock(event.getBlock()))
					{
						event.setCancelled(true);
					}
				}
			}
		}

		// Place redstonetorch under to the cannon
		if (event.getBlockPlaced().getType() == Material.REDSTONE_TORCH_ON || event.getBlockPlaced().getType() == Material.REDSTONE_TORCH_OFF)
		{
			plugin.logDebug("redstone torch 1");
			// check cannon
			Location loc = event.getBlock().getRelative(BlockFace.UP).getLocation();
			Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getName());
			if (cannon != null)
			{
				// check permissions
				if (event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()) == false)
				{

					plugin.logDebug("redstone torch 3");
					//check if the placed block is in the redstone torch interface
					if (cannon.isRedstoneTorchInterface(event.getBlock().getLocation()))
					{
						userMessages.displayMessage(event.getPlayer(), MessageEnum.PermissionErrorRedstone);
						event.setCancelled(true);
					}
				}
			}
		}

		// Place redstone wire next to the button
		if (event.getBlockPlaced().getType() == Material.REDSTONE_WIRE)
		{
			// check cannon
			for (Block b : CannonsUtil.HorizontalSurroundingBlocks(event.getBlock()))
			{
				Location loc = b.getLocation();
				Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getName());
				if (cannon != null)
				{
					// check permissions
					if (event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()) == false)
					{	
						//check if the placed block is in the redstone wire interface
						if (cannon.isRedstoneWireInterface(event.getBlock().getLocation()))
						{
							userMessages.displayMessage(event.getPlayer(), MessageEnum.PermissionErrorRedstone);
							event.setCancelled(true);
						}
					}
				}
			}
		}

		// cancel igniting of the cannon
		if (event.getBlock().getType() == Material.FIRE)
		{
			// check cannon
			Location loc = event.getBlockAgainst().getLocation();
			if (cannonManager.getCannon(loc, event.getPlayer().getName()) != null)
			{
				event.setCancelled(true);
			}
		}
	}

	/**
	 * handles redstone events (torch, wire, repeater, button)
	 * 
	 * @param event
	 */
	@EventHandler
	public void RedstoneEvent(BlockRedstoneEvent event)
	{
		Block block = event.getBlock();

		// ##########  redstone torch fire
		if (block.getType() == Material.REDSTONE_TORCH_ON)
		{
			// go one block up and check this is a cannon
			Cannon cannon = cannonManager.getCannon(block.getRelative(BlockFace.UP).getLocation(), null);

			if (cannon != null)
			{
				// there is cannon next to the torch - check if the torch is
				// place right
				if (cannon.isRedstoneTorchInterface(block.getLocation())) fireCannon.prepareFire(cannon, null, !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
			}
		}

		// ##########  redstone wire fire
		if (block.getType() == Material.REDSTONE_WIRE)
		{
			// block is powered
			if (block.getData() == 0)
			{
				// check all block next to this if there is a cannon
				for (Block b : CannonsUtil.HorizontalSurroundingBlocks(block))
				{
					Cannon cannon = cannonManager.getCannon(b.getLocation(), null);
					if (cannon != null)
					{
						// there is cannon next to the wire - check if the wire
						// is place right
						if (cannon.isRedstoneWireInterface(block.getLocation())) fireCannon.prepareFire(cannon, null, !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
					}
				}
			}
		}

		
		 // ##########  fire with Button 
		Cannon cannon = cannonManager.getCannon(event.getBlock().getLocation(), null);
		if (cannon != null) 
		{ 
			//check if the button is a loading firing interface of the cannon
			if (cannon.isRestoneTrigger(event.getBlock().getLocation()))
			{ 
				fireCannon.prepareFire(cannon, null, true); 
			}
		}
		 

	}



	/**
	 * Handles event if player interacts with the cannon
	 * @param event
	 */
	@EventHandler
	public void PlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			Block clickedBlock;
			if (event.getClickedBlock() == null)
			{
				// no clicked block - get block player is looking at

				clickedBlock = event.getPlayer().getTargetBlock(null, 4);
			}
			else
			{
				clickedBlock = event.getClickedBlock();
			}
			Player player = event.getPlayer();
			Location barrel = clickedBlock.getLocation();

			// find cannon or add it to the list
			Cannon cannon = cannonManager.getCannon(barrel, player.getName());

			if (cannon == null) return;

			// get cannon design
			CannonDesign design = cannon.getCannonDesign();

			// prevent eggs and snowball from firing when loaded into the gun
			Material ItemInHand = player.getItemInHand().getType();
			if (ItemInHand == Material.EGG || ItemInHand == Material.SNOW_BALL || ItemInHand == Material.MONSTER_EGG)
			{
				event.setCancelled(true);
			}

			plugin.logDebug("interact event");
			// ########## Load Projectile ######################
			Projectile projectile = plugin.getProjectile(event.getItem());
			if (cannon.isLoadingBlock(clickedBlock.getLocation()) && projectile != null)
			{
				plugin.logDebug("load projectile");
				// load projectile
				MessageEnum message = cannon.loadProjectile(projectile, player);
				// display message
				userMessages.displayMessage(player, message, cannon);
			}

			// ########## Barrel clicked with gunpowder
			if (cannon.isLoadingBlock(clickedBlock.getLocation()) && design.getGunpowderType().equalsFuzzy(event.getItem()))
			{
				plugin.logDebug("load gunpowder");
				// load gunpowder
				MessageEnum message = cannon.loadGunpowder(player);

				// display message
				userMessages.displayMessage(player, message, cannon);
			}

			// ############ Torch clicked ############################
			if (cannon.isRightClickTrigger(clickedBlock.getLocation()))
			{
				plugin.logDebug("fire torch");
				MessageEnum message = fireCannon.prepareFire(cannon, player, !design.isAmmoInfiniteForPlayer());

				// display message
				userMessages.displayMessage(player, message, cannon);
				return;
			}

			// ############ Button clicked ############################
			if (cannon.isRestoneTrigger(clickedBlock.getLocation()))
			{
				plugin.logDebug("fire button");
				//don't fire the cannon, only disply a message
				MessageEnum message = fireCannon.getPrepareFireMessage(cannon, player);
				userMessages.displayMessage(player, message, cannon);

				return;
			}

			// ############ set angle ################################
			if ((player.getItemInHand().getType() == Material.AIR || player.getItemInHand().getType() == Material.WATCH) && cannon.isLoadingBlock(clickedBlock.getLocation()))
			{
				calcAngle.ChangeAngle(cannon, event.getAction(), event.getBlockFace(), player);

				// update Signs
				cannon.updateCannonSigns();
				return;
			}
		}
		else
		{
			// all other actions will stop aiming mode
			if (event.getAction() == Action.RIGHT_CLICK_AIR) calcAngle.disableAimingMode(event.getPlayer());
		}
		return;
	}

}
