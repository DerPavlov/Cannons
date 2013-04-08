package at.pavlov.Cannons.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.enums.MessageEnum;
import at.pavlov.Cannons.utils.CannonsUtil;

public class UserMessages {
	private FileConfiguration customLanguage = null;
	private File customLanguageFile = null;
	
	
	private HashMap<String, String> messageMap = new HashMap<String, String>();

	
	public String BarrelTooHot;
	
	public String NoProjectile;
	public String NoSulphur;
	public String NoFlintAndSteel;
	public String MaximumGunpowderLoaded;
	public String ProjectileAlreadyLoaded;
	public String FireGun;
	
	public String enableAimingMode;
	public String disableAimingMode;
	
	public String settingCombinedAngle;
	public String settingVerticalAngleUp;
	public String settingVerticalAngleDown;
	public String settingHorizontalAngleRight;
	public String settingHorizontalAngleLeft;
	
	public String loadProjectile;
	public String loadGunpowder;
	public String twoTorches;
	public String tooManyGuns;
	
	public String cannonBuilt;
	public String cannonDestroyed;
	public String cannonsReseted;
	
	public String ErrorPermRestoneTorch;
	public String ErrorPermFire;
	public String ErrorPermLoad;
	public String ErrorPermAdjust;
	public String ErrorPermissionProjectile;
	
	public String HelpText;
	public String HelpBuild;
	public String HelpFire;
	public String HelpAdjust;
	
	
	private Config config;
	private Cannons plugin;
	
	public UserMessages(Cannons plugin, Config config){
		this.config = config;
		this.plugin = plugin;
	}
		 
	public void loadLanguage()
	{
		this.loadCustom("localization");
		
		//copy german language
		File localizationGerman = new File(plugin.getDataFolder(), "localization/localization_german.yml");
		if (!localizationGerman.exists())
		{
			localizationGerman.getParentFile().mkdirs();
			CannonsUtil.copyFile(plugin.getResource("localization/localization_german.yml"), localizationGerman);
		}
		//copy english language
		File localizationEnglish = new File(plugin.getDataFolder(), "localization/localization_english.yml");
		if (!localizationEnglish.exists())
		{
			localizationEnglish.getParentFile().mkdirs();
			CannonsUtil.copyFile(plugin.getResource("localization/localization.yml"), localizationEnglish);
		}
	}
		
	/**
	 * load the localization file from the disk
	 * @param filename
	 */
	private void loadCustom(String filename)
	{
		reloadcustomLanguage(filename);
		customLanguage.options().copyDefaults(true);
		savecustomLanguage();
		
		//load all messages
		for (MessageEnum keyEnum : MessageEnum.values())
		{
			String key = keyEnum.getString();
			String entry = getEntry(key);
			if (entry != null && entry != "")
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
		
	public String getEntry(String key)
	{
		String entry = customLanguage.getString(key);
		String replace = null;
		
		//replace red color
		replace = "&4";
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


	private String getDataFolder() 
	{
		return "plugins/Cannons/localization/";
	}


	private void savecustomLanguage()
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
	 * @param message
	 * @param cannon
	 */
	public void displayMessage(String player, MessageEnum messageEnum, Cannon cannon)
	{
		//no player no message
		if (player != null)
		{
			displayMessage(Bukkit.getPlayer(player), messageEnum, cannon);
		}
	}
	
	/**
	 * sends a message to the player
	 * @param player
	 * @param message
	 * @param cannon
	 */
	public void displayMessage(Player player, MessageEnum messageEnum, Cannon cannon)
	{
		//no message
		if (messageEnum == null) return;
			
		//no player - no message
		if (player == null) return;
		

				
		String message = messageMap.get(messageEnum.getString());
		
		//return if message was not found
		if (message == null)
		{
			plugin.logSevere("Message " + messageEnum.getString() + " not found.");
			return;
		}
		
		if (cannon != null)
		{
			//replace the loaded gunpowder
			message = message.replace("GUNPOWDER", Integer.toString(cannon.getLoadedGunpowder()));
			//replace the loaded projectilee
			message = message.replace("PROJECTILE", cannon.getLoadedProjectile().getName());
			//replace the horizontal angle
			message = message.replace("HDEGREE", Double.toString(cannon.getHorizontalAngle()));			
			//replace the vertical angle
			message = message.replace("VDEGREE", Double.toString(cannon.getVerticalAngle()));
			//replace the number of cannons
			message = message.replace("CANNONS", Integer.toString(plugin.getCannonManager().getNumberOfCannons(player.getName())));
			
		}
		
		player.sendMessage("message: " + message);
	}
	
}
