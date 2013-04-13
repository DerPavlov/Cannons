package at.pavlov.Cannons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.container.SimpleBlock;
import at.pavlov.Cannons.utils.CannonsUtil;

public class CannonManager
{
	private ArrayList<Cannon> cannonList = new ArrayList<Cannon>();

	private Cannons plugin;
	private UserMessages userMessages;
	private Config config;

	public CannonManager(Cannons cannons, UserMessages userMessages, Config config)
	{
		this.userMessages = userMessages;
		this.config = config;
		this.plugin = cannons;
	}


	/**
	 * removes a cannon from the list
	 * @param cannon
	 */
	public void removeCannon(Location loc)
	{
		Cannon cannon = getCannon(loc, null);
		removeCannon(cannon);
	}

	/**
	 * removes a cannon from the list
	 * @param cannon
	 */
	public void removeCannon(Cannon cannon)
	{
		if (cannon != null)
		{
			// send message to the owner
			Player player = null;
			if (cannon.getOwner() != null)
			{
				player = Bukkit.getPlayer(cannon.getOwner());
			}

			// destroy cannon (drops items, edit sign)
			MessageEnum message = cannon.destroyCannon();
			
			if (player != null) userMessages.displayMessage(player, message, cannon);

			//remove from list
			cannonList.remove(cannon);
		}
	}

