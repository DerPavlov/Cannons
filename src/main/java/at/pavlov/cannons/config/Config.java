package at.pavlov.cannons.config;


import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.projectile.ProjectileManager;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author DerPavlov
 * 
 */



public class Config
{
	//general
	private boolean debugMode;
    private boolean relayExplosionEvent;
    private int claimEdgeLength;
	
	//build limits
	private boolean buildLimitEnabled;
	private int buildLimitA;
	private int buildLimitB;
    //keepProjectileAlive
    private boolean keepAliveEnabled;
    private double keepAliveTeleportDistance;
	//tools
	private ItemHolder toolAdjust = new ItemHolder("minecraft:air");
	private ItemHolder toolAutoaim = new ItemHolder("minecraft:clock");
	private double toolAutoaimRange;
	private ItemHolder toolFiring = new ItemHolder("minecraft:flint_and_steel");
    private ItemHolder toolRamrod = new ItemHolder("minecraft:stick");
	private ItemHolder toolRotating = new ItemHolder("minecraft:rail");
    private ItemHolder toolThermometer = new ItemHolder("minecraft:gold_nugget");

    private int imitatedBlockMinimumDistance;
    private int imitatedBlockMaximumDistance;
    private int imitatedSoundMaximumDistance;
    private float imitatedSoundMaximumVolume;

    private boolean imitatedExplosionEnabled;
    private int imitatedExplosionSphereSize;
    private BlockData imitatedExplosionMaterial= Bukkit.createBlockData("minecraft:glowstone");
    private double imitatedExplosionTime;

    private boolean imitatedExplosionParticlesEnabled;
    private Particle imitatedExplosionParticlesType;
    private double imitatedExplosionParticlesDiameter;
    private int imitatedExplosionParticlesCount;

    private boolean imitatedAimingEnabled;
    private int imitatedAimingLineLength;
    private BlockData imitatedAimingMaterial = Bukkit.createBlockData("minecraft:glass");
    private double imitatedAimingTime;

    private boolean imitatedFiringEffectEnabled;
    private BlockData imitatedFireMaterial = Bukkit.createBlockData("minecraft:glowstone");
    private BlockData imitatedSmokeMaterial = Bukkit.createBlockData("minecraft:cobweb");
    private double imitatedFiringTime;

    private boolean imitatedPredictorEnabled;
    private int imitatedPredictorIterations;
    private double imitatedPredictorDistance;
    private BlockData imitatedPredictorMaterial = Bukkit.createBlockData("minecraft:glowstone");
    private double imitatedPredictorTime;


    //superbreakerBlocks
    private List<BlockData> superbreakerBlocks = new ArrayList<BlockData>();

    //unbreakableBlocks
    private List<BlockData> unbreakableBlocks = new ArrayList<BlockData>();

    //cancelEventForLoadingItem
    private List<ItemHolder> cancelItems = new ArrayList<>();


    private final UserMessages userMessage;
	private final Cannons plugin;
	private final DesignStorage designStorage;
	private final ProjectileStorage projectileStorage;
    private final CannonManager cannonManager;
    private final ProjectileManager projectileManager;

	public Config(Cannons plugin)
	{
		this.plugin = plugin;
		userMessage = new UserMessages(this.plugin);
		designStorage = new DesignStorage(this.plugin);
		projectileStorage = new ProjectileStorage(this.plugin);
        cannonManager = new CannonManager(plugin, userMessage, this);
        projectileManager = new ProjectileManager(plugin);
	}

