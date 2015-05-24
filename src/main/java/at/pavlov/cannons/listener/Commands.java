package at.pavlov.cannons.listener;

import java.util.*;

import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.Enum.SelectCannon;
import at.pavlov.cannons.cannon.Cannon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

    //<player,command to be performed>;
    private HashMap<UUID,SelectCannon> cannonSelector = new HashMap<UUID,SelectCannon>();



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
                if (args[0].equalsIgnoreCase("reload") )
                {
                    if (player == null || player.hasPermission("cannons.admin.reload"))
                    {
                        // reload config
                        config.loadConfig();
                        sendMessage(sender, ChatColor.GREEN + "[Cannons] Config loaded");
                    }
                    else
                        plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons save
                else if (args[0].equalsIgnoreCase("save"))
                {
                    if (player == null || player.hasPermission("cannons.admin.reload"))
                    {
                        // save database
                        persistenceDatabase.saveAllCannonsAsync();
                        sendMessage(sender, ChatColor.GREEN + "Cannons database saved ");
                    }
                    else
                        plugin.logDebug("No permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons load
                else if (args[0].equalsIgnoreCase("load"))
                {
                    if (player == null || player.hasPermission("cannons.admin.reload"))
                    {
                        // load database
                        persistenceDatabase.loadCannonsAsync();
                        sendMessage(sender, ChatColor.GREEN + "Cannons database loaded ");
                    }
                    else
                        plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons reset
                else if(args[0].equalsIgnoreCase("reset") && (player == null || player.hasPermission("cannons.admin.reset")))
                {
                    //try first if there is no player "all" or "all_players"
                    if (args.length >= 2 && (
                            (args[1].equals("all")&&Bukkit.getPlayer("all")==null)||
                            (args[1].equals("all_players")&&Bukkit.getPlayer("all_players")==null)))
                    {
                        //remove all cannons
                        persistenceDatabase.deleteAllCannonsAsync();
                        plugin.getCannonManager().deleteAllCannons();
                    }
                    else if (args.length >= 2 && args[1] != null)
                    {
                        // delete all cannon entries for this player
                        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(args[1]);
                        if (offplayer != null)
                        {
                            boolean b1 = plugin.getCannonManager().deleteCannons(offplayer.getUniqueId());
                            persistenceDatabase.deleteCannonsAsync(offplayer.getUniqueId());
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

                    }
                    else
                    {
                        sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "'/cannons reset <NAME>' or '/cannons reset all' or '/cannons reset all_players'");
                    }
                    return true;
                }
                //cannons list
                else if(args[0].equalsIgnoreCase("list") && (player == null || player.hasPermission("cannons.admin.list")))
                {
                    if (args.length >= 2)
                    {
                        //additional player name
                        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(args[1]);
                        if (offplayer != null) {
                            sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + offplayer.getName() + ChatColor.GREEN + ":");
                            for (Cannon cannon : plugin.getCannonManager().getCannonList().values()) {
                                if (cannon.getOwner().equals(offplayer.getUniqueId()))
                                    sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" + ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " location:" + ChatColor.GOLD + cannon.getOffset().toString());
                            }
                        }
                    }
                    else
                    {
                        //plot all cannons
                        sendMessage(sender, ChatColor.GREEN + "List of all cannons:");
                        for (Cannon cannon : plugin.getCannonManager().getCannonList().values())
                        {
                            OfflinePlayer owner = Bukkit.getOfflinePlayer(cannon.getOwner());
                            sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " owner:" + ChatColor.GOLD + owner.getName() +  ChatColor.GREEN +" location:" + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                    return true;
                }

                //cannons permissions
                else if(args[0].equalsIgnoreCase("permissions"))
                {
                    if (player == null || player.hasPermission("cannons.admin.permissions"))
                    {
                        //given name in args[1]
                        if (args.length >= 2 && args[1]!=null)
                        {
                            Player permPlayer = Bukkit.getPlayer(args[1]);
                            if (permPlayer!=null)
                                sendAllPermissions(sender,permPlayer);
                            else
                                sendMessage(sender, ChatColor.GREEN + "Player not found. Usage: " + ChatColor.GOLD + "'/cannons permissions <NAME>'");
                        }
                        //the command sender is also a player - return the permissions of the sender
                        else if (player != null)
                        {
                            sendAllPermissions(sender, player);
                        }
                        else
                            sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "'/cannons permissions <NAME>'");
                    }
                    else
                        plugin.logDebug("Missing permission 'cannons.admin.permissions' for this command");
                    return true;
                }



                //################### Player only commands #####################
                else if (player != null)
                {
                    //cannons build
                    if (args[0].equalsIgnoreCase("build"))
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        // how to build a cannon
                        userMessages.sendMessage(player, MessageEnum.HelpBuild);
                    }
                    //cannons fire
                    else if (args[0].equalsIgnoreCase("fire"))
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        // how to fire
                        userMessages.sendMessage(player, MessageEnum.HelpFire);
                    }
                    //cannons adjust
                    else if (args[0].equalsIgnoreCase("adjust"))
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        // how to adjust
                        userMessages.sendMessage(player, MessageEnum.HelpAdjust);
                    }
                    //cannons imitating toggle
                    else if (args[0].equalsIgnoreCase("imitate") && config.isImitatedAimingEnabled())
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        if (args.length >= 2 && (args[1].equalsIgnoreCase("true")||args[1].equalsIgnoreCase("enable")))
                            plugin.getAiming().enableImitating(player);
                        else if (args.length >= 2 && (args[1].equalsIgnoreCase("false")||args[1].equalsIgnoreCase("disable")))
                            plugin.getAiming().disableImitating(player);
                        else
                            plugin.getAiming().toggleImitating(player);
                    }
                    //rename cannon
                    else if(args[0].equalsIgnoreCase("rename"))
                    {
                        if (!player.hasPermission("cannons.player.rename"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        if (args.length >= 3 && args[1]!=null  && args[2]!=null)
                        {
                            //selection done by a string '/cannons rename OLD NEW'
                            Cannon cannon = plugin.getCannonManager().getCannon(args[1]);
                            if (cannon != null)
                            {
                                MessageEnum message = plugin.getCannonManager().renameCannon(player, cannon, args[2]);
                                userMessages.sendMessage(player, cannon, message);
                            }
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "Usage '/cannons rename <OLD_NAME> <NEW_NAME>'");
                        return true;
                    }
                    //add observer for cannon
                    else if(args[0].equalsIgnoreCase("observer"))
                    {
                        if (!player.hasPermission("cannons.player.observer"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        if (args.length >= 2 && (args[1].equalsIgnoreCase("off")||args[1].equalsIgnoreCase("disable")||args[1].equalsIgnoreCase("remove")))
                            plugin.getAiming().removeObserverForAllCannons(player);
                        else if (args.length < 2)
                            toggleCannonSelector(player, SelectCannon.OBSERVER);
                        else if (args.length >= 2 && args[1]!=null)
                        {
                            //selection done by a string '/cannons observer CANNON_NAME'
                            Cannon cannon = plugin.getCannonManager().getCannon(args[1]);
                            if (cannon!=null)
                                cannon.toggleObserver(player, false);
                            else
                                userMessages.sendMessage(player, MessageEnum.CmdCannonNotFound);
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "Usage '/cannons observer' or '/cannons observer <off|disable>' or '/cannons observer <CANNON NAME>'");
                    }
                    //get name of cannon
                    else if(args[0].equalsIgnoreCase("get"))
                    {
                        if (!player.hasPermission("cannons.player.get"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        toggleCannonSelector(player, SelectCannon.GET);
                    }
                    //get name of cannon
                    else if(args[0].equalsIgnoreCase("dismantle"))
                    {
                        if (!player.hasPermission("cannons.player.dismantle"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        toggleCannonSelector(player, SelectCannon.DISMANTLE);
                    }
                    //list cannons of this player name
                    else if(args[0].equalsIgnoreCase("list"))
                    {
                        if (!player.hasPermission("cannons.player.list"))
                        {
                            plugin.logDebug("[Cannons] Missing permission 'cannons.player.list' for command /cannons " + args[0]);
                            return true;
                        }
                        sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + ":");
                        for (Cannon cannon : plugin.getCannonManager().getCannonList().values())
                        {
                            if (cannon.getOwner().equals(player.getUniqueId()))
                                sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" +
                                        ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " loc: " + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                    }
                    //cannons reset
                    else if(args[0].equalsIgnoreCase("reset"))
                    {
                        if (!player.hasPermission("cannons.player.reset"))
                        {
                            plugin.logDebug("[Cannons] No permission for command /cannons " + args[0]);
                            return true;
                        }
                        // delete all cannon entries for this player
                        persistenceDatabase.deleteCannonsAsync(player.getUniqueId());
                        plugin.getCannonManager().deleteCannons(player.getUniqueId());
                        userMessages.sendMessage(player, MessageEnum.CannonsReseted);
                    }
                    //no help message if it is forbidden for this player
                    else
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] No permission for command " + args[0]);
                            return true;
                        }
                        // display help
                        userMessages.sendMessage(player, MessageEnum.HelpText);
                        return true;
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
                        userMessages.sendMessage(player, MessageEnum.HelpText);
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

    /**
     * sends a message to the console of the player. Console messages will be striped form color
     * @param sender player or console
     * @param str message
     */
    private void sendMessage(CommandSender sender, String str)
    {
        if (sender == null)
            return;

        //strip color of console messages
        if (!(sender instanceof Player))
            str = ChatColor.stripColor(str);

        sender.sendMessage(str);
    }

    /**
     * this player will be removed from the selecting mode
     * @param player the player will be removed
     * @param cmd this command will be performed when the cannon is selected
     */
    public void addCannonSelector(Player player, SelectCannon cmd)
    {
        if (player == null || cmd == null)
            return;

        if (!isSelectingMode(player))
        {
            cannonSelector.put(player.getUniqueId(),cmd);
            userMessages.sendMessage(player, MessageEnum.CmdSelectCannon);
        }
    }

    /**
     * this player will be removed from the selecting mode
     * @param player the player will be removed
     */
    public void removeCannonSelector(Player player)
    {
        if (player == null)
            return;

        if (isSelectingMode(player))
        {
            cannonSelector.remove(player.getUniqueId());
            userMessages.sendMessage(player, MessageEnum.CmdSelectCanceled);
        }
    }

    /**
     * selecting mode will be toggled
     * @param player the player using the selecting mode
     * @param cmd this command will be performed when the cannon is selected
     */
    public void toggleCannonSelector(Player player, SelectCannon cmd)
    {
        if (player == null)
            return;

        if (isSelectingMode(player))
            removeCannonSelector(player);
        else
            addCannonSelector(player, cmd);
    }


    /**
     * Checks if this player is in selecting mode
     * @param player player to check
     * @return true if in selecting mode
     */
    public boolean isSelectingMode(Player player)
    {
        if (player == null)
            return false;

        return cannonSelector.containsKey(player.getUniqueId());
    }

    /**
     * adds a new selected cannon for this player
     * @param player player that selected the cannon
     * @param cannon the selected cannon
     */
    public void setSelectedCannon(Player player, Cannon cannon)
    {
        if (player == null || cannon == null)
            return;

        SelectCannon cmd = cannonSelector.get(player.getUniqueId());
        if (cmd != null)
        {
            switch (cmd){
                case OBSERVER:{
                    MessageEnum message = cannon.toggleObserver(player,false);
                    userMessages.sendMessage(player, cannon, message);
                    break;
                }
                case GET:{
                    userMessages.sendMessage(player, cannon, MessageEnum.CannonGetName);
                    break;
                }
                case DISMANTLE:{
                    plugin.getCannonManager().removeCannon(cannon, false, false, BreakCause.Dismantling);
                    break;
                }
            }
        }
        cannonSelector.remove(player.getUniqueId());
    }

    /**
     * displays the given permission of the player
     * @param sender command sender
     * @param player the permission of this player will be checked
     * @param permission permission as string
     */
    public void displayPermission(CommandSender sender, Player player, String permission)
    {
        if (player == null || permission == null) return;

        //request permission
        Boolean hasPerm = player.hasPermission(permission);
        //add some color
        String perm;
        if (hasPerm)
            perm = ChatColor.GREEN + "TRUE";
        else
            perm = ChatColor.RED + "FALSE";
        sendMessage(sender, ChatColor.YELLOW + permission + ": " + perm);
    }


    /**
     * sends all default permissions of the player to the sender
     * @param sender command sender
     * @param permPlayer the permission of this player will be checked
     */
    public void sendAllPermissions(CommandSender sender, Player permPlayer)
    {
        sendMessage(sender, ChatColor.GREEN + "Permissions for " + ChatColor.GOLD + permPlayer.getName() + ChatColor.GREEN + ":");
        displayPermission(sender, permPlayer, "cannons.player.command");
        displayPermission(sender, permPlayer, "cannons.player.help");
        displayPermission(sender, permPlayer, "cannons.player.rename");
        displayPermission(sender, permPlayer, "cannons.player.build");
        displayPermission(sender, permPlayer, "cannons.player.dismantle");
        displayPermission(sender, permPlayer, "cannons.player.redstone");
        displayPermission(sender, permPlayer, "cannons.player.load");
        displayPermission(sender, permPlayer, "cannons.player.adjust");
        displayPermission(sender, permPlayer, "cannons.player.fire");
        displayPermission(sender, permPlayer, "cannons.player.autoaim");
        displayPermission(sender, permPlayer, "cannons.player.observer");
        displayPermission(sender, permPlayer, "cannons.player.tracking");
        displayPermission(sender, permPlayer, "cannons.player.autoreload");
        displayPermission(sender, permPlayer, "cannons.player.thermometer");
        displayPermission(sender, permPlayer, "cannons.player.ramrod");
        displayPermission(sender, permPlayer, "cannons.player.reset");
        displayPermission(sender, permPlayer, "cannons.player.list");
        displayPermission(sender, permPlayer, "cannons.projectile.default");
        displayPermission(sender, permPlayer, "cannons.limit.limitA");
        displayPermission(sender, permPlayer, "cannons.limit.limitB");
        int newBuildlimit = plugin.getCannonManager().getNewBuildLimit(permPlayer);
        if (newBuildlimit==Integer.MAX_VALUE)
            sendMessage(sender, ChatColor.YELLOW + "no Permission cannons.limit.x (with 0<=x<=100)");
        else
            displayPermission(sender, permPlayer, "cannons.limit."+newBuildlimit);
        int numberCannons = plugin.getCannonManager().getNumberOfCannons(permPlayer.getUniqueId());
        int maxCannons = plugin.getCannonManager().getCannonBuiltLimit(permPlayer);
        if (maxCannons == Integer.MAX_VALUE)
            sendMessage(sender, ChatColor.YELLOW + "Built cannons: " + ChatColor.GOLD + numberCannons);
        else
            sendMessage(sender, ChatColor.YELLOW + "Built cannons: " + ChatColor.GOLD + numberCannons + "/" + maxCannons);
        displayPermission(sender, permPlayer, "cannons.admin.reload");
        displayPermission(sender, permPlayer, "cannons.admin.reset");
        displayPermission(sender, permPlayer, "cannons.admin.list");
        displayPermission(sender, permPlayer, "cannons.admin.permissions");
    }
}
