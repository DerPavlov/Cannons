package at.pavlov.Cannons.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.schematic.SchematicFormat;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonBlocks;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.container.DesignFileName;
import at.pavlov.Cannons.container.MaterialHolder;
import at.pavlov.Cannons.container.SimpleBlock;
import at.pavlov.Cannons.utils.CannonsUtil;

public class DesignStorage
{
	private FileConfiguration cannonDesignConfig = null;
	private File cannonDesignFile = null;
	
	private List<CannonDesign> cannonDesignList;

	Cannons plugin;

	public DesignStorage(Cannons cannons)
	{
		plugin = cannons;
		cannonDesignList = new ArrayList<CannonDesign>();
	}

	/**
	 * loads all custom cannon desgins
	 */
	public void loadCannonDesigns()
	{
		plugin.logInfo("loading cannon designs");

		//clear designList before loading
		cannonDesignList.clear();
		
		// check if design folder is empty or does not exist
		if (CannonsUtil.isFolderEmpty(getPath()))
		{
			// the folder is empty, copy defaults
			plugin.logInfo("No cannon designs loaded - loading default designs");
			copyDefaultDesigns();
		}

		ArrayList<DesignFileName> designFileList = getDesignFiles();

		// stop if there are no files found
		if (designFileList == null || designFileList.size() == 0)
			return;

		for (DesignFileName designFile : designFileList)
		{
			CannonDesign cannonDesign = new CannonDesign();
			//load .yml
			loadDesignYml(cannonDesign, designFile.getYmlString());
			//load .shematic and add to list if valid
			if (loadDesignSchematic(cannonDesign, designFile.getSchematicString()) == true) 
				cannonDesignList.add(cannonDesign);
		}
	}

