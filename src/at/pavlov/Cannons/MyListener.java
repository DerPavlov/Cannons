package at.pavlov.Cannons;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Button;

import at.pavlov.Cannons.FireCannon.FlyingProjectile;

public class MyListener implements Listener
{
	private Config config;
	private UserMessages userMessages;
	private CannonPlugin plugin;
	private CannonList cannonList;
	private InventoryManagement InvManage;
	private FireCannon fireCannon;
	private CreateExplosion explosion;
	private CalcAngle calcAngle;

	public MyListener(CannonPlugin plugin)
	{
		this.InvManage = new InventoryManagement();
		this.config = plugin.getmyConfig();
		this.userMessages = plugin.getmyConfig().getUserMessages();
		this.plugin = plugin;
		this.cannonList = new CannonList(userMessages, config);
		this.explosion = new CreateExplosion(plugin, config);
		this.fireCannon = new FireCannon(plugin, config, userMessages, InvManage, explosion);
		this.calcAngle = new CalcAngle(plugin, userMessages, config);
		plugin.setListener(this);
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
			if (cannonList.contains(block.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}

	// ########### EntityExplode #######################################
	@EventHandler
	public void EntityExplode(EntityExplodeEvent event)
	{
		// is not canceled
		if (event.isCancelled() == false)
		{
			List<Block> blocks = event.blockList();

			// protect the cannon from explosion
			for (int i = 0; i < blocks.size(); i++)
			{
				Block block = blocks.get(i);
				UUID cannonId = cannonList.getID(block.getLocation());
				if (cannonId != null)
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
							cannonList.remove(cannonId);
						}
						else
						{
							// farther impact will not destroy the cannon
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
								block.breakNaturally();
							}
						}
						else
						{
							if (plugin.isCreeperHealLoaded() == false)
							{
								block.breakNaturally();
							}
						}
					}
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
			UUID cannonId = cannonList.getID(loc);
			if (cannonId != null)
			{
				cannonList.remove(cannonId);
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
			// if deleted block is cannonBlock delete cannon
			UUID cannonId = cannonList.getID(iter.next().getLocation());
			if (cannonId != null)
			{
				cannonList.remove(cannonId);
			}
		}
	}

	// ########### BlockBurn #######################################
	@EventHandler
	public void BlockBurn(BlockBurnEvent event)
	{
		// the cannon will not burn down
		UUID cannonId = cannonList.getID(event.getBlock().getLocation());
		if (cannonId != null)
		{
			event.setCancelled(true);
		}
	}

	// ########### BlockBreak #######################################
	@EventHandler
	public void BlockBreak(BlockBreakEvent event)
	{

		// if deleted block is cannonBlock delete cannon
		UUID cannonId = cannonList.getID(event.getBlock().getLocation());
		if (cannonId != null)
		{
			cannonList.remove(cannonId);
		}
	}

	// ########### PlayerBucketEmpty #######################################
	@EventHandler
	public void PlayerBucketEmpty(PlayerBucketEmptyEvent event)
	{
		// if player loads a lava/water bucket in the cannon
		if (CheckClickedBlock(event.getBlockClicked()))
		{
			Location barrel = event.getBlockClicked().getLocation();
			// check if block is cannonblock
			if (cannonList.contains(barrel) == true)
			{
				// delete projectile
				if (CheckLoadedProjectile(event.getBucket().getId()))
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
		// delete place projectile if clicked against the barrel
		if (event.getBlockAgainst() != null)
		{
			if (CheckClickedBlock(event.getBlockAgainst()))
			{
				Location barrel = event.getBlockAgainst().getLocation();
				// check if block is cannonblock
				if (cannonList.contains(barrel) == true)
				{
					// delete projectile
					if (CheckLoadedProjectile(event.getBlock().getTypeId()))
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
				for (Block b : cannonList.SurroundingBlocks(event.getBlock()))
				{
					if (b.getType() == config.Cannon_material)
					{
						Location loc = b.getLocation();
						if (cannonList.find_cannon(loc, event.getPlayer()) != null)
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
				for (Block b : cannonList.HorizontalSurroundingBlocks(event.getBlock()))
				{
					if (b.getType() == Material.STONE_BUTTON)
					{
						Location loc = b.getLocation();
						if (cannonList.find_cannon(loc, event.getPlayer()) != null)
						{
							event.getPlayer().sendMessage(userMessages.ErrorPermRestoneTorch);
							event.setCancelled(true);
						}
					}
				}
			}
		}


		// Cannon building complete - Checks also for permissions redstonetorches
		if (CheckClickedBlock(event.getBlockPlaced()))
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
			cannonList.find_cannon(barrel, event.getPlayer());
		}

		// cancel igniting of the cannon
		if (event.getBlock().getType() == Material.FIRE)
		{
			// check cannon
			Location loc = event.getBlockAgainst().getLocation();
			if (cannonList.find_cannon(loc, event.getPlayer()) != null)
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
			for (Block b : cannonList.SurroundingBlocks(block))
			{
				if (b.getType() == config.Cannon_material)
				{	
					Location barrel = b.getLocation();
					UUID id = cannonList.getID(barrel);
					if (id != null)
					{
						fireCannon.prepare_fire(cannonList.getCannon(id), null, !config.redstone_autoload);
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
				for (Block b : cannonList.HorizontalSurroundingBlocks(block))
				{
					if (b.getType() == Material.STONE_BUTTON)
					{	
						Location barrel = b.getLocation();
						UUID id = cannonList.getID(barrel);
						if (id != null)
						{
							fireCannon.prepare_fire(cannonList.getCannon(id), null, !config.redstone_autoload);
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
			if (cannonList.CheckAttachedTorch(block))
			{
				if (block.getType() == config.Cannon_material)
				{
					Location barrel = block.getLocation();
					UUID id = cannonList.getID(barrel);
					if (id != null)
					{
						fireCannon.prepare_fire(cannonList.getCannon(id), null, true);
					}
				}
			}
		}

	}
	

	// ########### ProjecitleHit #######################################
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
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) 
		{
			// check if it is a used material
			if (CheckClickedBlock(event.getClickedBlock()) == true) 
			{
				Player player = event.getPlayer();
				Location barrel = event.getClickedBlock().getLocation();
				Material material = event.getClickedBlock().getType();

				// find cannon or add it to the list
				CannonData cannon_loc = cannonList.find_cannon(barrel, player);
				if (cannon_loc == null)
					return;
				
				//prevent eggs and snowball from firing when loaded into the gun
				Material ItemInHand = player.getItemInHand().getType();
				if (ItemInHand == Material.EGG || ItemInHand == Material.SNOW_BALL || ItemInHand == Material.MONSTER_EGG)
				{
					event.setCancelled(true);
				}

				// ########## Load Projectile ######################
				if (material.equals(config.Cannon_material) && CheckLoadedProjectile(event.getPlayer()
								.getItemInHand().getTypeId())) 
				{
					if (CheckPermProjectile(player, cannon_loc)) 
					{
						// load projectile
						cannon_loc.projectile = ItemInHand;
						player.sendMessage(userMessages.getloadProjectile(ItemInHand));

						InvManage.TakeFromPlayerInventory(player, config.inventory_take);
						return;
					}
				}

				// ########## Barrel clicked with Sulphur ##########################
				if (material.equals(config.Cannon_material) && event.getMaterial() == Material.SULPHUR) 
				{
					if (CheckPermSulphur(player, cannon_loc)) 
					{
						cannon_loc.gunpowder++;
						player.sendMessage(userMessages.getloadGunpowder(cannon_loc.gunpowder));
						// take item from the player
						InvManage.TakeFromPlayerInventory(player, config.inventory_take);
						return;
					}
				}

				// ############ Torch clicked ############################
				if (material == Material.TORCH && config.fireTorch == true) {
					fireCannon.prepare_fire(cannon_loc, player, true);
					return;
				}
				
				// ############ Button clicked ############################
				if (material == Material.STONE_BUTTON && config.fireButton == true) {
					fireCannon.displayPrepareFireMessage(cannon_loc, player);
					return;
				}

				// ############ set angle ################################
				if ((player.getItemInHand().getType() == Material.AIR || player.getItemInHand().getType() == Material.WATCH) && material == config.Cannon_material) {
					calcAngle.ChangeAngle(cannon_loc, event.getAction(), event.getBlockFace(), player);
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
	// ############## CheckPermProjectile ################################
	private boolean CheckPermProjectile(Player player, CannonData cannon_loc)
	{
		// check for too many cannons
		if (cannon_loc.projectile != Material.AIR)
		{
			player.sendMessage(userMessages.getProjectileAlreadyLoaded(cannon_loc.gunpowder, cannon_loc.projectile));
			return false;
		}
		if (cannon_loc.gunpowder == 0)
		{
			player.sendMessage(userMessages.NoSulphur);
			return false;
		}
		if (player.hasPermission("cannons.player.load") == false)
		{
			player.sendMessage(userMessages.ErrorPermLoad);
			return false;
		}

		return true;
	}

	// ############## CheckPermSulphur ################################
	private boolean CheckPermSulphur(Player player, CannonData cannon_loc)
	{
		if (cannon_loc.projectile != Material.AIR)
		{
			player.sendMessage(userMessages.getProjectileAlreadyLoaded(cannon_loc.gunpowder, cannon_loc.projectile));
			return false;
		}
		
		//check if there is a division by zero
		int lengthDiff = (config.max_barrel_length - config.min_barrel_length);
		if (lengthDiff == 0) lengthDiff=1;
		// maximum loaded gunpowder depends on the length of the barrel
		int max_gunpowder = config.max_gunpowder;
		if (config.gunpowder_depends_on_length == true)
		{
			max_gunpowder = 1 + (config.max_gunpowder - 1) * (cannon_loc.barrel_length - config.min_barrel_length) / (config.max_barrel_length - config.min_barrel_length);
			
		}
		if (cannon_loc.gunpowder >= max_gunpowder)
		{
			player.sendMessage(userMessages.getMaximumGunpowderLoaded(cannon_loc.gunpowder));
			return false;
		}
		if (player.hasPermission("cannons.player.load") == false)
		{
			player.sendMessage(userMessages.ErrorPermLoad);
			return false;
		}
		return true;
	}

	// ############## CheckClickedBlock ################################
	private boolean CheckClickedBlock(Block block)
	{
		Iterator<Material> iter = config.usedMaterial.iterator();
		while (iter.hasNext())
		{
			if (iter.next().getId() == block.getTypeId()) { return true; }
		}
		return false;
	}

	// ############## CheckLoadedProjectile ################################
	private boolean CheckLoadedProjectile(int blockid)
	{
		Iterator<Projectile> iter = config.allowedProjectiles.iterator();
		while (iter.hasNext())
		{
			int next = iter.next().material.getId();
			// change redstone wire to redstone
			if (blockid == Material.REDSTONE_WIRE.getId())
			{
				blockid = Material.REDSTONE.getId();
			}
			// cake
			if (blockid == Material.CAKE_BLOCK.getId())
			{
				blockid = Material.CAKE.getId();
			}

			// compare
			if (next == blockid) { return true; }
		}
		return false;
	}

	// ############## CleanUpEntries ################################
	public void CleanUpEntries()
	{
		// displayArraySize();

		// check if the cannon exists
		cannonList.DeleteObsoletCannons();
		fireCannon.deleteOldSnowballs();
		explosion.deleteTransmittedEntities();

	}
}
