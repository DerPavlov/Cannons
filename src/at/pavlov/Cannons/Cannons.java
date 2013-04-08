package at.pavlov.Cannons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.nitnelave.CreeperHeal.CreeperHeal;
import com.pandemoneus.obsidianDestroyer.ObsidianDestroyer;

import de.tyranus.minecraft.bukkit.guildawards.GuildAwardsPlugin;
import de.tyranus.minecraft.bukkit.guildawards.external.GunnerGuildConnector;

import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.DesignStorage;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.ProjectileStorage;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.CannonBean;
import at.pavlov.Cannons.dao.MyDatabase;
import at.pavlov.Cannons.dao.PersistenceDatabase;
import at.pavlov.Cannons.listener.Commands;
import at.pavlov.Cannons.listener.EntityListener;
import at.pavlov.Cannons.listener.PlayerListener;
import at.pavlov.Cannons.listener.SignListener;
import at.pavlov.Cannons.mcstats.Metrics;
import at.pavlov.Cannons.projectile.Projectile;

public class Cannons extends JavaPlugin
{
	PluginManager pm;
	private final Logger logger = Logger.getLogger("Minecraft");

	private Config config;
	private DesignStorage designStorage;
	private ProjectileStorage projectileStorage;
	private UserMessages userMessages;
	private CannonManager cannonManager;
	private FireCannon fireCannon;
	private CreateExplosion explosion;
	private CalcAngle calcAngle;
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

	// cannon guild
	private GuildAwardsPlugin guildAwards;

	public Cannons()
	{

		this.config = new Config(this);
		this.designStorage = this.config.getDesignStorage();
		this.projectileStorage = this.config.getProjectileStorage();
		this.userMessages = this.config.getUserMessages();
		
		this.cannonManager = new CannonManager(this, userMessages, config);
		this.explosion = new CreateExplosion(this, config);
		this.fireCannon = new FireCannon(this, config, userMessages, explosion);
		this.calcAngle = new CalcAngle(this, userMessages, config);
		
		this.persistenceDatabase = new PersistenceDatabase(this);

		this.playerListener = new PlayerListener(this);
		this.entityListener = new EntityListener(this);
		this.signListener = new SignListener(this);	
		this.commands = new Commands(this);
		


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
		try
		{
			pm = getServer().getPluginManager();
			pm.registerEvents(playerListener, this);
			pm.registerEvents(entityListener, this);
			pm.registerEvents(signListener, this);
			//call command executer
			getCommand("cannons").setExecutor(commands);

			// obsidian Breaker
			creeperHeal = getCreeperHeal();
			obsidianDestroyer = getObsidianDestroyer();

			// cannon guild
			guildAwards = getGuildAwards();

			// load config
			config.loadConfig();
			logger.info(getLogPrefix() + "Cannons plugin v" + getPluginDescription().getVersion() + " has been enabled");

			// Initialize the database
			initializeDatabase();

			// load cannons from database
			persistenceDatabase.loadCannons();

			// setting up Aiming Mode Task
			calcAngle.initAimingMode();

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
			//System.out.print(String.format("[%s v%s] has been succesfully enabled!", getDescription().getName(), getDescription().getVersion()));


		}
		catch (Exception ex)
		{
			// Plugin failed to enable
			System.out.print(String.format("[%s v%s] could not be enabled!", getDescription().getName(), getDescription().getVersion()));

			// Print the stack trace of the actual cause
			Throwable t = ex;
			while (t != null)
			{
				if (t.getCause() == null)
				{
					System.out.println(String.format("[%s v%s] exception:", getDescription().getName(), getDescription().getVersion()));
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
				config.getBoolean("database.rebuild", false)
				);
		//.Formatter:on
		
		config.set("database.rebuild", false);
		saveConfig();
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

	public final Cannons xgetPlugin()
	{
		return this;
	}

	public final Config getmyConfig()
	{
		return config;
	}

	public void disablePlugin()
	{
		this.disablePlugin();
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
		//msg = ChatColor.translateAlternateColorCodes('&', msg);
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
	 * returns the creeperheal handle
	 * @return
	 */
	private CreeperHeal getCreeperHeal()
	{
		CreeperHeal creeperHeal = null;
		Plugin plug = pm.getPlugin("CreeperHeal");
		// CreeperHeal may not be loaded
		if (plug != null)
		{
			creeperHeal = ((CreeperHeal) plug);
			logger.info(getLogPrefix() + "CreeperHeal hook loaded");
		}
		return creeperHeal;
	}

	/**
	 * returns true if creeperHeal is loaded
	 * @return
	 */
	public boolean isCreeperHealLoaded()
	{
		if (creeperHeal != null) { return true; }

		return false;
	}

	/**
	 * returns the handle of obsidianDestroyer
	 * @return
	 */
	private ObsidianDestroyer getObsidianDestroyer()
	{
		ObsidianDestroyer obsidianDestroyer = null;
		Plugin plug = pm.getPlugin("ObsidianDestroyer");
		// ObsidianDestroyer may not be loaded
		if (plug != null)
		{
			obsidianDestroyer = ((ObsidianDestroyer) plug);
			logger.info(getLogPrefix() + "ObsidianDestroyer hook loaded");
		}
		return obsidianDestroyer;
	}

	/**
	 * return true if creeperHeal or obsidianDestroyer is loaded
	 * @return
	 */
	public boolean BlockBreakPluginLoaded()
	{
		if (creeperHeal != null)
			return true;
		if (obsidianDestroyer != null)
			return true;
		return false;
	}

	/**
	 * loads the guildawards hook
	 * @return
	 */
	private GuildAwardsPlugin getGuildAwards()
	{
		GuildAwardsPlugin guildAwards = null;
		Plugin plug = pm.getPlugin("GuildAwards");
		// GuildAwards may not be loaded
		if (plug != null)
		{
			guildAwards = ((GuildAwardsPlugin) plug);
			logger.info(getLogPrefix() + "GuildAwards hook loaded");
		}
		return guildAwards;
	}

	/**
	 * returns the guildAwardsConnector
	 * @return
	 */
	public GunnerGuildConnector getCannonGuildHandler()
	{
		if (guildAwards != null) { return guildAwards.getExternalInterface().getGunnerGuildConnector(); }
		return null;
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
	
	public CannonDesign getCannonDesign(int designId)
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
	
	public Projectile getProjectile(int id, int data)
	{
		return this.projectileStorage.getProjectile(id, data);
	}
	
	public Projectile getProjectile(ItemStack item)
	{
		return this.projectileStorage.getProjectile(item);
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
	
	public void createCannon(Cannon cannon)
	{
		this.getCannonManager().createCannon(cannon);
	}

}
