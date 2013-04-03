package at.pavlov.Cannons.cannon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.container.MaterialHolder;
import at.pavlov.Cannons.container.SimpleBlock;


public class CannonDesign
{
	//general
	private int uniqueID;
	private String designName;
	
	//sign
	private boolean isSignRequired; 
	
	//ammunition_consumption
	private MaterialHolder gunpowderType;
	private boolean ammoInfiniteForPlayer;
    private boolean ammoInfiniteForRedstone;
    private boolean autoreloadRedstone;
    
    //barrelProperties
	private int maxLoadableGunpowder;
	private double multiplierVelocity;
	private double spreadOfCannon;
	
	//timings
	private double backblastConfusion;
	private double fuseBurnTime;
    private double barrelCooldownTime;
	
    //angles
	private BlockFace defaultHorizonatalFacing;
	private double defaultVerticalAngle;
	private double maxHorizontalAngle;
	private double minHorizontalAngle;
	private double maxVerticalAngle;
	private double minVerticalAngle;
	private double angleStepSize;
	private double angleUpdateSpeed;

	//realisticBehaviour
	private boolean hasRecoil;
	private boolean isFrontloader;
	private boolean isRotabable;
	
	//permissions
	private String permissionBuild;
	private String permissionLoad;
	private String permissionFire;
	private String permissionAdjust;
	private String permissionAutoaim;
	private String permissionTargetTracking;
	private String permissionRedstone;
	private String permissionAutoreload;
	
	//accessRestriction
	private boolean accessForOwnerOnly;
	
	//allowedProjectile
	private List<String> allowedProjectiles;
	
	//constructionblocks:
	private MaterialHolder schematicBlockTypeIgnore;     				//this block this is ignored in the schematic file
    private MaterialHolder schematicBlockTypeMuzzle;					//location of the muzzle
    private MaterialHolder schematicBlockTypeRotationCenter;			//location of the muzzle
    private MaterialHolder schematicBlockTypeChest;						//locations of the chest
    private MaterialHolder schematicBlockTypeSign;						//locations of the sign
    private MaterialHolder schematicBlockTypeRedstoneTorch;				//locations of the redstone torches
    private MaterialHolder schematicBlockTypeRedstoneWire;				//locations of the redstone wires
    private MaterialHolder schematicBlockTypeRepeater;					//locations of the repeaters
    private MaterialHolder schematicBlockTypeRedstoneTrigger; 			//locations of button or levers
    private MaterialHolder ingameBlockTypeRedstoneTrigger;    			//block which is placed instead of the place holder
    private MaterialHolder schematicBlockTypeRightClickTrigger; 		//locations of the right click trigger 
    private MaterialHolder ingameBlockTypeRightClickTrigger;   			//block type of the tigger in game
    private MaterialHolder schematicBlockTypeFiringIndicator;			//location of the firing indicator
    private MaterialHolder ingameBlockTypeFiringIndicatorOff;			//block type of firing indicator off
    private MaterialHolder ingameBlockTypeFiringIndicatorOn;   			//block type of firing indicator on
    
