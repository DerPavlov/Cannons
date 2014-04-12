package at.pavlov.cannons;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import at.pavlov.cannons.API.CannonsAPI;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.config.*;
import at.pavlov.cannons.listener.*;
import at.pavlov.cannons.projectile.ProjectileManager;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.scheduler.FakeBlockHandler;
import at.pavlov.cannons.scheduler.ProjectileObserver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.dao.CannonBean;
import at.pavlov.cannons.dao.MyDatabase;
import at.pavlov.cannons.dao.PersistenceDatabase;
import at.pavlov.cannons.mcstats.Metrics;
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
	private final Commands commands;

    private final CannonsAPI cannonsAPI;
	
	//Listener
    private final BlockListener blockListener;
	private final PlayerListener playerListener;
	private final EntityListener entityListener;
	private final SignListener signListener;
	
	// database
	private final PersistenceDatabase persistenceDatabase;
	private MyDatabase database;



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
		persistenceDatabase.saveAllCannons();

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
                    initializeDatabase();
                    // load cannons from database
                    persistenceDatabase.loadCannonsAsync();
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
					persistenceDatabase.saveAllCannonsAsync();
				}
			}, 6000L, 6000L);
			
			try {
			    Metrics metrics = new Metrics(this);
			    metrics.start();
			} catch (IOException e) {
			    // Failed to submit the stats :-(
			}

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

	// set up ebean database
	private void initializeDatabase()
	{
        Configuration config = getConfig();

		database = new MyDatabase(this)
		{
			protected java.util.List<Class<?>> getDatabaseClasses()
			{
				List<Class<?>> list = new ArrayList<Class<?>>();
				list.add(CannonBean.class);

				return list;
			}
        };
		//.Formatter:off
		database.initializeDatabase(config.getString("database.driver", "org.sqlite.JDBC"),
				config.getString("database.url", "jdbc:sqlite:{DIR}{NAME}.db"), 
				config.getString("database.username", "bukkit"), 
				config.getString("database.password", "walrus"),
				config.getString("database.isolation", "SERIALIZABLE"), 
				getMyConfig().isDebugMode(),
				false//config.getBoolean("database.rebuild", false)
				);
		//.Formatter:on
		
		//config.set("database.rebuild", false);
		//saveConfig();
    }

	@Override
	public EbeanServer getDatabase()
	{
		return database.getDatabase();
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
		this.logger.severe(ChatColor.RED + getLogPrefix() + ChatColor.stripColor(msg));
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

	public Projectile getProjectile(Cannon cannon, int id, int data)
	{
		return this.getProjectileStorage().getProjectile(cannon, id, data);
	}
	
	public Projectile getProjectile(Cannon cannon, ItemStack item)
	{
		return this.getProjectileStorage().getProjectile(cannon, item);
	}

	public EntityListener getEntityListener()
	{
		return entityListener;
	}
	
	public void displayMessage(Player player, Cannon cannon, MessageEnum message)
	{
		this.config.getUserMessages().displayMessage(player, cannon, message);
	}

    public void displayImpactMessage(Player player, Location impact, boolean notCanceled)
    {
        this.config.getUserMessages().displayImpactMessage(player, impact, notCanceled);
    }
	
	public void createCannon(Cannon cannon)
	{
		this.getCannonManager().createCannon(cannon);
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
}
