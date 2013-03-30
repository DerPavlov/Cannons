package at.pavlov.Cannons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.nitnelave.CreeperHeal.CreeperHeal;
import com.pandemoneus.obsidianDestroyer.ObsidianDestroyer;

import de.tyranus.minecraft.bukkit.guildawards.GuildAwardsPlugin;
import de.tyranus.minecraft.bukkit.guildawards.external.GunnerGuildConnector;

import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.CannonBean;
import at.pavlov.Cannons.dao.MyDatabase;
import at.pavlov.Cannons.dao.PersistenceDatabase;
import at.pavlov.Cannons.listener.Commands;
import at.pavlov.Cannons.listener.PlayerListener;
import at.pavlov.Cannons.listener.SignListener;
import at.pavlov.Cannons.mcstats.Metrics;
import at.pavlov.Cannons.utils.InventoryManagement;

public class Cannons extends JavaPlugin
{
	PluginManager pm;
	private final Logger logger = Logger.getLogger("Minecraft");

	private Config config;
	private UserMessages userMessages;
	private CannonManager cannonManager;
	private InventoryManagement invManage;
	private FireCannon fireCannon;
	private CreateExplosion explosion;
	private CalcAngle calcAngle;
	private Commands commands;
	
	//Events
	private PlayerListener playerListener;
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

		this.invManage = new InventoryManagement();
		this.config = new Config(this);
		this.userMessages = this.config.getUserMessages();
		this.cannonManager = new CannonManager(this, userMessages, config);
		this.explosion = new CreateExplosion(this, config);
		this.fireCannon = new FireCannon(this, config, userMessages, invManage, explosion);
		this.calcAngle = new CalcAngle(this, userMessages, config);
		
		this.persistenceDatabase = new PersistenceDatabase(this);

		this.playerListener = new PlayerListener(this);
		this.signListener = new SignListener(this);	
		this.commands = new Commands(this);
		


	}

	public void onDisable()
	{
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
		this.logger.severe(getLogPrefix() + msg);
	}
	
	public void logInfo(String msg)
	{
		this.logger.info(getLogPrefix() + msg);
	}

	public void logDebug(String msg)
	{
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

	// ##################### getWorldGuard ###################################
	/*
	 * private WorldGuardPlugin getWorldGuard() { Plugin plugin =
	 * pm.getPlugin("WorldGuard");
	 * 
	 * // WorldGuard may not be loaded if (plugin == null || !(plugin instanceof
	 * WorldGuardPlugin)) { return null; // Maybe you want throw an exception
	 * instead }
	 * 
	 * logger.info(getLogPrefix() + "Worldguard hook loaded"); return
	 * (WorldGuardPlugin) plugin; }
	 */

	// ##################### getCreeperHeal ###################################
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

	// ##################### isCreeperHealLoaded ##############################
	public boolean isCreeperHealLoaded()
	{
		if (creeperHeal != null) { return true; }

		return false;
	}

	// ##################### getObsidianDestroyer #############################
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

	// ##################### BlockBreakPluginLoaded ###########################
	public boolean BlockBreakPluginLoaded()
	{
		if (creeperHeal != null)
			return true;
		if (obsidianDestroyer != null)
			return true;
		return false;
	}

	// ##################### getGuildAwards ###################################
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

	// ##################### getCannonGuildHandler #############################
	public GunnerGuildConnector getCannonGuildHandler()
	{
		if (guildAwards != null) { return guildAwards.getExternalInterface().getGunnerGuildConnector(); }
		return null;
	}

	// ################################# SENDMESSAGE #########################
	public void sendMessage(String string, CommandSender player, ChatColor chatcolor)
	{
		String[] message = string.split("\n "); // Split everytime the "\n" into
												// a new array value

		for (int x = 0; x < message.length; x++)
		{
			player.sendMessage(chatcolor + message[x]); // Send each argument in
														// the message
		}

	}

	public PersistenceDatabase getPersistenceDatabase()
	{
		return persistenceDatabase;
	}

	public CannonManager getCannonManager()
	{
		return cannonManager;
	}

	public InventoryManagement getInvManage()
	{
		return invManage;
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

}