	public void loadConfig()
	{
		// copy the default config to the disk if it does not exist
		plugin.saveDefaultConfig();

        plugin.logDebug("load Config");
        plugin.reloadConfig();
		
		//general
		setDebugMode(plugin.getConfig().getBoolean("general.debugMode", false));
        setRelayExplosionEvent(plugin.getConfig().getBoolean("general.relayExplosionEvent", false));
        setClaimEdgeLength(plugin.getConfig().getInt("general.claimEdgeLength", 60));
		
		//limitOfCannons
		setBuildLimitEnabled(plugin.getConfig().getBoolean("cannonLimits.useLimits", true));
		setBuildLimitA(plugin.getConfig().getInt("cannonLimits.buildLimitA", 10));
		setBuildLimitB(plugin.getConfig().getInt("cannonLimits.buildLimitB", 2));

        //keepProjectileAlive
        setKeepAliveEnabled(plugin.getConfig().getBoolean("keepProjectileAlive.enabled", true));
        setKeepAliveTeleportDistance(plugin.getConfig().getDouble("keepProjectileAlive.teleportProjectile", 5.0));

		//tools
		setToolAdjust(new ItemHolder(plugin.getConfig().getString("tools.adjust", "minecraft:air")));
		setToolAutoaim(new ItemHolder(plugin.getConfig().getString("tools.autoaim", "minecraft:clock")));
        setToolAutoaimRange(plugin.getConfig().getDouble("tools.autoaimRange", 4.0));
		setToolFiring(new ItemHolder(plugin.getConfig().getString("tools.firing", "minecraft:flint_and_steel")));
        setToolRamrod(new ItemHolder(plugin.getConfig().getString("tools.ramrod", "minecraft:stick")));
		setToolRotating(new ItemHolder(plugin.getConfig().getString("tools.adjust", "minecraft:rail")));
        setToolThermometer(new ItemHolder(plugin.getConfig().getString("tools.thermometer", "minecraft:gold_nugget")));

        //imitated effects
        setImitatedBlockMinimumDistance(plugin.getConfig().getInt("imitatedEffects.minimumBlockDistance", 40));
        setImitatedBlockMaximumDistance(plugin.getConfig().getInt("imitatedEffects.maximumBlockDistance", 200));
        setImitatedSoundMaximumDistance(plugin.getConfig().getInt("imitatedEffects.maximumSoundDistance", 200));
        setImitatedSoundMaximumVolume((float) plugin.getConfig().getDouble("imitatedEffects.maximumSoundVolume", 0.8));

        //imitated explosions block
        setImitatedExplosionEnabled(plugin.getConfig().getBoolean("imitatedEffects.explosion.enabled", false));
        setImitatedExplosionSphereSize(plugin.getConfig().getInt("imitatedEffects.explosion.sphereSize", 2));
        setImitatedExplosionMaterial(CannonsUtil.createBlockData(plugin.getConfig().getString("imitatedEffects.explosion.material", "minecraft:glowstone")));
        setImitatedExplosionTime(plugin.getConfig().getDouble("imitatedEffects.explosion.time", 1.0));

        //imitated explosions particles
        setImitatedExplosionParticlesEnabled(plugin.getConfig().getBoolean("imitatedEffects.explosionParticles.enabled", true));
        try {
            setImitatedExplosionParticlesType(Particle.valueOf(plugin.getConfig().getString("imitatedEffects.explosionParticles.type", "EXPLOSION_LARGE")));
        }
        catch(Exception e){
            plugin.logSevere("Type for Explosion particle  is not correct. Please check spelling of " + plugin.getConfig().getString("imitatedEffects.explosionParticles.type"));
            setImitatedExplosionParticlesType(Particle.EXPLOSION_LARGE);
        }
        setImitatedExplosionParticlesCount(plugin.getConfig().getInt("imitatedEffects.explosionParticles.count", 5));
        setImitatedExplosionParticlesDiameter(plugin.getConfig().getDouble("imitatedEffects.explosionParticles.diameter", 1));

        //imitated aiming
        setImitatedAimingEnabled(plugin.getConfig().getBoolean("imitatedEffects.aiming.enabled", false));
        setImitatedAimingLineLength(plugin.getConfig().getInt("imitatedEffects.aiming.length", 5));
        setImitatedAimingMaterial(CannonsUtil.createBlockData(plugin.getConfig().getString("imitatedEffects.aiming.block", "minecraft:glass")));
        setImitatedAimingTime(plugin.getConfig().getDouble("imitatedEffects.aiming.time", 1.0));

        //imitated firing effects
        setImitatedFiringEffectEnabled(plugin.getConfig().getBoolean("imitatedEffects.firing.enabled", false));
        setImitatedFireMaterial(CannonsUtil.createBlockData(plugin.getConfig().getString("imitatedEffects.firing.fireBlock", "minecraft:glowstone")));
        setImitatedSmokeMaterial(CannonsUtil.createBlockData(plugin.getConfig().getString("imitatedEffects.firing.smokeBlock", "'minecraft:cobweb")));
        setImitatedFiringTime(plugin.getConfig().getDouble("imitatedEffects.firing.time", 1.0));

        //imitaded predictor
        setImitatedPredictorEnabled(plugin.getConfig().getBoolean("imitatedEffects.predictor.enabled", true));
        setImitatedPredictorIterations(plugin.getConfig().getInt("imitatedEffects.predictor.maxIterations", 500));
        setImitatedPredictorDistance(plugin.getConfig().getDouble("imitatedEffects.predictor.maxDistance", 400.0));
        setImitatedPredictorMaterial(CannonsUtil.createBlockData(plugin.getConfig().getString("imitatedEffects.predictor.material", "minecraft:glowstone")));
        setImitatedPredictorTime(plugin.getConfig().getDouble("imitatedEffects.predictor.time", 1.0));

        //superbreakerBlocks
        setSuperbreakerBlocks(CannonsUtil.toBlockDataList(plugin.getConfig().getStringList("superbreakerBlocks")));
        //if this list is empty add some blocks
        if (superbreakerBlocks.size() == 0)
        {
            plugin.logInfo("superbreakerBlock list is empty");
        }

        //unbreakableBlocks
        setUnbreakableBlocks(CannonsUtil.toBlockDataList(plugin.getConfig().getStringList("unbreakableBlocks")));
        if (unbreakableBlocks.size() == 0)
        {
            plugin.logInfo("unbreakableBlocks list is empty");
        }

        //cancelEventForLoadingItem
        setCancelItems(CannonsUtil.toItemHolderList(plugin.getConfig().getStringList("cancelEventForLoadingItem")));
	
		//load other configs	
		projectileStorage.loadProjectiles();
		designStorage.loadCannonDesigns();
        cannonManager.updateCannons();
		userMessage.loadLanguage();

	}
	

