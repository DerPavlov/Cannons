package at.pavlov.cannons;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.logging.Logger;

import at.pavlov.cannons.API.CannonsAPI;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.config.*;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.listener.*;
import at.pavlov.cannons.projectile.ProjectileManager;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.scheduler.FakeBlockHandler;
import at.pavlov.cannons.scheduler.ProjectileObserver;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.dao.PersistenceDatabase;
import at.pavlov.cannons.projectile.Projectile;

public final class Cannons extends JavaPlugin
{
	private PluginManager pm;
	private final Logger logger = Logger.getLogger("Minecraft");

    private final Config config;
	private final FireCannon fireCannon;
	private final CreateExplosion explosion;
	private final Aiming aiming;
    private final ProjectileObserver observer;
    private final FakeBlockHandler fakeBlockHandler;

    private final CannonsAPI cannonsAPI;
    private Economy economy;
	
	//Listener
    private final BlockListener blockListener;
	private final PlayerListener playerListener;
	private final EntityListener entityListener;
	private final SignListener signListener;
    private final Commands commands;
	
	// database
	private final PersistenceDatabase persistenceDatabase;
	private Connection connection = null;

	private final String cannonDatabase = "cannonlist_2_4_6";
	private final String whitelistDatabase = "whitelist_2_4_6";


	public Cannons()
	{
		super();

        //setup all classes
        this.config = new Config(this);
        this.explosion = new CreateExplosion(this, config);
        this.fireCannon = new FireCannon(this, config);
        this.aiming = new Aiming(this);
        this.observer = new ProjectileObserver(this);
        this.fakeBlockHandler = new FakeBlockHandler(this);
        this.cannonsAPI = new CannonsAPI(this);

        this.persistenceDatabase = new PersistenceDatabase(this);

        this.blockListener = new BlockListener(this);
        this.playerListener = new PlayerListener(this);
        this.entityListener = new EntityListener(this);
        this.signListener = new SignListener(this);
        this.commands = new Commands(this);

    }

