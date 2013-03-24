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
					if (args[0].equalsIgnoreCase("build") && sender.hasPermission("cannons.player.command"))
					{
						// how to build a cannon
						plugin.sendMessage(userMessages.HelpBuild, sender, ChatColor.GREEN);
					}
					else if (args[0].equalsIgnoreCase("fire") && sender.hasPermission("cannons.player.command"))
					{
						// how to fire
						plugin.sendMessage(userMessages.HelpFire, sender, ChatColor.GREEN);
					}
					else if (args[0].equalsIgnoreCase("adjust") && sender.hasPermission("cannons.player.command"))
					{
						// how to adjust
						plugin.sendMessage(userMessages.HelpAdjust, sender, ChatColor.GREEN);
					}
					else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("cannons.admin.reload"))
					{
						// reload config
						config.loadConfig();
						plugin.sendMessage("Cannons config loaded ", sender, ChatColor.GREEN);
					}
					else if (args[0].equalsIgnoreCase("save") && sender.hasPermission("cannons.admin.reload"))
					{
						// save database
						persistenceDatabase.saveAllCannons();
						plugin.sendMessage("Cannons database saved ", sender, ChatColor.GREEN);
					}
					else if (args[0].equalsIgnoreCase("load") && sender.hasPermission("cannons.admin.reload"))
					{
						// load database
						persistenceDatabase.loadCannons();
						plugin.sendMessage("Cannons database loaed ", sender, ChatColor.GREEN);
					}
					else if(args[0].equalsIgnoreCase("load") && sender.hasPermission("cannons.player.reset"))
					{
						// delete all cannon entries for this player
						persistenceDatabase.deleteCannons(player.getName());
						plugin.getCannonManager().deleteCannons(player.getName());
					}
					else
					{
						// display help
						plugin.sendMessage(userMessages.HelpText, sender, ChatColor.GREEN);
					}
				}
				else
				{
					// display help
					plugin.sendMessage(userMessages.HelpText, sender, ChatColor.GREEN);
				}
			}
			return true;
		}
		return false;
	}




}
