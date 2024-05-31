package at.pavlov.cannons.config;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.utils.CannonsUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

public class UserMessages {
	private FileConfiguration customLanguage = null;
	private File customLanguageFile = null;
	
	
	private final HashMap<String, String> messageMap = new HashMap<String, String>();


	private final Cannons plugin;

	private final Random random = new Random();
	
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
		reloadCustomLanguage(filename);
		customLanguage.options().copyDefaults(true);
		saveCustomLanguage();
		
		//load all messages
		for (MessageEnum keyEnum : MessageEnum.values())
		{
			String key = keyEnum.getString();
			String entry = getEntry(key);
			if (!entry.equals(""))
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

	/**
	 * get a message string from the user messages
	 * @param key the requested message
	 * @return the message
	 */
	private String getEntry(String key)
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
		return ChatColor.translateAlternateColorCodes('&', entry);
	}



	private void reloadCustomLanguage(String filename)
	{
	    if (customLanguageFile == null) 
	    {
	    	customLanguageFile = new File(getDataFolder(), filename + ".yml");
	    }
	    customLanguage = YamlConfiguration.loadConfiguration(customLanguageFile);
	 
	    // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(plugin.getResource("localization/" + filename + ".yml"), "UTF8");
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customLanguage.setDefaults(defConfig);
        } catch (UnsupportedEncodingException e) {
            plugin.logSevere("Unsupported encoding: " + e);
        }

	}


	private String getDataFolder()
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
     * @param messageEnum message to display
     * @param player which player gets this message
     * @param cannon which cannon parameter will be displayed
     */
	public void sendMessage(MessageEnum messageEnum, UUID player, Cannon cannon)
	{
		//no player no message
		if (player != null)
            sendMessage(messageEnum, Bukkit.getPlayer(player), cannon);
	}
	
	/**
	 * sends a message to the player
     * @param messageEnum message to display
     * @param player which player gets this message
     */
	public void sendMessage(MessageEnum messageEnum, Player player)
	{
        sendMessage(messageEnum, player, null);
	}
	
	/**
	 * sends a message to the player
     * @param messageEnum message to display
     * @param player which player gets this message
     * @param cannon which cannon parameter will be displayed
     */
	public void sendMessage(MessageEnum messageEnum, Player player, Cannon cannon)
	{
		//no player no message
		if (player == null) return;
		if (messageEnum == null) return;
		
		//get message from map
		String message = getMessage(messageEnum, player, cannon);
		
		//send message to player
		sendMessage(message, player);
	}

    public void sendImpactMessage(Player player, Location impact, boolean canceled)
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

        if (!canceled) {
            //the projectile exploded
            messageEnum = MessageEnum.ProjectileExplosion;
        }
        else {
            //the explosion was canceled
            messageEnum = MessageEnum.ProjectileCanceled;
        }

        message = messageMap.get(messageEnum.getString());

        if (message == null){
            plugin.logSevere("No " + messageEnum.getString() + " in localization file");
            return;
        }
        //if the message is something like this Explosion: '' it will pass quietly
        if (message.isEmpty()) {
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
	 * @param messageEnum message to display
	 * @param player which player gets this message
	 * @param cannon which cannon parameter will be displayed
	 * @return the requested message from the localization file
	 */
    private String getMessage(MessageEnum messageEnum, Player player, Cannon cannon)
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
            return null;
		
		if (cannon != null)
		{
			// show target status
			if (cannon.getCannonDesign().isSentry()){
				if (cannon.isTargetMob())
					message = message.replace("TARGET_MOB", ChatColor.GREEN + "TRUE");
				else
					message = message.replace("TARGET_MOB", ChatColor.RED + "FALSE");
				if (cannon.isTargetPlayer())
					message = message.replace("TARGET_PLAYER", ChatColor.GREEN + "TRUE");
				else
					message = message.replace("TARGET_PLAYER", ChatColor.RED + "FALSE");
				if (cannon.isTargetCannon())
					message = message.replace("TARGET_CANNON", ChatColor.GREEN + "TRUE");
				else
					message = message.replace("TARGET_CANNON", ChatColor.RED + "FALSE");
				if (cannon.isTargetOther())
					message = message.replace("TARGET_OTHER", ChatColor.GREEN + "TRUE");
				else
					message = message.replace("TARGET_OTHER", ChatColor.RED + "FALSE");
			}
			//split message for sentry info
			String[] messages = message.split("-SENTRY-INFORMATION-");
			if (messages.length > 1) {
				if (cannon.getCannonDesign().isSentry())
					message = messages[0] + messages[1];
				else
					message = messages[0];
			}
			//replace the loaded gunpowder
            message = message.replace("MAX_GUNPOWDER", Integer.toString(cannon.getCannonDesign().getMaxLoadableGunpowderNormal()));
            message = message.replace("MAX_ABSOLUTE_GUNPOWDER", Integer.toString(cannon.getCannonDesign().getMaxLoadableGunpowderOverloaded()));
			message = message.replace("GUNPOWDER", Integer.toString(cannon.getLoadedGunpowder()));
			//replace the loaded projectile
			if (cannon.getLoadedProjectile()!=null)
				message = message.replace("PROJECTILE", cannon.getLoadedProjectile().getProjectileName());
            else
                message = message.replace("PROJECTILE", "none");
			//replace the horizontal angle
			message = message.replace("HDEGREE", String.format("%.1f", cannon.getHorizontalAngle()));
			//replace the vertical angle
			message = message.replace("VDEGREE", String.format("%.1f", cannon.getVerticalAngle()));
            //replace cannon temperature
            message = message.replace("EXPLOSION_CHANCE", String.format("%.1f", (cannon.getOverheatingChance()+cannon.getOverloadingExplosionChance())*100.0));
            message = message.replace("CANNON_TEMP", String.format("%.1f", cannon.getTemperature()));
            message = message.replace("MAX_TEMP", String.format("%.1f", cannon.getCannonDesign().getMaximumTemperature()));
            message = message.replace("CRIT_TEMP", String.format("%.1f", cannon.getCannonDesign().getCriticalTemperature()));
            message = message.replace("WARN_TEMP", String.format("%.1f", cannon.getCannonDesign().getWarningTemperature()));
            //cannon message name
            if (cannon.getCannonName()!=null)
                message = message.replace("CANNON_NAME", cannon.getCannonName());
            message = message.replace("CANNON", cannon.getCannonDesign().getMessageName());
			message = message.replace("DESCRIPTION", cannon.getCannonDesign().getDescription());
			if (cannon.getOwner() != null){
				OfflinePlayer offplayer = Bukkit.getOfflinePlayer(cannon.getOwner());
				if (offplayer != null)
					message = message.replace("OWNER", offplayer.getName());
			}
            //soot left
            message = message.replace("SOOT_LEFT", Integer.toString((int) Math.floor(cannon.getSoot())));
            message = message.replace("SOOT", String.format("%.1f", cannon.getSoot()));
            //pushing projectile
            message = message.replace("PUSHING_LEFT", Integer.toString(cannon.getProjectilePushed()));
			//economy
			if (plugin.getEconomy() != null) {
				message = message.replace("BUILD_COSTS", plugin.getEconomy().format(cannon.getCannonDesign().getEconomyBuildingCost()));
				message = message.replace("DISMANTLING_REFUND", plugin.getEconomy().format(cannon.getCannonDesign().getEconomyDismantlingRefund()));
				message = message.replace("DESTRUCTION_REFUND", plugin.getEconomy().format(cannon.getCannonDesign().getEconomyDestructionRefund()));
			}
			// show the name of the last whitelisted player
			if (cannon.getLastWhitelisted() != null) {
				OfflinePlayer whiteplayer = Bukkit.getOfflinePlayer(cannon.getLastWhitelisted());
				if (whiteplayer != null)
					message = message.replace("LASTWHITELISTED", whiteplayer.getName());
			}
			// show whitelist
			if (cannon.getWhitelist() != null){
				List<String> names = new ArrayList<String>();
				for (UUID playerUID : cannon.getWhitelist()){
					OfflinePlayer offplayer = Bukkit.getOfflinePlayer(playerUID);
					if (offplayer != null) {
						names.add(offplayer.getName());
					}
				}
				if (names.size() > 0)
					message = message.replace("WHITELIST", "\n " + ChatColor.GOLD + "  - " + StringUtils.join(names, "\n " + ChatColor.GOLD + "  - "));
				else
					message = message.replace("WHITELIST", ChatColor.GOLD + "none");
			}
        }

        if (player != null)
		{
			//replace the number of cannons
            message = message.replace("PLAYER", player.getName());
			message = message.replace("LIMIT", Integer.toString(plugin.getCannonManager().getNumberOfCannons(player.getUniqueId())));
		}
		return message;
	}

	/**
	 * returns a random death message
	 * @param killed killed player
	 * @param shooter shooter of the cannon
	 * @param cannon cannon that was used
	 * @param projectile fired projectile
	 * @return death message
	 */
	public String getDeathMessage(UUID killed, UUID shooter, Cannon cannon, Projectile projectile){
		MessageEnum messageEnum;
		switch (random.nextInt(3)){
			case 1:
				messageEnum = MessageEnum.DeathMessage2;
				break;
			case 2:
				messageEnum = MessageEnum.DeathMessage3;
				break;
			default:
				messageEnum = MessageEnum.DeathMessage1;
		}
		Player killedPlayer = null;
		if (killed != null) {
			killedPlayer = Bukkit.getPlayer(killed);
		}

		String shooterStr = "none";
		if (shooter != null){
			Player shooterPlayer = Bukkit.getPlayer(shooter);
			if (shooterPlayer != null)
				shooterStr = shooterPlayer.getName();
		}
		String projectileStr = "none";
		if (projectile != null)
			projectileStr = projectile.getProjectileName();

		String message = getMessage(messageEnum, killedPlayer, cannon);
		message = message.replace("SHOOTER", shooterStr);
		message = message.replace("CBALL", projectileStr);
		return message;
	}


	
	/**
	 * returns a message from the map
	 * @param messageEnum message to display
	 * @return the requested message from the localization file
	 */
	public String getMessage(MessageEnum messageEnum)
	{
		return getMessage(messageEnum, null, null);
	}
	
	/**
	 * sends a message to the player which can span several lines. Line break with '\n'.
	 * @param string message to send
	 * @param player which player gets this message
	 */
	private void sendMessage(String string, Player player)
	{
		if (string == null) return;
		if (player == null) return;
        if (string.equals(" "))  return;
		
		String[] message = string.split("\n "); // Split everytime the "\n" into a new array value

		for (String aMessage : message) {
			plugin.logDebug("Message for " + player.getName() + ": " + aMessage);
			player.sendMessage(aMessage); // Send each argument in the message
		}
	}
}
