package at.pavlov.Cannons.listener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.MessageEnum;
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
					if (args[0].equalsIgnoreCase("build") && player.hasPermission("cannons.player.command"))
					{
						// how to build a cannon
						userMessages.displayMessage(player, MessageEnum.HelpBuild);
					}
					//cannons fire
					else if (args[0].equalsIgnoreCase("fire") && player.hasPermission("cannons.player.command"))
					{
						// how to fire
						userMessages.displayMessage(player, MessageEnum.HelpFire);
					}
					//cannons adjust
					else if (args[0].equalsIgnoreCase("adjust") && player.hasPermission("cannons.player.command"))
					{
						// how to adjust
						userMessages.displayMessage(player, MessageEnum.HelpAdjust);
					}
					//cannons reload
					else if (args[0].equalsIgnoreCase("reload") && player.hasPermission("cannons.admin.reload"))
					{
						// reload config
						config.loadConfig();
						player.sendMessage(ChatColor.GREEN + "Cannons config loaded");
					}
					//cannons save
					else if (args[0].equalsIgnoreCase("save") && player.hasPermission("cannons.admin.reload"))
					{
						// save database
						persistenceDatabase.saveAllCannons();
						player.sendMessage(ChatColor.GREEN + "Cannons database saved ");
					}
					//cannons load
					else if (args[0].equalsIgnoreCase("load") && player.hasPermission("cannons.admin.reload"))
					{
						// load database
						persistenceDatabase.loadCannons();
						player.sendMessage(ChatColor.GREEN + "Cannons database loaded ");
					}
					//cannons reset
					else if(args[0].equalsIgnoreCase("reset") && player.hasPermission("cannons.player.reset"))
					{
						// delete all cannon entries for this player
						persistenceDatabase.deleteCannons(player.getName());
						plugin.getCannonManager().deleteCannons(player.getName());
						userMessages.displayMessage(player, MessageEnum.CannonsReseted);
					}
					else
					{
						// display help
						userMessages.displayMessage(player, MessageEnum.HelpText);
					}
				}
				else
				{
					// display help
					userMessages.displayMessage(player, MessageEnum.HelpText);
				}
			}
			return true;
		}
		return false;
	}




}
