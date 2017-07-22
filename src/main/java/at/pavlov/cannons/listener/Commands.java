package at.pavlov.cannons.listener;

import java.util.*;

import at.pavlov.cannons.Enum.SelectCannon;
import at.pavlov.cannons.cannon.Cannon;

import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonsUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
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
    //<player,playerUID>;
    private HashMap<UUID,UUID> whitelistPlayer = new HashMap<UUID,UUID>();



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
                        plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons save
                else if (args[0].equalsIgnoreCase("save"))
                {
                    if (player == null || player.hasPermission("cannons.admin.reload"))
                    {
                        // save database
                        persistenceDatabase.saveAllCannons(true);
                        sendMessage(sender, ChatColor.GREEN + "Cannons database saved ");
                    }
                    else
                        plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons load
                else if (args[0].equalsIgnoreCase("load"))
                {
                    if (player == null || player.hasPermission("cannons.admin.reload"))
                    {
                        // load database
                        persistenceDatabase.loadCannons();
                        sendMessage(sender, ChatColor.GREEN + "Cannons database loaded ");
                    }
                    else
                        plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons reset
                else if(args[0].equalsIgnoreCase("reset") && (player == null || player.hasPermission("cannons.admin.reset")))
                {
                    //try first if there is no player "all" or "all_players"
                    OfflinePlayer offall = CannonsUtil.getOfflinePlayer("all");
                    OfflinePlayer offallplayers = CannonsUtil.getOfflinePlayer("all_players");
                    if (args.length >= 2 && (
                            (args[1].equals("all") && (offall==null || !offall.hasPlayedBefore()))||
                            (args[1].equals("all_players") && (offallplayers==null || !offallplayers.hasPlayedBefore()))))
                    {
                        //remove all cannons
                        persistenceDatabase.deleteAllCannons();
                        plugin.getCannonManager().deleteAllCannons();
                        sendMessage(sender, ChatColor.GREEN + "All cannons have been deleted");
                    }
                    else if (args.length >= 2 && args[1] != null)
                    {
                        // delete all cannon entries for this player
                        OfflinePlayer offplayer = CannonsUtil.getOfflinePlayer(args[1]);
                        if (offplayer != null && offplayer.hasPlayedBefore())
                        {
                            boolean b1 = plugin.getCannonManager().deleteCannons(offplayer.getUniqueId());
                            persistenceDatabase.deleteCannons(offplayer.getUniqueId());
                            if (b1)
                            {
                                //there was an entry in the list
                                sendMessage(sender, ChatColor.GREEN + userMessages.getMessage(MessageEnum.CannonsReseted).replace("PLAYER", args[1]));
                            }
                            else
                            {
                                sendMessage(sender, ChatColor.RED + "Player " + ChatColor.GOLD + args[1] + ChatColor.RED + " has no cannons.");
                            }
                        }
                        else
                        {
                            sendMessage(sender, ChatColor.RED + "Player " + ChatColor.GOLD + args[1] + ChatColor.RED + " not found");
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
                        OfflinePlayer offplayer = CannonsUtil.getOfflinePlayer(args[1]);
                        if (offplayer != null && offplayer.hasPlayedBefore()) {
                            sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + offplayer.getName() + ChatColor.GREEN + ":");
                            for (Cannon cannon : CannonManager.getCannonList().values()) {
                                if (cannon.getOwner() != null && cannon.getOwner().equals(offplayer.getUniqueId()))
                                    sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" + ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " location:" + ChatColor.GOLD + cannon.getOffset().toString());
                            }
                        }
                    }
                    else
                    {
                        //plot all cannons
                        sendMessage(sender, ChatColor.GREEN + "List of all cannons:");
                        for (Cannon cannon : CannonManager.getCannonList().values())
                        {
                            if (cannon.getOwner() != null) {
                                OfflinePlayer owner = Bukkit.getOfflinePlayer(cannon.getOwner());
                                sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " owner:" + ChatColor.GOLD + owner.getName() + ChatColor.GREEN + " location:" + ChatColor.GOLD + cannon.getOffset().toString());
                            }
                        }
                    }
                    return true;
                }
                //cannons create
                else if(args[0].equalsIgnoreCase("create"))
                {
                    if (player != null && player.hasPermission("cannons.admin.create")) {
                        if (args.length >= 2) {
                            //check if the design name is valid
                            if (config.getDesignStorage().hasDesign(args[1])) {
                                sendMessage(sender, ChatColor.GREEN + "[Cannons] Create design: " + ChatColor.GOLD + args[1]);
                                CannonDesign cannonDesign = config.getDesignStorage().getDesign(args[1]);

                                Cannon cannon = new Cannon(cannonDesign, player.getWorld().getUID(), player.getLocation().toVector(), BlockFace.NORTH, player.getUniqueId());
                                //createCannon(cannon);
                                cannon.show();
                            }
                            else
                                sendMessage(sender, ChatColor.RED + "[Cannons] Design not found Available designs are: " + StringUtils.join(plugin.getMyConfig().getDesignStorage().getDesignIds(),", "));
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "[Cannons] Usage: '/cannons create <design>'");
                    }
                    else
                        plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                    return true;
                }
                //cannons give projectile
                else if(args[0].equalsIgnoreCase("give"))
                {
                    if (player != null && player.hasPermission("cannons.admin.give")){
                        if (args.length >= 2)
                        {
                            //check if the projectile id is valid
                            Projectile projectile = ProjectileStorage.getProjectile(args[1]);
                            if (projectile != null) {
                                sendMessage(sender, ChatColor.GREEN + "[Cannons] Give projectile: " + ChatColor.GOLD + args[1]);
                                int amount = 1;
                                if (args.length >= 3)
                                    try {
                                        amount = Integer.parseInt(args[2]);
                                    } catch (NumberFormatException ignored) {
                                    }
                                player.getInventory().addItem(projectile.getLoadingItem().toItemStack(amount));
                            }
                            else {
                                sendMessage(sender, ChatColor.RED + "[Cannons] Design not found. Available designs are: " + StringUtils.join(ProjectileStorage.getProjectileIds(), ", "));
                            }
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "[Cannons] Usage: '/cannons give <projectile> ] {amount}'");
                    }
                    else
                        plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
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
                                displayAllPermissions(sender, permPlayer);
                            else
                                sendMessage(sender, ChatColor.GREEN + "Player not found. Usage: " + ChatColor.GOLD + "'/cannons permissions <NAME>'");
                        }
                        //the command sender is also a player - return the permissions of the sender
                        else if (player != null)
                        {
                            displayAllPermissions(sender, player);
                        }
                        else
                            sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "'/cannons permissions <NAME>'");
                    }
                    else
                        plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
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
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        // how to build a cannon
                        userMessages.sendMessage(MessageEnum.HelpBuild, player);
                    }
                    //cannons fire
                    else if (args[0].equalsIgnoreCase("fire"))
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        // how to fire
                        userMessages.sendMessage(MessageEnum.HelpFire, player);
                    }
                    //cannons adjust
                    else if (args[0].equalsIgnoreCase("adjust"))
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        // how to adjust
                        userMessages.sendMessage(MessageEnum.HelpAdjust, player);
                    }
                    //cannons commands
                    else if (args[0].equalsIgnoreCase("commands"))
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        displayCommands(player);
                    }
                    //cannons imitating toggle
                    else if (args[0].equalsIgnoreCase("imitate") && config.isImitatedAimingEnabled())
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        if (args.length >= 2 && (args[1].equalsIgnoreCase("true")||args[1].equalsIgnoreCase("enable")))
                            plugin.getAiming().enableImitating(player);
                        else if (args.length >= 2 && (args[1].equalsIgnoreCase("false")||args[1].equalsIgnoreCase("disable")))
                            plugin.getAiming().disableImitating(player);
                        else
                            plugin.getAiming().toggleImitating(player);
                    }
                    //buy cannon
                    else if(args[0].equalsIgnoreCase("buy"))
                    {
                        if (!player.hasPermission("cannons.player.build"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        toggleBuyCannon(player, SelectCannon.BUY_CANNON);
                        return true;
                    }
                    //rename cannon
                    else if(args[0].equalsIgnoreCase("rename"))
                    {
                        if (!player.hasPermission("cannons.player.rename"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        if (args.length >= 3 && args[1]!=null  && args[2]!=null)
                        {
                            //selection done by a string '/cannons rename OLD NEW'
                            Cannon cannon = CannonManager.getCannon(args[1]);
                            if (cannon != null)
                            {
                                MessageEnum message = plugin.getCannonManager().renameCannon(player, cannon, args[2]);
                                userMessages.sendMessage(message, player, cannon);
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
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        if (args.length >= 2 && (args[1].equalsIgnoreCase("off")||args[1].equalsIgnoreCase("disable")||args[1].equalsIgnoreCase("remove")))
                            plugin.getAiming().removeObserverForAllCannons(player);
                        else if (args.length < 2)
                            toggleCannonSelector(player, SelectCannon.OBSERVER);
                        else if (args.length >= 2 && args[1]!=null)
                        {
                            //selection done by a string '/cannons observer CANNON_NAME'
                            Cannon cannon = CannonManager.getCannon(args[1]);
                            if (cannon!=null)
                                cannon.toggleObserver(player, false);
                            else
                                userMessages.sendMessage(MessageEnum.CmdCannonNotFound, player);
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "Usage '/cannons observer' or '/cannons observer <off|disable>' or '/cannons observer <CANNON NAME>'");
                    }
                    //add player to whitelist
                    else if(args[0].equalsIgnoreCase("whitelist"))
                    {
                        if (!player.hasPermission("cannons.player.whitelist"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        //selection done by a string '/cannons observer add|remove NAME'
                        if (args.length >= 3 && (args[1].equalsIgnoreCase("add"))) {
                            OfflinePlayer offPlayer = CannonsUtil.getOfflinePlayer(args[2]);
                            if (offPlayer != null && offPlayer.hasPlayedBefore()) {
                                whitelistPlayer.put(player.getUniqueId(), offPlayer.getUniqueId());
                                toggleCannonSelector(player, SelectCannon.WHITELIST_ADD);
                            }
                            else
                                userMessages.sendMessage(MessageEnum.ErrorPlayerNotFound, player);
                        }
                        else  if (args.length >= 3 && (args[1].equalsIgnoreCase("remove"))) {
                            OfflinePlayer offPlayer = CannonsUtil.getOfflinePlayer(args[2]);
                            if (offPlayer != null && offPlayer.hasPlayedBefore()) {
                                whitelistPlayer.put(player.getUniqueId(), offPlayer.getUniqueId());
                                toggleCannonSelector(player, SelectCannon.WHITELIST_REMOVE);
                            }
                            else
                                userMessages.sendMessage(MessageEnum.ErrorPlayerNotFound, player);
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "Usage '/cannons whitelist <add|remove> <NAME>'");
                    }
                    //toggle sentry target
                    else if(args[0].equalsIgnoreCase("target"))
                    {
                        if (!player.hasPermission("cannons.player.target"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        //selection done by a string '/cannons target mob|player|cannon'
                        if (args.length >= 2 && (args[1].equalsIgnoreCase("mob"))) {
                            toggleCannonSelector(player, SelectCannon.TARGET_MOB);
                        }
                        else if (args.length >= 2 && (args[1].equalsIgnoreCase("player"))) {
                            toggleCannonSelector(player, SelectCannon.TARGET_PLAYER);
                        }
                        else if (args.length >= 2 && (args[1].equalsIgnoreCase("cannon"))) {
                            toggleCannonSelector(player, SelectCannon.TARGET_CANNON);
                        }
                        else
                            sendMessage(sender, ChatColor.RED + "Usage '/cannons target <mob|player|cannon>'");
                    }
                    //get name of cannon
                    else if(args[0].equalsIgnoreCase("info"))
                    {
                        if (!player.hasPermission("cannons.player.info"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        toggleCannonSelector(player, SelectCannon.INFO);
                    }
                    //get name of cannon
                    else if(args[0].equalsIgnoreCase("dismantle"))
                    {
                        if (!player.hasPermission("cannons.player.dismantle") && !player.hasPermission("cannons.admin.dismantle"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
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
                        for (Cannon cannon : CannonManager.getCannonList().values())
                        {
                            if (cannon.getOwner() != null && cannon.getOwner().equals(player.getUniqueId()))
                                sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" +
                                        ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " loc: " + ChatColor.GOLD + cannon.getOffset().toString());
                        }
                        //show cannon limit
                        int buildlimit = plugin.getCannonManager().getCannonBuiltLimit(player);
                        if (buildlimit < Integer.MAX_VALUE){
                            int ncannon = plugin.getCannonManager().getNumberOfCannons(player.getUniqueId());
                            int newcannons = buildlimit - ncannon;
                            if (newcannons > 0)
                                sendMessage(sender, ChatColor.GREEN + "You can build " + ChatColor.GOLD + newcannons + ChatColor.GREEN + " additional cannons");
                            else
                                sendMessage(sender, ChatColor.RED + "You reached your maximum number of cannons");
                        }
                    }
                    //cannons reset
                    else if(args[0].equalsIgnoreCase("reset"))
                    {
                        if (!player.hasPermission("cannons.player.reset"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        // delete all cannon entries for this player
                        persistenceDatabase.deleteCannons(player.getUniqueId());
                        plugin.getCannonManager().deleteCannons(player.getUniqueId());
                        userMessages.sendMessage(MessageEnum.CannonsReseted, player);
                    }
                    //no help message if it is forbidden for this player
                    else
                    {
                        if (!player.hasPermission("cannons.player.command"))
                        {
                            plugin.logDebug("[Cannons] " + sender.getName() + " has no permission for command /cannons " + args[0]);
                            return true;
                        }
                        // display help
                        userMessages.sendMessage(MessageEnum.HelpText, player);
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
                        userMessages.sendMessage(MessageEnum.HelpText, player);
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
            userMessages.sendMessage(MessageEnum.CmdSelectCannon, player);
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
            userMessages.sendMessage(MessageEnum.CmdSelectCanceled, player);
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
     * this player will be removed from the buying mode
     * @param player the player will be removed
     * @param cmd this command will be performed when the cannon is selected
     */
    public void addBuyCannon(Player player, SelectCannon cmd)
    {
        if (player == null || cmd == null)
            return;

        if (!isSelectingMode(player))
        {
            cannonSelector.put(player.getUniqueId(),cmd);
            userMessages.sendMessage(MessageEnum.CmdBuyCannon, player);
        }
    }

    /**
     * this player will be removed from the buying mode
     * @param player the player will be removed
     */
    public void removeBuyCannon(Player player)
    {
        if (player == null)
            return;

        if (isSelectingMode(player))
        {
            cannonSelector.remove(player.getUniqueId());
            userMessages.sendMessage(MessageEnum.CmdSelectCanceled, player);
        }
    }

    /**
     * buying mode will be toggled
     * @param player the player using the selecting mode
     * @param cmd this command will be performed when the cannon is selected
     */
    public void toggleBuyCannon(Player player, SelectCannon cmd)
    {
        if (player == null)
            return;

        if (isSelectingMode(player))
            removeBuyCannon(player);
        else
            addBuyCannon(player, cmd);
    }


    /**
     * Checks if this player is in selecting mode
     * @param player player to check
     * @return true if in selecting mode
     */
    public boolean isSelectingMode(Player player) {
        return player != null && cannonSelector.containsKey(player.getUniqueId());
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
                    userMessages.sendMessage(message, player, cannon);
                    CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    break;
                }
                case INFO:{
                    userMessages.sendMessage(MessageEnum.CannonInfo, player, cannon);
                    CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    break;
                }
                case DISMANTLE:{
                    plugin.getCannonManager().dismantleCannon(cannon, player);
                    break;
                }
                case WHITELIST_ADD:{
                    if (!cannon.getCannonDesign().isSentry()){
                        userMessages.sendMessage(MessageEnum.CmdNoSentryWhitelist, player, cannon);
                        CannonsUtil.playErrorSound(cannon.getMuzzle());
                    }
                    else {
                        cannon.addWhitelistPlayer(whitelistPlayer.get(player.getUniqueId()));
                        whitelistPlayer.remove(player.getUniqueId());
                        userMessages.sendMessage(MessageEnum.CmdAddedWhitelist, player, cannon);
                        CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    }
                    break;
                }
                case WHITELIST_REMOVE:{
                    if (!cannon.getCannonDesign().isSentry()){
                        userMessages.sendMessage(MessageEnum.CmdNoSentryWhitelist, player, cannon);
                        CannonsUtil.playErrorSound(cannon.getMuzzle());
                    }
                    else {
                        cannon.removeWhitelistPlayer(whitelistPlayer.get(player.getUniqueId()));
                        whitelistPlayer.remove(player.getUniqueId());
                        userMessages.sendMessage(MessageEnum.CmdRemovedWhitelist, player, cannon);
                        CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    }
                    break;
                }
                case TARGET_MOB:{
                    if (player.getUniqueId() != cannon.getOwner()) {
                        userMessages.sendMessage(MessageEnum.ErrorNotTheOwner, player, cannon);
                        CannonsUtil.playErrorSound(cannon.getMuzzle());
                    }
                    else {
                        cannon.toggleTargetMob();
                        userMessages.sendMessage(MessageEnum.CmdToggledTargetMob, player, cannon);
                        CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    }
                    break;
                }
                case TARGET_PLAYER:{
                    if (player.getUniqueId() != cannon.getOwner()) {
                        userMessages.sendMessage(MessageEnum.ErrorNotTheOwner, player, cannon);
                        CannonsUtil.playErrorSound(cannon.getMuzzle());
                    }
                    else {
                        cannon.toggleTargetPlayer();
                        userMessages.sendMessage(MessageEnum.CmdToggledTargetPlayer, player, cannon);
                        CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    }
                    break;
                }
                case TARGET_CANNON:{
                    if (player.getUniqueId() != cannon.getOwner()) {
                        userMessages.sendMessage(MessageEnum.ErrorNotTheOwner, player, cannon);
                        CannonsUtil.playErrorSound(cannon.getMuzzle());
                    }
                    else {
                        cannon.toggleTargetCannon();
                        userMessages.sendMessage(MessageEnum.CmdToggledTargetCannon, player, cannon);
                        CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                    }
                    break;
                }
                case BUY_CANNON:{
                    if (cannon.isPaid()){
                        userMessages.sendMessage(MessageEnum.ErrorAlreadyPaid, player, cannon);
                        CannonsUtil.playErrorSound(cannon.getMuzzle());
                    }
                    else{
                        //redraw money if required
                        if (plugin.getEconomy() != null && cannon.getCannonDesign().getEconomyBuildingCost() > 0) {
                            EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, cannon.getCannonDesign().getEconomyBuildingCost());
                            if (!r.transactionSuccess()) {
                                userMessages.sendMessage(MessageEnum.ErrorNoMoney, player, cannon);
                                CannonsUtil.playErrorSound(cannon.getMuzzle());
                            }
                            else {
                                cannon.boughtByPlayer(player.getUniqueId());
                                //CannonsUtil.playSound();
                                userMessages.sendMessage(MessageEnum.CmdPaidCannon, player, cannon);
                                CannonsUtil.playSound(cannon.getMuzzle(), cannon.getCannonDesign().getSoundSelected());
                            }
                        }
                    }
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
    private void displayPermission(CommandSender sender, Player player, String permission)
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
     * display all default permissions of the player to the sender
     * @param sender command sender
     * @param permPlayer the permission of this player will be checked
     */
    private void displayAllPermissions(CommandSender sender, Player permPlayer)
    {
        sendMessage(sender, ChatColor.GREEN + "Permissions for " + ChatColor.GOLD + permPlayer.getName() + ChatColor.GREEN + ":");
        displayPermission(sender, permPlayer, "cannons.player.command");
        displayPermission(sender, permPlayer, "cannons.player.info");
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
        displayPermission(sender, permPlayer, "cannons.player.target");
        displayPermission(sender, permPlayer, "cannons.player.whitelist");
        displayPermission(sender, permPlayer, "cannons.player.reset");
        displayPermission(sender, permPlayer, "cannons.player.list");
        displayPermission(sender, permPlayer, "cannons.projectile.default");
        displayPermission(sender, permPlayer, "cannons.limit.limitA");
        displayPermission(sender, permPlayer, "cannons.limit.limitB");
        int newBuildlimit = plugin.getCannonManager().getNewBuildLimit(permPlayer);
        if (newBuildlimit==Integer.MAX_VALUE)
            sendMessage(sender, ChatColor.YELLOW + "no Permission cannons.limit.x (with 0<=x<=100)");
        else
            displayPermission(sender, permPlayer, "cannons.limit." + newBuildlimit);
        int numberCannons = plugin.getCannonManager().getNumberOfCannons(permPlayer.getUniqueId());
        int maxCannons = plugin.getCannonManager().getCannonBuiltLimit(permPlayer);
        if (maxCannons == Integer.MAX_VALUE)
            sendMessage(sender, ChatColor.YELLOW + "Built cannons: " + ChatColor.GOLD + numberCannons);
        else
            sendMessage(sender, ChatColor.YELLOW + "Built cannons: " + ChatColor.GOLD + numberCannons + "/" + maxCannons);
        displayPermission(sender, permPlayer, "cannons.admin.reload");
        displayPermission(sender, permPlayer, "cannons.admin.reset");
        displayPermission(sender, permPlayer, "cannons.admin.list");
        displayPermission(sender, permPlayer, "cannons.admin.create");
        displayPermission(sender, permPlayer, "cannons.admin.dismantle");
        displayPermission(sender, permPlayer, "cannons.admin.give");
        displayPermission(sender, permPlayer, "cannons.admin.permissions");
    }

    /**
     * displays the given permission of the player
     * @param player the permission of this player will be checked
     * @param permission permission as string
     */
    private void displayCommand(Player player, String command, String permission)
    {
        if (player == null) return;

        if (permission == null || player.hasPermission(permission))
            sendMessage(player, ChatColor.YELLOW + command);
    }


    /**
     * displays all possible commands for the player
     * @param player the permission of this player will be checked
     */
    private void displayCommands(Player player) {
        sendMessage(player, ChatColor.GOLD + "Player commands:" + ChatColor.YELLOW);
        displayCommand(player, "/cannons build", "cannons.player.command");
        displayCommand(player, "/cannons fire", "cannons.player.command");
        displayCommand(player, "/cannons adjust", "cannons.player.command");
        displayCommand(player, "/cannons commands", "cannons.player.command");
        displayCommand(player, "/cannons imitate", null);
        displayCommand(player, "/cannons rename [OLD] [NEW]", "cannons.player.rename");
        displayCommand(player, "/cannons observer", "cannons.player.observer");
        displayCommand(player, "/cannons info", "cannons.player.info");
        displayCommand(player, "/cannons list", "cannons.player.list");
        displayCommand(player, "/cannons target", "cannons.player.target");
        displayCommand(player, "/cannons whitelist add [NAME]", "cannons.player.whitelist");
        displayCommand(player, "/cannons whitelist remove [NAME]", "cannons.player.whitelist");
        sendMessage(player, ChatColor.GOLD + "Admin commands:" + ChatColor.YELLOW);
        displayCommand(player, "/cannons list [NAME]", "cannons.admin.list");
        displayCommand(player, "/cannons create [DESIGN]", "cannons.admin.create");
        displayCommand(player, "/cannons dismantle", "cannons.admin.dismantle");
        displayCommand(player, "/cannons reset", "cannons.admin.reset");
        displayCommand(player, "/cannons reload", "cannons.admin.reload");
        displayCommand(player, "/cannons save", "cannons.admin.save");
        displayCommand(player, "/cannons load", "cannons.admin.load");
        displayCommand(player, "/cannons give", "cannons.admin.give");
        displayCommand(player, "/cannons permissions [NAME]", "cannons.admin.permissions");
    }


}
