package at.pavlov.cannons.cannon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.pavlov.cannons.container.SoundHolder;
import at.pavlov.cannons.projectile.ProjectileStorage;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.container.SimpleBlock;
import at.pavlov.cannons.projectile.Projectile;


public class CannonDesign
{
	//general
	private String designID;
	private String designName;
    private String messageName;
    private String description;
    private boolean lastUserBecomesOwner;
	
	//sign
	private boolean isSignRequired; 
	
	//ammunition_consumption
	private String gunpowderName;
	private ItemHolder gunpowderType;
    private boolean needsGunpowder;
    private boolean gunpowderConsumption;
    private boolean projectileConsumption;
	private boolean ammoInfiniteForPlayer;
    private boolean ammoInfiniteForRedstone;
    private boolean autoreloadRedstone;
	private boolean removeChargeAfterFiring;
	private boolean autoloadChargeWhenLoadingProjectile;
	private boolean preloaded;

    //barrelProperties
	private int maxLoadableGunpowder;
	private double multiplierVelocity;
	private double spreadOfCannon;
	
	//timings
	private double blastConfusion;
	private double fuseBurnTime;
	private double fuseBurnTimeRandomness;
    private double barrelCooldownTime;
	private double loadTime;
	
    //angles
	private BlockFace defaultHorizontalFacing;
	private double defaultVerticalAngle;
	private double maxHorizontalAngle;
	private double minHorizontalAngle;
	private double maxVerticalAngle;
	private double minVerticalAngle;
    private double maxHorizontalAngleOnShip;
    private double minHorizontalAngleOnShip;
    private double maxVerticalAngleOnShip;
    private double minVerticalAngleOnShip;
	private double angleStepSize;
	private double angleLargeStepSize;
	private int angleUpdateSpeed;
	private boolean angleUpdateMessage;

    //impactPredictor
    private boolean predictorEnabled;
    private int predictorDelay;         //in ms
    private int predictorUpdate;        //in ms

	//sentry
	private boolean sentry;
	private boolean sentryIndirectFire;
    private int sentryMinRange;
    private int sentryMaxRange;
	private double sentrySpread;
	private int sentryUpdateTime;				//in ms
    private int sentrySwapTime;				    //in ms

	//linkCannons
	private boolean linkCannonsEnabled;
	private int linkCannonsDistance;

    //heatManagment
    private boolean heatManagementEnabled;
    private boolean automaticTemperatureControl;
    private double burnDamage;
    private double burnSlowing;
    private double heatIncreasePerGunpowder;
    private double coolingCoefficient;
    private double coolingAmount;
    private boolean automaticCooling;
    private double warningTemperature;
    private double criticalTemperature;
    private double maximumTemperature;
    private List<ItemHolder> itemCooling = new ArrayList<>();
    private List<ItemHolder> itemCoolingUsed = new ArrayList<>();

    //Overloading stuff
    private boolean overloadingEnabled;
    private boolean overloadingRealMode;
    private double overloadingExponent;
    private double overloadingChanceInc;
    private int overloadingMaxOverloadableGunpowder;
    private double overloadingChanceOfExplosionPerGunpowder;
    private boolean overloadingDependsOfTemperature;

	private double economyBuildingCost;
	private double economyDismantlingRefund;
	private double economyDestructionRefund;

	//realisticBehaviour
	private boolean FiringItemRequired;
    private double sootPerGunpowder;
    private int projectilePushing;
	private boolean hasRecoil;
	private boolean isFrontloader;
	private boolean isRotatable;
    private int massOfCannon;
    private int startingSoot;
    private double explodingLoadedCannons;
    private boolean fireAfterLoading;
    private double dismantlingDelay;
	
	//permissions
	private String permissionBuild;
	private String permissionDismantle;
    private String permissionRename;
	private String permissionLoad;
	private String permissionFire;
	private String permissionAdjust;
	private String permissionAutoaim;
    private String permissionObserver;
	private String permissionTargetTracking;
	private String permissionRedstone;
    private String permissionThermometer;
    private String permissionRamrod;
	private String permissionAutoreload;
	private String permissionSpreadMultiplier;
	
	//accessRestriction
	private boolean accessForOwnerOnly;
	
	//allowedProjectile
	private List<String> allowedProjectiles;

    //sounds
    private SoundHolder soundCreate;
    private SoundHolder soundDestroy;
	private SoundHolder soundDismantle;
    private SoundHolder soundAdjust;
    private SoundHolder soundIgnite;
    private SoundHolder soundFiring;
    private SoundHolder soundGunpowderLoading;
    private SoundHolder soundGunpowderOverloading;
    private SoundHolder soundCool;
    private SoundHolder soundHot;
    private SoundHolder soundRamrodCleaning;
    private SoundHolder soundRamrodCleaningDone;
    private SoundHolder soundRamrodPushing;
    private SoundHolder soundRamrodPushingDone;
    private SoundHolder soundThermometer;
    private SoundHolder soundEnableAimingMode;
    private SoundHolder soundDisableAimingMode;
	private SoundHolder soundSelected;

	
	//constructionblocks:
	private BlockData schematicBlockTypeIgnore;     				//this block this is ignored in the schematic file
    private BlockData schematicBlockTypeMuzzle;					//location of the muzzle
    private BlockData schematicBlockTypeRotationCenter;			//location of the roatation
    private BlockData schematicBlockTypeChestAndSign;				//locations of the chest and sign
    private BlockData schematicBlockTypeRedstoneTorch;				//locations of the redstone torches
    private BlockData schematicBlockTypeRedstoneWireAndRepeater;	//locations of the redstone wires and repeaters
    private BlockData schematicBlockTypeRedstoneTrigger; 			//locations of button or levers
    private BlockData ingameBlockTypeRedstoneTrigger;    			//block which is placed instead of the place holder
    private BlockData schematicBlockTypeRightClickTrigger; 		//locations of the right click trigger
    private BlockData ingameBlockTypeRightClickTrigger;   			//block type of the tigger in game
    private BlockData schematicBlockTypeFiringIndicator;			//location of the firing indicator
    private List<BlockData> schematicBlockTypeProtected;				//list of blocks that are protected from explosions (e.g. buttons)
    
