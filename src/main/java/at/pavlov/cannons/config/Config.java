package at.pavlov.cannons.config;


import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.projectile.ProjectileManager;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.inventory.ItemStack;

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
	
	//build limits
	private boolean buildLimitEnabled;
	private int buildLimitA;
	private int buildLimitB;
    //keepProjectileAlive
    private boolean keepAliveEnabled;
    private double keepAliveTeleportDistance;
	//tools
	private MaterialHolder toolAdjust = new MaterialHolder("minecraft:air");
	private MaterialHolder toolAutoaim = new MaterialHolder("minecraft:clock");
	private MaterialHolder toolFiring = new MaterialHolder("minecraft:flint_and_steel");
    private MaterialHolder toolRamrod = new MaterialHolder("minecraft:stick");
	private MaterialHolder toolRotating = new MaterialHolder("minecraft:rail");
    private MaterialHolder toolThermometer = new MaterialHolder("minecraft:gold_nugget");

    private int imitatedBlockMinimumDistance;
    private int imitatedBlockMaximumDistance;
    private int imitatedSoundMaximumDistance;
    private float imitatedSoundMaximumVolume;

    private boolean imitatedExplosionEnabled;
    private int imitatedExplosionSphereSize;
    private MaterialHolder imitatedExplosionMaterial= new MaterialHolder("minecraft:glowstone");
    private double imitatedExplosionTime;

    private boolean imitatedAimingEnabled;
    private int imitatedAimingLineLength;
    private MaterialHolder imitatedAimingMaterial = new MaterialHolder("minecraft:glass");
    private double imitatedAimingTime;

    private boolean imitatedFiringEffectEnabled;
    private MaterialHolder imitatedFireMaterial = new MaterialHolder("minecraft:glowstone");
    private MaterialHolder imitatedSmokeMaterial = new MaterialHolder("minecraft:cobweb");
    private double imitatedFiringTime;

    private boolean imitatedPredictorEnabled;
    private int imitatedPredictorIterations;
    private double imitatedPredictorDistance;
    private MaterialHolder imitatedPredictorMaterial = new MaterialHolder("minecraft:glowstone");
    private double imitatedPredictorTime;


    //superbreakerBlocks
    private List<MaterialHolder> superbreakerBlocks = new ArrayList<MaterialHolder>();

    //unbreakableBlocks
    private List<MaterialHolder> unbreakableBlocks = new ArrayList<MaterialHolder>();

    //cancelEventForLoadingItem
    private List<MaterialHolder> cancelItems = new ArrayList<MaterialHolder>();


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
		
		//limitOfCannons
		setBuildLimitEnabled(plugin.getConfig().getBoolean("cannonLimits.useLimits", true));
		setBuildLimitA(plugin.getConfig().getInt("cannonLimits.buildLimitA", 10));
		setBuildLimitB(plugin.getConfig().getInt("cannonLimits.buildLimitB", 2));

        //keepProjectileAlive
        setKeepAliveEnabled(plugin.getConfig().getBoolean("keepProjectileAlive.enabled", true));
        setKeepAliveTeleportDistance(plugin.getConfig().getDouble("keepProjectileAlive.teleportProjectile", 5.0));

		//tools
		setToolAdjust(new MaterialHolder(plugin.getConfig().getString("tools.adjust", "minecraft:air")));
		setToolAutoaim(new MaterialHolder(plugin.getConfig().getString("tools.autoaim", "minecraft:clock")));
		setToolFiring(new MaterialHolder(plugin.getConfig().getString("tools.firing", "minecraft:flint_and_steel")));
        setToolRamrod(new MaterialHolder(plugin.getConfig().getString("tools.ramrod", "minecraft:stick")));
		setToolRotating(new MaterialHolder(plugin.getConfig().getString("tools.adjust", "minecraft:rail")));
        setToolThermometer(new MaterialHolder(plugin.getConfig().getString("tools.thermometer", "minecraft:gold_nugget")));

        //imitated effects
        setImitatedBlockMinimumDistance(plugin.getConfig().getInt("imitatedEffects.minimumBlockDistance", 40));
        setImitatedBlockMaximumDistance(plugin.getConfig().getInt("imitatedEffects.maximumBlockDistance", 200));
        setImitatedSoundMaximumDistance(plugin.getConfig().getInt("imitatedEffects.maximumSoundDistance", 200));
        setImitatedSoundMaximumVolume((float) plugin.getConfig().getDouble("imitatedEffects.maximumSoundVolume", 0.8));

        //imitated explosions
        setImitatedExplosionEnabled(plugin.getConfig().getBoolean("imitatedEffects.explosion.enabled", false));
        setImitatedExplosionSphereSize(plugin.getConfig().getInt("imitatedEffects.explosion.sphereSize", 2));
        setImitatedExplosionMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedEffects.explosion.material", "minecraft:glowstone")));
        setImitatedExplosionTime(plugin.getConfig().getDouble("imitatedEffects.explosion.time", 1.0));

        //imitated aiming
        setImitatedAimingEnabled(plugin.getConfig().getBoolean("imitatedEffects.aiming.enabled", false));
        setImitatedAimingLineLength(plugin.getConfig().getInt("imitatedEffects.aiming.length", 5));
        setImitatedAimingMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedEffects.aiming.block", "minecraft:glass")));
        setImitatedAimingTime(plugin.getConfig().getDouble("imitatedEffects.aiming.time", 1.0));

        //imitated firing effects
        setImitatedFiringEffectEnabled(plugin.getConfig().getBoolean("imitatedEffects.firing.enabled", false));
        setImitatedFireMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedEffects.firing.fireBlock", "minecraft:glowstone")));
        setImitatedSmokeMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedEffects.firing.smokeBlock", "'minecraft:cobweb")));
        setImitatedFiringTime(plugin.getConfig().getDouble("imitatedEffects.firing.time", 1.0));

        //imitaded predictor
        setImitatedPredictorEnabled(plugin.getConfig().getBoolean("imitatedEffects.predictor.enabled", true));
        setImitatedPredictorIterations(plugin.getConfig().getInt("imitatedEffects.predictor.maxIterations", 500));
        setImitatedPredictorDistance(plugin.getConfig().getDouble("imitatedEffects.predictor.maxDistance", 400.0));
        setImitatedPredictorMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedEffects.predictor.material", "minecraft:glowstone")));
        setImitatedPredictorTime(plugin.getConfig().getDouble("imitatedEffects.predictor.time", 1.0));

        //superbreakerBlocks
        setSuperbreakerBlocks(CannonsUtil.toMaterialHolderList(plugin.getConfig().getStringList("superbreakerBlocks")));
        //if this list is empty add some blocks
        if (superbreakerBlocks.size() == 0)
        {
            plugin.logInfo("superbreakerBlock list is empty");
        }

        //unbreakableBlocks
        setUnbreakableBlocks(CannonsUtil.toMaterialHolderList(plugin.getConfig().getStringList("unbreakableBlocks")));
        if (unbreakableBlocks.size() == 0)
        {
            plugin.logInfo("unbreakableBlocks list is empty");
        }

        //cancelEventForLoadingItem
        setCancelItems(CannonsUtil.toMaterialHolderList(plugin.getConfig().getStringList("cancelEventForLoadingItem")));
	
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

	public MaterialHolder getToolAdjust()
	{
		return toolAdjust;
	}

	void setToolAdjust(MaterialHolder toolAdjust)
	{
		this.toolAdjust = toolAdjust;
	}

	public MaterialHolder getToolAutoaim()
	{
		return toolAutoaim;
	}

	void setToolAutoaim(MaterialHolder toolAutoaim)
	{
		this.toolAutoaim = toolAutoaim;
	}

	public MaterialHolder getToolRotating()
	{
		return toolRotating;
	}

	void setToolRotating(MaterialHolder toolRotating)
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

	public MaterialHolder getToolFiring()
	{
		return toolFiring;
	}

	void setToolFiring(MaterialHolder toolFiring)
	{
		this.toolFiring = toolFiring;
	}


    public List<MaterialHolder> getSuperbreakerBlocks() {
        return superbreakerBlocks;
    }

    void setSuperbreakerBlocks(List<MaterialHolder> superbreakerBlocks) {
        this.superbreakerBlocks = superbreakerBlocks;
    }

    public List<MaterialHolder> getUnbreakableBlocks() {
        return unbreakableBlocks;
    }

    void setUnbreakableBlocks(List<MaterialHolder> unbreakableBlocks) {
        this.unbreakableBlocks = unbreakableBlocks;
    }

    public CannonManager getCannonManager() {
        return cannonManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public MaterialHolder getToolThermometer() {
        return toolThermometer;
    }

    public void setToolThermometer(MaterialHolder toolThermometer) {
        this.toolThermometer = toolThermometer;
    }

    public MaterialHolder getToolRamrod() {
        return toolRamrod;
    }

    public void setToolRamrod(MaterialHolder toolRamrod) {
        this.toolRamrod = toolRamrod;
    }

    public List<MaterialHolder> getCancelItems() {
        return cancelItems;
    }

    public void setCancelItems(List<MaterialHolder> cancelItems) {
        this.cancelItems = cancelItems;
    }

    public boolean isCancelItem(ItemStack item)
    {
        for (MaterialHolder item2 : getCancelItems())
        {
            if (item2.equalsFuzzy(item))
                return true;
        }
        return false;
    }

    public MaterialHolder getImitatedExplosionMaterial() {
        return imitatedExplosionMaterial;
    }

    public void setImitatedExplosionMaterial(MaterialHolder imitatedExplosionMaterial) {
        this.imitatedExplosionMaterial = imitatedExplosionMaterial;
    }

    public double getImitatedExplosionTime() {
        return imitatedExplosionTime;
    }

    public void setImitatedExplosionTime(double imitatedExplosionTime) {
        this.imitatedExplosionTime = imitatedExplosionTime;
    }

    public MaterialHolder getImitatedAimingMaterial() {
        return imitatedAimingMaterial;
    }

    public void setImitatedAimingMaterial(MaterialHolder imitatedAimingMaterial) {
        this.imitatedAimingMaterial = imitatedAimingMaterial;
    }

    public MaterialHolder getImitatedFireMaterial() {
        return imitatedFireMaterial;
    }

    public void setImitatedFireMaterial(MaterialHolder imitatedFireMaterial) {
        this.imitatedFireMaterial = imitatedFireMaterial;
    }

    public MaterialHolder getImitatedSmokeMaterial() {
        return imitatedSmokeMaterial;
    }

    public void setImitatedSmokeMaterial(MaterialHolder imitatedSmokeMaterial) {
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

    public MaterialHolder getImitatedPredictorMaterial() {
        return imitatedPredictorMaterial;
    }

    public void setImitatedPredictorMaterial(MaterialHolder imitatedPredictorMaterial) {
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
}