	/**
	 * returns the class UserMessages
	 * @return
	 */
	public UserMessages getUserMessages()
	{
		return userMessage;
	}

	public DesignStorage getDesignStorage()
	{
		return designStorage;
	}

	public ProjectileStorage getProjectileStorage()
	{
		return projectileStorage;
	}

	public boolean isBuildLimitEnabled()
	{
		return buildLimitEnabled;
	}

	void setBuildLimitEnabled(boolean buildLimitEnabled)
	{
		this.buildLimitEnabled = buildLimitEnabled;
	}

	public int getBuildLimitA()
	{
		return buildLimitA;
	}

	void setBuildLimitA(int buildLimitA)
	{
		this.buildLimitA = buildLimitA;
	}

	public int getBuildLimitB()
	{
		return buildLimitB;
	}

	void setBuildLimitB(int buildLimitB)
	{
		this.buildLimitB = buildLimitB;
	}

	public ItemHolder getToolAdjust()
	{
		return toolAdjust;
	}

	void setToolAdjust(ItemHolder toolAdjust)
	{
		this.toolAdjust = toolAdjust;
	}

	public ItemHolder getToolAutoaim()
	{
		return toolAutoaim;
	}

	void setToolAutoaim(ItemHolder toolAutoaim)
	{
		this.toolAutoaim = toolAutoaim;
	}

	public ItemHolder getToolRotating()
	{
		return toolRotating;
	}

	void setToolRotating(ItemHolder toolRotating)
	{
		this.toolRotating = toolRotating;
	}

	public boolean isDebugMode()
	{
		return debugMode;
	}

	void setDebugMode(boolean debugMode)
	{
		this.debugMode = debugMode;
	}

	public ItemHolder getToolFiring()
	{
		return toolFiring;
	}

	void setToolFiring(ItemHolder toolFiring)
	{
		this.toolFiring = toolFiring;
	}


    public List<BlockData> getSuperbreakerBlocks() {
        return superbreakerBlocks;
    }

    void setSuperbreakerBlocks(List<BlockData> superbreakerBlocks) {
        this.superbreakerBlocks = superbreakerBlocks;
    }

    public List<BlockData> getUnbreakableBlocks() {
        return unbreakableBlocks;
    }

    void setUnbreakableBlocks(List<BlockData> unbreakableBlocks) {
        this.unbreakableBlocks = unbreakableBlocks;
    }

