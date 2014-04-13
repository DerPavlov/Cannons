package at.pavlov.cannons.listener;

import java.util.HashMap;

import at.pavlov.cannons.Aiming;
import at.pavlov.cannons.cannon.Cannon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.dao.PersistenceDatabase;


public class Commands implements CommandExecutor
{
    private final Cannons plugin;
    private final Config config;
    private final UserMessages userMessages;
    private final PersistenceDatabase persistenceDatabase;



    public Commands(Cannons plugin)
    {
        this.plugin = plugin;
        config = this.plugin.getMyConfig();
        userMessages = this.plugin.getMyConfig().getUserMessages();
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
            if (args.length >= 1)
            {
                //############## console and player commands ######################
                //cannons reload
                if (args[0].equalsIgnoreCase("reload") && (player == null || player.hasPermission("cannons.admin.reload")))
                {
                    // reload config
                    config.loadConfig();
                    sendMessage(sender, ChatColor.GREEN + "[Cannons] Config loaded");
                }
                //cannons save
                else if (args[0].equalsIgnoreCase("save") && (player == null || player.hasPermission("cannons.admin.reload")))
                {
                    // save database
                    persistenceDatabase.saveAllCannonsAsync();
                    sendMessage(sender, ChatColor.GREEN + "Cannons database saved ");
                }
                //cannons load
                else if (args[0].equalsIgnoreCase("load") && (player == null || player.hasPermission("cannons.admin.reload")))
                {
                    // load database
                    persistenceDatabase.loadCannonsAsync();
                    sendMessage(sender, ChatColor.GREEN + "Cannons database loaded ");
                }
                //cannons reset
                else if(args[0].equalsIgnoreCase("reset") && (player == null || player.hasPermission("cannons.admin.reset")))
                {
                    if (args.length >= 2 && args[1] != null)
                    {
                        // delete all cannon entries for this player
                        boolean b1 = plugin.getCannonManager().deleteCannons(args[1]);
                        persistenceDatabase.deleteCannonsAsync(args[1]);
                        if (b1)
                        {
                            //there was an entry in the list
                            sendMessage(sender, ChatColor.GREEN + userMessages.getMessage(MessageEnum.CannonsReseted).replace("PLAYER", args[1]));
                        }
                        else
                        {
                            sendMessage(sender, ChatColor.GREEN + "Player " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " not found in the storage");
                        }
                    }
                    else
                    {
                        sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "/cannons reset <NAME>");
                    }
                }
                //cannons list
                else if(args[0].equalsIgnoreCase("list") && (player == null || player.hasPermission("cannons.admin.list")) )
                {
                    if (args.length >= 2)
                    {
                        //additional player name
                        for (Cannon cannon : plugin.getCannonManager().getCannonList())
                        {
                            sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + args[1]);
                            if (cannon.getOwner().equalsIgnoreCase(args[1]))
                                sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" + ChatColor.GOLD + cannon.getCannonDesign().getDesignName() +  ChatColor.GREEN +" location:" + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                    else
                    {
                        //plot all cannons
                        sendMessage(sender, ChatColor.GREEN + "List of all cannons:");
                        for (Cannon cannon : plugin.getCannonManager().getCannonList())
                        {
                            sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " owner:" + ChatColor.GOLD + cannon.getOwner() +  ChatColor.GREEN +" location:" + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                }



                //################### Player only commands #####################
                else if (player != null)
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
                    //list cannons of this player name
                    else if(args[0].equalsIgnoreCase("list") && player.hasPermission("cannons.player.list"))
                    {
                        for (Cannon cannon : plugin.getCannonManager().getCannonList())
                        {
                            player.sendMessage(ChatColor.GREEN +"Cannon list for " + args[1] + ":");
                            if (cannon.getOwner().equalsIgnoreCase(player.getName()))
                                player.sendMessage(ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" +
                                        ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " loc: " + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                    //cannons imitating toggle
                    else if (args[0].equalsIgnoreCase("imitate") && player.hasPermission("cannons.player.command") && config.isImitatedAimingEnabled())
                    {
                        if (args.length >= 2 && (args[1].equalsIgnoreCase("true")||args[1].equalsIgnoreCase("enable")))
                            plugin.getAiming().enableImitating(player);
                        else if (args.length >= 2 && (args[1].equalsIgnoreCase("false")||args[1].equalsIgnoreCase("disable")))
                            plugin.getAiming().disableImitating(player);
                        else
                            plugin.getAiming().toggleImitating(player);
                    }

                    //cannons reset
                    else if(args[0].equalsIgnoreCase("reset") && player.hasPermission("cannons.player.reset"))
                    {
                        // delete all cannon entries for this player
                        persistenceDatabase.deleteCannonsAsync(player.getName());
                        plugin.getCannonManager().deleteCannons(player.getName());
                        userMessages.displayMessage(player, MessageEnum.CannonsReseted);
                    }
                    //no help message if it is forbidden for this player
                    else if (player.hasPermission("cannons.player.command"))
                    {
                        // display help
                        userMessages.displayMessage(player, MessageEnum.HelpText);
                    }
                }
                else
                {
                    plugin.logDebug("This command can only be used by a player");
                    return false;
                }



            }
            //console command
            else
            {
                //no help message if it is forbidden for this player
                if(player != null)
                {
                    if(player.hasPermission("cannons.player.command"))
                    {
                        // display help
                        userMessages.displayMessage(player, MessageEnum.HelpText);
                    }
                    else
                    {
                        plugin.logInfo("Player has no permission: cannons.player.command");
                    }

                }
                else
                {
                    plugin.logInfo("Cannons plugin v" + plugin.getPluginDescription().getVersion() + " is running");
                }
            }
            return true;
        }
        return false;
    }


    private void sendMessage(CommandSender sender, String str)
    {
        //strip color of console messages
        if (!(sender instanceof Player))
            str = ChatColor.stripColor(str);

        sender.sendMessage(str);
    }




}
