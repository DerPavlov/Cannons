package at.pavlov.cannons.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import at.pavlov.cannons.Enum.MessageEnum;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.utils.CannonsUtil;

public class UserMessages {
	private FileConfiguration customLanguage = null;
	private File customLanguageFile = null;
	
	
	private final HashMap<String, String> messageMap = new HashMap<String, String>();


	private final Cannons plugin;
	
	public UserMessages(Cannons plugin){
		this.plugin = plugin;
	}
		 
	public void loadLanguage()
	{
		this.loadCustom("localization");
		
		//copy german language
		File localizationGerman = new File(plugin.getDataFolder(), "localization/localization_german.yml");

        localizationGerman.getParentFile().mkdirs();
		if (!localizationGerman.exists())
		{
			CannonsUtil.copyFile(plugin.getResource("localization/localization_german.yml"), localizationGerman);
		}
		//copy english language
		File localizationEnglish = new File(plugin.getDataFolder(), "localization/localization_english.yml");
		if (!localizationEnglish.exists())
		{
			CannonsUtil.copyFile(plugin.getResource("localization/localization.yml"), localizationEnglish);
		}
	}
		
	/**
	 * load the localization file from the disk
	 * @param filename - name of the file
	 */
	private void loadCustom(String filename)
	{
		reloadcustomLanguage(filename);
		customLanguage.options().copyDefaults(true);
		saveCustomLanguage();
		
		//load all messages
		for (MessageEnum keyEnum : MessageEnum.values())
		{
			String key = keyEnum.getString();
			String entry = getEntry(key);
			if (entry != null && !entry.equals(""))
			{
				messageMap.put(key, entry);
			}
			else
			{
				plugin.logSevere("Missing entry " + key + " in the localization file");
				messageMap.put(key, "Missing entry");
			}
		}
	}
		
	String getEntry(String key)
	{
		String entry = customLanguage.getString(key);
		String replace;
		
		if (entry == null)
		{
			entry = "missing string: " + key;
			plugin.logSevere("Missing string " + key + " in localization file: ");
		}
		
		
		//replace red color
		replace = "" + ChatColor.RED;
		entry = entry.replace("ChatColor.RED ", replace);
		entry = entry.replace("RED ", replace);
		//replace green color
		replace = "&A";
		entry = entry.replace("ChatColor.GREEN ", replace);
		entry = entry.replace("GREEN ", replace);
		//replace yellow color
		replace = "&E";
		entry = entry.replace("ChatColor.YELLOW ", replace);
		entry = entry.replace("YELLOW ", replace);
		//replace gold color
		replace = "&6";
		entry = entry.replace("ChatColor.GOLD ", replace);
		entry = entry.replace("GOLD ", replace);
		entry = ChatColor.translateAlternateColorCodes('&', entry);
		
		//replace new line
		replace = "\n ";
		entry =  entry.replace("NEWLINE ", replace);
		//plugin.logDebug(entry);
		return entry;
	}



	private void reloadcustomLanguage(String filename) 
	{
	    if (customLanguageFile == null) 
	    {
	    	customLanguageFile = new File(getDataFolder(), filename + ".yml");
	    }
	    customLanguage = YamlConfiguration.loadConfiguration(customLanguageFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = plugin.getResource("localization/" + filename + ".yml");
	    if (defConfigStream != null) 
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customLanguage.setDefaults(defConfig);
	    }
	}


	private final String getDataFolder()
	{
		return "plugins/Cannons/localization/";
	}


	private void saveCustomLanguage()
	{
	    if (customLanguage == null || customLanguageFile == null) 
	    {
	    return;
	    }
	    try 
	    {
	        customLanguage.save(customLanguageFile);
	    } catch (IOException ex) 
	    {
	        plugin.logSevere("Could not save config to " + customLanguageFile);
	    }
	}

	/**
	 * sends a message to the player
	 * @param player
	 * @param messageEnum
	 * @param cannon
	 */
	public void displayMessage(String player, MessageEnum messageEnum, Cannon cannon)
	{
		//no player no message
		if (player != null)
		{
			displayMessage(Bukkit.getPlayer(player), cannon, messageEnum);
		}
	}
	
	/**
	 * sends a message to the player
	 * @param player
	 * @param messageEnum
	 */
	public void displayMessage(Player player, MessageEnum messageEnum)
	{
		displayMessage(player, null, messageEnum);
	}
	
	/**
	 * sends a message to the player
	 * @param player
	 * @param messageEnum
	 * @param cannon
	 */
	public void displayMessage(Player player, Cannon cannon, MessageEnum messageEnum)
	{
		//no player no message
		if (player == null) return;
		if (messageEnum == null) return;
		
		//get message from map
		String message = getMessage(messageEnum, cannon, player);
		
		//send message to player
		sendMessage(message, player);
	}

