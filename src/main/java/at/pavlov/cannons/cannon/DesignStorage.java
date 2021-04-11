package at.pavlov.cannons.cannon;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.block.BlockState;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;




import at.pavlov.cannons.container.SoundHolder;
import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.container.DesignFileName;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.container.SimpleBlock;
import at.pavlov.cannons.utils.CannonsUtil;
import at.pavlov.cannons.utils.DesignComparator;

public class DesignStorage
{
	
	private final List<CannonDesign> cannonDesignList;
	private final Cannons plugin;
	private final List<Material> cannonBlockMaterials;

	public DesignStorage(Cannons cannons)
	{
		plugin = cannons;
		cannonDesignList = new ArrayList<CannonDesign>();
		cannonBlockMaterials = new ArrayList<>();
	}

	/**
	 * returns a list of all cannon design names
	 * @return list of all cannon design names
	 */
	public ArrayList<String> getDesignIds(){
		ArrayList<String> list = new ArrayList<String>();
		for (CannonDesign design : cannonDesignList){
			list.add(design.getDesignID());
		}
		return list;
	}

	/**
	 * loads all custom cannon desgins
	 */
	public void loadCannonDesigns()
	{
		plugin.logInfo("Loading cannon designs");

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
			plugin.logDebug("loading cannon " + designFile.getYmlString());
			CannonDesign cannonDesign = new CannonDesign();
			//load .yml
			loadDesignYml(cannonDesign, designFile.getYmlString());
			//load .shematic and add to list if valid
			if (loadDesignSchematic(cannonDesign, designFile.getSchematicString()))
				cannonDesignList.add(cannonDesign);
		}
		
		//sort the list so the designs with more cannon blocks comes first
		//important if there is a design with one block less but else identically 
		Comparator<CannonDesign> comparator = new DesignComparator();
		cannonDesignList.sort(comparator);

		for (CannonDesign cannonDesign : getCannonDesignList()) {
			for (SimpleBlock sBlock : cannonDesign.getAllCannonBlocks(BlockFace.NORTH)){
				Material material = sBlock.getBlockData().getMaterial();
				if (material != Material.AIR && !cannonBlockMaterials.contains(material)) {
					cannonBlockMaterials.add(sBlock.getBlockData().getMaterial());
				}
			}
		}