	/**
	 * Checks if the name of a cannon is unique
	 * 
	 * @param name
	 * @return true if the name is unique
	 */
	private boolean isCannonNameUnique(String name, String owner)
	{
		for (Cannon cannon : cannonList)
		{
			if (cannon.getCannonName() != null && name != null)
			{
				if (cannon.getCannonName().equals(name))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * generates a new unique cannon name
	 * @return
	 */
	private String newCannonName(Cannon cannon)
	{
		if (cannon.getOwner() == null) return "missing Owner";
			
		String name;
		CannonDesign design = cannon.getCannonDesign();
		if (design != null)
			name = design.getDesignName();
		else
			name = "cannon";
	

		for (int i = 1; i < Integer.MAX_VALUE; i++)
		{
			String cannonName = name + " " + i;

			if (isCannonNameUnique(cannonName, cannon.getOwner()) == true)
			{
				return cannonName;
			}
		}

		return "no unique name";
	}

	/**
	 * adds a new cannon to the list of cannons
	 * @param cannon
	 * @param owner
	 */
	public void createCannon(Cannon cannon)
	{
		//the owner can't be null
		if (cannon.getOwner() == null) return;
		
		//if the cannonName is empty make a new one
		if (cannon.getCannonName() ==  null || cannon.getCannonName() == "")
			cannon.setCannonName(newCannonName(cannon));
		
		// add cannon to the list
		cannonList.add(cannon);
		
		return ;
	}

	/**
	 * get cannon by cannonName and Owner - used for Signs
	 * @param cannonName
	 * @return
	 */
	public Cannon getCannonFromStorage(String cannonName, String owner)
	{
		if (cannonName == null || owner == null) return null;

		for (Cannon cannon : cannonList)
		{
			if (cannonName.equals(cannon.getCannonName()))
			{
				if (owner.equals(cannon.getOwner()))
				{
					return cannon;
				}
			}

		}
		return null;
	}

	/**
	 * Searches the storage if there is already a cannonblock on this location
	 * and returns the cannon
	 * @param loc
	 * @return
	 */
	private Cannon getCannonFromStorage(Location loc)
	{
		for (Cannon cannon : cannonList)
		{
			if (cannon.isCannonBlock(loc.getBlock()))
			{
				return cannon;
			}
		}
		return null;
	}
	
	/**
	 * searches for a cannon and creates a new entry if it does not exist
	 * 
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
	public Cannon getCannon(Location cannonBlock, String owner)
	{
		Cannon cannon = getCannonFromStorage(cannonBlock);

		// this block is a part of an existing cannon
		if (cannon != null)
		{

			// search cannon that is written on the sign
			// Cannon cannonFromSign =
			// getCannonFromStorage(cannon.getCannonNameFromSign(design),
			// cannon.getOwnerFromSign(design));

			// check if the name matches with the attached sign or if the name
			// is not valid
			// if (cannonFromSign == null || cannon.isCannonEqualSign(design) ==
			// true )
			{
				return cannon;
			}
			// else
			// {
			// different cannon, search the database for the right entry by the
			// cannon name from the sign
			// return cannonFromSign;
			// }
		}
		// else
		{
			// no existing cannon in storage -> check if there is a cannon
			return checkCannon(cannonBlock, owner);
		}

	}

	/**
	 * searches if this block is part of a cannon and create a new one
	 * @param cannonBlock
	 * @param owner
	 * @return
	 */
	public Cannon checkCannon(Location cannonBlock, String owner)
	{
		// get world
		World world = cannonBlock.getWorld();

		plugin.logDebug("check cannon");

		// check all cannon design if this block is part of the design
		for (CannonDesign cannonDesign : plugin.getDesignStorage().getCannonDesignList())
		{
			plugin.logDebug("design " + cannonDesign.toString());
			// check of all directions
			BlockFace cannonDirection = BlockFace.NORTH;
			for (int i = 0; i < 4; i++)
			{
				// for all blocks for the design
				List<SimpleBlock> designBlockList = cannonDesign.getAllCannonBlocks(cannonDirection);
				for (SimpleBlock designBlock : designBlockList)
				{
					// compare blocks
					if (designBlock.compareBlockFuzzy(cannonBlock.getBlock()))
					{
						// this block is same as in the design, get the offset
						Vector offset = designBlock.substractInverted(cannonBlock).toVector();

						// check all other blocks of the cannon
						boolean isCannon = true;
						for (SimpleBlock checkBlocks : designBlockList)
						{
							 plugin.logDebug("check block " +
							 checkBlocks.getID() + " " + checkBlocks.getData()
							 + " loc " + checkBlocks.toVector().add(offset));
							if (!checkBlocks.compareBlockFuzzy(world, offset))
							{
								// if the block does not match this is not the
								// right one
								isCannon = false;
								break;
							}
						}

						// this is a cannon
						if (isCannon == true)
						{
							// make a new cannon if there is no sign on the
							// cannon
							Cannon cannon = new Cannon(cannonDesign, world.getName(), offset, cannonDirection, owner);
							cannon.setCannonName(newCannonName(cannon));
					
							//check the permissions for redstone
							MessageEnum message = cannon.checkRedstonePermission(owner);
							
							//send messages
							userMessages.displayMessage(owner, message, cannon);
							
							//add cannon to the list if everything was fine and return the cannon
							if (message != null && message == MessageEnum.CannonCreated)
							{
								plugin.logDebug("create cannon");
								createCannon(cannon);
								return cannon;
							}
						}
					}
				}

				// rotate cannon direction
				cannonDirection = CannonsUtil.roatateFace(cannonDirection);
			}

		}

		return null;
	}

	/**
	 * returns the number of owned cannons of a player
	 * 
	 * @param player
	 * @return
	 */
	public int getNumberOfCannons(String player)
	{
		int i = 1;
		for (Cannon cannon : cannonList)
		{
			if (cannon.getOwner() == null)
			{
				plugin.logSevere("Cannon has no owner. Contact the plugin developer");
			}
			else if (cannon.getOwner().equalsIgnoreCase(player))
			{
				i++;
			}
		}
		return i;
	}

	/**
	 * 
	 * @return List of cannons
	 */
	public List<Cannon> getCannonList()
	{
		return cannonList;
	}
	
	/**
	 * 
	 * @return List of cannons
	 */
	public void clearCannonList()
	{
		cannonList.clear();
	}

	/**
	 * returns the number of cannons manged by the plugin
	 * 
	 * @return
	 */
	public int getcannonListSize()
	{
		return cannonList.size();
	}

	/**
	 * returns the amout of cannons a player can build
	 * 
	 * @param player
	 * @return
	 */
	public int getCannonBuiltLimit(Player player)
	{
		// the player is not valid - no limit check
		if (player == null) return Integer.MAX_VALUE;
		if (player.getName() == null) return Integer.MAX_VALUE;

		// both limitA/B and cannons.player.limit.5 work
		// if all notes are enabled, set limit to a high number
		int newBuiltLimit = -1;
		if (player.hasPermission("cannons.player.limit." + Integer.MAX_VALUE))
		{
			newBuiltLimit = Integer.MAX_VALUE;
		}

		// else check all nodes for the player
		for (int i = 100; i > 0; i--)
		{
			if (player.hasPermission("cannons.player.limit." + i)) newBuiltLimit = i;
		}

		// config implementation
		if (newBuiltLimit == -1)
		{
			if (player.hasPermission("cannons.player.limitB"))
			{
				// return the
				return config.getBuildLimitB();
			}
			// limit B is stronger
			else if (player.hasPermission("cannons.player.limitA"))
			{
				return config.getBuildLimitA();
			}
		}
		// player implementation
		else
		{
			return newBuiltLimit;
		}
		return Integer.MAX_VALUE;
	}
	/**
	 * checks if the player can build a cannon (permission, builtLimit)
	 * 
	 * @param cannon
	 * @param player
	 * @return
	 */
	public MessageEnum canBuildCannon(Cannon cannon, Player player)
	{
		CannonDesign design = cannon.getCannonDesign();

		// check if player has permission to build
		if (!player.hasPermission(design.getPermissionBuild()))
		{
			return MessageEnum.PermissionErrorBuild;
		}
		// player does not have too many guns
		if (getCannonBuiltLimit(player) > getNumberOfCannons(player.getName()))
		{
			return MessageEnum.ErrorCannonBuiltLimit;
		}
		// player has sufficient permission to build a cannon
		return MessageEnum.CannonCreated;
	}

	/*
	 * // #################################### ADD_CANNON ######################
	 * private Cannon addCannon(CannonAttribute att_cannon, String owner) {
	 * 
	 * // create a new cannon Cannon new_cannon = new Cannon();
	 * 
	 * 
	 * 
	 * new_cannon.firingLocation = att_cannon.barrel; new_cannon.face =
	 * att_cannon.face; new_cannon.barrel_length = att_cannon.barrel_length;
	 * 
	 * 
	 * //search if there is a entry with this name written on the sign of the
	 * cannon with the same length Cannon old_cannon =
	 * getCannonFromStorage(new_cannon.getCannonNameFromSign(),
	 * new_cannon.getOwnerFromSign());
	 * 
	 * //there is a cannon with this name in the storage -> update entry if
	 * (old_cannon != null && old_cannon.barrel_length ==
	 * new_cannon.barrel_length) {
	 * 
	 * old_cannon.firingLocation = new_cannon.firingLocation; old_cannon.face =
	 * new_cannon.face;
	 * 
	 * 
	 * // add cannon blocks old_cannon.setCannonBlocks(new
	 * ArrayList<Location>()); old_cannon = addCannonBlocks(old_cannon);
	 * 
	 * old_cannon.updateCannonSigns(); return old_cannon; } //no cannon stored,
	 * this is a new cannon - make a new entry else { //there needs to be a
	 * player to create a cannon if (owner == null ) return null;
	 * 
	 * Player player = Bukkit.getPlayer(owner);
	 * 
	 * // check if player is allowed to build a new cannon boolean create =
	 * true; if (config.enableLimits == true) { create =
	 * CheckCannonsAmount(player); } if (create == true) {
	 * new_cannon.setOwner(owner); new_cannon.setName(newCannonName(owner));
	 * new_cannon.setLastFired(0); new_cannon.setLoadedGunpowder(0);
	 * new_cannon.setProjectileID(Material.AIR.getId());
	 * new_cannon.setProjectileData(0); new_cannon.setHorizontalAngle(0.0);
	 * new_cannon.setVerticalAngle(0.0); new_cannon.setDesignID(0);
	 * new_cannon.setValid(true);
	 * 
	 * // add cannon blocks new_cannon = addCannonBlocks(new_cannon); // add
	 * cannon createCannon(new_cannon, player);
	 * 
	 * new_cannon.updateCannonSigns(); return new_cannon; }
	 * 
	 * }
	 * 
	 * return null; }
	 */

	/*
	 * // #################################### CHECK_CANNON
	 * ########################### private CannonAttribute check_Cannon(Location
	 * barrel, Player player) { boolean find_cannon = false; Block block =
	 * barrel.getBlock(); BlockFace face = BlockFace.SELF;
	 * 
	 * if (block.getType() == Material.TORCH) { Torch torch = (Torch)
	 * block.getState().getData(); // get attached Block block =
	 * block.getRelative(torch.getAttachedFace()); } else if (block.getType() ==
	 * Material.STONE_BUTTON) { Button button = (Button)
	 * block.getState().getData(); // get attached Block block =
	 * block.getRelative(button.getAttachedFace()); }
	 * 
	 * boolean redo = false; int length = 0, length_plus = 0, length_minus = 0;
	 * if (CannonsUtil.hasIdData(block, config.CannonMaterialId,
	 * config.CannonMaterialData)) { do { // Select Barrel direction if
	 * (CannonsUtil.hasIdData(block.getRelative(BlockFace.EAST),
	 * config.CannonMaterialId, config.CannonMaterialData) && redo == false) {
	 * face = BlockFace.EAST; } else if
	 * (CannonsUtil.hasIdData(block.getRelative(BlockFace.WEST),
	 * config.CannonMaterialId, config.CannonMaterialData) && redo == false) {
	 * face = BlockFace.WEST; } else if
	 * (CannonsUtil.hasIdData(block.getRelative(BlockFace.SOUTH),
	 * config.CannonMaterialId, config.CannonMaterialData)) { face =
	 * BlockFace.SOUTH; } else if
	 * (CannonsUtil.hasIdData(block.getRelative(BlockFace.NORTH),
	 * config.CannonMaterialId, config.CannonMaterialData)) { face =
	 * BlockFace.NORTH; } else { face = BlockFace.NORTH; }
	 * 
	 * // get barrel length do { length_plus++; } while
	 * (CannonsUtil.hasIdData(block.getRelative(face, length_plus),
	 * config.CannonMaterialId, config.CannonMaterialData) && length_plus <
	 * config.max_barrel_length); do { length_minus++; } while
	 * (CannonsUtil.hasIdData(block.getRelative(face.getOppositeFace(),
	 * length_minus), config.CannonMaterialId, config.CannonMaterialData) &&
	 * length_minus < config.max_barrel_length);
	 * 
	 * // Check Buttons and Torch if
	 * (CannonsUtil.CheckAttachedButton(block.getRelative
	 * (face.getOppositeFace(), length_minus - 1), face.getOppositeFace())) { if
	 * (CannonsUtil.CheckAttachedButton(block.getRelative(face, length_plus -
	 * 1), face)) { if
	 * (CannonsUtil.CheckAttachedTorch(block.getRelative(face.getOppositeFace(),
	 * length_minus - 1))) { if
	 * (!CannonsUtil.CheckAttachedTorch(block.getRelative(face, length_plus -
	 * 1))) { // no change of Face necessary barrel = block.getRelative(face,
	 * length_plus - 1).getLocation(); find_cannon = true; } } else if
	 * (CannonsUtil.CheckAttachedTorch(block.getRelative(face, length_plus -
	 * 1))) { if
	 * (!CannonsUtil.CheckAttachedTorch(block.getRelative(face.getOppositeFace
	 * (), length_minus - 1))) { face = face.getOppositeFace(); barrel =
	 * block.getRelative(face, length_minus - 1).getLocation(); find_cannon =
	 * true; } } } }
	 * 
	 * 
	 * length = length_plus + length_minus - 1; if (!(length >=
	 * config.min_barrel_length && length <= config.max_barrel_length &&
	 * find_cannon == true)) { find_cannon = false; } if (find_cannon == false
	 * && redo == false) { // try other direction redo = true; length_plus = 0;
	 * length_minus = 0; } else { redo = false; }
	 * 
	 * } while (redo); }
	 * 
	 * // check redstonetorches around the cannon if (find_cannon == true) { if
	 * (player != null) { // player is not allowed to place redstonetorch if
	 * (player.hasPermission("cannons.player.placeRedstoneTorch") == false) { //
	 * check all barrel blocks if (CheckRedstone(barrel, length, face) == true)
	 * { player.sendMessage(message.ErrorPermRestoneTorch); } } } }
	 * 
	 * CannonAttribute cannon = new CannonAttribute(); cannon.barrel = barrel;
	 * cannon.face = face; cannon.find = find_cannon; cannon.barrel_length =
	 * length; return cannon; }
	 */

	/**
	 * Deletes all cannons of this player in the database to reset the cannon
	 * limit
	 * 
	 * @param player
	 */
	public void deleteCannons(String owner)
	{
		Iterator<Cannon> iter = cannonList.iterator();

		while (iter.hasNext())
		{
			Cannon next = iter.next();
			if (next.getOwner().equals(owner))
			{
				next.destroyCannon();
				iter.remove();
			}
		}
	}



}