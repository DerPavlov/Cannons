package at.pavlov.Cannons.listener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.dao.PersistenceDatabase;


public class Commands implements CommandExecutor
{
	Cannons plugin;
	Config config;
	UserMessages userMessages;
	PersistenceDatabase persistenceDatabase;
	
	public Commands(Cannons plugin)
	{
		this.plugin = plugin;
		config = this.plugin.getmyConfig();
		userMessages = this.plugin.getmyConfig().getUserMessages();
		persistenceDatabase = this.plugin.getPersistenceDatabase();
	}


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
			if (player == null)
			{
				sender.sendMessage("this command can only be run by a player!");
			}
			else
			{
				if (args.length >= 1)
				{
					//cannons build
					if (args[0].equalsIgnoreCase("build") && sender.hasPermission("cannons.player.command"))
					{
						// how to build a cannon
						sendMessage(userMessages.HelpBuild, sender, ChatColor.GREEN);
					}
					//cannons fire
					else if (args[0].equalsIgnoreCase("fire") && sender.hasPermission("cannons.player.command"))
					{
						// how to fire
						sendMessage(userMessages.HelpFire, sender, ChatColor.GREEN);
					}
					//cannons adjust
					else if (args[0].equalsIgnoreCase("adjust") && sender.hasPermission("cannons.player.command"))
					{
						// how to adjust
						sendMessage(userMessages.HelpAdjust, sender, ChatColor.GREEN);
					}
					//cannons reload
					else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("cannons.admin.reload"))
					{
						// reload config
						config.loadConfig();
						sendMessage("Cannons config loaded ", sender, ChatColor.GREEN);
					}
					//cannons save
					else if (args[0].equalsIgnoreCase("save") && sender.hasPermission("cannons.admin.reload"))
					{
						// save database
						persistenceDatabase.saveAllCannons();
						sendMessage("Cannons database saved ", sender, ChatColor.GREEN);
					}
					//cannons load
					else if (args[0].equalsIgnoreCase("load") && sender.hasPermission("cannons.admin.reload"))
					{
						// load database
						persistenceDatabase.loadCannons();
						sendMessage("Cannons database loaed ", sender, ChatColor.GREEN);
					}
					//cannons reset
					else if(args[0].equalsIgnoreCase("reset") && sender.hasPermission("cannons.player.reset"))
					{
						// delete all cannon entries for this player
						persistenceDatabase.deleteCannons(player.getName());
						plugin.getCannonManager().deleteCannons(player.getName());
						sendMessage(userMessages.cannonsReseted, sender, ChatColor.GREEN);
					}
					else
					{
						// display help
						sendMessage(userMessages.HelpText, sender, ChatColor.GREEN);
					}
				}
				else
				{
					// display help
					sendMessage(userMessages.HelpText, sender, ChatColor.GREEN);
				}
			}
			return true;
		}
		return false;
	}


	/**
	 * sends a message to the player which can span several lines. Linebreak with '\n'.
	 * @param string
	 * @param player
	 * @param chatcolor
	 */
	private void sendMessage(String string, CommandSender player, ChatColor chatcolor)
	{
		String[] message = string.split("\n "); // Split everytime the "\n" into
												// a new array value

		for (int x = 0; x < message.length; x++)
		{
			player.sendMessage(chatcolor + message[x]); // Send each argument in
														// the message
		}

	}

}
