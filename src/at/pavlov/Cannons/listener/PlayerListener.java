package at.pavlov.Cannons.listener;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Button;

import at.pavlov.Cannons.CalcAngle;
import at.pavlov.Cannons.CannonManager;
import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.CreateExplosion;
import at.pavlov.Cannons.FireCannon;
import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.projectile.FlyingProjectile;
import at.pavlov.Cannons.projectile.Projectile;
import at.pavlov.Cannons.utils.CannonsUtil;

public class PlayerListener implements Listener
{
	private Config config;
	private UserMessages userMessages;
	private Cannons plugin;
	private CannonManager cannonManager;
	private FireCannon fireCannon;
	private CreateExplosion explosion;
	private CalcAngle calcAngle;

	public PlayerListener(Cannons plugin)
	{
		this.plugin = plugin;
		this.config = this.plugin.getmyConfig();
		this.userMessages = this.plugin.getmyConfig().getUserMessages();
		this.cannonManager = this.plugin.getCannonManager();
		this.explosion = this.plugin.getExplosion();
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
			if (cannonManager.isCannonBlock(block.getLocation()))
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
			if (cannonManager.isCannonBlock(block.getLocation()))
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
			Cannon cannon = cannonManager.getCannon(loc);
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
			Cannon cannon = cannonManager.getCannon(iter.next().getLocation());
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
		if (cannonManager.isCannonBlock(event.getBlock().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	// ########### BlockBreak #######################################
	@EventHandler
	public void BlockBreak(BlockBreakEvent event)
	{

		// if deleted block is cannonBlock delete cannon
		Cannon cannon = cannonManager.getCannon(event.getBlock().getLocation());
		if (cannon != null)
		{
			cannonManager.removeCannon(cannon);
		}
	}

	// ########### PlayerBucketEmpty #######################################
	@EventHandler
	public void PlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		// if player loads a lava/water bucket in the cannon
		if (config.isCannonBarrel(event.getBlockClicked()))
		{
			Location barrel = event.getBlockClicked().getLocation();
			// check if block is cannonblock
			if (cannonManager.isCannonBlock(barrel) == true)
			{
				// delete projectile
				//if (CheckLoadedProjectile(event.getBucket().getId(), 0))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	// ########### BlockPlace #######################################
	@EventHandler
	public void BlockPlace(BlockPlaceEvent event)
	{

		// Cannon building complete - Checks also for permissions redstonetorches
		if (config.isCannonBlock(event.getBlockPlaced()))
		{
			Block block = event.getBlockPlaced();
			Location barrel = block.getLocation();

			// check cannon
			if (block.getType() == Material.TORCH || block.getType() == Material.STONE_BUTTON)
			{
				// change location to barrel
				Block barrelBlock = event.getBlockAgainst();

				// if placed on snow the torch must be moved one down
				if (barrelBlock.getLocation().equals(event.getBlockPlaced().getLocation()))
				{
					barrelBlock = barrelBlock.getRelative(BlockFace.DOWN);
				}
				barrel = barrelBlock.getLocation();
			}
			// will check for redstonetorches and delete them
			cannonManager.getCannon(barrel, event.getPlayer().getName());
		}

		// delete place projectile if clicked against the barrel
		if (event.getBlockAgainst() != null)
		{
			if (config.isCannonBlock(event.getBlockAgainst()))
			{
				Location barrel = event.getBlockAgainst().getLocation();
				
				// check if block is cannonblock
				if (cannonManager.isCannonBlock(barrel) == true )// && cannonManager.isPartOfCannon(event.getBlock().getLocation()) == false)
				{
					// delete projectile
					//if (CheckLoadedProjectile(event.getBlock()))
					{
						//event.setCancelled(true);
					}
				}
			}
			
		}
		

		
		
	

		// Place redstonetorch next to the cannon
		if (event.getBlockPlaced().getType() == Material.REDSTONE_TORCH_ON || event.getBlockPlaced().getType() == Material.REDSTONE_TORCH_OFF)
		{
			// check permissions
			if (event.getPlayer().hasPermission("cannons.player.placeRedstoneTorch") == false)
			{
				// check cannon
				for (Block b : cannonManager.SurroundingBlocks(event.getBlock()))
				{
					if (config.isCannonBarrel(b))
					{
						Location loc = b.getLocation();
						if (cannonManager.getCannon(loc, event.getPlayer().getName()) != null)
						{
							userMessages.displayMessage(event.getPlayer(), MessageEnum.PermissionErrorRedstone);
							event.setCancelled(true);
						}

					}
				}
			}
		}
			
		// Place redstone wire next to the button
		if (event.getBlockPlaced().getType() == Material.REDSTONE_WIRE)
		{
			// check permissions
			if (event.getPlayer().hasPermission("cannons.player.placeRedstoneTorch") == false)
			{
				// check cannon
				for (Block b : cannonManager.HorizontalSurroundingBlocks(event.getBlock()))
				{
					if (b.getType() == Material.STONE_BUTTON)
					{
						Location loc = b.getLocation();
						if (cannonManager.getCannon(loc, event.getPlayer().getName()) != null)
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

	// ########### RedstoneEvent #######################################
	@EventHandler
	public void RedstoneEvent(BlockRedstoneEvent event)
	{
		// redstone torch fire
		Block block = event.getBlock();
		if (block.getType() == Material.REDSTONE_TORCH_ON)
		{
			for (Block b : cannonManager.SurroundingBlocks(block))
			{
				if (config.isCannonBarrel(b))
				{	
					Location barrel = b.getLocation();
					Cannon cannon = cannonManager.getCannon(barrel);
					if (cannon != null)
					{
						fireCannon.prepare_fire(cannon, null, !config.redstone_autoload);
					}
				}
			}
		}
		
		// redstone wire fire
		block = event.getBlock();
		if (block.getType() == Material.REDSTONE_WIRE)
		{
			//block is powered
			if (block.getData() == 0)
			{	
				for (Block b : cannonManager.HorizontalSurroundingBlocks(block))
				{
					if (b.getType() == Material.STONE_BUTTON)
					{	
						Location barrel = b.getLocation();
						Cannon cannon = cannonManager.getCannon(barrel);
						if (cannon != null)
						{
							fireCannon.prepare_fire(cannon, null, !config.redstone_autoload);
						}
					}
				}
			}	
		}
		
		// fire with Button
		if (event.getBlock().getType() == Material.STONE_BUTTON && config.fireButton == true)
		{
			Button button = (Button) event.getBlock().getState().getData();
			block = block.getRelative(button.getAttachedFace());
			if (CannonsUtil.CheckAttachedTorch(block))
			{
				if (config.isCannonBarrel(block))
				{
					Location barrel = block.getLocation();
					Cannon cannon = cannonManager.getCannon(barrel);
					if (cannon != null)
					{
						fireCannon.prepare_fire(cannon, null, true);
					}
				}
			}
		}

	}
	

	/**
	 * Cannon snowball hits the ground
	 * @param event
	 */
	@EventHandler
	public void ProjectileHit(ProjectileHitEvent event)
	{

		// get FlyingProjectiles
		LinkedList<FlyingProjectile> flying_projectiles = fireCannon.getProjectiles();

		// iterate the list
		if (!flying_projectiles.isEmpty())
		{
			Iterator<FlyingProjectile> iterator = flying_projectiles.iterator();

			while (iterator.hasNext())
			{
				FlyingProjectile flying = iterator.next();
				if (event.getEntity().equals(flying.snowball))
				{
					flying.snowball = (Snowball) event.getEntity();
					explosion.detonate(flying);
					iterator.remove();
				}
			}
		}
	}

	// #################################### Player Interact ##############################
	@EventHandler
	public void PlayerInteract(PlayerInteractEvent event) 
	{		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) 
		{
			Block clickedBlock;
			if (event.getClickedBlock() == null)
			{
				//no clicked block - get block player is looking at

				clickedBlock = event.getPlayer().getTargetBlock(null, 4);
			}
			else
			{
				clickedBlock = event.getClickedBlock();
			}
			// check if it is a used material
			if (config.isCannonBlock(clickedBlock) == true) 
			{
				Player player = event.getPlayer();
				Location barrel = clickedBlock.getLocation();

				// find cannon or add it to the list
				Cannon cannon = cannonManager.getCannon(barrel, player.getName());
				
				if (cannon == null)
					return;
				
				//get cannon design
				CannonDesign design = cannon.getCannonDesign();
				
				//prevent eggs and snowball from firing when loaded into the gun
				Material ItemInHand = player.getItemInHand().getType();
				if (ItemInHand == Material.EGG || ItemInHand == Material.SNOW_BALL || ItemInHand == Material.MONSTER_EGG)
				{
					event.setCancelled(true);
				}
				

				plugin.logDebug("interact event + is loading block " + cannon.isLoadingBlock(clickedBlock.getLocation()) + " gunpowder " + design.getGunpowderType().equalsFuzzy(event.getItem()));

				// ########## Load Projectile ######################
				Projectile projectile = plugin.getProjectile(event.getItem());
				if (cannon.isLoadingBlock(clickedBlock.getLocation()) && projectile != null) 
				{
					plugin.logDebug("load projectile");
					//load projectile
					MessageEnum message = cannon.loadProjectile(projectile, player);
					//display message
					userMessages.displayMessage(player, message, cannon);	
				}

				// ########## Barrel clicked with gunpowder ##########################
				if (cannon.isLoadingBlock(clickedBlock.getLocation()) && design.getGunpowderType().equalsFuzzy(event.getItem())) 
				{
					plugin.logDebug("load gunpowder");
					//load gunpowder
					MessageEnum message = cannon.loadGunpowder(player);
					//display message
					userMessages.displayMessage(player, message, cannon);		
				}

				// ############ Torch clicked ############################
				if (cannon.isRightClickTrigger(clickedBlock.getLocation())) {
					fireCannon.prepare_fire(cannon, player, true);
				
					return;
				}
				
				// ############ Button clicked ############################
				if (cannon.isRestoneTrigger(clickedBlock.getLocation())) {
					fireCannon.displayPrepareFireMessage(cannon, player);
					
					return;
				}

				// ############ set angle ################################
				if ((player.getItemInHand().getType() == Material.AIR || player.getItemInHand().getType() == Material.WATCH) && cannon.isLoadingBlock(clickedBlock.getLocation())) {
					calcAngle.ChangeAngle(cannon, event.getAction(), event.getBlockFace(), player);
					
					//update Signs
					cannon.updateCannonSigns();
					return;
				}
			} else {
				// clicked Block is no cannon block
				calcAngle.disableAimingMode(event.getPlayer());
			}
		} else {
			// all other actions will stop aiming mode
			if (event.getAction() == Action.RIGHT_CLICK_AIR)
			calcAngle.disableAimingMode(event.getPlayer());
		}
		return;
	}

}