    public static Cannons getPlugin() {
        return (Cannons) Bukkit.getPluginManager().getPlugin("Cannons");
    }

	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);

		// save database on shutdown
		logger.info(getLogPrefix() + "Wait until scheduler is finished");
		while(getPlugin().getPersistenceDatabase().isSaveTaskRunning()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		logger.info(getLogPrefix() + "Scheduler finished");
		persistenceDatabase.saveAllCannons(false);
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		logger.info(getLogPrefix() + "Cannons plugin v" + getPluginDescription().getVersion() + " has been disabled");
	}

	public void onEnable()
	{
        long startTime = System.nanoTime();

		//load some global variables
		pm = getServer().getPluginManager();
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		
		//inform the user if worldedit it missing
		if (!checkWorldEdit())
		{
			//no worldEdit has been loaded. Disable plugin
			console.sendMessage(ChatColor.RED + "[Cannons] Please install WorldEdit, else Cannons can't load.");
			console.sendMessage(ChatColor.RED + "[Cannons] Plugin is now disabled.");
			
			pm.disablePlugin(this);
			return;
		}
        setupEconomy();
		

		try
		{
			pm.registerEvents(blockListener, this);
			pm.registerEvents(playerListener, this);
			pm.registerEvents(entityListener, this);
			pm.registerEvents(signListener, this);
			//call command executer
			getCommand("cannons").setExecutor(commands);

			// load config
			config.loadConfig();

			// Initialize the database
			getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
			{
				public void run()
				{
					try {
						openConnection();
						Statement statement = connection.createStatement();
						statement.close();
						getPlugin().logInfo("Connected to database");
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
					}
					//create the tables for the database in case they don't exist
					persistenceDatabase.createTables();
					// load cannons from database
					persistenceDatabase.loadCannons();
				}
			});


			// setting up Aiming Mode Task
			aiming.initAimingMode();
            // setting up the Teleporter
            observer.setupScheduler();
            fakeBlockHandler.setupScheduler();

			// save cannons
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
			{
				public void run()
				{
					persistenceDatabase.saveAllCannons(true);
				}
			}, 6000L, 6000L);

			Metrics metrics = new Metrics(this, 4048);

            logDebug("Time to enable cannons: " + new DecimalFormat("0.00").format((System.nanoTime() - startTime)/1000000.0) + "ms");

            // Plugin succesfully enabled
            logger.info(getLogPrefix() + "Cannons plugin v" + getPluginDescription().getVersion() + " has been enabled");
		}
		catch (Exception ex)
		{
			// Plugin failed to enable
			logSevere(String.format("[%s v%s] could not be enabled!", getDescription().getName(), getDescription().getVersion()));

			// Print the stack trace of the actual cause
			Throwable t = ex;
			while (t != null)
			{
				if (t.getCause() == null)
				{
					logSevere(String.format("[%s v%s] exception:", getDescription().getName(), getDescription().getVersion()));
					t.printStackTrace();
				}

				t = t.getCause();
			}
		}
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }




	// set up ebean database
	private void openConnection() throws SQLException, ClassNotFoundException
	{
		String driver = getConfig().getString("database.driver", "org.sqlite.JDBC");
		String url = getConfig().getString("database.url", "jdbc:sqlite:{DIR}{NAME}.db");
		String username = getConfig().getString("database.username", "bukkit");
		String password = getConfig().getString("database.password", "walrus");
		//String serializable = getConfig().getString("database.isolation", "SERIALIZABLE");

		url = url.replace("{DIR}{NAME}.db", "plugins/Cannons/Cannons.db");

		if (connection != null && !connection.isClosed()) {
			return;
		}

		synchronized (this) {
			if (connection != null && !connection.isClosed()) {
				return;
			}
			Class.forName(driver);
			connection = DriverManager.getConnection(url, username, password);
		}
    }

	public boolean hasConnection() {
		return this.connection != null;
	}

	public boolean isPluginEnabled()
	{
		return this.isEnabled();
	}

	public final Config getMyConfig()
	{
		return config;
	}

	public void disablePlugin()
	{
		pm.disablePlugin(this);
	}

	private String getLogPrefix()
	{
		return "[" + getPluginDescription().getName() + "] ";
	}

	public void logSevere(String msg)
	{
		//msg = ChatColor.translateAlternateColorCodes('&', msg);
		this.logger.severe(getLogPrefix() + ChatColor.stripColor(msg));
	}
	
	public void logInfo(String msg)
	{
		//msg = ChatColor.translateAlternateColorCodes('&', msg);
		this.logger.info(getLogPrefix() + ChatColor.stripColor(msg));
	}

	public void logDebug(String msg)
	{
		if (config.isDebugMode())
			this.logger.info(getLogPrefix() + ChatColor.stripColor(msg));
	}

	public boolean isDebugMode()
	{
		return config.isDebugMode();
	}

	public void broadcast(String msg)
	{
		this.getServer().broadcastMessage(msg);
	}

	public PluginDescriptionFile getPluginDescription()
	{
		return this.getDescription();
	}
	
	/**
	 * checks if WorldEdit is running
	 * @return true is WorldEdit is running
	 */
	private boolean checkWorldEdit()
	{
		Plugin plug = pm.getPlugin("WorldEdit");
        return plug != null;
    }

    public Connection getConnection(){
		return this.connection;
	}

	public PersistenceDatabase getPersistenceDatabase()
	{
		return persistenceDatabase;
	}

	public CannonManager getCannonManager()
	{
		return this.config.getCannonManager();
	}

	public FireCannon getFireCannon()
	{
		return fireCannon;
	}

	public CreateExplosion getExplosion()
	{
		return explosion;
	}

	public Aiming getAiming()
	{
		return aiming;
	}

	public PlayerListener getPlayerListener()
	{
		return playerListener;
	}

	public SignListener getSignListener()
	{
		return signListener;
	}

	public DesignStorage getDesignStorage()
	{
		return this.config.getDesignStorage();
	}
	
	public CannonDesign getCannonDesign(Cannon cannon)
	{
		return getDesignStorage().getDesign(cannon);
	}
	
	public CannonDesign getCannonDesign(String designId)
	{
		return getDesignStorage().getDesign(designId);
	}

	public ProjectileStorage getProjectileStorage()
	{
		return this.config.getProjectileStorage();
	}

	public Projectile getProjectile(Cannon cannon, ItemHolder materialHolder)
	{
		return ProjectileStorage.getProjectile(cannon, materialHolder);
	}
	
	public Projectile getProjectile(Cannon cannon, ItemStack item)
	{
		return ProjectileStorage.getProjectile(cannon, item);
	}

    public Cannon getCannon(UUID id)
    {
        return CannonManager.getCannon(id);
    }

	public EntityListener getEntityListener()
	{
		return entityListener;
	}
	
	public void sendMessage(Player player, Cannon cannon, MessageEnum message)
	{
		this.config.getUserMessages().sendMessage(message, player, cannon);
	}

    public void sendImpactMessage(Player player, Location impact, boolean canceled)
    {
        this.config.getUserMessages().sendImpactMessage(player, impact, canceled);
    }
	
	public void createCannon(Cannon cannon, boolean saveToDatabase)
	{
		this.getCannonManager().createCannon(cannon, saveToDatabase);
	}

    public ProjectileObserver getProjectileObserver() {
        return observer;
    }

    public ProjectileManager getProjectileManager(){
        return this.config.getProjectileManager();
    }

    public CannonsAPI getCannonsAPI() {
        return cannonsAPI;
    }

    public BlockListener getBlockListener() {
        return blockListener;
    }

    public FakeBlockHandler getFakeBlockHandler() {
        return fakeBlockHandler;
    }

    public Commands getCommandListener() {
        return commands;
    }

    public Economy getEconomy(){
        return this.economy;
    }

	public String getCannonDatabase() {
		return cannonDatabase;
	}

	public String getWhitelistDatabase() {
		return whitelistDatabase;
	}
}
