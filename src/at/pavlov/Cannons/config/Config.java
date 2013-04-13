package at.pavlov.Cannons.config;


import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.container.MaterialHolder;

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
	private MaterialHolder toolRotating = new MaterialHolder(350, 0);
	
	

	private UserMessages userMessage;
	private Cannons plugin;
	private DesignStorage designStorage;
	private ProjectileStorage projectileStorage;

	public Config(Cannons plugin)
	{
		this.plugin = plugin;
		userMessage = new UserMessages(this.plugin);
		designStorage = new DesignStorage(this.plugin);
		projectileStorage = new ProjectileStorage(this.plugin);
	}

	public void loadConfig()
	{
		plugin.reloadConfig();
		
		//load Config
		
		//general
		setDebugMode(plugin.getConfig().getBoolean("general.debugMode", false));
		
		//limitOfCannons
		setBuildLimitEnabled(plugin.getConfig().getBoolean("limitOfCannons.useLimits", true));
		setBuildLimitA(plugin.getConfig().getInt("limitOfCannons.buildLimitA", 10));
		setBuildLimitB(plugin.getConfig().getInt("limitOfCannons.buildLimitB", 2));

		//tools
		setToolAdjust(new MaterialHolder(plugin.getConfig().getString("tools.adjust", "0:0")));
		setToolAutoaim(new MaterialHolder(plugin.getConfig().getString("tools.autoaim", "347:0")));
		setToolRotating(new MaterialHolder(plugin.getConfig().getString("tools.adjust", "350:0")));
		
	
		// save defaults
		plugin.getConfig().options().copyHeader(true);
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();
		
		
		projectileStorage.loadProjectiles();
		designStorage.loadCannonDesigns();
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

	public void setBuildLimitEnabled(boolean buildLimitEnabled)
	{
		this.buildLimitEnabled = buildLimitEnabled;
	}

	public int getBuildLimitA()
	{
		return buildLimitA;
	}

	public void setBuildLimitA(int buildLimitA)
	{
		this.buildLimitA = buildLimitA;
	}

	public int getBuildLimitB()
	{
		return buildLimitB;
	}

	public void setBuildLimitB(int buildLimitB)
	{
		this.buildLimitB = buildLimitB;
	}

	public MaterialHolder getToolAdjust()
	{
		return toolAdjust;
	}

	public void setToolAdjust(MaterialHolder toolAdjust)
	{
		this.toolAdjust = toolAdjust;
	}

	public MaterialHolder getToolAutoaim()
	{
		return toolAutoaim;
	}

	public void setToolAutoaim(MaterialHolder toolAutoaim)
	{
		this.toolAutoaim = toolAutoaim;
	}

	public MaterialHolder getToolRotating()
	{
		return toolRotating;
	}

	public void setToolRotating(MaterialHolder toolRotating)
	{
		this.toolRotating = toolRotating;
	}

	public boolean isDebugMode()
	{
		return debugMode;
	}

	public void setDebugMode(boolean debugMode)
	{
		this.debugMode = debugMode;
	}


	
	

}