	/**
	 * returns a list with valid cannon designs (.yml + .schematic)
	 * 
	 * @return
	 */
	private ArrayList<DesignFileName> getDesignFiles()
	{
		ArrayList<DesignFileName> designList = new ArrayList<DesignFileName>();

		try
		{
			// check plugin/cannons/designs for .yml and .schematic files
			String ymlFile;
			File folder = new File(getPath());

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++)
			{

				if (listOfFiles[i].isFile())
				{
					ymlFile = listOfFiles[i].getName();
					if (ymlFile.endsWith(".yml") || ymlFile.endsWith(".yaml"))
					{
						String schematicFile = CannonsUtil.changeExtension(ymlFile, ".schematic");
						if (new File(getPath() + schematicFile).isFile())
						{
							// there is a shematic file and a .yml file
							designList.add(new DesignFileName(ymlFile, schematicFile));
						}
						else
						{
							plugin.logSevere(schematicFile + " is missing");
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			plugin.logSevere("Error while checking yml and schematic " + e);
		}
		return designList;
	}

	/**
	 * loads the config for one cannon from the .yml file
	 * @param ymlPath
	 *            of the cannon config file
	 */
	private void loadDesignYml(CannonDesign cannonDesign, String ymlFile)
	{
		// load .yml file
		if (cannonDesignFile == null)
		{
			cannonDesignFile = new File(getPath() + ymlFile);
		}
		cannonDesignConfig = YamlConfiguration.loadConfiguration(cannonDesignFile);

		// load all entries of the config file

		// general
		cannonDesign.setDesignID(CannonsUtil.removeExtension(ymlFile));
		cannonDesign.setDesignName(cannonDesignConfig.getString("general.designName", "noCannonName"));

		// sign
		cannonDesign.setSignRequired(cannonDesignConfig.getBoolean("signs.isSignRequired", false));

		// ammunition
		cannonDesign.setGunpowderName(cannonDesignConfig.getString("ammunition.gunpowderName", "gunpowder"));
		cannonDesign.setGunpowderType(new MaterialHolder(cannonDesignConfig.getString("ammunition.gunpowderType", "0:0")));
		cannonDesign.setAmmoInfiniteForPlayer(cannonDesignConfig.getBoolean("ammunition.ammoInfiniteForPlayer", false));
		cannonDesign.setAmmoInfiniteForRedstone(cannonDesignConfig.getBoolean("ammunition.setAmmoInfiniteForRedstone", false));
		cannonDesign.setAutoreloadRedstone(cannonDesignConfig.getBoolean("ammunition.autoreloadRedstone", false));

		// barrelProperties
		cannonDesign.setMaxLoadableGunpowder(cannonDesignConfig.getInt("barrelProperties.maxLoadableGunpowder", 1));
		cannonDesign.setMultiplierVelocity(cannonDesignConfig.getDouble("barrelProperties.multiplierVelocity", 1.0));
		cannonDesign.setSpreadOfCannon(cannonDesignConfig.getDouble("barrelProperties.spreadOfCannon", 5.0));

		// timings
		cannonDesign.setBlastConfusion(cannonDesignConfig.getDouble("timings.blastConfusion", 5.0));
		cannonDesign.setFuseBurnTime(cannonDesignConfig.getDouble("timings.fuseBurnTime", 1.0));
		cannonDesign.setBarrelCooldownTime(cannonDesignConfig.getDouble("timings.barrelCooldownTime", 1.0));

		// angles
		cannonDesign.setDefaultHorizonatalFacing(BlockFace.valueOf(cannonDesignConfig.getString("angles.defaultHorizonatalFacing", "NORTH").toUpperCase()));
		cannonDesign.setDefaultVerticalAngle(cannonDesignConfig.getDouble("angles.defaultVerticalAngle", 0.0));
		cannonDesign.setMaxHorizontalAngle(cannonDesignConfig.getDouble("angles.maxHorizontalAngle", 45.0));
		cannonDesign.setMinHorizontalAngle(cannonDesignConfig.getDouble("angles.minHorizontalAngle", -45.0));
		cannonDesign.setMaxVerticalAngle(cannonDesignConfig.getDouble("angles.maxVerticalAngle", 45.0));
		cannonDesign.setMinVerticalAngle(cannonDesignConfig.getDouble("angles.minVerticalAngle", -45.0));
		cannonDesign.setAngleStepSize(cannonDesignConfig.getDouble("angles.angleStepSize", 1.0));
		cannonDesign.setAngleUpdateSpeed(cannonDesignConfig.getDouble("angles.angleUpdateSpeed", 1.0));

		// realisticBehaviour
		cannonDesign.setFlintAndSteelRequired(cannonDesignConfig.getBoolean("realisticBehaviour.fireWithFlintAndSteel", false));
		cannonDesign.setHasRecoil(cannonDesignConfig.getBoolean("realisticBehaviour.hasRecoil", false));
		cannonDesign.setFrontloader(cannonDesignConfig.getBoolean("realisticBehaviour.isFrontloader", false));
		cannonDesign.setRotabable(cannonDesignConfig.getBoolean("realisticBehaviour.isRotabable", false));

		// permissions
		cannonDesign.setPermissionBuild(cannonDesignConfig.getString("permissions.build", "cannon.player.build"));
		cannonDesign.setPermissionLoad(cannonDesignConfig.getString("permissions.load", "cannons.player.load"));
		cannonDesign.setPermissionFire(cannonDesignConfig.getString("permissions.fire", "cannons.player.fire"));
		cannonDesign.setPermissionAutoaim(cannonDesignConfig.getString("permissions.autoaim", "cannons.player.autoaim"));
		cannonDesign.setPermissionTargetTracking(cannonDesignConfig.getString("permissions.targetTracking", "cannons.player.targetTracking"));
		cannonDesign.setPermissionRedstone(cannonDesignConfig.getString("permissions.redstone", "cannons.player.redstone"));
		cannonDesign.setPermissionAutoreload(cannonDesignConfig.getString("permissions.autoreload", "cannons.player.autoreload"));

		// accessRestriction
		cannonDesign.setAccessForOwnerOnly(cannonDesignConfig.getBoolean("realisticBehaviour.ownerOnly", false));

		// allowedProjectiles
		cannonDesign.setAllowedProjectiles(cannonDesignConfig.getStringList("allowedProjectiles"));

		// constructionBlocks
		cannonDesign.setSchematicBlockTypeIgnore(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.ignore", "12:0")));
		cannonDesign.setSchematicBlockTypeMuzzle(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.muzzle", "80:0")));
		cannonDesign.setSchematicBlockTypeFiringIndicator(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.firingIndicator", "50:5")));
		cannonDesign.setSchematicBlockTypeRotationCenter(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.rotationCenter", "73:0")));
		cannonDesign.setSchematicBlockTypeChestAndSign(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.chestAndSign", "63:-1")));
		cannonDesign.setSchematicBlockTypeRedstoneTorch(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.redstoneTorch", "76:5")));
		cannonDesign.setSchematicBlockTypeRedstoneWireAndRepeater(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.restoneWireAndRepeater", "55:-1")));
		// RedstoneTrigger
		cannonDesign.setSchematicBlockTypeRedstoneTrigger(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.redstoneTrigger.schematic", "69:1")));
		cannonDesign.setIngameBlockTypeRedstoneTrigger(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.redstoneTrigger.ingame", "77:1")));
		// rightClickTrigger
		cannonDesign.setSchematicBlockTypeRightClickTrigger(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.rightClickTrigger.schematic", "50:5")));
		cannonDesign.setIngameBlockTypeRightClickTrigger(new MaterialHolder(cannonDesignConfig.getString("constructionBlocks.rightClickTrigger.ingame", "50:5")));
	}

	/**
	 * loads the schematic of the config file
	 * 
	 * @param schematicPath
	 *            path of the schematic file
	 */
	private boolean loadDesignSchematic(CannonDesign cannonDesign, String schematicFile)
	{
		
		// load shematic with worldedit
		CuboidClipboard cc;
		try
		{
			SchematicFormat schematic = SchematicFormat.getFormat(new File(getPath() + schematicFile));
		
			cc = schematic.load(new File(getPath() + schematicFile));
		}
		catch (Exception e)
		{
			plugin.logSevere("Error while loading shematic " + e);
			return false;
		}
		//failed to load schematic
		if (cc == null) 
		{
			plugin.logSevere("Error while loading shematic");
			return false;
		}
		
		// convert all schematic blocks from the config to BaseBlocks so they
		// can be rotated
		BaseBlock blockIgnore = cannonDesign.getSchematicBlockTypeIgnore().toBaseBlock();
		BaseBlock blockMuzzle = cannonDesign.getSchematicBlockTypeMuzzle().toBaseBlock();
		BaseBlock blockFiringIndicator = cannonDesign.getSchematicBlockTypeFiringIndicator().toBaseBlock();
		BaseBlock blockRotationCenter = cannonDesign.getSchematicBlockTypeRotationCenter().toBaseBlock();
		BaseBlock blockChestAndSign = cannonDesign.getSchematicBlockTypeChestAndSign().toBaseBlock();
		BaseBlock blockRedstoneTorch = cannonDesign.getSchematicBlockTypeRedstoneTorch().toBaseBlock();
		BaseBlock blockRedstoneWireAndRepeater = cannonDesign.getSchematicBlockTypeRedstoneWireAndRepeater().toBaseBlock();
		BaseBlock blockRedstoneTrigger = cannonDesign.getSchematicBlockTypeRedstoneTrigger().toBaseBlock();
		BaseBlock blockRightClickTrigger = cannonDesign.getSchematicBlockTypeRightClickTrigger().toBaseBlock();
		BaseBlock replaceRedstoneTrigger = cannonDesign.getIngameBlockTypeRedstoneTrigger().toBaseBlock();
		BaseBlock replaceRightClickTrigger = cannonDesign.getIngameBlockTypeRightClickTrigger().toBaseBlock();
		
		// get facing of the cannon
		BlockFace cannonDirection = cannonDesign.getDefaultHorizonatalFacing();

		// for all directions
		for (int i = 0; i < 4; i++)
		{
			// read out blocks
			int width = cc.getWidth();
			int height = cc.getHeight();
			int length = cc.getLength();
			
			// to set the muzzle location the maximum and mininum x, y, z values
			// of all muzzle blocks have to be found
			Vector minMuzzle = new Vector(0, 0, 0);
			Vector maxMuzzle = new Vector(0, 0, 0);
			boolean firstEntryMuzzle = true;
			
			// to set the rotation Center maximum and mininum x, y, z values
			// of all rotation blocks have to be found
			// setting max to the size of the marked area is a good approximation
			// if no rotationblock is given
			Vector minRotation = new Vector(0, 0, 0);
			Vector maxRotation = new Vector(width, height, length);
			boolean firstEntryRotation = true;
			
			// create CannonBlocks entry
			CannonBlocks cannonBlocks = new CannonBlocks();
			
			for (int x = 0; x < width; ++x)
			{
				for (int y = 0; y < height; ++y)
				{
					for (int z = 0; z < length; ++z)
					{
						BlockVector pt = new BlockVector(x, y, z);
						BaseBlock block = cc.getPoint(pt);

						// ignore if block is AIR or the IgnoreBlock type
						if (block.getId() != 0 && !block.equalsFuzzy(blockIgnore))
						{

							plugin.logDebug("x:" + x + " y:" + y + " z:" + z + " blockType " + block.getId() + " blockData " + block.getData());

							// #############  find the min and max for muzzle blocks so the
							// cannonball is fired from the middle
							if (block.equalsFuzzy(blockMuzzle))
							{
								// reset for the first entry
								if (firstEntryMuzzle == true)
								{
									firstEntryMuzzle = false;
									minMuzzle = new Vector(x, y, z);
									maxMuzzle = new Vector(x, y, z);
								}
								else
								{
									minMuzzle = findMinimum(x, y, z, minMuzzle);
									maxMuzzle = findMaximum(x, y, z, maxMuzzle);
								}
								//muzzle blocks need to be air - else the projectile would spawn in a block
								cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, Material.AIR.getId(), 0));
								
							}
							
							// #############  find the min and max for rotation blocks
							else if (block.equalsFuzzy(blockRotationCenter))
							{
								// reset for the first entry
								if (firstEntryRotation == true)
								{
									firstEntryRotation = false;
									minRotation = new Vector(x, y, z);
									maxRotation= new Vector(x, y, z);
								}
								else
								{
									minRotation = findMinimum(x, y, z, minRotation);
									maxRotation = findMaximum(x, y, z, maxRotation);
								}
							}


							// #############  chests and signs
							else if (block.equalsFuzzy(blockChestAndSign))
							{
								// the id does not matter, but the data is important for signs
								cannonBlocks.getChestsAndSigns().add(new SimpleBlock(x, y, z, Material.WALL_SIGN.getId(), block.getData()));
								// if a sign is requried the sign is a cannonblock
								if (cannonDesign.isSignRequired())
								{
									cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, Material.SIGN.getId(), block.getData()));
								}
							}
							// #############  redstoneTorch
							else if (block.equalsFuzzy(blockRedstoneTorch))
								cannonBlocks.getRedstoneTorches().add(new Vector(x, y, z));
							// #############  redstoneWire and Repeater
							else if (block.equalsFuzzy(blockRedstoneWireAndRepeater))
								cannonBlocks.getRedstoneWiresAndRepeater().add(new SimpleBlock(x, y, z, Material.DIODE.getId(), block.getData()));
							// #############  redstoneTrigger
							else if (block.equalsFuzzy(blockRedstoneTrigger))
							{
								cannonBlocks.getRedstoneTrigger().add(new Vector(x, y, z));
								// buttons or levers are part of the cannon
								cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, replaceRedstoneTrigger));
							}
							// #############  rightClickTrigger
							else if (block.equalsFuzzy(blockRightClickTrigger))
							{
								cannonBlocks.getRightClickTrigger().add(new Vector(x, y, z));
								// firing blocks are also part of the cannon are
								// part of the cannon
								cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, replaceRightClickTrigger));
							}
							// #############  loading Interface is a cannonblock that is non of
							// the previous blocks
							else
							{
								// all remaining blocks are loading interface or
								// cannonBlocks
								cannonBlocks.getLoadingInterface().add(new Vector(x, y, z));
								cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, block));
							}

							// #############  firingIndicator
							// can be everywhere on the cannon
							if (block.equalsFuzzy(blockFiringIndicator))
								cannonBlocks.getFiringIndicator().add(new Vector(x, y, z));
						}
					}
				}
			}
			// calculate the muzzle location
			maxMuzzle.add(new Vector(1, 1, 1));
			cannonBlocks.setMuzzle(maxMuzzle.add(minMuzzle).multiply(0.5));
			
			// calculate the rotation Center
			maxRotation.add(new Vector(1, 1, 1));
			cannonBlocks.setRotationCenter(maxRotation.add(maxRotation).multiply(0.5));

			plugin.logDebug("rotation loc " + cannonBlocks.getRotationCenter());

			// add blocks to the HashMap
			cannonDesign.getCannonBlockMap().put(cannonDirection, cannonBlocks);
			
			//rotate blocks for the next iteration
			blockIgnore.rotate90();
			blockMuzzle.rotate90();
			blockFiringIndicator.rotate90();
			blockRotationCenter.rotate90();
			blockChestAndSign.rotate90();
			blockRedstoneTorch.rotate90();
			blockRedstoneWireAndRepeater.rotate90();
			blockRedstoneTrigger.rotate90();
			blockRightClickTrigger.rotate90();
			replaceRedstoneTrigger.rotate90();
			replaceRightClickTrigger.rotate90();
			
			//rotate clipboard
			cc.rotate2D(90);
			
			//rotate cannonDirection
			cannonDirection = CannonsUtil.roatateFace(cannonDirection);
			

		}
		return true;
	}

	private Vector findMinimum(int x, int y, int z, Vector min)
	{
		if (x < min.getBlockX())
			min.setX(x);
		if (y < min.getBlockY())
			min.setY(y);
		if (z < min.getBlockZ())
			min.setZ(z);

		return min;
	}

	private Vector findMaximum(int x, int y, int z, Vector max)
	{
		if (x > max.getBlockX())
			max.setX(x);
		if (y > max.getBlockY())
			max.setY(y);
		if (z > max.getBlockZ())
			max.setZ(z);

		return max;
	}

	/**
	 * copy the default designs from the .jar to the disk
	 */
	private void copyDefaultDesigns()
	{
		File classicYmlFile = new File(plugin.getDataFolder(), "designs/classic.yml");
		File classicSchematicFile = new File(plugin.getDataFolder(), "designs/classic.schematic");

		if (!classicYmlFile.exists())
		{
			classicYmlFile.getParentFile().mkdirs();
			CannonsUtil.copyFile(plugin.getResource("designs/classic.yml"), classicYmlFile);
		}
		if (!classicSchematicFile.exists())
		{
			classicSchematicFile.getParentFile().mkdirs();
			CannonsUtil.copyFile(plugin.getResource("designs/classic.schematic"), classicSchematicFile);
		}
	}
	
	private String getPath()
	{
		// Directory path here
		return "plugins/Cannons/designs/";
	}
	
	public List<CannonDesign> getCannonDesignList()
	{
		return cannonDesignList;
	}
	
	/**
	 * returns the cannon design of the cannon
	 * @param cannon
	 * @return
	 */
	public CannonDesign getDesign(Cannon cannon)
	{
		return getDesign(cannon.getDesignID());
	}
	
	/**
	 * returns the cannon design by its id
	 * @param cannon
	 * @return
	 */
	public CannonDesign getDesign(String designId)
	{
		for (CannonDesign cannonDesign : cannonDesignList)
		{
			if (cannonDesign.getDesignID().equals(designId))
				return cannonDesign;
		}
		return null;
	}
	
}