    public void displayImpactMessage(Player player, Location impact, boolean notCanceled)
    {
        //no player no message
        if (player == null)
            return;
        //no permission no message
        if (!player.hasPermission("cannons.player.impactMessage"))
            return;

        Location playerLoc = player.getLocation();

        String message;
        MessageEnum messageEnum;

        if (notCanceled)
        {
            //the projectile exploded
            messageEnum = MessageEnum.ProjectileExplosion;
        }
        else
        {
            //the explosion was canceled
            messageEnum = MessageEnum.ProjectileCanceled;
        }

        message = messageMap.get(messageEnum.getString());

        if (message == null)
        {
            plugin.logSevere("No " + messageEnum.getString() + " in localization file");
            return;
        }
        //if the message is something like this Explosion: '' it will pass quietly
        if (message.isEmpty())
        {
            return;
        }
        //replace tags
        message = message.replace("IMPACT_X", Integer.toString(impact.getBlockX()));
        message = message.replace("IMPACT_Y", Integer.toString(impact.getBlockY()));
        message = message.replace("IMPACT_Z", Integer.toString(impact.getBlockZ()));
        message = message.replace("IMPACT_DISTANCE", Long.toString(Math.round(impact.distance(playerLoc))));
        message = message.replace("IMPACT_YDIFF", Integer.toString(impact.getBlockY() - playerLoc.getBlockY()));

        if (message != null)
            sendMessage(message, player);
    }
	
	/**
	 * returns the message from the Map
	 * @param messageEnum
	 * @param cannon
	 * @param player
	 * @return
	 */
    String getMessage(MessageEnum messageEnum, Cannon cannon, Player player)
	{
		//no message
		if (messageEnum == null) return null;
	
		String message = messageMap.get(messageEnum.getString());
		
		//return if message was not found
		if (message == null)
		{
			plugin.logSevere("Message " + messageEnum.getString() + " not found.");
			return null;
		}
        //if the message is something like this Explosion: '' it will pass quietly
        if (message.isEmpty())
        {
            return null;
        }
		
		if (cannon != null)
		{
			//replace the loaded gunpowder
            message = message.replace("MAX_GUNPOWDER", Integer.toString(cannon.getCannonDesign().getMaxLoadableGunpowder_Normal()));
			message = message.replace("GUNPOWDER", Integer.toString(cannon.getLoadedGunpowder()));
			//replace the loaded projectile
			if (cannon.getLoadedProjectile()!=null)
				message = message.replace("PROJECTILE", cannon.getLoadedProjectile().getProjectileName());
			//replace the horizontal angle
			message = message.replace("HDEGREE", String.format("%.1f", cannon.getHorizontalAngle()));
			//replace the vertical angle
			message = message.replace("VDEGREE", String.format("%.1f", cannon.getVerticalAngle()));
            //replace cannon temperature
            message = message.replace("CANNON_TEMP", String.format("%.1f", cannon.getTemperature()));
            message = message.replace("MAX_TEMP", String.format("%.1f", cannon.getCannonDesign().getMaximumTemperature()));
            message = message.replace("CRIT_TEMP", String.format("%.1f", cannon.getCannonDesign().getCriticalTemperature()));
            message = message.replace("WARN_TEMP", String.format("%.1f", cannon.getCannonDesign().getWarningTemperature()));
            //cannon message name
            message = message.replace("CANNON", cannon.getCannonDesign().getMessageName());
            //soot left
            message = message.replace("SOOT_LEFT", Integer.toString((int) Math.floor(cannon.getSoot())));
            //pushing projectile
            message = message.replace("PUSHING_LEFT", Integer.toString(cannon.getProjectilePushed()));

		}

		if (player != null)
		{
			//replace the number of cannons
			message = message.replace("CANNONS", Integer.toString(plugin.getCannonManager().getNumberOfCannons(player.getName())-1));
		}
		return message;
	}


	
	/**
	 * returns a message from the map
	 * @param messageEnum
	 * @return
	 */
	public String getMessage(MessageEnum messageEnum)
	{
		return getMessage(messageEnum, null, null);
	}
	
	/**
	 * sends a message to the player which can span several lines. Line break with '\n'.
	 * @param string
	 * @param player
	 */
	private void sendMessage(String string, Player player)
	{
		if (string == null) return;
		if (player == null) return;
        if (string.equals(" "))  return;
		
		String[] message = string.split("\n "); // Split everytime the "\n" into a new array value

		for (int x = 0; x < message.length; x++)
		{
            plugin.logDebug("Message for " + player.getName() + ": " + message[x]);
			player.sendMessage(message[x]); // Send each argument in the message
		}

	}
	
}
