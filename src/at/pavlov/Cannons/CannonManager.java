package at.pavlov.Cannons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
	private ArrayList<CannonData> CannonList = new  ArrayList<CannonData>();
	//private HashMap<Location, Integer> CannonBlocks = new HashMap<Location, Integer>();
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
	public void removeCannon(Location loc)
	{
		CannonData cannon = getCannon(loc);
		removeCannon(cannon);
	}
	
	// ############### remove ###############################
	public void removeCannon(CannonData cannon)
	{
		if (cannon != null)
		{
			// send message to the owner
			Player player = Bukkit.getPlayer(cannon.owner);
			if (player != null)
				player.sendMessage(message.cannonDestroyed);
			
			// drop items
			cannon.destroyCannon();
			
			CannonList.remove(cannon);
		}
	}
	
	
	/**
	 * Checks if the name of a cannon is unique
	 * @param name
	 * @return true if the name is unique
	 */
	private boolean isCannonNameUnique(String name)
	{
		for (CannonData cannon : CannonList)
		{
			if (cannon.name != null && name != null)
			{
				if (cannon.name.equals(name))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private String newCannonName()
	{
		String[] nameList = {"Cannon", "Artillery", "Gun"};
		
		Random r = new Random();
		String pre = nameList[r.nextInt(nameList.length)];

		for (int i=1; i < Integer.MAX_VALUE; i++)
		{
			String cannonName = pre + " " + i;

			if (isCannonNameUnique(cannonName) == true)
			{
				return cannonName;
			}
		}
		
		
		return "no unique name";
	}


	// ############### createCannon ###############################
	private void createCannon(UUID Id, CannonData cannon, Player player)
	{
		CannonList.add(cannon);
		if (player != null)
			player.sendMessage(message.cannonBuilt);
	}
	
	// ############### getCannon ###############################
	public CannonData getCannon(String cannonName)
	{
		for (CannonData cannon : CannonList)
		{
			if (cannon.name.equals(cannonName))
			{
				return cannon;
			}
			
		}
		return null;
	}

	// ############### getCannon ###############################
	public CannonData getCannon(Location loc)
	{
		for (CannonData cannon : CannonList)
		{
			for (Location blockLoc : cannon.CannonBlocks)
			{
				if (blockLoc.equals(loc))
				{
					return cannon;
				}
			}
		}
		return null;
	}

	// ############### contains ###############################
	public boolean isCannonBlock(Location loc)
	{
		if (getCannon(loc) == null)
		{
			//no cannon block
			return false;
		}
		else
		{
			return true;
		}
	}


	// ############### getCannonAmount ###############################
	public int getCannonAmount(Player player)
	{
		int i = 1;
		for (CannonData cannon: CannonList)
		{
			if (cannon.owner.equalsIgnoreCase(player.getName()))
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
		return CannonList;
	}

	// ############### getCannonListSize ###############################
	public int getCannonListSize()
	{
		return CannonList.size();
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
		//if all notes are enabled, set limit to a high number
		if(player.hasPermission("cannons.player.limit." + Integer.MAX_VALUE))
		{
			return Integer.MAX_VALUE;
		}
			
		//else check all nodes for the player
		for(int i = 100; i > 0; i--)
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
			new_cannon.name = newCannonName();
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
		CannonData cannon = getCannon(cannonBlock);

		if (cannon != null)
		{
			// Cannon found, return value can be null
			return cannon;
		}
		else
		{
			// no existing cannon in storage -> check if there is a cannon
			cannon_att att_cannon = check_Cannon(cannonBlock, player);
			if (att_cannon.find == true)
			{
				// cannon found -> add cannon
				return add_cannon(att_cannon, player);
			}
		}

		return null;
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
				if (BlockHelper.CheckAttachedButton(block.getRelative(face.getOppositeFace(), length_minus - 1), face.getOppositeFace()))
				{
					if (BlockHelper.CheckAttachedButton(block.getRelative(face, length_plus - 1), face))
					{
						if (BlockHelper.CheckAttachedTorch(block.getRelative(face.getOppositeFace(), length_minus - 1)))
						{
							if (!BlockHelper.CheckAttachedTorch(block.getRelative(face, length_plus - 1)))
							{
								// no change of Face necessary
								barrel = block.getRelative(face, length_plus - 1).getLocation();
								find_cannon = true;
							}
						}
						else if (BlockHelper.CheckAttachedTorch(block.getRelative(face, length_plus - 1)))
						{
							if (!BlockHelper.CheckAttachedTorch(block.getRelative(face.getOppositeFace(), length_minus - 1)))
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