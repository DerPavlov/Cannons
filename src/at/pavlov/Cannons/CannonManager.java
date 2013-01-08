package at.pavlov.Cannons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Button;
import org.bukkit.material.Torch;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.CannonData;
import at.pavlov.Cannons.utils.BlockHelper;

public class CannonManager
{
	private HashMap<UUID, CannonData> Cannon_list = new HashMap<UUID, CannonData>();
	private HashMap<Location, UUID> CannonBlocks = new HashMap<Location, UUID>();
	// private HashMap<UUID,vessel> vesselList = new HashMap<UUID,vessel>();

	@SuppressWarnings("unused")
	private Cannons plugin;
	private UserMessages message;
	private Config config;

	public CannonManager(Cannons cannons, UserMessages userMessages, Config config)
	{
		this.message = userMessages;
		this.config = config;
		this.plugin = cannons;
	}

	public class cannon_att
	{
		public Location barrel;
		public BlockFace face;
		public int barrel_length;
		public boolean find;
	}

	// ############### remove ###############################
	public void remove(UUID Id)
	{
		CannonData cannon = Cannon_list.get(Id);
		if (cannon != null)
		{
			// send message to the owner
			Player player = Bukkit.getPlayer(cannon.owner);
			if (player != null)
				player.sendMessage(message.cannonDestroyed);

			if (CannonBlocks != null)
			{
				// remove all cannon blocks
				Iterator<Location> iter = cannon.CannonBlocks.iterator();
				while (iter.hasNext())
				{
					Location next = iter.next();
					CannonBlocks.remove(next);
				}
			}
			cannon.isValid = false;
			Cannon_list.remove(Id);
		}
	}

	// ############### remove ###############################
	public void remove(Location loc)
	{
		CannonBlocks.remove(loc);
	}

	// ############### createCannon ###############################
	private void createCannon(UUID Id, CannonData cannon, Player player)
	{
		Cannon_list.put(Id, cannon);
		Iterator<Location> iter = cannon.CannonBlocks.iterator();
		while (iter.hasNext())
		{
			Location loc = iter.next();
			CannonBlocks.put(loc, Id);
		}
		if (player != null)
			player.sendMessage(message.cannonBuilt);
	}

	// ############### getCannon ###############################
	public CannonData getCannon(UUID id)
	{
		return Cannon_list.get(id);
	}

	// ############### getID ###############################
	public UUID getID(Location loc)
	{
		return CannonBlocks.get(loc);
	}

	// ############### contains ###############################
	public boolean contains(Location loc)
	{
		return CannonBlocks.containsKey(loc);
	}

	// ############### contains ###############################
	public boolean contains(UUID id)
	{
		return Cannon_list.containsKey(id);
	}

