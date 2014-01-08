package at.pavlov.cannons.listener;

import java.util.Iterator;

import at.pavlov.cannons.event.CannonRedstoneEvent;
import at.pavlov.cannons.event.CannonUseEvent;
import at.pavlov.cannons.event.InteractAction;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import at.pavlov.cannons.scheduler.CalcAngle;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.FireCannon;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.utils.CannonsUtil;

public class PlayerListener implements Listener
{
	private Config config;
	private UserMessages userMessages;
	private Cannons plugin;
	private CannonManager cannonManager;
	private FireCannon fireCannon;
	private CalcAngle calcAngle;

	public PlayerListener(Cannons plugin)
	{
		this.plugin = plugin;
		this.config = this.plugin.getMyConfig();
		this.userMessages = this.plugin.getMyConfig().getUserMessages();
		this.cannonManager = this.plugin.getCannonManager();
		this.fireCannon = this.plugin.getFireCannon();
		this.calcAngle = this.plugin.getCalcAngle();
	}

	// ########### PlayerMove #######################################
	@EventHandler
	public void PlayerMove(PlayerMoveEvent event)
	{
		// only active if the player is in aiming mode
		if (calcAngle.distanceCheck(event.getPlayer(), calcAngle.getCannonInAimingMode(event.getPlayer())) == false)
        {
            userMessages.displayMessage(event.getPlayer(), MessageEnum.AimingModeTooFarAway);
            MessageEnum message = calcAngle.disableAimingMode(event.getPlayer());
            userMessages.displayMessage(event.getPlayer(), message);
        }


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
			Projectile projectile = plugin.getProjectile(cannon, event.getBucket().getId(), -1);
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

		// delete placed projectile or gunpowder if clicked against the barrel
		if (event.getBlockAgainst() != null)
		{
			Location barrel = event.getBlockAgainst().getLocation();

			// check if block is cannonblock
			Cannon cannon = cannonManager.getCannon(barrel, event.getPlayer().getName(), true);
			if (cannon != null)
			{
				// delete projectile
				Projectile projectile = plugin.getProjectile(cannon, block.getTypeId(), block.getData());
				if (projectile != null && cannon.getCannonDesign().canLoad(projectile))
				{
					// check if the placed block is not part of the cannon
					if (!cannon.isCannonBlock(event.getBlock()))
					{
						event.setCancelled(true);
					}
				}
				// delete gunpowder block
				if (cannon.getCannonDesign().getGunpowderType().equalsFuzzy(event.getBlock()))
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
			// check cannon
			Location loc = event.getBlock().getRelative(BlockFace.UP).getLocation();
			Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getName(), true);
			if (cannon != null)
			{
				// check permissions
				if (event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()) == false)
				{
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
				Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getName(), true);
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
			if (cannonManager.getCannon(loc, event.getPlayer().getName(), true) != null)
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
		if (block == null) return;
		
		plugin.logDebug("Redstone event was fired by " + block.getType());

		// ##########  redstone torch fire
		// off because it turn form off to on
		if (block.getType() == Material.REDSTONE_TORCH_ON)
		{
			// go one block up and check this is a cannon
			Cannon cannon = cannonManager.getCannon(block.getRelative(BlockFace.UP).getLocation(), null);

			if (cannon != null)
			{
				// there is cannon next to the torch - check if the torch is
				// place right

				plugin.logDebug("redstone torch");
				if (cannon.isRedstoneTorchInterface(block.getLocation())) 
                {
                    CannonRedstoneEvent redEvent = new CannonRedstoneEvent(cannon);
                    Bukkit.getServer().getPluginManager().callEvent(redEvent);

                    if (redEvent.isCancelled())
                        return;

                    MessageEnum message = fireCannon.prepareFire(cannon, null, cannon.getCannonDesign().isAutoreloadRedstone(), !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
                    plugin.logDebug("fire cannon returned: " + message.getString());
                }
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

						plugin.logDebug("redstone wire ");
						if (cannon.isRedstoneWireInterface(block.getLocation()))
                        {
                            CannonRedstoneEvent redEvent = new CannonRedstoneEvent(cannon);
                            Bukkit.getServer().getPluginManager().callEvent(redEvent);

                            if (redEvent.isCancelled())
                                return;

                            MessageEnum message = fireCannon.prepareFire(cannon, null, cannon.getCannonDesign().isAutoreloadRedstone(), !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
                            plugin.logDebug("fire cannon returned: " + message.getString());
                        }
					}
				}
			}
		}
		
		// ##########  redstone repeater and comparator fire
		if (block.getType() == Material.DIODE_BLOCK_OFF || block.getType() == Material.REDSTONE_COMPARATOR_OFF)
		{
			// check all block next to this if there is a cannon
			for (Block b : CannonsUtil.HorizontalSurroundingBlocks(block))
			{
				Cannon cannon = cannonManager.getCannon(b.getLocation(), null);
				if (cannon != null)
				{
					// there is cannon next to the wire - check if the wire
					// is place right
					plugin.logDebug("redstone repeater ");
					if (cannon.isRedstoneRepeaterInterface(block.getLocation())) 
                    {
                        CannonRedstoneEvent redEvent = new CannonRedstoneEvent(cannon);
                        Bukkit.getServer().getPluginManager().callEvent(redEvent);

                        if (redEvent.isCancelled())
                            return;

                        MessageEnum message = fireCannon.prepareFire(cannon, null, cannon.getCannonDesign().isAutoreloadRedstone(), !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
                        plugin.logDebug("fire cannon returned: " + message.getString());
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

                //get the user of the cannon
                Player player = null;
                if (cannon.getLastUser() != null)
                    player = Bukkit.getPlayer(cannon.getLastUser());
                //reset user
                cannon.setLastUser("");
                if (player == null)
                    return;


                plugin.logDebug("Redfire with button by " + player.getName());

                //register event with bukkit
                CannonUseEvent useEvent = new CannonUseEvent(cannon, player, InteractAction.fireButton);
                Bukkit.getServer().getPluginManager().callEvent(useEvent);

                if (useEvent.isCancelled())
                    return;

                //execute event
                boolean autoreload = player.isSneaking() && player.hasPermission(cannon.getCannonDesign().getPermissionAutoreload());
                MessageEnum message =  fireCannon.prepareFire(cannon, player, autoreload, !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
                userMessages.displayMessage(player, message, cannon);
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
			Cannon cannon = cannonManager.getCannon(barrel, player.getName(), true);

			//no cannon found - maybe the player has click into the air to stop aiming
			if (cannon == null)
			{
				// all other actions will stop aiming mode
				if (event.getAction() == Action.RIGHT_CLICK_AIR)
                {
					MessageEnum message = calcAngle.disableAimingMode(event.getPlayer());
                    userMessages.displayMessage(player, message);
                }
				return;
			}

			// get cannon design
			CannonDesign design = cannon.getCannonDesign();

			// prevent eggs and snowball from firing when loaded into the gun
			Material ItemInHand = player.getItemInHand().getType();
			if (ItemInHand == Material.EGG || ItemInHand == Material.SNOW_BALL || ItemInHand == Material.MONSTER_EGG || ItemInHand == Material.ENDER_PEARL || ItemInHand == Material.FIREWORK)
			{
				event.setCancelled(true);
			}

			plugin.logDebug("player interact event fired");
			// ############ set angle ################################
			if ((config.getToolAdjust().equalsFuzzy(player.getItemInHand()) || config.getToolAutoaim().equalsFuzzy(player.getItemInHand())) && cannon.isLoadingBlock(clickedBlock.getLocation()))
			{
				plugin.logDebug("change cannon angle");

                //fire event
                CannonUseEvent useEvent = new CannonUseEvent(cannon, player, InteractAction.adjust);
                Bukkit.getServer().getPluginManager().callEvent(useEvent);

                if (useEvent.isCancelled())
                    return;

				MessageEnum message = calcAngle.ChangeAngle(cannon, event.getAction(), event.getBlockFace(), player);

				userMessages.displayMessage(player, message, cannon);
				
				// update Signs
				cannon.updateCannonSigns();
				return;
			}
			// ########## Load Projectile ######################
			Projectile projectile = plugin.getProjectile(cannon, event.getItem());
			if (cannon.isLoadingBlock(clickedBlock.getLocation()) && projectile != null)
			{
				plugin.logDebug("load projectile");

                //fire event
                CannonUseEvent useEvent = new CannonUseEvent(cannon, player, InteractAction.loadProjectile);
                Bukkit.getServer().getPluginManager().callEvent(useEvent);

                if (useEvent.isCancelled())
                    return;

				// load projectile
				MessageEnum message = cannon.loadProjectile(projectile, player);
				// display message
				userMessages.displayMessage(player, message, cannon);
			}

			// ########## Barrel clicked with gunpowder
			if (cannon.isLoadingBlock(clickedBlock.getLocation()) && design.getGunpowderType().equalsFuzzy(event.getItem()))
			{
				plugin.logDebug("load gunpowder");

                //fire event
                CannonUseEvent useEvent = new CannonUseEvent(cannon, player, InteractAction.loadGunpowder);
                Bukkit.getServer().getPluginManager().callEvent(useEvent);

                if (useEvent.isCancelled())
                    return;


                // load gunpowder
				MessageEnum message = cannon.loadGunpowder(player);

   				// display message
				userMessages.displayMessage(player, message, cannon);
			}

			// ############ Torch clicked ############################
			if (cannon.isRightClickTrigger(clickedBlock.getLocation()))
			{
				plugin.logDebug("fire torch");

                //fire event
                CannonUseEvent useEvent = new CannonUseEvent(cannon, player, InteractAction.fireTorch);
                Bukkit.getServer().getPluginManager().callEvent(useEvent);

                if (useEvent.isCancelled())
                    return;

                boolean autoreload = player.isSneaking() && player.hasPermission(design.getPermissionAutoreload());
                MessageEnum message = fireCannon.prepareFire(cannon, player, autoreload, !design.isAmmoInfiniteForPlayer());

				// display message
				userMessages.displayMessage(player, message, cannon);
				return;
			}

			// ############ Button clicked ############################
			if (cannon.isRestoneTrigger(clickedBlock.getLocation()))
			{
				plugin.logDebug("interact event: fire button");
                cannon.setLastUser(player.getName());

				return;
			}


		}
		return;
	}

}
