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
	
	//build limits
	private boolean buildLimitEnabled;
	private int buildLimitA;
	private int buildLimitB;
	//tools
	private MaterialHolder toolAdjust = new MaterialHolder(0, 0);
	private MaterialHolder toolAutoaim = new MaterialHolder(347, 0);
	private MaterialHolder toolFiring = new MaterialHolder(259, 0);
    private MaterialHolder toolRamrod = new MaterialHolder(280, 0);
	private MaterialHolder toolRotating = new MaterialHolder(350, 0);
    private MaterialHolder toolThermometer = new MaterialHolder(371, 0);

    private double imitatedExplosionMinimumDistance;
    private double imitatedExplosionMaximumDistance;
    private int imitatedExplosionSphereSize;
    private MaterialHolder imitatedExplosionMaterial;
    private double imitatedExplosionTime;

    private boolean imitatedAimingEnabled;
    private int imitatedAimingLineLength;
    private MaterialHolder imitatedAimingMaterial = new MaterialHolder(20, 0);
    private boolean imitatedFiringEffect;
    private MaterialHolder imitatedFireMaterial = new MaterialHolder(35, 14);
    private MaterialHolder imitatedSmokeMaterial = new MaterialHolder(30, 0);

    //superbreakerBlocks
    private List<MaterialHolder> superbreakerBlocks = new ArrayList<MaterialHolder>();

    //unbreakableBlocks
    private List<MaterialHolder> unbreakableBlocks = new ArrayList<MaterialHolder>();

    //cancelEventForLoadingItem
    private List<MaterialHolder> cancelItems = new ArrayList<MaterialHolder>();


    private final UserMessages userMessage;
	private final Cannons plugin;
	private final DesignStorage designStorage;
	private ProjectileStorage projectileStorage;
    private CannonManager cannonManager;
    private ProjectileManager projectileManager;

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
		
		//limitOfCannons
		setBuildLimitEnabled(plugin.getConfig().getBoolean("cannonLimits.useLimits", true));
		setBuildLimitA(plugin.getConfig().getInt("cannonLimits.buildLimitA", 10));
		setBuildLimitB(plugin.getConfig().getInt("cannonLimits.buildLimitB", 2));

		//tools
		setToolAdjust(new MaterialHolder(plugin.getConfig().getString("tools.adjust", "0:0")));
		setToolAutoaim(new MaterialHolder(plugin.getConfig().getString("tools.autoaim", "347:0")));
		setToolFiring(new MaterialHolder(plugin.getConfig().getString("tools.firing", "259:0")));
        setToolRamrod(new MaterialHolder(plugin.getConfig().getString("tools.ramrod", "280:0")));
		setToolRotating(new MaterialHolder(plugin.getConfig().getString("tools.adjust", "350:0")));

        //imitatedExplosions
        setImitatedExplosionMinimumDistance(plugin.getConfig().getDouble("imitatedExplosion.minimumDistance", 40.0));
        setImitatedExplosionMaximumDistance(plugin.getConfig().getDouble("imitatedExplosion.maximumDistance", 200.0));
        setImitatedExplosionSphereSize(plugin.getConfig().getInt("imitatedExplosion.sphereSize", 2));
        setImitatedExplosionMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedExplosion.material", "35:14")));
        setImitatedExplosionTime(plugin.getConfig().getDouble("imitatedExplosion.time", 1.0));

        //imitateCannonEffects
        setImitatedAimingEnabled(plugin.getConfig().getBoolean("imitatedCannonEffects.aimingEnabled", false));
        setImitatedAimingLineLength(plugin.getConfig().getInt("imitatedCannonEffects.aimingLineLength", 5));
        setImitatedAimingMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedCannonEffects.aimingBlock", "35:14")));
        setImitatedFiringEffect(plugin.getConfig().getBoolean("imitatedCannonEffects.firingEffects", false));
        setImitatedFireMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedCannonEffects.fireBlock", "35:14")));
        setImitatedSmokeMaterial(new MaterialHolder(plugin.getConfig().getString("imitatedCannonEffects.smokeBlock", "35:0")));

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
        cannonManager.updateCannonDesigns();
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

	public void setProjectileStorage(ProjectileStorage projectileStorage)
	{
		this.projectileStorage = projectileStorage;
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

    public void setCannonManager(CannonManager cannonManager) {
        this.cannonManager = cannonManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public void setProjectileManager(ProjectileManager projectileManager) {
        this.projectileManager = projectileManager;
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

    public double getImitatedExplosionMaximumDistance() {
        return imitatedExplosionMaximumDistance;
    }

    public void setImitatedExplosionMaximumDistance(double imitatedExplosionMaximumDistance) {
        this.imitatedExplosionMaximumDistance = imitatedExplosionMaximumDistance;
    }

    public int getImitatedExplosionSphereSize() {
        return imitatedExplosionSphereSize;
    }

    public void setImitatedExplosionSphereSize(int imitatedExplosionSphereSize) {
        this.imitatedExplosionSphereSize = imitatedExplosionSphereSize;
    }

    public double getImitatedExplosionMinimumDistance() {
        return imitatedExplosionMinimumDistance;
    }

    public void setImitatedExplosionMinimumDistance(double imitatedExplosionMinimumDistance) {
        this.imitatedExplosionMinimumDistance = imitatedExplosionMinimumDistance;
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

    public boolean isImitatedFiringEffect() {
        return imitatedFiringEffect;
    }

    public void setImitatedFiringEffect(boolean imitatedFiringEffect) {
        this.imitatedFiringEffect = imitatedFiringEffect;
    }

    public int getImitatedAimingLineLength() {
        return imitatedAimingLineLength;
    }

    public void setImitatedAimingLineLength(int imitatedAimingLineLength) {
        this.imitatedAimingLineLength = imitatedAimingLineLength;
    }
}
