package at.pavlov.Cannons;

import java.util.logging.Logger;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.alternejtiw.AreaGuard.AreaGuard;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.FFlag;
import com.nitnelave.CreeperHeal.CreeperHeal;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.pandemoneus.obsidianDestroyer.ObsidianDestroyer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import de.tyranus.minecraft.bukkit.guildawards.GuildAwardsPlugin;
import de.tyranus.minecraft.bukkit.guildawards.external.GunnerGuildConnector;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

import at.pavlov.Cannons.MyListener;

public class CannonPlugin extends JavaPlugin {
	public CannonPlugin plugin = this;
	private final Logger logger = Logger.getLogger("Minecraft");
	private Config config;
	private UserMessages userMessages;
	private MyListener myListener;
	PluginManager pm;
	
	//block protection
	private WorldGuardPlugin worldguard;
	private PreciousStones ps;
	private Towny towny;
	private Plugin factions;
	private GriefPrevention griefPrevention;
	private AreaGuard areaGuard;
	
	//creeperHeal to restore blocks
	private CreeperHeal creeperHeal;
	private ObsidianDestroyer obsidianDestroyer;
	
	//cannon guild
	private GuildAwardsPlugin guildAwards;
	
	public CannonPlugin()
	{
		config = new Config(this);
		userMessages = config.getUserMessages();
	}
	
   public void onDisable()
   {
	   logger.info(getLogPrefix() + "Cannons plugin v" + getPluginDescription().getVersion()  + " has been disabled" );
   }
   