		for (CannonDesign design : cannonDesignList)
		{
			plugin.logDebug("design " + design.toString());
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
            if (listOfFiles == null)
            {
                plugin.logSevere("Design folder empty");
                return designList;
            }


			for (File listOfFile : listOfFiles) {
				if (listOfFile.isFile()) {
					ymlFile = listOfFile.getName();
					if (ymlFile.endsWith(".yml") || ymlFile.endsWith(".yaml")) {
						String schematicFile = CannonsUtil.changeExtension(ymlFile, ".schematic");
						String schemFile = CannonsUtil.changeExtension(ymlFile, ".schem");
						if (new File(getPath() + schematicFile).isFile()) {
							// there is a shematic file and a .yml file
							designList.add(new DesignFileName(ymlFile, schematicFile));
						} else if (new File(getPath() + schemFile).isFile()) {
							// there is a shematic file and a .yml file
							designList.add(new DesignFileName(ymlFile, schemFile));
						} else {
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
     * @param cannonDesign design of the cannon
	 * @param ymlFile of the cannon config file
	 */
	private void loadDesignYml(CannonDesign cannonDesign, String ymlFile)
	{
		// load .yml file
		File cannonDesignFile = new File(getPath() + ymlFile);
		FileConfiguration cannonDesignConfig = YamlConfiguration.loadConfiguration(cannonDesignFile);

		// load all entries of the config file

		// general
		cannonDesign.setDesignID(CannonsUtil.removeExtension(ymlFile));
        cannonDesign.setDesignName(cannonDesignConfig.getString("general.designName", "no cannonName"));
        cannonDesign.setMessageName(cannonDesignConfig.getString("general.messageName", "no messageName"));
        cannonDesign.setDescription(cannonDesignConfig.getString("general.description", "no description for this cannon"));
        cannonDesign.setLastUserBecomesOwner(cannonDesignConfig.getBoolean("general.lastUserBecomesOwner", false));

		// sign
		cannonDesign.setSignRequired(cannonDesignConfig.getBoolean("signs.isSignRequired", false));

		// ammunition
		cannonDesign.setGunpowderName(cannonDesignConfig.getString("ammunition.gunpowderName", "gunpowder"));
		cannonDesign.setGunpowderType(new ItemHolder(cannonDesignConfig.getString("ammunition.gunpowderType", "SULPHUR:0")));
        cannonDesign.setNeedsGunpowder(cannonDesignConfig.getBoolean("ammunition.needsGunpowder", true));
        cannonDesign.setGunpowderConsumption(cannonDesignConfig.getBoolean("ammunition.gunpowderConsumption", true));
        cannonDesign.setProjectileConsumption(cannonDesignConfig.getBoolean("ammunition.projectileConsumption", true));
		cannonDesign.setAmmoInfiniteForPlayer(cannonDesignConfig.getBoolean("ammunition.ammoInfiniteForPlayer", false));
		cannonDesign.setAmmoInfiniteForRedstone(cannonDesignConfig.getBoolean("ammunition.ammoInfiniteForRedstone", false));
		cannonDesign.setAutoreloadRedstone(cannonDesignConfig.getBoolean("ammunition.autoreloadRedstone", false));
		cannonDesign.setRemoveChargeAfterFiring(cannonDesignConfig.getBoolean("ammunition.removeChargeAfterFiring", true));
		cannonDesign.setAutoloadChargeWhenLoadingProjectile(cannonDesignConfig.getBoolean("ammunition.autoloadChargeWhenLoadingProjectile", false));
		cannonDesign.setPreloaded(cannonDesignConfig.getBoolean("ammunition.preloaded", false));

		// barrelProperties
		cannonDesign.setMaxLoadableGunpowder(cannonDesignConfig.getInt("barrelProperties.maxLoadableGunpowder", 1));
		if (cannonDesign.getMaxLoadableGunpowderNormal() <= 0)
			cannonDesign.setMaxLoadableGunpowder(1);
		cannonDesign.setMultiplierVelocity(cannonDesignConfig.getDouble("barrelProperties.multiplierVelocity", 1.0));
		cannonDesign.setSpreadOfCannon(cannonDesignConfig.getDouble("barrelProperties.spreadOfCannon", 5.0));

		// timings
		cannonDesign.setBlastConfusion(cannonDesignConfig.getDouble("timings.blastConfusion", 5.0));
		cannonDesign.setFuseBurnTime(cannonDesignConfig.getDouble("timings.fuseBurnTime", 1.0));
		cannonDesign.setBarrelCooldownTime(cannonDesignConfig.getDouble("timings.barrelCooldownTime", 1.0));
		cannonDesign.setLoadTime(cannonDesignConfig.getDouble("timings.loadTime", 3.0));

		// angles
		cannonDesign.setDefaultHorizontalFacing(BlockFace.valueOf(cannonDesignConfig.getString("angles.defaultHorizontalFacing", "NORTH").toUpperCase()));
		cannonDesign.setDefaultVerticalAngle(cannonDesignConfig.getDouble("angles.defaultVerticalAngle", 0.0));
		cannonDesign.setMaxHorizontalAngleNormal(cannonDesignConfig.getDouble("angles.maxHorizontalAngle", 45.0));
		cannonDesign.setMinHorizontalAngleNormal(cannonDesignConfig.getDouble("angles.minHorizontalAngle", -45.0));
		cannonDesign.setMaxVerticalAngleNormal(cannonDesignConfig.getDouble("angles.maxVerticalAngle", 45.0));
		cannonDesign.setMinVerticalAngleNormal(cannonDesignConfig.getDouble("angles.minVerticalAngle", -45.0));
        cannonDesign.setMaxHorizontalAngleOnShip(cannonDesignConfig.getDouble("angles.maxHorizontalAngleOnShip", 20.0));
        cannonDesign.setMinHorizontalAngleOnShip(cannonDesignConfig.getDouble("angles.minHorizontalAngleOnShip", -20.0));
        cannonDesign.setMaxVerticalAngleOnShip(cannonDesignConfig.getDouble("angles.maxVerticalAngleOnShip", 30.0));
        cannonDesign.setMinVerticalAngleOnShip(cannonDesignConfig.getDouble("angles.minVerticalAngleOnShip", -30.0));
		cannonDesign.setAngleStepSize(cannonDesignConfig.getDouble("angles.angleStepSize", 0.1));
		cannonDesign.setAngleLargeStepSize(cannonDesignConfig.getDouble("angles.largeStepSize", 1.0));
		cannonDesign.setAngleUpdateSpeed((int) (cannonDesignConfig.getDouble("angles.angleUpdateSpeed", 1.0) * 1000.0));
        cannonDesign.setAngleUpdateMessage(cannonDesignConfig.getBoolean("angles.angleUpdateMessage", false));

        //impactPredictor
        cannonDesign.setPredictorEnabled(cannonDesignConfig.getBoolean("impactPredictor.enabled", true));
        cannonDesign.setPredictorDelay((int) (cannonDesignConfig.getDouble("impactPredictor.delay", 1.0) * 1000.0));
        cannonDesign.setPredictorUpdate((int) (cannonDesignConfig.getDouble("impactPredictor.update", 0.1) * 1000.0));

		//sentry
		cannonDesign.setSentry(cannonDesignConfig.getBoolean("sentry.isSentry", false));
		cannonDesign.setSentryIndirectFire(cannonDesignConfig.getBoolean("sentry.indirectFire", false));
        cannonDesign.setSentryMinRange(cannonDesignConfig.getInt("sentry.minRange", 5));
        cannonDesign.setSentryMaxRange(cannonDesignConfig.getInt("sentry.maxRange", 40));
		cannonDesign.setSentrySpread(cannonDesignConfig.getDouble("sentry.spread", 0.5));
        cannonDesign.setSentryUpdateTime((int) (cannonDesignConfig.getDouble("sentry.update", 1.0) * 1000.0));
        cannonDesign.setSentrySwapTime((int) (cannonDesignConfig.getDouble("sentry.swapTime", 10.0)*1000.0));

        //heatManagement
        cannonDesign.setHeatManagementEnabled(cannonDesignConfig.getBoolean("heatManagement.enabled", false));
        cannonDesign.setAutomaticTemperatureControl(cannonDesignConfig.getBoolean("heatManagement.automaticTemperatureControl", false));
        cannonDesign.setBurnDamage(cannonDesignConfig.getDouble("heatManagement.burnDamage", 0.5));
        cannonDesign.setBurnSlowing(cannonDesignConfig.getDouble("heatManagement.burnSlowing", 5.0));
        cannonDesign.setHeatIncreasePerGunpowder(cannonDesignConfig.getDouble("heatManagement.heatIncreasePerGunpowder", 10.0));
        cannonDesign.setCoolingCoefficient(cannonDesignConfig.getDouble("heatManagement.coolingTimeCoefficient", 160.0));
        cannonDesign.setCoolingAmount(cannonDesignConfig.getDouble("heatManagement.coolingAmount", 50.0));
        cannonDesign.setAutomaticCooling(cannonDesignConfig.getBoolean("heatManagement.automaticCooling", false));
        cannonDesign.setWarningTemperature(cannonDesignConfig.getDouble("heatManagement.warningTemperature", 100.0));
        cannonDesign.setCriticalTemperature(cannonDesignConfig.getDouble("heatManagement.criticalTemperature", 150.0));
        cannonDesign.setMaximumTemperature(cannonDesignConfig.getDouble("heatManagement.maximumTemperature", 200.0));
        cannonDesign.setItemCooling(CannonsUtil.toItemHolderList(cannonDesignConfig.getStringList("heatManagement.coolingItems")));
        cannonDesign.setItemCoolingUsed(CannonsUtil.toItemHolderList(cannonDesignConfig.getStringList("heatManagement.coolingItemsUsed")));
        if (cannonDesign.getItemCooling().size() != cannonDesign.getItemCoolingUsed().size())
            plugin.logSevere("CoolingItemsUsed and CoolingItems lists must have the same size. Check if both lists have the same number of entries");

        // overloading stuff
        cannonDesign.setOverloadingEnabled(cannonDesignConfig.getBoolean("overloading.enabled",false));
        cannonDesign.setOverloadingRealMode(cannonDesignConfig.getBoolean("overloading.realMode",false));
        cannonDesign.setOverloadingExponent(cannonDesignConfig.getDouble("overloading.exponent",1));
        cannonDesign.setOverloadingChangeInc(cannonDesignConfig.getDouble("overloading.chanceInc",0.1));
        cannonDesign.setOverloadingMaxOverloadableGunpowder(cannonDesignConfig.getInt("overloading.maxOverloadableGunpowder",3));
        cannonDesign.setOverloadingChanceOfExplosionPerGunpowder(cannonDesignConfig.getDouble("overloading.chanceOfExplosionPerGunpowder",0.01));
        cannonDesign.setOverloadingDependsOfTemperature(cannonDesignConfig.getBoolean("overloading.dependsOfTemperature",false));

        //economy
        cannonDesign.setEconomyBuildingCost(cannonDesignConfig.getDouble("economy.buildingCosts", 0.0));
        cannonDesign.setEconomyDismantlingRefund(cannonDesignConfig.getDouble("economy.dismantlingRefund", 0.0));
        cannonDesign.setEconomyDestructionRefund(cannonDesignConfig.getDouble("economy.destructionRefund", 0.0));

        // realisticBehaviour
		cannonDesign.setFiringItemRequired(cannonDesignConfig.getBoolean("realisticBehaviour.isFiringItemRequired", false));
        cannonDesign.setSootPerGunpowder(cannonDesignConfig.getDouble("realisticBehaviour.sootPerGunpowder", 0.0));
        cannonDesign.setProjectilePushing(cannonDesignConfig.getInt("realisticBehaviour.projectilePushing", 0));
		cannonDesign.setHasRecoil(cannonDesignConfig.getBoolean("realisticBehaviour.hasRecoil", false));
		cannonDesign.setFrontloader(cannonDesignConfig.getBoolean("realisticBehaviour.isFrontloader", false));
		cannonDesign.setRotatable(cannonDesignConfig.getBoolean("realisticBehaviour.isRotatable", false));
        cannonDesign.setMassOfCannon(cannonDesignConfig.getInt("realisticBehaviour.massOfCannon", 1000));//What means 1000?
        cannonDesign.setStartingSoot(cannonDesignConfig.getInt("realisticBehaviour.startingSoot",10));
        cannonDesign.setExplodingLoadedCannons(cannonDesignConfig.getDouble("realisticBehaviour.explodingLoadedCannon",2.0));
        cannonDesign.setFireAfterLoading(cannonDesignConfig.getBoolean("realisticBehaviour.fireAfterLoading", false));
		cannonDesign.setDismantlingDelay(cannonDesignConfig.getDouble("realisticBehaviour.dismantlingDelay", 1.75));

		// permissions
		cannonDesign.setPermissionBuild(cannonDesignConfig.getString("permissions.build", "cannons.player.build"));
		cannonDesign.setPermissionDismantle(cannonDesignConfig.getString("permissions.dismantle", "cannons.player.dismantle"));
        cannonDesign.setPermissionRename(cannonDesignConfig.getString("permissions.rename", "cannons.player.rename"));
		cannonDesign.setPermissionLoad(cannonDesignConfig.getString("permissions.load", "cannons.player.load"));
		cannonDesign.setPermissionFire(cannonDesignConfig.getString("permissions.fire", "cannons.player.fire"));
        cannonDesign.setPermissionAdjust(cannonDesignConfig.getString("permissions.adjust", "cannons.player.adjust"));
		cannonDesign.setPermissionAutoaim(cannonDesignConfig.getString("permissions.autoaim", "cannons.player.autoaim"));
        cannonDesign.setPermissionObserver(cannonDesignConfig.getString("permissions.observer", "cannons.player.observer"));
		cannonDesign.setPermissionTargetTracking(cannonDesignConfig.getString("permissions.targetTracking", "cannons.player.targetTracking"));
		cannonDesign.setPermissionRedstone(cannonDesignConfig.getString("permissions.redstone", "cannons.player.redstone"));
        cannonDesign.setPermissionThermometer(cannonDesignConfig.getString("permissions.thermometer", "cannons.player.thermometer"));
        cannonDesign.setPermissionRamrod(cannonDesignConfig.getString("permissions.ramrod", "cannons.player.ramrod"));
		cannonDesign.setPermissionAutoreload(cannonDesignConfig.getString("permissions.autoreload", "cannons.player.autoreload"));
		cannonDesign.setPermissionSpreadMultiplier(cannonDesignConfig.getString("permissions.spreadMultiplier", "cannons.player.spreadMultiplier"));

		// accessRestriction
		cannonDesign.setAccessForOwnerOnly(cannonDesignConfig.getBoolean("accessRestriction.ownerOnly", false));

		// allowedProjectiles
		cannonDesign.setAllowedProjectiles(cannonDesignConfig.getStringList("allowedProjectiles"));

        // sounds
        cannonDesign.setSoundCreate(new SoundHolder(cannonDesignConfig.getString("sounds.create","BLOCK_ANVIL_LAND:1:0.5")));
        cannonDesign.setSoundDestroy(new SoundHolder(cannonDesignConfig.getString("sounds.destroy","ENTITY_ZOMBIE_ATTACK_IRON_DOOR:1:0.5")));
        cannonDesign.setSoundDismantle(new SoundHolder(cannonDesignConfig.getString("sounds.dismantle", "BLOCK_ANVIL_USE:1:0.5")));
        cannonDesign.setSoundAdjust(new SoundHolder(cannonDesignConfig.getString("sounds.adjust","ENTITY_IRON_GOLEM_STEP:1:0.5")));
        cannonDesign.setSoundIgnite(new SoundHolder(cannonDesignConfig.getString("sounds.ignite","ENTITY_TNT_PRIMED:5:1")));
        cannonDesign.setSoundFiring(new SoundHolder(cannonDesignConfig.getString("sounds.firing","ENTITY_GENERIC_EXPLODE:20:1.5")));
        cannonDesign.setSoundGunpowderLoading(new SoundHolder(cannonDesignConfig.getString("sounds.gunpowderLoading","BLOCK_SAND_HIT:1:1.5")));
        cannonDesign.setSoundGunpowderOverloading(new SoundHolder(cannonDesignConfig.getString("sounds.gunpowderOverloading","BLOCK_GRASS_HIT:1:1.5")));
        cannonDesign.setSoundCool(new SoundHolder(cannonDesignConfig.getString("sounds.cool","BLOCK_FIRE_EXTINGUISH:1:1")));
        cannonDesign.setSoundHot(new SoundHolder(cannonDesignConfig.getString("sounds.hot","BLOCK_FIRE_EXTINGUISH:1:1")));
        cannonDesign.setSoundRamrodCleaning(new SoundHolder(cannonDesignConfig.getString("sounds.ramrodCleaning","BLOCK_SNOW_HIT:0.5:0")));
        cannonDesign.setSoundRamrodCleaningDone(new SoundHolder(cannonDesignConfig.getString("sounds.ramrodCleaningDone","BLOCK_SNOW_HIT:0.5:1")));
        cannonDesign.setSoundRamrodPushing(new SoundHolder(cannonDesignConfig.getString("sounds.ramrodPushing","BLOCK_STONE_HIT:0.5:0")));
        cannonDesign.setSoundRamrodPushingDone(new SoundHolder(cannonDesignConfig.getString("sounds.ramrodPushingDone","BLOCK_ANVIL_LAND:0.5:0")));
        cannonDesign.setSoundThermometer(new SoundHolder(cannonDesignConfig.getString("sounds.thermometer","BLOCK_ANVIL_LAND:1:1")));
        cannonDesign.setSoundEnableAimingMode(new SoundHolder(cannonDesignConfig.getString("sounds.enableAimingMode","NONE:1:1")));
        cannonDesign.setSoundDisableAimingMode(new SoundHolder(cannonDesignConfig.getString("sounds.disableAimingMode","NONE:1:1")));
		cannonDesign.setSoundSelected(new SoundHolder(cannonDesignConfig.getString("sounds.selected","BLOCK_ANVIL_LAND:1:2")));

		// constructionBlocks
		cannonDesign.setSchematicBlockTypeIgnore(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.ignore", "minecraft:sand")));
		cannonDesign.setSchematicBlockTypeMuzzle(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.muzzle", "minecraft:snow_block")));
		cannonDesign.setSchematicBlockTypeFiringIndicator(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.firingIndicator", "minecraft:torch")));
		cannonDesign.setSchematicBlockTypeRotationCenter(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.rotationCenter", "minecraft:redstone_ore")));
		cannonDesign.setSchematicBlockTypeChestAndSign(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.chestAndSign", "minecraft:oak_wall_sign")));
		cannonDesign.setSchematicBlockTypeRedstoneTorch(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.redstoneTorch", "minecraft:redstone_torch")));
		cannonDesign.setSchematicBlockTypeRedstoneWireAndRepeater(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.restoneWireAndRepeater", "minecraft:repeater")));
		// RedstoneTrigger
		cannonDesign.setSchematicBlockTypeRedstoneTrigger(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.redstoneTrigger.schematic", "minecraft:lever")));
		cannonDesign.setIngameBlockTypeRedstoneTrigger(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.redstoneTrigger.ingame", "minecraft:stone_button")));
		// rightClickTrigger
		cannonDesign.setSchematicBlockTypeRightClickTrigger(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.rightClickTrigger.schematic", "minecraft:torch")));
		cannonDesign.setIngameBlockTypeRightClickTrigger(CannonsUtil.createBlockData(cannonDesignConfig.getString("constructionBlocks.rightClickTrigger.ingame", "minecraft:torch")));
		// protected Blocks
		cannonDesign.setSchematicBlockTypeProtected(CannonsUtil.toBlockDataList(cannonDesignConfig.getStringList("constructionBlocks.protectedBlocks")));
	}

	/**
	 * loads the schematic of the config file
	 * @param cannonDesign design of the cannon
	 * @param schematicFile path of the schematic file
	 */
	private boolean loadDesignSchematic(CannonDesign cannonDesign, String schematicFile)
	{
        long startTime = System.nanoTime();
		
		// load schematic with worldedit
        Clipboard cc;
        File f = new File(getPath() + schematicFile);
		ClipboardFormat format = ClipboardFormats.findByFile(f);
		try (Closer closer = Closer.create()) {
			FileInputStream fis = closer.register(new FileInputStream(f));
			BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
			ClipboardReader reader = closer.register(format.getReader(bis));

			cc = reader.read();
		} catch (IOException e) {
			plugin.logSevere("Error while loading schematic " + getPath() + schematicFile + " :" + e  + "; does file exist: " + f.exists());
			return false;
		}
		//failed to load schematic
		if (cc == null) 
		{
			plugin.logSevere("Failed to loading schematic");
			return false;
		}

        AffineTransform transform = new AffineTransform().translate(cc.getMinimumPoint().multiply(-1));
        BlockTransformExtent extent = new BlockTransformExtent(cc, transform);
        ForwardExtentCopy copy = new ForwardExtentCopy(extent, cc.getRegion(), cc.getOrigin(), cc, BlockVector3.ZERO);
        copy.setTransform(transform);
        try {
            Operations.complete(copy);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }

		// convert all schematic blocks from the config to BaseBlocks so they
		// can be rotated
		BlockData blockIgnore = cannonDesign.getSchematicBlockTypeIgnore();
		BlockData blockMuzzle = cannonDesign.getSchematicBlockTypeMuzzle();
		BlockData blockFiringIndicator = cannonDesign.getSchematicBlockTypeFiringIndicator();
		BlockData blockRotationCenter = cannonDesign.getSchematicBlockTypeRotationCenter();
		BlockData blockChestAndSign = cannonDesign.getSchematicBlockTypeChestAndSign();
		BlockData blockRedstoneTorch = cannonDesign.getSchematicBlockTypeRedstoneTorch();
		BlockData blockRedstoneWireAndRepeater = cannonDesign.getSchematicBlockTypeRedstoneWireAndRepeater();
		BlockData blockRedstoneTrigger = cannonDesign.getSchematicBlockTypeRedstoneTrigger();
		BlockData blockRightClickTrigger = cannonDesign.getSchematicBlockTypeRightClickTrigger();
		BlockData replaceRedstoneTrigger = cannonDesign.getIngameBlockTypeRedstoneTrigger();
		BlockData replaceRightClickTrigger = cannonDesign.getIngameBlockTypeRightClickTrigger();
        List<BlockData> blockProtectedList = new ArrayList<BlockData>(cannonDesign.getSchematicBlockTypeProtected());
		
		
		// get facing of the cannon
		BlockFace cannonDirection = cannonDesign.getDefaultHorizontalFacing();

		// read out blocks
		int width = cc.getDimensions().getBlockX();
		int height = cc.getDimensions().getBlockY();
		int length = cc.getDimensions().getBlockZ();

		cc.setOrigin(BlockVector3.ZERO);

		//plugin.logDebug("design: " + schematicFile);
		ArrayList<SimpleBlock> schematiclist = new ArrayList<>();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {

					BlockVector3 pt = BlockVector3.at(x, y, z);
					BlockState blockState = cc.getBlock(pt.add(cc.getMinimumPoint()));
					//plugin.logDebug("blockstate: " + blockState.getAsString());

					BlockData block = Bukkit.getServer().createBlockData(blockState.getAsString());


					// ignore if block is AIR or the IgnoreBlock type
					if (!block.getMaterial().equals(Material.AIR) && !block.matches(blockIgnore)) {
						schematiclist.add(new SimpleBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), block));
					}
				}
			}
		}

		for (int i = 0; i < 4; i++)
		{
			// create CannonBlocks entry
            CannonBlocks cannonBlocks = new CannonBlocks();

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

            for (SimpleBlock sblock : schematiclist) {
                int x = sblock.getLocX();
                int y = sblock.getLocY();
                int z = sblock.getLocZ();

                // #############  find the min and max for muzzle blocks so the
                // cannonball is fired from the middle
                if (sblock.compareMaterial(blockMuzzle))
                {
                    // reset for the first entry
                    if (firstEntryMuzzle)
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
                    cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, Material.AIR));
                }
                // #############  find the min and max for rotation blocks
                else if (sblock.compareMaterial(blockRotationCenter))
                {
                    // reset for the first entry
                    if (firstEntryRotation)
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
                // #############  redstoneTorch
                else if (sblock.compareMaterial(blockRedstoneTorch))
                    cannonBlocks.getRedstoneTorches().add(new Vector(x, y, z));
                    // #############  redstoneWire and Repeater
                else if (sblock.compareMaterial(blockRedstoneWireAndRepeater))
                    cannonBlocks.getRedstoneWiresAndRepeater().add(new SimpleBlock(x, y, z, Material.REPEATER));
                    // #############  redstoneTrigger
                else if (sblock.compareMaterialAndFacing(blockRedstoneTrigger))
                {
                    cannonBlocks.getRedstoneTrigger().add(new Vector(x, y, z));
                    // buttons or levers are part of the cannon
                    cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, replaceRedstoneTrigger));
                    // this can be a destructible block
                    if (!isInList(blockProtectedList, sblock.getBlockData()))
                        cannonBlocks.getDestructibleBlocks().add(new Vector(x, y, z));
                }
                // #############  rightClickTrigger
                else if (sblock.compareMaterialAndFacing(blockRightClickTrigger))
                {
                    cannonBlocks.getRightClickTrigger().add(new Vector(x, y, z));
                    //can be also a sign
                    if (sblock.compareMaterialAndFacing(blockChestAndSign))
                        // the id does not matter, but the data is important for signs
                        cannonBlocks.getChestsAndSigns().add(new SimpleBlock(x, y, z, sblock.getBlockData())); //Material.WALL_SIGN
                    // firing blocks are also part of the cannon are
                    // part of the cannon
                    cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, replaceRightClickTrigger));
                    // this can be a destructible block
                    if (!isInList(blockProtectedList, sblock.getBlockData()))
                        cannonBlocks.getDestructibleBlocks().add(new Vector(x, y, z));
                }
                // #############  chests and signs
                else if (sblock.compareMaterial(blockChestAndSign))
                {
                    // the id does not matter, but the data is important for signs
                    cannonBlocks.getChestsAndSigns().add(new SimpleBlock(x, y, z, sblock.getBlockData())); //Material.WALL_SIGN
                }
                // #############  loading Interface is a cannonblock that is non of
                // the previous blocks
                else
                {
                    // all remaining blocks are loading interface or cannonBlocks
                    cannonBlocks.getBarrelBlocks().add(new Vector(x, y, z));
                    cannonBlocks.getAllCannonBlocks().add(new SimpleBlock(x, y, z, sblock.getBlockData()));
                    // this can be a destructible block
                    if (!isInList(blockProtectedList, sblock.getBlockData()))
                        cannonBlocks.getDestructibleBlocks().add(new Vector(x, y, z));
                }

                // #############  firingIndicator
                // can be everywhere on the cannon
                if (sblock.compareMaterialAndFacing(blockFiringIndicator))
                    cannonBlocks.getFiringIndicator().add(new Vector(x, y, z));
            }

			// calculate the muzzle location
			maxMuzzle.add(new Vector(1, 1, 1));
			cannonBlocks.setMuzzle(maxMuzzle.add(minMuzzle).multiply(0.5));

			// calculate the rotation Center
			maxRotation.add(new Vector(1, 1, 1));
			cannonBlocks.setRotationCenter(maxRotation.add(maxRotation).multiply(0.5));

            //set the muzzle location
            Vector compensation = new Vector(cannonBlocks.getMuzzle().getBlockX(), cannonBlocks.getMuzzle().getBlockY(), cannonBlocks.getMuzzle().getBlockZ());

            for (SimpleBlock block : cannonBlocks.getAllCannonBlocks())
                block.subtract_noCopy(compensation);
            for (Vector block : cannonBlocks.getBarrelBlocks())
                block.subtract(compensation);
            for (SimpleBlock block : cannonBlocks.getChestsAndSigns())
                block.subtract_noCopy(compensation);
            for (Vector block : cannonBlocks.getRedstoneTorches())
                block.subtract(compensation);
            for (SimpleBlock block : cannonBlocks.getRedstoneWiresAndRepeater())
                block.subtract_noCopy(compensation);
            for (Vector block : cannonBlocks.getRedstoneTrigger())
                block.subtract(compensation);
            for (Vector block : cannonBlocks.getRightClickTrigger())
                block.subtract(compensation);
            for (Vector block : cannonBlocks.getFiringIndicator())
                block.subtract(compensation);
            for (Vector block : cannonBlocks.getDestructibleBlocks())
                block.subtract(compensation);
            cannonBlocks.getMuzzle().subtract(compensation);
            cannonBlocks.getRotationCenter().subtract(compensation);

			// add blocks to the HashMap
			cannonDesign.getCannonBlockMap().put(cannonDirection, cannonBlocks);

			//rotate blocks for the next iteration
            CannonsUtil.roateBlockFacingClockwise(blockIgnore);
            CannonsUtil.roateBlockFacingClockwise(blockMuzzle);
            CannonsUtil.roateBlockFacingClockwise(blockFiringIndicator);
            CannonsUtil.roateBlockFacingClockwise(blockRotationCenter);
            CannonsUtil.roateBlockFacingClockwise(blockChestAndSign);
            CannonsUtil.roateBlockFacingClockwise(blockRedstoneTorch);
//			if (blockRedstoneWireAndRepeater.getData() != -1)
//                CannonsUtil.roateBlockFacingClockwise(blockRedstoneWireAndRepeater);
            CannonsUtil.roateBlockFacingClockwise(blockRedstoneTrigger);
            CannonsUtil.roateBlockFacingClockwise(blockRightClickTrigger);
            CannonsUtil.roateBlockFacingClockwise(replaceRedstoneTrigger);
            CannonsUtil.roateBlockFacingClockwise(replaceRightClickTrigger);
			for (BlockData aBlockProtectedList : blockProtectedList) {
                CannonsUtil.roateBlockFacingClockwise(aBlockProtectedList);
			}

			//rotate schematic blocks
			for (SimpleBlock simpleBlock : schematiclist){
				simpleBlock.rotate90();
			}

            //rotate cannonDirection
			cannonDirection = CannonsUtil.roatateFace(cannonDirection);


		}
        plugin.logDebug("Time to load designs: " + new DecimalFormat("0.00").format((System.nanoTime() - startTime)/1000000.0) + "ms");

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
		copyFile("classic");
        copyFile("mortar");
        copyFile("ironCannon");
		copyFile("sentry");
	}

    /**
     * Copys the given .yml and .schematic from the .jar to the disk
     * @param fileName - name of the design file
     */
    private void copyFile(String fileName)
    {
        File YmlFile = new File(plugin.getDataFolder(), "designs/" + fileName + ".yml");
        File SchematicFile = new File(plugin.getDataFolder(), "designs/" + fileName + ".schematic");

        SchematicFile.getParentFile().mkdirs();
        if (!YmlFile.exists())
        {
            CannonsUtil.copyFile(plugin.getResource("designs/" + fileName + ".yml"), YmlFile);
        }
        if (!SchematicFile.exists())
        {
            CannonsUtil.copyFile(plugin.getResource("designs/" + fileName + ".schematic"), SchematicFile);
        }
    }
	
	private boolean isInList(List<BlockData> list, BlockData block)
	{
		if (block == null) return true;
		
		for (BlockData listBlock : list)
		{
			if (listBlock != null && listBlock.getMaterial().equals(block.getMaterial()))
				return true;
		}
		return false;
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
	 * @param cannon the cannon
	 * @return design of cannon
	 */
	public CannonDesign getDesign(Cannon cannon)
	{
		return getDesign(cannon.getDesignID());
	}
	
	/**
	 * returns the cannon design by its id
	 * @param designId Name of the design
	 * @return cannon design
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

	/**
	 * is there a cannon design with the give name
	 * @param name name of the design
	 * @return true if there is a cannon design with this name
     */
	public boolean hasDesign(String name){
		for (CannonDesign design : cannonDesignList){
			if (design.getDesignID().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public List<Material> getCannonBlockMaterials() {
		return cannonBlockMaterials;
	}

	public boolean isCannonBlockMaterial(Material material) {
		return material != Material.AIR && cannonBlockMaterials.contains(material);
	}
}