    public CannonManager getCannonManager() {
        return cannonManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public ItemHolder getToolThermometer() {
        return toolThermometer;
    }

    public void setToolThermometer(ItemHolder toolThermometer) {
        this.toolThermometer = toolThermometer;
    }

    public ItemHolder getToolRamrod() {
        return toolRamrod;
    }

    public void setToolRamrod(ItemHolder toolRamrod) {
        this.toolRamrod = toolRamrod;
    }

    public List<ItemHolder> getCancelItems() {
        return cancelItems;
    }

    public void setCancelItems(List<ItemHolder> cancelItems) {
        this.cancelItems = cancelItems;
    }

    public boolean isCancelItem(ItemStack item)
    {
        for (ItemHolder item2 : getCancelItems())
        {
            if (item2.equalsFuzzy(item))
                return true;
        }
        return false;
    }

    public BlockData getImitatedExplosionMaterial() {
        return imitatedExplosionMaterial;
    }

    public void setImitatedExplosionMaterial(BlockData imitatedExplosionMaterial) {
        this.imitatedExplosionMaterial = imitatedExplosionMaterial;
    }

    public double getImitatedExplosionTime() {
        return imitatedExplosionTime;
    }

    public void setImitatedExplosionTime(double imitatedExplosionTime) {
        this.imitatedExplosionTime = imitatedExplosionTime;
    }

    public BlockData getImitatedAimingMaterial() {
        return imitatedAimingMaterial;
    }

    public void setImitatedAimingMaterial(BlockData imitatedAimingMaterial) {
        this.imitatedAimingMaterial = imitatedAimingMaterial;
    }

    public BlockData getImitatedFireMaterial() {
        return imitatedFireMaterial;
    }

    public void setImitatedFireMaterial(BlockData imitatedFireMaterial) {
        this.imitatedFireMaterial = imitatedFireMaterial;
    }

    public BlockData getImitatedSmokeMaterial() {
        return imitatedSmokeMaterial;
    }

    public void setImitatedSmokeMaterial(BlockData imitatedSmokeMaterial) {
        this.imitatedSmokeMaterial = imitatedSmokeMaterial;
    }

    public boolean isImitatedAimingEnabled() {
        return imitatedAimingEnabled;
    }

    public void setImitatedAimingEnabled(boolean imitatedAimingEnabled) {
        this.imitatedAimingEnabled = imitatedAimingEnabled;
    }

    public boolean isImitatedFiringEffectEnabled() {
        return imitatedFiringEffectEnabled;
    }

    public void setImitatedFiringEffectEnabled(boolean imitatedFiringEffectEnabled) {
        this.imitatedFiringEffectEnabled = imitatedFiringEffectEnabled;
    }

    public int getImitatedAimingLineLength() {
        return imitatedAimingLineLength;
    }

    public void setImitatedAimingLineLength(int imitatedAimingLineLength) {
        this.imitatedAimingLineLength = imitatedAimingLineLength;
    }

    public double getImitatedBlockMinimumDistance() {
        return imitatedBlockMinimumDistance;
    }

    public void setImitatedBlockMinimumDistance(int imitatedBlockMinimumDistance) {
        this.imitatedBlockMinimumDistance = imitatedBlockMinimumDistance;
    }

    public double getImitatedBlockMaximumDistance() {
        return imitatedBlockMaximumDistance;
    }

    public void setImitatedBlockMaximumDistance(int imitatedBlockMaximumDistance) {
        this.imitatedBlockMaximumDistance = imitatedBlockMaximumDistance;
    }

    public int getImitatedSoundMaximumDistance() {
        return imitatedSoundMaximumDistance;
    }

    public void setImitatedSoundMaximumDistance(int imitatedSoundMaximumDistance) {
        this.imitatedSoundMaximumDistance = imitatedSoundMaximumDistance;
    }

    public int getImitatedExplosionSphereSize() {
        return imitatedExplosionSphereSize;
    }

    public void setImitatedExplosionSphereSize(int imitatedExplosionSphereSize) {
        this.imitatedExplosionSphereSize = imitatedExplosionSphereSize;
    }

    public boolean isImitatedExplosionEnabled() {
        return imitatedExplosionEnabled;
    }

    public void setImitatedExplosionEnabled(boolean imitatedExplosionEnabled) {
        this.imitatedExplosionEnabled = imitatedExplosionEnabled;
    }

    public double getImitatedAimingTime() {
        return imitatedAimingTime;
    }

    public void setImitatedAimingTime(double imitatedAimingTime) {
        this.imitatedAimingTime = imitatedAimingTime;
    }

    public double getImitatedFiringTime() {
        return imitatedFiringTime;
    }

    public void setImitatedFiringTime(double imitatedFiringTime) {
        this.imitatedFiringTime = imitatedFiringTime;
    }

    public boolean isKeepAliveEnabled() {
        return keepAliveEnabled;
    }

    public void setKeepAliveEnabled(boolean keepAliveEnabled) {
        this.keepAliveEnabled = keepAliveEnabled;
    }

    public double getKeepAliveTeleportDistance() {
        return keepAliveTeleportDistance;
    }

    public void setKeepAliveTeleportDistance(double keepAliveTeleportDistance) {
        this.keepAliveTeleportDistance = keepAliveTeleportDistance;
    }

    public boolean isImitatedPredictorEnabled() {
        return imitatedPredictorEnabled;
    }

    public void setImitatedPredictorEnabled(boolean imitatedPredictorEnabled) {
        this.imitatedPredictorEnabled = imitatedPredictorEnabled;
    }

    public int getImitatedPredictorIterations() {
        return imitatedPredictorIterations;
    }

    public void setImitatedPredictorIterations(int imitatedPredictorIterations) {
        this.imitatedPredictorIterations = imitatedPredictorIterations;
    }

    public double getImitatedPredictorDistance() {
        return imitatedPredictorDistance;
    }

    public void setImitatedPredictorDistance(double imitatedPredictorDistance) {
        this.imitatedPredictorDistance = imitatedPredictorDistance;
    }

    public BlockData getImitatedPredictorMaterial() {
        return imitatedPredictorMaterial;
    }

    public void setImitatedPredictorMaterial(BlockData imitatedPredictorMaterial) {
        this.imitatedPredictorMaterial = imitatedPredictorMaterial;
    }

    public double getImitatedPredictorTime() {
        return imitatedPredictorTime;
    }

    public void setImitatedPredictorTime(double imitatedPredictorTime) {
        this.imitatedPredictorTime = imitatedPredictorTime;
    }

    public boolean isRelayExplosionEvent() {
        return relayExplosionEvent;
    }

    public void setRelayExplosionEvent(boolean relayExplosionEvent) {
        this.relayExplosionEvent = relayExplosionEvent;
    }

    public float getImitatedSoundMaximumVolume() {
        return imitatedSoundMaximumVolume;
    }

    public void setImitatedSoundMaximumVolume(float imitatedSoundMaximumVolume) {
        this.imitatedSoundMaximumVolume = imitatedSoundMaximumVolume;
    }

    public int getClaimEdgeLength() {
        return claimEdgeLength;
    }

    public void setClaimEdgeLength(int claimEdgeLength) {
        this.claimEdgeLength = claimEdgeLength;
    }

    public double getToolAutoaimRange() {
        return toolAutoaimRange;
    }

    public void setToolAutoaimRange(double toolAutoaimRange) {
        this.toolAutoaimRange = toolAutoaimRange;
    }

    public boolean isImitatedExplosionParticlesEnabled() {
        return imitatedExplosionParticlesEnabled;
    }

    public void setImitatedExplosionParticlesEnabled(boolean imitatedExplosionParticlesEnabled) {
        this.imitatedExplosionParticlesEnabled = imitatedExplosionParticlesEnabled;
    }

    public double getImitatedExplosionParticlesDiameter() {
        return imitatedExplosionParticlesDiameter;
    }

    public void setImitatedExplosionParticlesDiameter(double imitatedExplosionParticlesDiameter) {
        this.imitatedExplosionParticlesDiameter = imitatedExplosionParticlesDiameter;
    }

    public int getImitatedExplosionParticlesCount() {
        return imitatedExplosionParticlesCount;
    }

    public void setImitatedExplosionParticlesCount(int imitatedExplosionParticlesCount) {
        this.imitatedExplosionParticlesCount = imitatedExplosionParticlesCount;
    }

    public Particle getImitatedExplosionParticlesType() {
        return imitatedExplosionParticlesType;
    }

    public void setImitatedExplosionParticlesType(Particle imitatedExplosionParticlesType) {
        this.imitatedExplosionParticlesType = imitatedExplosionParticlesType;
    }
}
