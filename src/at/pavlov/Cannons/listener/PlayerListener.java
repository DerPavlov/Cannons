package at.pavlov.Cannons.listener;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Button;

import at.pavlov.Cannons.CalcAngle;
import at.pavlov.Cannons.CannonManager;
import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.CreateExplosion;
import at.pavlov.Cannons.FireCannon;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.Projectile;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.CannonData;
import at.pavlov.Cannons.utils.BlockHelper;
import at.pavlov.Cannons.utils.FlyingProjectile;
import at.pavlov.Cannons.utils.InventoryManagement;

public class PlayerListener implements Listener
{
	private Config config;
	private UserMessages userMessages;
	private Cannons plugin;
	private CannonManager cannonManager;
	private InventoryManagement InvManage;
	private FireCannon fireCannon;
	private CreateExplosion explosion;
	private CalcAngle calcAngle;

	public PlayerListener(Cannons plugin)
	{
		this.InvManage = plugin.getInvManage();
		this.config = plugin.getmyConfig();
		this.userMessages = plugin.getmyConfig().getUserMessages();
		this.plugin = plugin;
		this.cannonManager = plugin.getCannonManager();
		this.explosion = plugin.getExplosion();
		this.fireCannon = plugin.getFireCannon();
		this.calcAngle = plugin.getCalcAngle();
	}
	
	// ########### PlayerMove #######################################
	@EventHandler
	public void PlayerMove(PlayerMoveEvent event)
	{
		// only active if the player is in aiming mode
		calcAngle.PlayerMove(event.getPlayer());
	}