    //cannon design block lists for every direction (NORTH, EAST, SOUTH, WEST)
    private HashMap<BlockFace, CannonBlocks> cannonBlockMap = new HashMap<BlockFace, CannonBlocks>();
    
    
    /**
     * returns the rotation center of a cannon design
     * @param cannonDirection
     * @param offset
     * @return
     */
    public Vector getRotationCenter(String cannonDirection, Vector offset)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getRotationCenter().add(offset);
    	}
    	
    	return null;
    } 
    
    
    /**
     * returns the muzzle location
     * @param cannonDirection
     * @param offset
     * @return
     */
    public Vector getMuzzle(BlockFace cannonDirection, Vector offset)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getMuzzle().add(offset);
    	}
    	
    	return null;
    }
    
    /**
     * returns one trigger location
     * @param cannonDirection
     * @param offset
     * @return
     */
    public Vector getFiringTriggerLocation(BlockFace cannonDirection, Vector offset)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getFiringTrigger().add(offset);
    	}
    	
    	return null;
    }
    
    /**
     * returns a list of the trigger indicator location
     * @param cannonDirection
     * @param offset
     * @return
     */
    public List<Vector> getFiringIndicatorLocations(BlockFace cannonDirection)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getFiringIndicator();
    	}
    	
    	return null;
    }
    
    /**
     * returns a list of all cannonBlocks
     * @param cannonDirection
     * @return
     */
    public List<SimpleBlock> getAllCannonBlocks(String cannonDirection)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getAllCannonBlocks();
    	}
    	
    	return new ArrayList<SimpleBlock>();
    }
    
    /**
     * returns a list of all loading interface blocks
     * @param cannonDirection
     * @return
     */
    public List<Vector> getLoadingInterface(String cannonDirection)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getLoadingInterface();
    	}
    	
    	return new ArrayList<Vector>();
    }
    
    /**
     * returns a list of all sign blocks
     * @param cannonDirection
     * @return
     */
    public List<Vector> getSignsLocations(BlockFace cannonDirection)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getSigns();
    	}
    	//return a empty list
    	return new ArrayList<Vector>();
    }
    
    /**
     * returns a list of all chest blocks
     * @param cannonDirection
     * @return
     */
    public List<Vector> getChestsLocations(BlockFace cannonDirection)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannonDirection);
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getChests();
    	}
    	//return a empty list
    	return new ArrayList<Vector>();
    }
     
    
    
	public int getUniqueID()
	{
		return uniqueID;
	}
	public void setUniqueID(int uniqueID)
	{
		this.uniqueID = uniqueID;
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
	public MaterialHolder getGunpowderType()
	{
		return gunpowderType;
	}
	public void setGunpowderType(MaterialHolder gunpowderType)
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
	public int getMaxLoadableGunpowder()
	{
		return maxLoadableGunpowder;
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
	public double getBackblastConfusion()
	{
		return backblastConfusion;
	}
	public void setBackblastConfusion(double backblastConfusion)
	{
		this.backblastConfusion = backblastConfusion;
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
	public BlockFace getDefaultHorizonatalFacing()
	{
		return defaultHorizonatalFacing;
	}
	public void setDefaultHorizonatalFacing(BlockFace defaultHorizonatalFacing)
	{
		this.defaultHorizonatalFacing = defaultHorizonatalFacing;
	}
	public double getDefaultVerticalAngle()
	{
		return defaultVerticalAngle;
	}
	public void setDefaultVerticalAngle(double defaultVerticalAngle)
	{
		this.defaultVerticalAngle = defaultVerticalAngle;
	}
	public double getMaxHorizontalAngle()
	{
		return maxHorizontalAngle;
	}
	public void setMaxHorizontalAngle(double maxHorizontalAngle)
	{
		this.maxHorizontalAngle = maxHorizontalAngle;
	}
	public double getMinHorizontalAngle()
	{
		return minHorizontalAngle;
	}
	public void setMinHorizontalAngle(double minHorizontalAngle)
	{
		this.minHorizontalAngle = minHorizontalAngle;
	}
	public double getMaxVerticalAngle()
	{
		return maxVerticalAngle;
	}
	public void setMaxVerticalAngle(double maxVerticalAngle)
	{
		this.maxVerticalAngle = maxVerticalAngle;
	}
	public double getMinVerticalAngle()
	{
		return minVerticalAngle;
	}
	public void setMinVerticalAngle(double minVerticalAngle)
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
	public double getAngleUpdateSpeed()
	{
		return angleUpdateSpeed;
	}
	public void setAngleUpdateSpeed(double angleUpdateSpeed)
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
	public boolean isRotabable()
	{
		return isRotabable;
	}
	public void setRotabable(boolean isRotabable)
	{
		this.isRotabable = isRotabable;
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
	public MaterialHolder getSchematicBlockTypeIgnore()
	{
		return schematicBlockTypeIgnore;
	}
	public void setSchematicBlockTypeIgnore(MaterialHolder schematicBlockTypeIgnore)
	{
		this.schematicBlockTypeIgnore = schematicBlockTypeIgnore;
	}
	public MaterialHolder getSchematicBlockTypeMuzzle()
	{
		return schematicBlockTypeMuzzle;
	}
	public void setSchematicBlockTypeMuzzle(MaterialHolder schematicBlockTypeMuzzle)
	{
		this.schematicBlockTypeMuzzle = schematicBlockTypeMuzzle;
	}
	public MaterialHolder getSchematicBlockTypeRotationCenter()
	{
		return schematicBlockTypeRotationCenter;
	}
	public void setSchematicBlockTypeRotationCenter(MaterialHolder schematicBlockTypeRotationCenter)
	{
		this.schematicBlockTypeRotationCenter = schematicBlockTypeRotationCenter;
	}
	public MaterialHolder getSchematicBlockTypeChest()
	{
		return schematicBlockTypeChest;
	}
	public void setSchematicBlockTypeChest(MaterialHolder schematicBlockTypeChest)
	{
		this.schematicBlockTypeChest = schematicBlockTypeChest;
	}
	public MaterialHolder getSchematicBlockTypeSign()
	{
		return schematicBlockTypeSign;
	}
	public void setSchematicBlockTypeSign(MaterialHolder schematicBlockTypeSign)
	{
		this.schematicBlockTypeSign = schematicBlockTypeSign;
	}
	public MaterialHolder getSchematicBlockTypeRedstoneTorch()
	{
		return schematicBlockTypeRedstoneTorch;
	}
	public void setSchematicBlockTypeRedstoneTorch(MaterialHolder schematicBlockTypeRedstoneTorch)
	{
		this.schematicBlockTypeRedstoneTorch = schematicBlockTypeRedstoneTorch;
	}
	public MaterialHolder getSchematicBlockTypeRedstoneWire()
	{
		return schematicBlockTypeRedstoneWire;
	}
	public void setSchematicBlockTypeRedstoneWire(MaterialHolder schematicBlockTypeRedstoneWire)
	{
		this.schematicBlockTypeRedstoneWire = schematicBlockTypeRedstoneWire;
	}
	public MaterialHolder getSchematicBlockTypeRepeater()
	{
		return schematicBlockTypeRepeater;
	}
	public void setSchematicBlockTypeRepeater(MaterialHolder schematicBlockTypeRepeater)
	{
		this.schematicBlockTypeRepeater = schematicBlockTypeRepeater;
	}
	public MaterialHolder getSchematicBlockTypeRedstoneTrigger()
	{
		return schematicBlockTypeRedstoneTrigger;
	}
	public void setSchematicBlockTypeRedstoneTrigger(MaterialHolder schematicBlockTypeRedstoneTrigger)
	{
		this.schematicBlockTypeRedstoneTrigger = schematicBlockTypeRedstoneTrigger;
	}
	public MaterialHolder getIngameBlockTypeRedstoneTrigger()
	{
		return ingameBlockTypeRedstoneTrigger;
	}
	public void setIngameBlockTypeRedstoneTrigger(MaterialHolder ingameBlockTypeRedstoneTrigger)
	{
		this.ingameBlockTypeRedstoneTrigger = ingameBlockTypeRedstoneTrigger;
	}
	public MaterialHolder getSchematicBlockTypeRightClickTrigger()
	{
		return schematicBlockTypeRightClickTrigger;
	}
	public void setSchematicBlockTypeRightClickTrigger(MaterialHolder schematicBlockTypeRightClickTrigger)
	{
		this.schematicBlockTypeRightClickTrigger = schematicBlockTypeRightClickTrigger;
	}
	public MaterialHolder getIngameBlockTypeRightClickTrigger()
	{
		return ingameBlockTypeRightClickTrigger;
	}
	public void setIngameBlockTypeRightClickTrigger(MaterialHolder ingameBlockTypeRightClickTrigger)
	{
		this.ingameBlockTypeRightClickTrigger = ingameBlockTypeRightClickTrigger;
	}
	public MaterialHolder getSchematicBlockTypeFiringIndicator()
	{
		return schematicBlockTypeFiringIndicator;
	}
	public void setSchematicBlockTypeFiringIndicator(MaterialHolder schematicBlockTypeFiringIndicator)
	{
		this.schematicBlockTypeFiringIndicator = schematicBlockTypeFiringIndicator;
	}
	public MaterialHolder getIngameBlockTypeFiringIndicatorOff()
	{
		return ingameBlockTypeFiringIndicatorOff;
	}
	public void setIngameBlockTypeFiringIndicatorOff(MaterialHolder ingameBlockTypeFiringIndicatorOff)
	{
		this.ingameBlockTypeFiringIndicatorOff = ingameBlockTypeFiringIndicatorOff;
	}
	public MaterialHolder getIngameBlockTypeFiringIndicatorOn()
	{
		return ingameBlockTypeFiringIndicatorOn;
	}
	public void setIngameBlockTypeFiringIndicatorOn(MaterialHolder ingameBlockTypeFiringIndicatorOn)
	{
		this.ingameBlockTypeFiringIndicatorOn = ingameBlockTypeFiringIndicatorOn;
	}
	public HashMap<BlockFace, CannonBlocks> getCannonBlockMap()
	{
		return cannonBlockMap;
	}
	public void setCannonBlockMap(HashMap<BlockFace, CannonBlocks> cannonBlockMap)
	{
		this.cannonBlockMap = cannonBlockMap;
	}
	
}
