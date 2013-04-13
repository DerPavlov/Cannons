package at.pavlov.Cannons.cannon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.container.MaterialHolder;
import at.pavlov.Cannons.container.SimpleBlock;
import at.pavlov.Cannons.projectile.Projectile;


public class CannonDesign
{
	//general
	private String designID;
	private String designName;
	
	//sign
	private boolean isSignRequired; 
	
	//ammunition_consumption
	private String gunpowderName;
	private MaterialHolder gunpowderType;
	private boolean ammoInfiniteForPlayer;
    private boolean ammoInfiniteForRedstone;
    private boolean autoreloadRedstone;
    
    //barrelProperties
	private int maxLoadableGunpowder;
	private double multiplierVelocity;
	private double spreadOfCannon;
	
	//timings
	private double blastConfusion;
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
	private boolean flintAndSteelRequired;
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
    private MaterialHolder schematicBlockTypeChestAndSign;				//locations of the chest and sign
    private MaterialHolder schematicBlockTypeRedstoneTorch;				//locations of the redstone torches
    private MaterialHolder schematicBlockTypeRedstoneWireAndRepeater;	//locations of the redstone wires and repeaters
    private MaterialHolder schematicBlockTypeRedstoneTrigger; 			//locations of button or levers
    private MaterialHolder ingameBlockTypeRedstoneTrigger;    			//block which is placed instead of the place holder
    private MaterialHolder schematicBlockTypeRightClickTrigger; 		//locations of the right click trigger 
    private MaterialHolder ingameBlockTypeRightClickTrigger;   			//block type of the tigger in game
    private MaterialHolder schematicBlockTypeFiringIndicator;			//location of the firing indicator
    
    //cannon design block lists for every direction (NORTH, EAST, SOUTH, WEST)
    private HashMap<BlockFace, CannonBlocks> cannonBlockMap = new HashMap<BlockFace, CannonBlocks>();
    
   

    
    /**
     * returns the rotation center of a cannon design
     * @param cannonDirection
     * @param offset
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
     * @param cannonDirection
     * @param offset
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
     * @param cannonDirection
     * @param offset
     * @return
     */
    public Location getFiringTrigger(Cannon cannon)
    {
    	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	if (cannonBlocks != null)
    	{
    		return cannonBlocks.getFiringTrigger().clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit());
    	}
    	
    	System.out.println("[Cannons] missing FiringIndicator for cannon design" + cannon.getCannonName());
    	return cannon.getOffset().toLocation(cannon.getWorldBukkit());
    }
    
    /**
     * returns a list of all cannonBlocks
     * @param cannonDirection
     * @return
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
     * returns a list of all firingIndicator blocks
     * @param cannonDirection
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
     * @param cannonDirection
     * @return
     */
    public List<Location> getLoadingInterface(Cannon cannon)
    {
     	CannonBlocks cannonBlocks  = cannonBlockMap.get(cannon.getCannonDirection());
    	List<Location> locList = new ArrayList<Location>();
    	if (cannonBlocks != null)
    	{
    		for (Vector vect : cannonBlocks.getLoadingInterface())
    		{
    			locList.add(vect.clone().add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns a list of all right click trigger blocks
     * @param cannonDirection
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
     * @param cannonDirection
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
     * @param cannonDirection
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
    			locList.add(block.add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns a list of all redstone torch blocks
     * @param cannonDirection
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
     * @param cannonDirection
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
    			locList.add(block.add(cannon.getOffset()).toLocation(cannon.getWorldBukkit()));
    		}
    	}
		return locList;
    }
    
    /**
     * returns true if the projectile has the same Id of a allowed projectile
     * @param projectile
     * @return
     */
    public boolean canLoad(Projectile projectile)
    {
    	for (String p : allowedProjectiles)
    	{
    		if (projectile.getProjectileID() == p)
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
	public MaterialHolder getSchematicBlockTypeRedstoneTorch()
	{
		return schematicBlockTypeRedstoneTorch;
	}
	public void setSchematicBlockTypeRedstoneTorch(MaterialHolder schematicBlockTypeRedstoneTorch)
	{
		this.schematicBlockTypeRedstoneTorch = schematicBlockTypeRedstoneTorch;
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
	public HashMap<BlockFace, CannonBlocks> getCannonBlockMap()
	{
		return cannonBlockMap;
	}
	public void setCannonBlockMap(HashMap<BlockFace, CannonBlocks> cannonBlockMap)
	{
		this.cannonBlockMap = cannonBlockMap;
	}
	
	public String toString()
	{
		return "designID:" + designID + " name:" + designName;
	}


	public MaterialHolder getSchematicBlockTypeChestAndSign()
	{
		return schematicBlockTypeChestAndSign;
	}


	public void setSchematicBlockTypeChestAndSign(MaterialHolder schematicBlockTypeChestAndSign)
	{
		this.schematicBlockTypeChestAndSign = schematicBlockTypeChestAndSign;
	}


	public MaterialHolder getSchematicBlockTypeRedstoneWireAndRepeater()
	{
		return schematicBlockTypeRedstoneWireAndRepeater;
	}


	public void setSchematicBlockTypeRedstoneWireAndRepeater(MaterialHolder schematicBlockTypeRedstoneWireAndRepeater)
	{
		this.schematicBlockTypeRedstoneWireAndRepeater = schematicBlockTypeRedstoneWireAndRepeater;
	}


	public MaterialHolder getSchematicBlockTypeFiringIndicator()
	{
		return schematicBlockTypeFiringIndicator;
	}


	public void setSchematicBlockTypeFiringIndicator(MaterialHolder schematicBlockTypeFiringIndicator)
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


	public boolean isFlintAndSteelRequired()
	{
		return flintAndSteelRequired;
	}


	public void setFlintAndSteelRequired(boolean flintAndSteelRequired)
	{
		this.flintAndSteelRequired = flintAndSteelRequired;
	}
	

	
}