	// ########### EntityDeath #######################################
	@EventHandler
	public void EntityDeath(EntityDeathEvent event)
	{
		// plugin.broadcast("entity " +
		// event.getEntity().getLastDamageCause().getCause());
		// plugin.broadcast("entity " + event.getEntity().getKiller());
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
	@EventHandler(priority = EventPriority.HIGHEST)
	public void EntityExplode(EntityExplodeEvent event)
	{
		//map explosions to TNT event
		if (config.forceTNTexplosion)
		{
			if((event.getEntity() ==  null || event.getEntity() instanceof Snowball) && !event.blockList().isEmpty())
			{
				Location eventLoc = event.getLocation();
				TNTPrimed tnt = eventLoc.getWorld().spawn(eventLoc, TNTPrimed.class);
				//new event
				EntityExplodeEvent newEvent = new EntityExplodeEvent(tnt,eventLoc, event.blockList(), 0.3f);
				plugin.getServer().getPluginManager().callEvent(newEvent);
			
				tnt.remove();
				if (newEvent.isCancelled() == true)
				{
					event.setCancelled(newEvent.isCancelled());
					event.blockList().clear();
				}
			}
		}
		
		
		// handle normal events not canceled
		if (!event.isCancelled())
		{
			List<Block> blocks = event.blockList();

			// protect the cannon from explosion
			for (int i = 0; i < blocks.size(); i++)
			{
				Block block = blocks.get(i);
				
				// if it is a cannon block
				if (cannonManager.isCannonBlock(block.getLocation()))
				{
					Entity explosionEntity = event.getEntity();
					if (explosionEntity == null)
					{
						// dont allow this event to break a cannon
						blocks.remove(i--);
					}
					else
					{
						// get distance to the impact
						Location impact = explosionEntity.getLocation();
						double distance = impact.distance(block.getLocation());
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

			// break water, lava, obsidian if cannon projectile
			for (int i = 0; i < blocks.size(); i++)
			{
				Block block = blocks.get(i);
				if (event.getEntity() != null)
				{
					block = blocks.get(i);
					if (event.getEntity().toString().equals("CraftSnowball"))
					{
						Material material = block.getType();
						if ((material == Material.OBSIDIAN || material == Material.WATER || material == Material.LAVA))
						{
							// break Obsidian if no other plugin is loaded
							if (plugin.BlockBreakPluginLoaded() == false)
							{
								BlockBreak(block,event.getYield());
							}
						}
						else
						{
							if (plugin.BlockBreakPluginLoaded() == false)
							{
								BlockBreak(block,event.getYield());
							}
						}
					}
				}
			}
		}
		

	}
	
	private void BlockBreak(Block block, float yield)
	{
		Random r = new Random();
		if (r.nextFloat() > yield) 
		{
			block.breakNaturally();
		}
		else
		{
			block.setTypeId(0);
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
			CannonData cannon = cannonManager.getCannon(loc);
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
			CannonData cannon = cannonManager.getCannon(iter.next().getLocation());
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
		CannonData cannon = cannonManager.getCannon(event.getBlock().getLocation());
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
				if (CheckLoadedProjectile(event.getBucket().getId(), 0))
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

			cannonManager.getCannon(barrel, event.getPlayer());
			
			return;
		}
		
		
		// delete place projectile if clicked against the barrel
		if (event.getBlockAgainst() != null)
		{
			if (config.isCannonBlock(event.getBlockAgainst()))
			{
				Location barrel = event.getBlockAgainst().getLocation();
				// check if block is cannonblock
				if (cannonManager.isCannonBlock(barrel) == true)
				{
					// delete projectile
					if (CheckLoadedProjectile(event.getBlock()))
					{
						event.setCancelled(true);
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
						if (cannonManager.getCannon(loc, event.getPlayer()) != null)
						{
							event.getPlayer().sendMessage(userMessages.ErrorPermRestoneTorch);
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
						if (cannonManager.getCannon(loc, event.getPlayer()) != null)
						{
							event.getPlayer().sendMessage(userMessages.ErrorPermRestoneTorch);
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
			if (cannonManager.getCannon(loc, event.getPlayer()) != null)
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
					CannonData cannon = cannonManager.getCannon(barrel);
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
						CannonData cannon = cannonManager.getCannon(barrel);
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
			if (BlockHelper.CheckAttachedTorch(block))
			{
				if (config.isCannonBarrel(block))
				{
					Location barrel = block.getLocation();
					CannonData cannon = cannonManager.getCannon(barrel);
					if (cannon != null)
					{
						fireCannon.prepare_fire(cannon, null, true);
					}
				}
			}
		}

	}
	

	/**
	 * Cannon snowball this the ground
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
					explosion.create_explosion(flying);
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
				CannonData cannon = cannonManager.getCannon(barrel, player);
				if (cannon == null)
					return;
				
				//prevent eggs and snowball from firing when loaded into the gun
				Material ItemInHand = player.getItemInHand().getType();
				if (ItemInHand == Material.EGG || ItemInHand == Material.SNOW_BALL || ItemInHand == Material.MONSTER_EGG)
				{
					event.setCancelled(true);
				}

				// ########## Load Projectile ######################
				if (config.isCannonBarrel(clickedBlock) && CheckLoadedProjectile(event.getPlayer()
								.getItemInHand())) 
				{
					if (CheckPermProjectile(player, cannon)) 
					{
						// load projectile
						cannon.projectileID = ItemInHand.getId();
						player.sendMessage(userMessages.getloadProjectile(ItemInHand.getId()));

						InvManage.TakeFromPlayerInventory(player, config.inventory_take);
						
						//update Signs
						cannon.updateCannonSigns();
						return;
					}
				}

				// ########## Barrel clicked with Sulphur ##########################
				if (config.isCannonBarrel(clickedBlock) && event.getMaterial() == Material.SULPHUR) 
				{
					if (CheckPermSulphur(player, cannon)) 
					{
						cannon.gunpowder++;
						player.sendMessage(userMessages.getloadGunpowder(cannon.gunpowder));
						// take item from the player
						InvManage.TakeFromPlayerInventory(player, config.inventory_take);
						
						//update Signs
						cannon.updateCannonSigns();
						return;
					}
				}

				// ############ Torch clicked ############################
				if (clickedBlock.getType() == Material.TORCH && config.fireTorch == true) {
					fireCannon.prepare_fire(cannon, player, true);
				
					return;
				}
				
				// ############ Button clicked ############################
				if (clickedBlock.getType() == Material.STONE_BUTTON && config.fireButton == true) {
					fireCannon.displayPrepareFireMessage(cannon, player);
					
					return;
				}

				// ############ set angle ################################
				if ((player.getItemInHand().getType() == Material.AIR || player.getItemInHand().getType() == Material.WATCH) && config.isCannonBarrel(clickedBlock)) {
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
	/**
	 * Check the if the cannons can be loaded
	 * @param player whose permissions are checked
	 * @param cannon cannon to check
	 * @return true if the player and cannons can load the projectile
	 */
	private boolean CheckPermProjectile(Player player, CannonData cannon)
	{
		if (cannon.isLoaded())
		{
			player.sendMessage(userMessages.getProjectileAlreadyLoaded(cannon.gunpowder, cannon.projectileID));
			return false;
		}
		if (cannon.gunpowder == 0)
		{
			player.sendMessage(userMessages.NoSulphur);
			return false;
		}
		if (player.hasPermission("cannons.player.load") == false)
		{
			player.sendMessage(userMessages.ErrorPermLoad);
			return false;
		}
		String ItemInHand = player.getItemInHand().getType().toString();
		if(!player.hasPermission("cannons.projectile." + ItemInHand.toString()) && !player.hasPermission("cannons.projectile")) 
		{
			player.sendMessage(userMessages.ErrorPermissionProjectile);
			return false;
		}
		return true;
	}

	/**
	 * Check if cannons can be loaded with gunpowder by the player
	 * @param player check permissions of this player
	 * @param cannon check if this cannon can be loaded
	 * @return true if the cannon can be loaded
	 */
	private boolean CheckPermSulphur(Player player, CannonData cannon)
	{
		if (cannon.isLoaded())
		{
			player.sendMessage(userMessages.getProjectileAlreadyLoaded(cannon.gunpowder, cannon.projectileID));
			return false;
		}
		
		//check if there is a division by zero
		int lengthDiff = (config.max_barrel_length - config.min_barrel_length);
		if (lengthDiff == 0) lengthDiff=1;
		// maximum loaded gunpowder depends on the length of the barrel
		int max_gunpowder = config.max_gunpowder;
		if (config.gunpowder_depends_on_length == true)
		{
			max_gunpowder = 1 + (config.max_gunpowder - 1) * (cannon.barrel_length - config.min_barrel_length) / (config.max_barrel_length - config.min_barrel_length);
		}
		if (cannon.gunpowder >= max_gunpowder)
		{
			player.sendMessage(userMessages.getMaximumGunpowderLoaded(cannon.gunpowder));
			return false;
		}
		if (player.hasPermission("cannons.player.load") == false)
		{
			player.sendMessage(userMessages.ErrorPermLoad);
			return false;
		}
		return true;
	}

	
	private boolean CheckLoadedProjectile(ItemStack item)
	{
		return CheckLoadedProjectile(item.getTypeId(), item.getData().getData());
	}
	
	
	private boolean CheckLoadedProjectile(Block block)
	{
		return CheckLoadedProjectile(block.getTypeId(), block.getData());
	}

	// ############## CheckLoadedProjectile ################################
	private boolean CheckLoadedProjectile(int blockId, int blockData)
	{
		Iterator<Projectile> iter = config.allowedProjectiles.iterator();
		while (iter.hasNext())
		{
			Projectile next = iter.next();
			// change redstone wire to redstone
			if (blockId == Material.REDSTONE_WIRE.getId())
			{
				blockId = Material.REDSTONE.getId();
			}
			// cake
			if (blockId == Material.CAKE_BLOCK.getId())
			{
				blockId = Material.CAKE.getId();
			}

			// compare
			if (next.isEqual(blockId, blockData)) 
			{ 
				return true; 
			}
		}
		return false;
	}


}