	// ############### getCannonAmount ###############################
	public int getCannonAmount(Player player)
	{
		int i = 1;
		for (Map.Entry<UUID, CannonData> cannon : Cannon_list.entrySet())
		{
			CannonData next = cannon.getValue();
			if (next.owner.equalsIgnoreCase(player.getName()))
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
	public List<CannonData> getCannonList()
	{
		return new ArrayList<CannonData>(Cannon_list.values());
	}

	// ############### getCannonListSize ###############################
	public int getCannonListSize()
	{
		return Cannon_list.size();
	}

	// ############### getBlockListSize ###############################
	public int getBlockListSize()
	{
		return CannonBlocks.size();
	}

	// ############### getCannonListEntrySet ###############################
	public Set<Entry<UUID, CannonData>> getCannonListEntrySet()
	{
		return Cannon_list.entrySet();
	}

	// ############### getCannonBlocksEntrySet ###############################
	public Set<Entry<Location, UUID>> getCannonBlocksEntrySet()
	{
		return CannonBlocks.entrySet();
	}

	// ############## CheckCannonsAmount ################################
	private boolean CheckCannonsAmount(Player player)
	{
		// the player is not valid - no limit check
		if (player == null) return true;
		
		// both limitA/B and cannons.player.limit.5 work
		int newBuildLimit = getBuildLimit(player);
		int i = getCannonAmount(player); 

		if (newBuildLimit == -1)
		{
			// previous implementation
			if (player.hasPermission("cannons.player.limitB") && i > config.cannonLimitB)
			{
				player.sendMessage(message.getTooManyGuns(config.cannonLimitB));
				return false;
			}
			// limit B is stronger
			if (player.hasPermission("cannons.player.limitB") == false && player.hasPermission("cannons.player.limitA") && i > config.cannonLimitA)
			{
				player.sendMessage(message.getTooManyGuns(config.cannonLimitA));
				return false;
			}
			return true;
		}
		else
		{
			// new implementation
			if (i > newBuildLimit)
			{
				player.sendMessage(message.getTooManyGuns(newBuildLimit));
				return false;
			}
			return true;
		}
	}

	/**
	 * Extracts the build limit of a player
	 * 
	 * @param player
	 * @return
	 */
	private int getBuildLimit(Player player)
	{	
		for(int i = 0; i < 100; i++)
		{
			if(player.hasPermission("cannons.player.limit." + i))
				return i;
		}
		
		return -1;
	}

	// #################################### addCannonBlocks #########################
	private CannonData addCannonBlocks(CannonData cannon, UUID uniqueId)
	{
		if (cannon == null)
			return null;

		// is cannonmaterial
		Block block = cannon.location.getBlock();
		BlockFace reverse = cannon.face.getOppositeFace();

		// button in front
		Location loc = block.getRelative(cannon.face).getLocation();
		cannon.addBlock(loc);

		// barrel
		loc = block.getLocation();
		cannon.addBlock(loc);

		// go back along the barrel + button backside
		int length = 1;
		do
		{
			length++;
			block = block.getRelative(reverse, 1);
			loc = block.getLocation();
			cannon.addBlock(loc);
		} while (length < cannon.barrel_length + 1);
		block = block.getRelative(cannon.face, 1);

		// torch backside
		loc = block.getRelative(BlockFace.UP).getLocation();
		cannon.addBlock(loc);

		return cannon;
	}

	// #################################### ADD_CANNON ######################
	private CannonData add_cannon(cannon_att att_cannon, Player player)
	{
		// check if player is allowed to build a new cannon
		boolean create = true;
		if (config.enableLimits == true)
		{
			create = CheckCannonsAmount(player);
		}
		if (create == true)
		{
			// add
			CannonData new_cannon = new CannonData();
			new_cannon.location = att_cannon.barrel;
			new_cannon.face = att_cannon.face;
			new_cannon.barrel_length = att_cannon.barrel_length;
			new_cannon.LastFired = 0;
			new_cannon.gunpowder = 0;
			new_cannon.projectileID = Material.AIR.getId();
			new_cannon.horizontal_angle = 0;
			new_cannon.vertical_angle = 0;
			if (player != null)
				new_cannon.owner = player.getName();
			new_cannon.designId = 0; // not used at the moment
			new_cannon.isValid = true;
			UUID id = UUID.randomUUID();

			// add cannon blocks
			new_cannon = addCannonBlocks(new_cannon, id);
			// add cannon
			createCannon(id, new_cannon, player);

			return new_cannon;
		}
		return null;
	}

	// #################################### FIND_CANNON ####################
	public CannonData find_cannon(Location cannonBlock, Player player)
	{
		UUID id = getID(cannonBlock);

		if (id != null)
		{
			// Cannon found, return value can be null
			return getCannon(id);
		}
		else
		{
			// no existing cannon -> check if there is a cannon
			cannon_att att_cannon = check_Cannon(cannonBlock, player);
			if (att_cannon.find == true)
			{
				// cannon found -> add cannon
				return add_cannon(att_cannon, player);
			}
		}

		return null;
	}

	// ############## DeleteObsoletCannons ################################
	@Deprecated
	public void DeleteObsoletCannons()
	{
		// check if the cannon exists
		for (Map.Entry<UUID, CannonData> entry : Cannon_list.entrySet())
		{
			CannonData cannon = entry.getValue();
			if (CheckExistingCannon(cannon) == false)
			{
				remove(entry.getKey());
			}
		}

		// delete Blocks with wrong UUID
		for (Map.Entry<Location, UUID> entry : CannonBlocks.entrySet())
		{
			if (contains(entry.getValue()) == false)
			{
				remove(entry.getValue());
			}
		}
	}

	// #################################### CHECK_CANNON ###########################
	private cannon_att check_Cannon(Location barrel, Player player)
	{
		boolean find_cannon = false;
		Block block = barrel.getBlock();
		BlockFace face = BlockFace.SELF;

		if (block.getType() == Material.TORCH)
		{
			Torch torch = (Torch) block.getState().getData();
			// get attached Block
			block = block.getRelative(torch.getAttachedFace());
		}
		else if (block.getType() == Material.STONE_BUTTON)
		{
			Button button = (Button) block.getState().getData();
			// get attached Block
			block = block.getRelative(button.getAttachedFace());
		}

		boolean redo = false;
		int length = 0, length_plus = 0, length_minus = 0;
		if (BlockHelper.hasIdData(block, config.CannonMaterialId, config.CannonMaterialData))
		{
			do
			{
				// Select Barrel direction
				if (BlockHelper.hasIdData(block.getRelative(BlockFace.EAST), config.CannonMaterialId, config.CannonMaterialData) && redo == false)
				{
					face = BlockFace.EAST;
				}
				else if (BlockHelper.hasIdData(block.getRelative(BlockFace.WEST), config.CannonMaterialId, config.CannonMaterialData) && redo == false)
				{
					face = BlockFace.WEST;
				}
				else if (BlockHelper.hasIdData(block.getRelative(BlockFace.SOUTH), config.CannonMaterialId, config.CannonMaterialData))
				{
					face = BlockFace.SOUTH;
				}
				else if (BlockHelper.hasIdData(block.getRelative(BlockFace.NORTH), config.CannonMaterialId, config.CannonMaterialData))
				{
					face = BlockFace.NORTH;
				}
				else
				{
					face = BlockFace.NORTH;
				}

				// get barrel length
				do
				{
					length_plus++;
				} while (BlockHelper.hasIdData(block.getRelative(face, length_plus), config.CannonMaterialId, config.CannonMaterialData) && length_plus < config.max_barrel_length);
				do
				{
					length_minus++;
				} while (BlockHelper.hasIdData(block.getRelative(face.getOppositeFace(), length_minus), config.CannonMaterialId, config.CannonMaterialData) && length_minus < config.max_barrel_length);

				// Check Buttons and Torch
				if (CheckAttachedButton(block.getRelative(face.getOppositeFace(), length_minus - 1), face.getOppositeFace()))
				{
					if (CheckAttachedButton(block.getRelative(face, length_plus - 1), face))
					{
						if (CheckAttachedTorch(block.getRelative(face.getOppositeFace(), length_minus - 1)))
						{
							if (!CheckAttachedTorch(block.getRelative(face, length_plus - 1)))
							{
								// no change of Face necessary
								barrel = block.getRelative(face, length_plus - 1).getLocation();
								find_cannon = true;
							}
						}
						else if (CheckAttachedTorch(block.getRelative(face, length_plus - 1)))
						{
							if (!CheckAttachedTorch(block.getRelative(face.getOppositeFace(), length_minus - 1)))
							{
								face = face.getOppositeFace();
								barrel = block.getRelative(face, length_minus - 1).getLocation();
								find_cannon = true;
							}
						}
					}
				}
				

				length = length_plus + length_minus - 1;
				if (!(length >= config.min_barrel_length && length <= config.max_barrel_length && find_cannon == true))
				{
					find_cannon = false;
				}
				if (find_cannon == false && redo == false)
				{
					// try other direction
					redo = true;
					length_plus = 0;
					length_minus = 0;
				}
				else
				{
					redo = false;
				}

			} while (redo);
		}

		// check redstonetorches around the cannon
		if (find_cannon == true)
		{
			if (player != null)
			{
				// player is not allowed to place redstonetorch
				if (player.hasPermission("cannons.player.placeRedstoneTorch") == false)
				{
					// check all barrel blocks
					if (CheckRedstone(barrel, length, face) == true)
					{
						player.sendMessage(message.ErrorPermRestoneTorch);
					}
				}
			}
		}

		cannon_att cannon;
		cannon = new cannon_att();
		cannon.barrel = barrel;
		cannon.face = face;
		cannon.find = find_cannon;
		cannon.barrel_length = length;
		return cannon;
	}

	// ############## CheckRedstone ##############################
	private boolean CheckRedstone(Location barrel, int length, BlockFace face)
	{

		// torch
		Block barrelBlock = barrel.getBlock();
		Boolean broke = false;
		for (int i = 0; i < length; i++)
		{
			Block next = barrelBlock.getRelative(face.getOppositeFace(), i);
			for (Block b : SurroundingBlocks(next))
			{
				if (b.getType() == Material.REDSTONE_TORCH_ON || b.getType() == Material.REDSTONE_TORCH_OFF)
				{
					broke = true;
					b.breakNaturally();
				}
			}
		}

		// wire
		Block button = barrelBlock.getRelative(face.getOppositeFace(), length);
		for (Block b : HorizontalSurroundingBlocks(button))
		{
			if (b.getType() == Material.REDSTONE_WIRE)
			{
				broke = true;
				b.breakNaturally();
			}
		}

		return broke;
	}

	// ################### CheckExistingCannon ##########################
	@Deprecated
	private boolean CheckExistingCannon(CannonData cannon)
	{
		if (cannon == null)
			return false;

		// is cannonmaterial
		Block block = cannon.location.getBlock();
		if (BlockHelper.hasIdData(block, config.CannonMaterialId, config.CannonMaterialData))
		{
			BlockFace reverse = cannon.face.getOppositeFace();
			// button in front
			if (!CheckAttachedButton(block, cannon.face)) { return false; }
			// go back along the barrel
			int length = 1;
			do
			{
				length++;
				block = block.getRelative(reverse, 1);
			} while (length < cannon.barrel_length + 1);
			block = block.getRelative(cannon.face, 1);
			// button backside
			if (!CheckAttachedButton(block, reverse)) { return false; }
			// torch backside
			if (!CheckAttachedTorch(block)) { return false; }
			// cannon ok
			return true;
		}

		return false;
	}

	// ################# CheckAttachedButton ###########################
	private boolean CheckAttachedButton(Block block, BlockFace face)
	{
		Block attachedBlock = block.getRelative(face);
		if (attachedBlock.getType() == Material.STONE_BUTTON)
		{
			Button button = (Button) attachedBlock.getState().getData();
			if (button.getAttachedFace() != null)
			{
				if (attachedBlock.getRelative(button.getAttachedFace()).equals(block)) { return true; }
			}
			// attached face not available
			else
			{
				return true;
			}
		}
		return false;
	}

	// ################# CheckAttachedTorch ###########################
	public boolean CheckAttachedTorch(Block block)
	{
		Block attachedBlock = block.getRelative(BlockFace.UP);
		if (attachedBlock.getType() == Material.TORCH)
		{
			Torch torch = (Torch) attachedBlock.getState().getData();
			if (torch.getAttachedFace() != null)
			{
				if (attachedBlock.getRelative(torch.getAttachedFace()).equals(block)) { return true; }
			}
			// attached face not available
			else
			{
				return true;
			}
		}
		return false;
	}

	// ########### SurroundingBlocks #######################################
	public ArrayList<Block> SurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.UP));
		Blocks.add(block.getRelative(BlockFace.DOWN));
		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}

	// ########### SurroundingBlocks #######################################
	public ArrayList<Block> HorizontalSurroundingBlocks(Block block)
	{
		ArrayList<Block> Blocks = new ArrayList<Block>();

		Blocks.add(block.getRelative(BlockFace.SOUTH));
		Blocks.add(block.getRelative(BlockFace.WEST));
		Blocks.add(block.getRelative(BlockFace.NORTH));
		Blocks.add(block.getRelative(BlockFace.EAST));
		return Blocks;
	}

}