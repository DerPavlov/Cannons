package at.pavlov.cannons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import at.pavlov.cannons.scheduler.CalcAngle;
import at.pavlov.cannons.scheduler.Teleporter;
import io.snw.obsidiandestroyer.ObsidianDestroyer;
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
import com.nitnelave.CreeperHeal.CreeperHeal;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.DesignStorage;
import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.config.ProjectileStorage;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.dao.CannonBean;
import at.pavlov.cannons.dao.MyDatabase;
import at.pavlov.cannons.dao.PersistenceDatabase;
import at.pavlov.cannons.listener.Commands;
import at.pavlov.cannons.listener.EntityListener;
import at.pavlov.cannons.listener.PlayerListener;
import at.pavlov.cannons.listener.SignListener;
import at.pavlov.cannons.mcstats.Metrics;
import at.pavlov.cannons.projectile.Projectile;

public class Cannons extends JavaPlugin
{
	PluginManager pm;
	private final Logger logger = Logger.getLogger("Minecraft");
	private ConsoleCommandSender console;

	private Config config;
	private DesignStorage designStorage;
	private ProjectileStorage projectileStorage;
	private UserMessages userMessages;
	private CannonManager cannonManager;
	private FireCannon fireCannon;
	private CreateExplosion explosion;
	private CalcAngle calcAngle;
    private Teleporter teleporter;
	private Commands commands;
	
	//Events
	private PlayerListener playerListener;
	private EntityListener entityListener;
	private SignListener signListener;
	
	// database
	private PersistenceDatabase persistenceDatabase;
	private MyDatabase database;

	// creeperHeal to restore blocks
	private CreeperHeal creeperHeal;
	private ObsidianDestroyer obsidianDestroyer;


	public Cannons()
	{
		super();
	}

	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		
		// save database on shutdown
		if (persistenceDatabase != null)
			persistenceDatabase.saveAllCannons();
		

		logger.info(getLogPrefix() + "Cannons plugin v" + getPluginDescription().getVersion() + " has been disabled");
	}

	public void onEnable()
	{
		//load some global variables
		pm = getServer().getPluginManager();
		console = Bukkit.getServer().getConsoleSender();
		
		//inform the user if worldedit it missing
		if (!checkWorldEdit())
		{
			//no worldEdit has been loaded. Disable plugin
			console.sendMessage(ChatColor.RED + "[Cannons] Please install WorldEdit, else Cannons can't load.");
			console.sendMessage(ChatColor.RED + "[Cannons] Plugin is now disabled.");
			
			pm.disablePlugin(this);
			return;
		}
		
		//setup all classes
		this.config = new Config(this);
		this.designStorage = this.config.getDesignStorage();
		this.projectileStorage = this.config.getProjectileStorage();
		this.userMessages = this.config.getUserMessages();
		
		this.cannonManager = new CannonManager(this, userMessages, config);
		this.explosion = new CreateExplosion(this, config);
		this.fireCannon = new FireCannon(this, config, explosion);
		this.calcAngle = new CalcAngle(this, userMessages, config);
        this.teleporter = new Teleporter(this);
		
		this.persistenceDatabase = new PersistenceDatabase(this);

		this.playerListener = new PlayerListener(this);
		this.entityListener = new EntityListener(this);
		this.signListener = new SignListener(this);	
		this.commands = new Commands(this);		
		
		try
		{
			
			pm.registerEvents(playerListener, this);
			pm.registerEvents(entityListener, this);
			pm.registerEvents(signListener, this);
			//call command executer
			getCommand("cannons").setExecutor(commands);

			// load config
			config.loadConfig();

			// Initialize the database
			initializeDatabase();

			// load cannons from database
			persistenceDatabase.loadCannons();

			// setting up Aiming Mode Task
			calcAngle.initAimingMode();
            // setting up the Teleporter
            teleporter.setupScheduler();

			// save cannons
			//.Formatter:off
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
			{
				public void run()
				{
					persistenceDatabase.saveAllCannons();
				}
			}, 6000L, 6000L);
			//.Formatter:on
			
			try {
			    Metrics metrics = new Metrics(this);
			    metrics.start();
			} catch (IOException e) {
			    // Failed to submit the stats :-(
			}

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
			};
		};
		//.Formatter:off
		database.initializeDatabase(config.getString("database.driver", "org.sqlite.JDBC"),
				config.getString("database.url", "jdbc:sqlite:{DIR}{NAME}.db"), 
				config.getString("database.username", "bukkit"), 
				config.getString("database.password", "walrus"),
				config.getString("database.isolation", "SERIALIZABLE"), 
				config.getBoolean("database.logging", false),
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

	public final Config getmyConfig()
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
		this.logger.severe(getLogPrefix() + msg);
	}
	
	public void logInfo(String msg)
	{
		//msg = ChatColor.translateAlternateColorCodes('&', msg);
		this.logger.info(getLogPrefix() + msg);
	}

	public void logDebug(String msg)
	{
		if (config.isDebugMode())
			this.logger.info(getLogPrefix() + msg);
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
	 * checks if worldEdit is running
	 * @return
	 */
	private boolean checkWorldEdit()
	{
		Plugin plug = pm.getPlugin("WorldEdit");
		// CreeperHeal may not be loaded
		if (plug == null)
		{
			return false;
		}
		return true;
	}

	public PersistenceDatabase getPersistenceDatabase()
	{
		return persistenceDatabase;
	}

	public CannonManager getCannonManager()
	{
		return cannonManager;
	}

	public FireCannon getFireCannon()
	{
		return fireCannon;
	}

	public CreateExplosion getExplosion()
	{
		return explosion;
	}

	public CalcAngle getCalcAngle()
	{
		return calcAngle;
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
		return designStorage;
	}

	public void setDesignStorage(DesignStorage designStorage)
	{
		this.designStorage = designStorage;
	}
	
	public CannonDesign getCannonDesign(Cannon cannon)
	{
		return designStorage.getDesign(cannon);
	}
	
	public CannonDesign getCannonDesign(String designId)
	{
		return designStorage.getDesign(designId);
	}

	public ProjectileStorage getProjectileStorage()
	{
		return projectileStorage;
	}

	public void setProjectileStorage(ProjectileStorage projectileStorage)
	{
		this.projectileStorage = projectileStorage;
	}
	
	public Projectile getProjectile(Cannon cannon, int id, int data)
	{
		return this.projectileStorage.getProjectile(cannon, id, data);
	}
	
	public Projectile getProjectile(Cannon cannon, ItemStack item)
	{
		return this.projectileStorage.getProjectile(cannon, item);
	}

	public EntityListener getEntityListener()
	{
		return entityListener;
	}

	public void setEntityListener(EntityListener entityListener)
	{
		this.entityListener = entityListener;
	}
	
	public void displayMessage(Player player, MessageEnum message, Cannon cannon)
	{
		this.userMessages.displayMessage(player, message, cannon);
	}

    public void displayImpactMessage(Player player, Location impact, boolean canceled)
    {
        this.userMessages.displayImpactMessage(player, impact, canceled);
    }
	
	public void createCannon(Cannon cannon)
	{
		this.getCannonManager().createCannon(cannon);
	}

    public Teleporter getTeleporter() {
        return teleporter;
    }

    public void setTeleporter(Teleporter teleporter) {
        this.teleporter = teleporter;
    }
}