    //cannon design block lists for every direction (NORTH, EAST, SOUTH, WEST)
    private HashMap<BlockFace, CannonBlocks> cannonBlockMap = new HashMap<BlockFace, CannonBlocks>();


    
    /**
     * returns the rotation center of a cannon design
     * @param cannon
     * @return
     */
    public Location getRotationCenter(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getRotationCenter().clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit());
    	}
    	
    	System.out.println("[Cannons] missing rotation center for cannon design " + cannon.getCannonName());
    	return cannon.getOffset().toLocation(cannon.getWorldBukkit());
    } 
    
    
    /**
     * returns the muzzle location
     * @param cannon
     * @return
     */
    public Location getMuzzle(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getMuzzle().clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit());
    	}

    	System.out.println("[Cannons] missing muzzle location for cannon design " + cannon.getCannonName());
    	return cannon.getOffset().toLocation(cannon.getWorldBukkit());
    }
    
    /**
     * returns one trigger location
     * @param cannon the used cannon
     * @return the firing trigger of the cannon - can be null if the cannon has no trigger
     */
    public Location getFiringTrigger(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	if (cannonBlocks != null && cannonBlocks.getFiringTrigger() != null)
    	{
            return cannonBlocks.getFiringTrigger().clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit());
    	}
    	return null;
    }
    
    /**
     * returns a list of all cannonBlocks
     * @param cannonDirection - the direction the cannon is facing
     * @return List of cannon blocks
     */
    public List<SimpleBlock> getAllCannonBlocks(BlockFace cannonDirection)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getAllCannonBlocks();
    	}
    	
    	return new ArrayList<SimpleBlock>();
    }


    /**
     * returns a list of all cannonBlocks
     * @param cannon
     * @return
     */
    public List<Location> getAllCannonBlocks(Cannon cannon)
    {
        CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
        List<Location> locList = new ArrayList<Location>();
        if (cannonBlocks != null)
        {
            for (SimpleBlock block : cannonBlocks.getAllCannonBlocks())
            {
                Vector vect = block.toVector();
                locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
            }
        }
        return locList;
    }

    /**
     * returns a list of all destructible blocks
     * @param cannon
     * @return
     */
    public List<Location> getDestructibleBlocks(Cannon cannon)
    {
     	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (Vector vect : cannonBlocks.getDestructibleBlocks())
    		{
    			locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    
    /**
     * returns a list of all firingIndicator blocks
     * @param cannon
     * @return
     */
    public List<Location> getFiringIndicator(Cannon cannon)
    {
     	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (Vector vect : cannonBlocks.getFiringIndicator())
    		{
    			locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns a list of all loading interface blocks
     * @param cannon
     * @return
     */
    public List<Location> getLoadingInterface(Cannon cannon)
    {
        CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
        List<Location> locList = new ArrayList<Location>();
        if (cannonBlocks != null)
        {
            for (Vector vect : cannonBlocks.getBarrelBlocks())
            {
                locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
            }
        }
        return locList;
    }

    /**
     * returns a list of all barrel blocks
     * @param cannon
     * @return
     */
    public List<Location> getBarrelBlocks(Cannon cannon)
    {
        CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
        List<Location> locList = new ArrayList<Location>();
        if (cannonBlocks != null)
        {
            for (Vector vect : cannonBlocks.getBarrelBlocks())
            {
                locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
            }
        }
        return locList;
    }
    
    /**
     * returns a list of all right click trigger blocks
     * @param cannon
     * @return
     */
    public List<Location> getRightClickTrigger(Cannon cannon)
    {
     	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (Vector vect : cannonBlocks.getRightClickTrigger())
    		{
    			locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns a list of all redstone trigger blocks
     * @param cannon
     * @return
     */
    public List<Location> getRedstoneTrigger(Cannon cannon)
    {
     	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (Vector vect : cannonBlocks.getRedstoneTrigger())
    		{
    			locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    
    /**
     * returns a list of all chest/sign blocks
     * @param cannon
     * @return
     */
    public List<Location> getChestsAndSigns(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (SimpleBlock block : cannonBlocks.getChestsAndSigns())
    		{
    			locList.add(block.toLocation(cannon.getWorldBukkit(), cannon.getOffset()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns a list of all redstone torch blocks
     * @param cannon
     * @return
     */
    public List<Location> getRedstoneTorches(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (Vector vect : cannonBlocks.getRedstoneTorches())
    		{
    			locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns a list of all redstone wire/repeater blocks
     * @param cannon
     * @return
     */
    public List<Location> getRedstoneWireAndRepeater(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (SimpleBlock block : cannonBlocks.getRedstoneWiresAndRepeater())
    		{
    			locList.add(block.toLocation(cannon.getWorldBukkit(),cannon.getOffset()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns true if the projectile has the same Id of a allowed projectile
     * @param projectile projectile to load
     * @return true if the projectile can be loaded in this type of cannon
     */
    public boolean canLoad(Projectile projectile)
    {
    	for (String p : allowedProjectiles)
    	{
    		if (projectile.getProjectileId().equals(p))
    			return true;
    	}
    	
    	return false;
    }

	public String getDesignID()
	{
		return designID;
	}
	public void setDesignID(String designID)
	{
		this.designID = designID;
	}
	public String getDesignName()
	{
		return designName;
	}
	public void setDesignName(String designName)
	{
		this.designName = designName;
	}
	public boolean isSignRequired()
	{
		return isSignRequired;
	}
	public void setSignRequired(boolean isSignRequired)
	{
		this.isSignRequired = isSignRequired;
	}
	public ItemHolder getGunpowderType()
	{
		return gunpowderType;
	}
	public void setGunpowderType(ItemHolder gunpowderType)
	{
		this.gunpowderType = gunpowderType;
	}
	public boolean isAmmoInfiniteForPlayer()
	{
		return ammoInfiniteForPlayer;
	}
	public void setAmmoInfiniteForPlayer(boolean ammoInfiniteForPlayer)
	{
		this.ammoInfiniteForPlayer = ammoInfiniteForPlayer;
	}
	public boolean isAmmoInfiniteForRedstone()
	{
		return ammoInfiniteForRedstone;
	}
	public void setAmmoInfiniteForRedstone(boolean ammoInfiniteForRedstone)
	{
		this.ammoInfiniteForRedstone = ammoInfiniteForRedstone;
	}
	public boolean isAutoreloadRedstone()
	{
		return autoreloadRedstone;
	}
	public void setAutoreloadRedstone(boolean autoreloadRedstone)
	{
		this.autoreloadRedstone = autoreloadRedstone;
	}
	/**
	 * Normal means without overloading stuff
	 * @return maxLoadableGunpowder
	 */
	public int getMaxLoadableGunpowderNormal()
	{
		return maxLoadableGunpowder;
	}
	/**
	 * Absolute means maximum loadable gunpowder
	 * @return if overloading stuff is enabled for this cannon, returns maxLoadableGunpowder+overloading_maxOverloadableGunpowder, else returns maxLoadableGunpowder
	 */
	public int getMaxLoadableGunpowderOverloaded()
	{
		if(overloadingEnabled)
            return maxLoadableGunpowder+overloadingMaxOverloadableGunpowder;
		else
            return getMaxLoadableGunpowderNormal();
	}
	public void setMaxLoadableGunpowder(int maxLoadableGunpowder)
	{
		this.maxLoadableGunpowder = maxLoadableGunpowder;
	}
	public double getMultiplierVelocity()
	{
		return multiplierVelocity;
	}
	public void setMultiplierVelocity(double multiplierVelocity)
	{
		this.multiplierVelocity = multiplierVelocity;
	}
	public double getSpreadOfCannon()
	{
		return spreadOfCannon;
	}
	public void setSpreadOfCannon(double spreadOfCannon)
	{
		this.spreadOfCannon = spreadOfCannon;
	}
	public double getBlastConfusion()
	{
		return blastConfusion;
	}
	public void setBlastConfusion(double blastConfusion)
	{
		this.blastConfusion = blastConfusion;
	}
	public double getFuseBurnTime()
	{
		return fuseBurnTime;
	}
	public void setFuseBurnTime(double fuseBurnTime)
	{
		this.fuseBurnTime = fuseBurnTime;
	}
	public double getBarrelCooldownTime()
	{
		return barrelCooldownTime;
	}
	public void setBarrelCooldownTime(double barrelCooldownTime)
	{
		this.barrelCooldownTime = barrelCooldownTime;
	}
	public BlockFace getDefaultHorizontalFacing()
	{
		return defaultHorizontalFacing;
	}
	public void setDefaultHorizontalFacing(BlockFace defaultHorizontalFacing)
	{
		this.defaultHorizontalFacing = defaultHorizontalFacing;
	}
	public double getDefaultVerticalAngle()
	{
		return defaultVerticalAngle;
	}
	public void setDefaultVerticalAngle(double defaultVerticalAngle)
	{
		this.defaultVerticalAngle = defaultVerticalAngle;
	}
	public double getMaxHorizontalAngleNormal()
	{
		return maxHorizontalAngle;
	}
	public void setMaxHorizontalAngleNormal(double maxHorizontalAngle)
	{
		this.maxHorizontalAngle = maxHorizontalAngle;
	}
	public double getMinHorizontalAngleNormal()
	{
		return minHorizontalAngle;
	}
	public void setMinHorizontalAngleNormal(double minHorizontalAngle)
	{
		this.minHorizontalAngle = minHorizontalAngle;
	}
	public double getMaxVerticalAngleNormal()
	{
		return maxVerticalAngle;
	}
	public void setMaxVerticalAngleNormal(double maxVerticalAngle)
	{
		this.maxVerticalAngle = maxVerticalAngle;
	}
	public double getMinVerticalAngleNormal()
	{
		return minVerticalAngle;
	}
	public void setMinVerticalAngleNormal(double minVerticalAngle)
	{
		this.minVerticalAngle = minVerticalAngle;
	}
	public double getAngleStepSize()
	{
		return angleStepSize;
	}
	public void setAngleStepSize(double angleStepSize)
	{
		this.angleStepSize = angleStepSize;
	}
	public int getAngleUpdateSpeed()
	{
		return angleUpdateSpeed;
	}
	public void setAngleUpdateSpeed(int angleUpdateSpeed)
	{
		this.angleUpdateSpeed = angleUpdateSpeed;
	}
	public boolean isHasRecoil()
	{
		return hasRecoil;
	}
	public void setHasRecoil(boolean hasRecoil)
	{
		this.hasRecoil = hasRecoil;
	}
	public boolean isFrontloader()
	{
		return isFrontloader;
	}
	public void setFrontloader(boolean isFrontloader)
	{
		this.isFrontloader = isFrontloader;
	}
	public boolean isRotatable()
	{
		return isRotatable;
	}
	public void setRotatable(boolean isRotatable)
	{
		this.isRotatable = isRotatable;
	}
	public String getPermissionBuild()
	{
		return permissionBuild;
	}
	public void setPermissionBuild(String permissionBuild)
	{
		this.permissionBuild = permissionBuild;
	}
	public String getPermissionLoad()
	{
		return permissionLoad;
	}
	public void setPermissionLoad(String permissionLoad)
	{
		this.permissionLoad = permissionLoad;
	}
	public String getPermissionFire()
	{
		return permissionFire;
	}
	public void setPermissionFire(String permissionFire)
	{
		this.permissionFire = permissionFire;
	}
	public String getPermissionAdjust()
	{
		return permissionAdjust;
	}
	public void setPermissionAdjust(String permissionAdjust)
	{
		this.permissionAdjust = permissionAdjust;
	}
	public String getPermissionAutoaim()
	{
		return permissionAutoaim;
	}
	public void setPermissionAutoaim(String permissionAutoaim)
	{
		this.permissionAutoaim = permissionAutoaim;
	}
	public String getPermissionTargetTracking()
	{
		return permissionTargetTracking;
	}
	public void setPermissionTargetTracking(String permissionTargetTracking)
	{
		this.permissionTargetTracking = permissionTargetTracking;
	}
	public String getPermissionRedstone()
	{
		return permissionRedstone;
	}
	public void setPermissionRedstone(String permissionRedstone)
	{
		this.permissionRedstone = permissionRedstone;
	}
	public String getPermissionAutoreload()
	{
		return permissionAutoreload;
	}
	public void setPermissionAutoreload(String permissionAutoreload)
	{
		this.permissionAutoreload = permissionAutoreload;
	}
	public boolean isAccessForOwnerOnly()
	{
		return accessForOwnerOnly;
	}
	public void setAccessForOwnerOnly(boolean accessForOwnerOnly)
	{
		this.accessForOwnerOnly = accessForOwnerOnly;
	}
	public List<String> getAllowedProjectiles()
	{
		return allowedProjectiles;
	}
	public void setAllowedProjectiles(List<String> allowedProjectiles)
	{
		this.allowedProjectiles = allowedProjectiles;
	}
	public BlockData getSchematicBlockTypeIgnore()
	{
		return schematicBlockTypeIgnore;
	}
	public void setSchematicBlockTypeIgnore(BlockData schematicBlockTypeIgnore)
	{
		this.schematicBlockTypeIgnore = schematicBlockTypeIgnore;
	}
	public BlockData getSchematicBlockTypeMuzzle()
	{
		return schematicBlockTypeMuzzle;
	}
	public void setSchematicBlockTypeMuzzle(BlockData schematicBlockTypeMuzzle)
	{
		this.schematicBlockTypeMuzzle = schematicBlockTypeMuzzle;
	}
	public BlockData getSchematicBlockTypeRotationCenter()
	{
		return schematicBlockTypeRotationCenter;
	}
	public void setSchematicBlockTypeRotationCenter(BlockData schematicBlockTypeRotationCenter)
	{
		this.schematicBlockTypeRotationCenter = schematicBlockTypeRotationCenter;
	}
	public BlockData getSchematicBlockTypeRedstoneTorch()
	{
		return schematicBlockTypeRedstoneTorch;
	}
	public void setSchematicBlockTypeRedstoneTorch(BlockData schematicBlockTypeRedstoneTorch)
	{
		this.schematicBlockTypeRedstoneTorch = schematicBlockTypeRedstoneTorch;
	}
	public BlockData getSchematicBlockTypeRedstoneTrigger()
	{
		return schematicBlockTypeRedstoneTrigger;
	}
	public void setSchematicBlockTypeRedstoneTrigger(BlockData schematicBlockTypeRedstoneTrigger)
	{
		this.schematicBlockTypeRedstoneTrigger = schematicBlockTypeRedstoneTrigger;
	}
	public BlockData getIngameBlockTypeRedstoneTrigger()
	{
		return ingameBlockTypeRedstoneTrigger;
	}
	public void setIngameBlockTypeRedstoneTrigger(BlockData ingameBlockTypeRedstoneTrigger)
	{
		this.ingameBlockTypeRedstoneTrigger = ingameBlockTypeRedstoneTrigger;
	}
	public BlockData getSchematicBlockTypeRightClickTrigger()
	{
		return schematicBlockTypeRightClickTrigger;
	}
	public void setSchematicBlockTypeRightClickTrigger(BlockData schematicBlockTypeRightClickTrigger)
	{
		this.schematicBlockTypeRightClickTrigger = schematicBlockTypeRightClickTrigger;
	}
	public BlockData getIngameBlockTypeRightClickTrigger()
	{
		return ingameBlockTypeRightClickTrigger;
	}
	public void setIngameBlockTypeRightClickTrigger(BlockData ingameBlockTypeRightClickTrigger)
	{
		this.ingameBlockTypeRightClickTrigger = ingameBlockTypeRightClickTrigger;
	}
	public HashMap<BlockFace, CannonBlocks> getCannonBlockMap()
	{
		return cannonBlockMap;
	}
	public void setCannonBlockMap(HashMap<BlockFace, CannonBlocks> cannonBlockMap)
	{
		this.cannonBlockMap = cannonBlockMap;
	}
	
	@Override
	public String toString()
	{
		return "designID:" + designID + " name:" + designName + " blocks:" + getAllCannonBlocks(BlockFace.NORTH).size();
	}


	public BlockData getSchematicBlockTypeChestAndSign()
	{
		return schematicBlockTypeChestAndSign;
	}


	public void setSchematicBlockTypeChestAndSign(BlockData schematicBlockTypeChestAndSign)
	{
		this.schematicBlockTypeChestAndSign = schematicBlockTypeChestAndSign;
	}


	public BlockData getSchematicBlockTypeRedstoneWireAndRepeater()
	{
		return schematicBlockTypeRedstoneWireAndRepeater;
	}


	public void setSchematicBlockTypeRedstoneWireAndRepeater(BlockData schematicBlockTypeRedstoneWireAndRepeater)
	{
		this.schematicBlockTypeRedstoneWireAndRepeater = schematicBlockTypeRedstoneWireAndRepeater;
	}


	public BlockData getSchematicBlockTypeFiringIndicator()
	{
		return schematicBlockTypeFiringIndicator;
	}


	public void setSchematicBlockTypeFiringIndicator(BlockData schematicBlockTypeFiringIndicator)
	{
		this.schematicBlockTypeFiringIndicator = schematicBlockTypeFiringIndicator;
	}


	public String getGunpowderName()
	{
		return gunpowderName;
	}


	public void setGunpowderName(String gunpowderName)
	{
		this.gunpowderName = gunpowderName;
	}


	public boolean isFiringItemRequired()
	{
		return FiringItemRequired;
	}


	public void setFiringItemRequired(boolean firingItemRequired)
	{
		FiringItemRequired = firingItemRequired;
	}


	public List<BlockData> getSchematicBlockTypeProtected()
	{
		return schematicBlockTypeProtected;
	}


	public void setSchematicBlockTypeProtected(List<BlockData> schematicBlockTypeProtected)
	{
		this.schematicBlockTypeProtected = schematicBlockTypeProtected;
	}


	public String getPermissionSpreadMultiplier()
	{
		return permissionSpreadMultiplier;
	}


	public void setPermissionSpreadMultiplier(String permissionSpreadMultiplier)
	{
		this.permissionSpreadMultiplier = permissionSpreadMultiplier;
	}

    public boolean isGunpowderConsumption() {
        return gunpowderConsumption;
    }

    public void setGunpowderConsumption(boolean gunpowderConsumption) {
        this.gunpowderConsumption = gunpowderConsumption;
    }

    public boolean isProjectileConsumption() {
        return projectileConsumption;
    }

    public void setProjectileConsumption(boolean projectileConsumption) {
        this.projectileConsumption = projectileConsumption;
    }

    public int getMassOfCannon() {
        return massOfCannon;
    }

    public void setMassOfCannon(int massOfCannon) {
        this.massOfCannon = massOfCannon;
    }

    public boolean isHeatManagementEnabled() {
        return heatManagementEnabled;
    }

    public void setHeatManagementEnabled(boolean heatManagementEnabled) {
        this.heatManagementEnabled = heatManagementEnabled;
    }

    public double getHeatIncreasePerGunpowder() {
        return heatIncreasePerGunpowder;
    }

    public void setHeatIncreasePerGunpowder(double heatIncreasePerGunpowder) {
        this.heatIncreasePerGunpowder = heatIncreasePerGunpowder;
    }

    public double getCoolingCoefficient() {
        return coolingCoefficient;
    }

    public void setCoolingCoefficient(double coolingCoefficient) {
        this.coolingCoefficient = coolingCoefficient;
    }

    public double getWarningTemperature() {
        return warningTemperature;
    }

    public void setWarningTemperature(double warningTemperature) {
        this.warningTemperature = warningTemperature;
    }

    public double getMaximumTemperature() {
        return maximumTemperature;
    }

    public void setMaximumTemperature(double maximumTemperature) {
        this.maximumTemperature = maximumTemperature;
    }

    public double getBurnSlowing() {
        return burnSlowing;
    }

    public void setBurnSlowing(double burnSlowing) {
        this.burnSlowing = burnSlowing;
    }

    public double getBurnDamage() {
        return burnDamage;
    }

    public void setBurnDamage(double burnDamage) {
        this.burnDamage = burnDamage;
    }

    public double getCriticalTemperature() {
        return criticalTemperature;
    }

    public void setCriticalTemperature(double criticalTemperature) {
        this.criticalTemperature = criticalTemperature;
    }

    public double getCoolingAmount() {
        return coolingAmount;
    }

    public void setCoolingAmount(double coolingAmount) {
        this.coolingAmount = coolingAmount;
    }

    public boolean isAutomaticCooling() {
        return automaticCooling;
    }

    public void setAutomaticCooling(boolean automaticCooling) {
        this.automaticCooling = automaticCooling;
    }

    public List<ItemHolder> getItemCooling() {
        return itemCooling;
    }

    public void setItemCooling(List<ItemHolder> itemCooling) {
        this.itemCooling = itemCooling;
    }

    public List<ItemHolder> getItemCoolingUsed() {
        return itemCoolingUsed;
    }

    public void setItemCoolingUsed(List<ItemHolder> itemCoolingUsed) {
        this.itemCoolingUsed = itemCoolingUsed;
    }

    /**
     * is this Item a cooling tool to cool down a cannon
     * @param item - item to check
     * @return - true if this item is in the list of cooling items
     */
    public boolean isCoolingTool(ItemStack item)
    {
    	//todo rework tool properties
        for (ItemHolder mat : itemCooling)
        {
            if (mat.equalsFuzzy(item))
                return true;
        }
        return false;
    }

    /**
     * returns the used used item. E.g. a water bucket will be an empty bucket.
     * @param item - the item used for the event
     * @return the new item which replaces the old one
     */
    public ItemStack getCoolingToolUsed(ItemStack item)
    {
        for (int i=0; i < itemCooling.size(); i++)
        {
			//todo rework tool properties
            if (itemCooling.get(i).equalsFuzzy(item))
            {
                return itemCoolingUsed.get(i).toItemStack(item.getAmount());
            }
        }
        return null;
    }

    public boolean isAutomaticTemperatureControl() {
        return automaticTemperatureControl;
    }

    public void setAutomaticTemperatureControl(boolean automaticTemperatureControl) {
        this.automaticTemperatureControl = automaticTemperatureControl;
    }

    public String getPermissionThermometer() {
        return permissionThermometer;
    }

    public void setPermissionThermometer(String permissionThermometer) {
        this.permissionThermometer = permissionThermometer;
    }

    public String getPermissionRamrod() {
        return permissionRamrod;
    }

    public void setPermissionRamrod(String permissionRamrod) {
        this.permissionRamrod = permissionRamrod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public double getSootPerGunpowder() {
        return sootPerGunpowder;
    }

    public void setSootPerGunpowder(double sootPerGunpowder) {
        this.sootPerGunpowder = sootPerGunpowder;
    }

    public int getProjectilePushing() {
        return projectilePushing;
    }

    public void setProjectilePushing(int projectilePushing) {
        this.projectilePushing = projectilePushing;
    }

    public double getMaxHorizontalAngleOnShip() {
        return maxHorizontalAngleOnShip;
    }

    public void setMaxHorizontalAngleOnShip(double maxHorizontalAngleOnShip) {
        this.maxHorizontalAngleOnShip = maxHorizontalAngleOnShip;
    }

    public double getMinHorizontalAngleOnShip() {
        return minHorizontalAngleOnShip;
    }

    public void setMinHorizontalAngleOnShip(double minHorizontalAngleOnShip) {
        this.minHorizontalAngleOnShip = minHorizontalAngleOnShip;
    }

    public double getMaxVerticalAngleOnShip() {
        return maxVerticalAngleOnShip;
    }

    public void setMaxVerticalAngleOnShip(double maxVerticalAngleOnShip) {
        this.maxVerticalAngleOnShip = maxVerticalAngleOnShip;
    }

    public double getMinVerticalAngleOnShip() {
        return minVerticalAngleOnShip;
    }

    public void setMinVerticalAngleOnShip(double minVerticalAngleOnShip) {
        this.minVerticalAngleOnShip = minVerticalAngleOnShip;
    }

    
    public void setOverloadingExponent(double overloadingExponent)
    {
        this.overloadingExponent = overloadingExponent;
    }
    
    public double getOverloadingExponent()
    {
        return overloadingExponent;
    }
    
    public void setOverloadingChangeInc(double overloadingChanceInc)
    {
        this.overloadingChanceInc = overloadingChanceInc;
    }
    
    public double getOverloadingChangeInc()
    {
        return overloadingChanceInc;
    }
    
    public void setOverloadingMaxOverloadableGunpowder(int overloadingMaxOverloadableGunpowder)
    {
        this.overloadingMaxOverloadableGunpowder = overloadingMaxOverloadableGunpowder;
    }

    public int getOverloadingMaxOverloadableGunpowder()
    {
        return overloadingMaxOverloadableGunpowder;
    }

    public void setOverloadingChanceOfExplosionPerGunpowder(double overloadingChanceOfExplosionPerGunpowder)
    {
        this.overloadingChanceOfExplosionPerGunpowder = overloadingChanceOfExplosionPerGunpowder;
    }
    
    public double getOverloadingChanceOfExplosionPerGunpowder()
    {
        return overloadingChanceOfExplosionPerGunpowder;
    }


	public boolean isOverloadingDependsOfTemperature()
	{
		return overloadingDependsOfTemperature;
	}


	public void setOverloadingDependsOfTemperature(boolean overloadingDependsOfTemperature)
	{
		this.overloadingDependsOfTemperature = overloadingDependsOfTemperature;
	}


	public int getStartingSoot()
	{
		return startingSoot;
	}


	public void setStartingSoot(int startingSoot)
	{
		this.startingSoot = startingSoot;
	}


	public boolean isOverloadingRealMode()
	{
		return overloadingRealMode;
	}


	public void setOverloadingRealMode(boolean overloadingRealMode)
	{
		this.overloadingRealMode = overloadingRealMode;
	}


	public boolean isOverloadingEnabled()
	{
		return overloadingEnabled;
	}


	public void setOverloadingEnabled(boolean overloadingEnabled)
	{
		this.overloadingEnabled = overloadingEnabled;
	}


	public boolean isLastUserBecomesOwner()
	{
		return lastUserBecomesOwner;
	}


	public void setLastUserBecomesOwner(boolean lastUserBecomesOwner)
	{
		this.lastUserBecomesOwner = lastUserBecomesOwner;
	}

    public String getPermissionRename() {
        return permissionRename;
    }

    public void setPermissionRename(String permissionRename) {
        this.permissionRename = permissionRename;
    }

    public String getPermissionObserver() {
        return permissionObserver;
    }

    public void setPermissionObserver(String permissionObserver) {
        this.permissionObserver = permissionObserver;
    }

    public double getExplodingLoadedCannons() {
        return explodingLoadedCannons;
    }

    public void setExplodingLoadedCannons(double explodingLoadedCannons) {
        this.explodingLoadedCannons = explodingLoadedCannons;
    }

    public boolean isGunpowderNeeded() {
        return needsGunpowder;
    }

    public void setNeedsGunpowder(boolean needsGunpowder) {
        this.needsGunpowder = needsGunpowder;
    }

    public boolean isFireAfterLoading() {
        return fireAfterLoading;
    }

    public void setFireAfterLoading(boolean fireAfterLoading) {
        this.fireAfterLoading = fireAfterLoading;
    }

    public boolean isPredictorEnabled() {
        return predictorEnabled;
    }

    public void setPredictorEnabled(boolean predictorEnabled) {
        this.predictorEnabled = predictorEnabled;
    }

    public int getPredictorDelay() {
        return predictorDelay;
    }

    public void setPredictorDelay(int predictorDelay) {
        this.predictorDelay = predictorDelay;
    }

    public int getPredictorUpdate() {
        return predictorUpdate;
    }

    public void setPredictorUpdate(int predictorUpdate) {
        this.predictorUpdate = predictorUpdate;
    }

    public SoundHolder getSoundAdjust() {
        return soundAdjust;
    }

    public void setSoundAdjust(SoundHolder soundAdjust) {
        this.soundAdjust = soundAdjust;
    }

    public SoundHolder getSoundCreate() {
        return soundCreate;
    }

    public void setSoundCreate(SoundHolder soundCreate) {
        this.soundCreate = soundCreate;
    }

    public SoundHolder getSoundIgnite() {
        return soundIgnite;
    }

    public void setSoundIgnite(SoundHolder soundIgnite) {
        this.soundIgnite = soundIgnite;
    }

    public SoundHolder getSoundFiring() {
        return soundFiring;
    }

    public void setSoundFiring(SoundHolder soundFiring) {
        this.soundFiring = soundFiring;
    }

    public SoundHolder getSoundGunpowderLoading() {
        return soundGunpowderLoading;
    }

    public void setSoundGunpowderLoading(SoundHolder soundGunpowderLoading) {
        this.soundGunpowderLoading = soundGunpowderLoading;
    }

    public SoundHolder getSoundGunpowderOverloading() {
        return soundGunpowderOverloading;
    }

    public void setSoundGunpowderOverloading(SoundHolder soundGunpowderOverloading) {
        this.soundGunpowderOverloading = soundGunpowderOverloading;
    }

    public SoundHolder getSoundCool() {
        return soundCool;
    }

    public void setSoundCool(SoundHolder soundCool) {
        this.soundCool = soundCool;
    }

    public SoundHolder getSoundHot() {
        return soundHot;
    }

    public void setSoundHot(SoundHolder soundHot) {
        this.soundHot = soundHot;
    }

    public SoundHolder getSoundRamrodCleaning() {
        return soundRamrodCleaning;
    }

    public void setSoundRamrodCleaning(SoundHolder soundRamrodCleaning) {
        this.soundRamrodCleaning = soundRamrodCleaning;
    }

    public SoundHolder getSoundRamrodCleaningDone() {
        return soundRamrodCleaningDone;
    }

    public void setSoundRamrodCleaningDone(SoundHolder soundRamrodCleaningDone) {
        this.soundRamrodCleaningDone = soundRamrodCleaningDone;
    }

    public SoundHolder getSoundRamrodPushing() {
        return soundRamrodPushing;
    }

    public void setSoundRamrodPushing(SoundHolder soundRamrodPushing) {
        this.soundRamrodPushing = soundRamrodPushing;
    }

    public SoundHolder getSoundRamrodPushingDone() {
        return soundRamrodPushingDone;
    }

    public void setSoundRamrodPushingDone(SoundHolder soundRamrodPushingDone) {
        this.soundRamrodPushingDone = soundRamrodPushingDone;
    }

    public SoundHolder getSoundThermometer() {
        return soundThermometer;
    }

    public void setSoundThermometer(SoundHolder soundThermometer) {
        this.soundThermometer = soundThermometer;
    }

    public SoundHolder getSoundEnableAimingMode() {
        return soundEnableAimingMode;
    }

    public void setSoundEnableAimingMode(SoundHolder soundEnableAimingMode) {
        this.soundEnableAimingMode = soundEnableAimingMode;
    }

    public SoundHolder getSoundDisableAimingMode() {
        return soundDisableAimingMode;
    }

    public void setSoundDisableAimingMode(SoundHolder soundDisableAimingMode) {
        this.soundDisableAimingMode = soundDisableAimingMode;
    }

    public SoundHolder getSoundDestroy() {
        return soundDestroy;
    }

    public void setSoundDestroy(SoundHolder soundDestroy) {
        this.soundDestroy = soundDestroy;
    }

	public boolean isRemoveChargeAfterFiring() {
		return removeChargeAfterFiring;
	}

	public void setRemoveChargeAfterFiring(boolean removeChargeAfterFiring) {
		this.removeChargeAfterFiring = removeChargeAfterFiring;
	}

	public boolean isAngleUpdateMessage() {
		return angleUpdateMessage;
	}

	public void setAngleUpdateMessage(boolean angleUpdateMessage) {
		this.angleUpdateMessage = angleUpdateMessage;
	}

	public double getEconomyBuildingCost() {
		return economyBuildingCost;
	}

	public void setEconomyBuildingCost(double economyBuildingCost) {
		this.economyBuildingCost = economyBuildingCost;
	}

	public double getEconomyDismantlingRefund() {
		return economyDismantlingRefund;
	}

	public void setEconomyDismantlingRefund(double economyDismantlingRefund) {
		this.economyDismantlingRefund = economyDismantlingRefund;
	}

	public double getEconomyDestructionRefund() {
		return economyDestructionRefund;
	}

	public void setEconomyDestructionRefund(double economyDestructionRefund) {
		this.economyDestructionRefund = economyDestructionRefund;
	}

	public SoundHolder getSoundDismantle() {
		return soundDismantle;
	}

	public void setSoundDismantle(SoundHolder soundDismantle) {
		this.soundDismantle = soundDismantle;
	}

	public boolean isSentry() {
		return sentry;
	}

	public void setSentry(boolean sentry) {
		this.sentry = sentry;
	}

	public int getSentryUpdateTime() {
		return sentryUpdateTime;
	}

	public void setSentryUpdateTime(int sentryUpdateTime) {
		this.sentryUpdateTime = sentryUpdateTime;
	}

    public int getSentryMaxRange() {
        return sentryMaxRange;
    }

    public void setSentryMaxRange(int sentryMaxRange) {
        this.sentryMaxRange = sentryMaxRange;
    }

    public int getSentrySwapTime() {
        return sentrySwapTime;
    }

    public void setSentrySwapTime(int sentrySwapTime) {
        this.sentrySwapTime = sentrySwapTime;
    }

    public int getSentryMinRange() {
        return sentryMinRange;
    }

    public void setSentryMinRange(int sentryMinRange) {
        this.sentryMinRange = sentryMinRange;
    }

	public boolean isSentryIndirectFire() {
		return sentryIndirectFire;
	}

	public void setSentryIndirectFire(boolean sentryIndirectFire) {
		this.sentryIndirectFire = sentryIndirectFire;
	}

	public String getPermissionDismantle() {
		return permissionDismantle;
	}

	public void setPermissionDismantle(String permissionDismantle) {
		this.permissionDismantle = permissionDismantle;
	}

    public double getDismantlingDelay() {
        return dismantlingDelay;
    }

    public void setDismantlingDelay(double dismantlingDelay) {
        this.dismantlingDelay = dismantlingDelay;
    }

	public double getAngleLargeStepSize() {
		return angleLargeStepSize;
	}

	public void setAngleLargeStepSize(double angleLargeStepSize) {
		this.angleLargeStepSize = angleLargeStepSize;
	}

	public double getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(double loadTime) {
		this.loadTime = loadTime;
	}

	public double getSentrySpread() {
		return sentrySpread;
	}

	public void setSentrySpread(double sentrySpread) {
		this.sentrySpread = sentrySpread;
	}

	public SoundHolder getSoundSelected() {
		return soundSelected;
	}

	public void setSoundSelected(SoundHolder soundSelected) {
		this.soundSelected = soundSelected;
	}

	public boolean isPreloaded() {
		return preloaded;
	}

	public void setPreloaded(boolean preloaded) {
		this.preloaded = preloaded;
	}

	public boolean isAutoloadChargeWhenLoadingProjectile() {
		return autoloadChargeWhenLoadingProjectile;
	}

	public void setAutoloadChargeWhenLoadingProjectile(boolean autoloadChargeWhenLoadingProjectile) {
		this.autoloadChargeWhenLoadingProjectile = autoloadChargeWhenLoadingProjectile;
	}

	public double getFuseBurnTimeRandomness() {
		return fuseBurnTimeRandomness;
	}

	public void setFuseBurnTimeRandomness(double fuseBurnTimeRandomness) {
		this.fuseBurnTimeRandomness = fuseBurnTimeRandomness;
	}

	public boolean isLinkCannonsEnabled() {
		return linkCannonsEnabled;
	}

	public void setLinkCannonsEnabled(boolean linkCannonsEnabled) {
		this.linkCannonsEnabled = linkCannonsEnabled;
	}

	public int getLinkCannonsDistance() {
		return linkCannonsDistance;
	}

	public void setLinkCannonsDistance(int linkCannonsDistance) {
		this.linkCannonsDistance = linkCannonsDistance;
	}
}