   public void onEnable()
   {
	   pm = getServer().getPluginManager();
   	   pm.registerEvents(new MyListener(this),this);
   	   
   	   //load protection hooks
   	   worldguard = getWorldGuard();
   	   ps = getPreciousStones();
   	   towny = getTowny();
   	   factions = getFactions();
   	   griefPrevention = getGriefPrevention();
   	   areaGuard = getAreaGuard();
   	   
   	   //obsidian Breaker
   	   creeperHeal = getCreeperHeal();
   	   obsidianDestroyer = getObsidianDestroyer();
   	   
   	   //cannon guild
   	   guildAwards = getGuildAwards();
   	   
   	   //load config
	   config.loadConfig();
	   logger.info(getLogPrefix() + "Cannons plugin v" + getPluginDescription().getVersion()  + " has been enabled" );
	   
	   //clean up old obsolete entries
	   getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() 
	   {
		    public void run() 
		    {
		    	myListener.CleanUpEntries();
		    }
		}, 6000L, 6000L);
   }
  
	public boolean isPluginEnabled() 
	{
		return plugin.isEnabled();
	}

	public final CannonPlugin xgetPlugin() 
	{
		return this;
	}
	
	public final Config getmyConfig() 
	{
		return config;
	}

	public void disablePlugin() 
	{
		plugin.disablePlugin();
	}
	
	public void setListener(MyListener listener)
	{
		this.myListener=listener;
	}
	
	private String getLogPrefix() 
	{
		return "[" + getPluginDescription().getName() + "] ";
	}

	public void logSevere(String msg) 
	{
		this.logger.severe(getLogPrefix() + msg);
	}
	
	public void broadcast(String msg) 
	{
		this.getServer().broadcastMessage(msg);
	}

	public PluginDescriptionFile getPluginDescription() 
	{
		return this.getDescription();
	}
		
	//##################### getWorldGuard ###################################
	private WorldGuardPlugin getWorldGuard() 
	{
		Plugin plugin = pm.getPlugin("WorldGuard");
		 
		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) 
		{
			return null; // Maybe you want throw an exception instead
		}
		    
		logger.info(getLogPrefix() + "Worldguard hook loaded");
		return (WorldGuardPlugin) plugin;
	}	
	
	
	//##################### getPreciousStones ###################################
	private PreciousStones getPreciousStones() 
	{
		PreciousStones ps = null;
		Plugin plug = pm.getPlugin("PreciousStones");
		//PreciousStones may not be loaded
	    if (plug != null) 
	    {
	        ps = ((PreciousStones) plug);
			logger.info(getLogPrefix() + "PreciousStones hook loaded");
	    }
		return ps;
	}
	
	//##################### getTowny ###################################
	private Towny getTowny() 
	{
		Towny towny = null;
		Plugin plug = pm.getPlugin("Towny");
		//Towny may not be loaded
	    if (plug != null) 
	    {
	    	towny = ((Towny) plug);
			logger.info(getLogPrefix() + "Towny hook loaded");
	    }
		return towny;
	}
	
	//##################### getFactions ###################################
	private Plugin getFactions() 
	{
		Plugin plug = pm.getPlugin("Factions");	
		//Factions may not be loaded
	    if (plug != null) 
	    {
			logger.info(getLogPrefix() + "Factions hook loaded");
	    }

		return plug;
	}
	
	//##################### getGriefPrevention ###################################
	private GriefPrevention getGriefPrevention() 
	{
		GriefPrevention grief = null;
		Plugin plug = pm.getPlugin("GriefPrevention");	
		//GriefPrevention may not be loaded
	    if (plug != null) 
	    {
	    	grief = ((GriefPrevention) plug);
			logger.info(getLogPrefix() + "GriefPrevention hook loaded");
	    }

		return grief;
	}
	
	//##################### getGriefPrevention ###################################
	private AreaGuard getAreaGuard() 
	{
		AreaGuard areaGuard = null;
		Plugin plug = pm.getPlugin("AreaGuard");	
		//AreaGuard may not be loaded
	    if (plug != null) 
	    {
	    	areaGuard = ((AreaGuard) plug);
			logger.info(getLogPrefix() + "AreaGuard hook loaded");
	    }

		return areaGuard;
	}
	
	//##################### checkPermission #################################
	public boolean checkPermission(Location loc)
	{
		//false will cancel explosions
		
		//no enable hooks enabled -> no explosions are blocked
		if (config.enableProtectionHook == false) return true;
		
		//Worldguard
		if (worldguard != null)
		{
			Vector pt = toVector(loc); // This also takes a location
			RegionManager regionManager = worldguard.getRegionManager(loc.getWorld());
			if (regionManager != null)
			{
				ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
				if (set.allows(DefaultFlag.TNT) == false) return false;
			}
		}
		//PreciousStones
	    if (ps != null)
	    {
	        if (ps.getForceFieldManager().hasSourceField(loc, FieldFlag.PREVENT_EXPLOSIONS) == true) return false;
	    }
		//Towny
	    if (towny != null)
	    {
	    	//towny.getTownyUniverse();
			TownBlock townblock = TownyUniverse.getTownBlock(loc);
	    	if (townblock != null){
	    		if (townblock.getPermissions().explosion == false) return false;
	    	}
	       
	    }
		//Factions
	    if (factions != null)
	    {	
	    	FLocation floc = new FLocation (loc);
			Faction faction = Board.getFactionAt(floc);
			if (faction != null)
			{//peaceful, safezones -> no tnt allowed
				// || faction.getFlag(FFlag.PEACEFUL) == true
				if (faction.getFlag(FFlag.EXPLOSIONS) == false) return false;
			}
       
	    }
		//GriefPrevention
	    if (griefPrevention != null)
	    {
	    	Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
	    	if (claim != null) return false;
       
	    }
		//AreaGuard
	    if (areaGuard != null)
	    {
	    	//areaGuard.
	    	//if (claim != null) return false;
       
	    }
		return true;
	}
	
	
	//##################### getCreeperHeal ###################################
	private CreeperHeal getCreeperHeal() 
	{
		CreeperHeal creeperHeal = null;
		Plugin plug = pm.getPlugin("CreeperHeal");	
		//CreeperHeal may not be loaded
	    if (plug != null) 
	    {
	    	creeperHeal = ((CreeperHeal) plug);
			logger.info(getLogPrefix() + "CreeperHeal hook loaded");
	    }
		return creeperHeal;
	}
	
	//##################### isCreeperHealLoaded ###################################
	public boolean isCreeperHealLoaded() 
	{
		if (creeperHeal != null)
		{
			return true;
		}

		return false;
	}
	
	//##################### getObsidianDestroyer ###################################
	private ObsidianDestroyer getObsidianDestroyer() 
	{
		ObsidianDestroyer obsidianDestroyer = null;
		Plugin plug = pm.getPlugin("ObsidianDestroyer");	
		//ObsidianDestroyer may not be loaded
	    if (plug != null) 
	    {
	    	obsidianDestroyer = ((ObsidianDestroyer) plug);
			logger.info(getLogPrefix() + "ObsidianDestroyer hook loaded");
	    }
		return obsidianDestroyer;
	}
	
	//##################### BlockBreakPluginLoaded #################################
	public boolean BlockBreakPluginLoaded()
	{
		if (creeperHeal != null) return true;
		if (obsidianDestroyer != null) return true;
		return false;
	}
	
	//##################### getGuildAwards ###################################
	private GuildAwardsPlugin getGuildAwards() 
	{
		GuildAwardsPlugin guildAwards = null;
		Plugin plug = pm.getPlugin("GuildAwards");	
		//GuildAwards may not be loaded
	    if (plug != null) 
	    {
	    	guildAwards = ((GuildAwardsPlugin) plug);
			logger.info(getLogPrefix() + "GuildAwards hook loaded");
	    }
		return guildAwards;
	}
	
	//##################### getCannonGuildHandler #################################
	public GunnerGuildConnector getCannonGuildHandler()
	{
		if (guildAwards != null)
		{
			return guildAwards.getExternalInterface().getGunnerGuildConnector();
		}
		return null;
	}
   
	//########################### OnCommand #########################################################
	  @Override
	    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	    {
	    
		  Player player = null;
		  if (sender instanceof Player) 
		  {
			  player = (Player) sender;
		  }
	         
	       if (cmd.getName().equalsIgnoreCase("cannons")) 
	       {
	        		if (player == null) {
	        			sender.sendMessage("this command can only be run by a player!");
	        		} 
	        		else {
	        			if (args.length >= 1) 
	        			{
        					if (args[0].equalsIgnoreCase("build") && sender.hasPermission("cannons.player.command")) 
        					{
        						//how to build a cannon
        						sendMessage(userMessages.HelpBuild,sender, ChatColor.GREEN);
        					}
        					else if (args[0].equalsIgnoreCase("fire") && sender.hasPermission("cannons.player.command"))
        					{
        						//how to fire
        						sendMessage(userMessages.HelpFire,sender,ChatColor.GREEN);
        					}
        					else if (args[0].equalsIgnoreCase("adjust") && sender.hasPermission("cannons.player.command"))
        					{
        						//how to adjust
        						sendMessage(userMessages.HelpAdjust,sender,ChatColor.GREEN);
        					}
        					else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("cannons.admin.reload"))
        					{
        						//reload config
        						config.loadConfig();
        						sendMessage("Cannons config loaded ",sender,ChatColor.GREEN);
        					}
        					else 
        					{
	        					//display help
	        					sendMessage(userMessages.HelpText,sender,ChatColor.GREEN);
	        				}
        				}
        				else 
        				{
        					//display help
        					sendMessage(userMessages.HelpText,sender,ChatColor.GREEN);
        				}
	        		}
	        		return true;
	        	}	   
	       return false;
	    }
	  
	  //################################# SENDMESSAGE ####################################
	  public void sendMessage(String string, CommandSender player, ChatColor chatcolor)
	  {
		  String[] message = string.split("\n "); // Split everytime the "\n" into a new array value
			  
		  for(int x=0 ; x<message.length ; x++) 
		  {
			  player.sendMessage(chatcolor + message[x]); // Send each argument in the message
		  }
			  
	  }
}
