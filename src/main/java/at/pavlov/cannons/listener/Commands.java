package at.pavlov.cannons.listener;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.CommandList;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.Enum.SelectCannon;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.cannon.DesignStorage;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.dao.PersistenceDatabase;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonSelector;
import at.pavlov.cannons.utils.CannonsUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@CommandAlias("cannons")
public class Commands extends BaseCommand {
    private static final String tag = "[Cannons] ";

    @HelpCommand
    @CommandPermission("cannons.player.command")
    public static void onHelpCommand(Player sender) {
        Cannons plugin = Cannons.getPlugin();
        UserMessages userMessages = plugin.getMyConfig().getUserMessages();
        userMessages.sendMessage(MessageEnum.HelpText, sender);
    }

    @Subcommand("reload")
    @CommandPermission("cannons.admin.reload")
    public static void onReload(CommandSender sender) {
        Config config = Cannons.getPlugin().getMyConfig();
        config.loadConfig();
        DesignStorage.getInstance().loadCannonDesigns();
        sendMessage(sender, ChatColor.GREEN + tag + "Config loaded");
    }


    @Default
    public static void onCommand(CommandSender sender, String[] args) {

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        Cannons plugin = Cannons.getPlugin();
        Config config = plugin.getMyConfig();
        UserMessages userMessages = config.getUserMessages();
        CannonSelector selector = CannonSelector.getInstance();
        PersistenceDatabase persistenceDatabase = plugin.getPersistenceDatabase();

        String noPerm = " has no permission for command /cannons ";
        
        if (args.length < 1) { //console command
            //no help message if it is forbidden for this player
            if (player == null) {
                plugin.logInfo("Cannons plugin v" + plugin.getPluginDescription().getVersion() + " is running");
                return;
            }
            return;
        }

        //############## console and player commands ######################
        //cannons save
        if (args[0].equalsIgnoreCase("save")) {
            if (player == null || player.hasPermission("cannons.admin.reload")) {
                // save database
                persistenceDatabase.saveAllCannons(true);
                sendMessage(sender, ChatColor.GREEN + "Cannons database saved ");
            } else
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
            return;
        }
        //cannons load
        else if (args[0].equalsIgnoreCase("load")) {
            if (player == null || player.hasPermission("cannons.admin.reload")) {
                // load database
                persistenceDatabase.loadCannons();
                sendMessage(sender, ChatColor.GREEN + "Cannons database loaded ");
            } else
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
            return;
        }
        //cannons reset
        else if (args[0].equalsIgnoreCase("reset") && (player == null || player.hasPermission("cannons.admin.reset"))) {
            //try first if there is no player "all" or "all_players"
            OfflinePlayer offall = CannonsUtil.getOfflinePlayer("all");
            OfflinePlayer offallplayers = CannonsUtil.getOfflinePlayer("all_players");
            if (args.length >= 2 && (
                    (args[1].equals("all") && (offall == null || !offall.hasPlayedBefore())) ||
                            (args[1].equals("all_players") && (offallplayers == null || !offallplayers.hasPlayedBefore())))) {
                //remove all cannons
                persistenceDatabase.deleteAllCannons();
                plugin.getCannonManager().deleteAllCannons();
                sendMessage(sender, ChatColor.GREEN + "All cannons have been deleted");
                return;
            }

            if (args.length >= 2 && args[1] != null) {
                // delete all cannon entries for this player
                OfflinePlayer offplayer = CannonsUtil.getOfflinePlayer(args[1]);
                if (offplayer == null || !offplayer.hasPlayedBefore()) {
                    sendMessage(sender, ChatColor.RED + "Player " + ChatColor.GOLD + args[1] + ChatColor.RED + " not found");
                    return;
                }

                boolean b1 = plugin.getCannonManager().deleteCannons(offplayer.getUniqueId());
                persistenceDatabase.deleteCannons(offplayer.getUniqueId());
                if (b1) {
                    //there was an entry in the list
                    sendMessage(sender, ChatColor.GREEN + userMessages.getMessage(MessageEnum.CannonsReseted).replace("PLAYER", args[1]));
                } else {
                    sendMessage(sender, ChatColor.RED + "Player " + ChatColor.GOLD + args[1] + ChatColor.RED + " has no cannons.");
                }
                return;
            }

            sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "'/cannons reset <NAME>' or '/cannons reset all' or '/cannons reset all_players'");
            return;
        }
        //cannons list
        else if (args[0].equalsIgnoreCase("list") && (player == null || player.hasPermission("cannons.admin.list"))) {
            if (args.length >= 2) {
                //additional player name
                OfflinePlayer offplayer = CannonsUtil.getOfflinePlayer(args[1]);
                if (offplayer == null || !offplayer.hasPlayedBefore()) {
                    return;
                }

                sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + offplayer.getName() + ChatColor.GREEN + ":");
                for (Cannon cannon : CannonManager.getCannonList().values()) {
                    if (cannon.getOwner() != null && cannon.getOwner().equals(offplayer.getUniqueId()))
                        sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" + ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " location:" + ChatColor.GOLD + cannon.getOffset().toString());
                }
                return;
            }

            //plot all cannons
            sendMessage(sender, ChatColor.GREEN + "List of all cannons:");
            for (Cannon cannon : CannonManager.getCannonList().values()) {
                if (cannon.getOwner() != null) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(cannon.getOwner());
                    sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " owner:" + ChatColor.GOLD + owner.getName() + ChatColor.GREEN + " location:" + ChatColor.GOLD + cannon.getOffset().toString());
                }
            }
            return;
        }
        //cannons create
        else if (args[0].equalsIgnoreCase("create")) {
            if (player == null || !player.hasPermission("cannons.admin.create")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }

            if (args.length < 2) {
                sendMessage(sender, ChatColor.RED + tag + "Usage: '/cannons create <design>'");
                return;
            }
            //check if the design name is valid
            if (!plugin.getDesignStorage().hasDesign(args[1])) {
                sendMessage(sender, ChatColor.RED + tag + "Design not found Available designs are: " + StringUtils.join(plugin.getDesignStorage().getDesignIds(), ", "));
                return;
            }

            sendMessage(sender, ChatColor.GREEN + tag + "Create design: " + ChatColor.GOLD + args[1]);
            CannonDesign cannonDesign = plugin.getDesignStorage().getDesign(args[1]);

            Cannon cannon = new Cannon(cannonDesign, player.getWorld().getUID(), player.getLocation().toVector(), BlockFace.NORTH, player.getUniqueId());
            //createCannon(cannon);
            cannon.show();
            return;
        }
        //cannons give projectile
        else if (args[0].equalsIgnoreCase("give")) {
            if (player == null || !player.hasPermission("cannons.admin.give")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }

            if (args.length < 2) {
                sendMessage(sender, ChatColor.RED + tag +"Usage: '/cannons give <projectile> ] {amount}'");
                return;
            }

            //check if the projectile id is valid
            Projectile projectile = ProjectileStorage.getProjectile(args[1]);
            if (projectile == null) {
                sendMessage(sender, ChatColor.RED + tag + "Design not found. Available designs are: " + StringUtils.join(ProjectileStorage.getProjectileIds(), ", "));
                return;
            }
            sendMessage(sender, ChatColor.GREEN + tag + "Give projectile: " + ChatColor.GOLD + args[1]);
            int amount = 1;
            if (args.length >= 3)
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                }
            player.getInventory().addItem(projectile.getLoadingItem().toItemStack(amount));
            return;
        }
        //cannons permissions
        else if (args[0].equalsIgnoreCase("permissions")) {
            if (player != null && !player.hasPermission("cannons.admin.permissions")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }

            //given name in args[1]
            if (args.length >= 2 && args[1] != null) {
                Player permPlayer = Bukkit.getPlayer(args[1]);
                if (permPlayer != null)
                    displayAllPermissions(sender, permPlayer);
                else
                    sendMessage(sender, ChatColor.GREEN + "Player not found. Usage: " + ChatColor.GOLD + "'/cannons permissions <NAME>'");
            }
            //the command sender is also a player - return the permissions of the sender
            else if (player != null) {
                displayAllPermissions(sender, player);
            } else
                sendMessage(sender, ChatColor.GREEN + "Missing player name " + ChatColor.GOLD + "'/cannons permissions <NAME>'");
            return;
        }
        //################### Player only commands #####################
        else if (player == null) {
            plugin.logDebug("This command can only be used by a player");
            return;
        }
        //cannons build
        if (args[0].equalsIgnoreCase("build")) {
            if (!player.hasPermission("cannons.player.command")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            // how to build a cannon
            userMessages.sendMessage(MessageEnum.HelpBuild, player);
        }
        //cannons fire
        else if (args[0].equalsIgnoreCase("fire")) {
            if (!player.hasPermission("cannons.player.command")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            // how to fire
            userMessages.sendMessage(MessageEnum.HelpFire, player);
        }
        //cannons adjust
        else if (args[0].equalsIgnoreCase("adjust")) {
            if (!player.hasPermission("cannons.player.command")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            // how to adjust
            userMessages.sendMessage(MessageEnum.HelpAdjust, player);
        }
        //cannons commands
        else if (args[0].equalsIgnoreCase("commands")) {
            if (!player.hasPermission("cannons.player.command")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            displayCommands(player);
        }
        //cannons imitating toggle
        else if (args[0].equalsIgnoreCase("imitate") && config.isImitatedAimingEnabled()) {
            if (!player.hasPermission("cannons.player.command")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            if (args.length >= 2 && (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("enable")))
                plugin.getAiming().enableImitating(player);
            else if (args.length >= 2 && (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("disable")))
                plugin.getAiming().disableImitating(player);
            else
                plugin.getAiming().toggleImitating(player);
        }
        //buy cannon
        else if (args[0].equalsIgnoreCase("buy")) {
            if (!player.hasPermission("cannons.player.build")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            selector.toggleBuyCannon(player, SelectCannon.BUY_CANNON);
            return;
        }
        //rename cannon
        else if (args[0].equalsIgnoreCase("rename")) {
            if (!player.hasPermission("cannons.player.rename")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            if (args.length >= 3 && args[1] != null && args[2] != null) {
                //selection done by a string '/cannons rename OLD NEW'
                Cannon cannon = CannonManager.getCannon(args[1]);
                if (cannon != null) {
                    MessageEnum message = plugin.getCannonManager().renameCannon(player, cannon, args[2]);
                    userMessages.sendMessage(message, player, cannon);
                }
            } else
                sendMessage(sender, ChatColor.RED + "Usage '/cannons rename <OLD_NAME> <NEW_NAME>'");
            return;
        }
        //add observer for cannon
        else if (args[0].equalsIgnoreCase("observer")) {
            if (!player.hasPermission("cannons.player.observer")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            if (args.length >= 2 && (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("disable") || args[1].equalsIgnoreCase("remove")))
                plugin.getAiming().removeObserverForAllCannons(player);
            else if (args.length < 2)
                selector.toggleCannonSelector(player, SelectCannon.OBSERVER);
            else if (args.length >= 2 && args[1] != null) {
                //selection done by a string '/cannons observer CANNON_NAME'
                Cannon cannon = CannonManager.getCannon(args[1]);
                if (cannon != null)
                    cannon.toggleObserver(player, false);
                else
                    userMessages.sendMessage(MessageEnum.CmdCannonNotFound, player);
            } else
                sendMessage(sender, ChatColor.RED + "Usage '/cannons observer' or '/cannons observer <off|disable>' or '/cannons observer <CANNON NAME>'");
        }
        //add player to whitelist
        else if (args[0].equalsIgnoreCase("whitelist")) {
            if (!player.hasPermission("cannons.player.whitelist")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            //selection done by a string '/cannons observer add|remove NAME'
            if (args.length >= 3 && (args[1].equalsIgnoreCase("add"))) {
                OfflinePlayer offPlayer = CannonsUtil.getOfflinePlayer(args[2]);
                if (offPlayer != null && offPlayer.hasPlayedBefore()) {
                    selector.toggleCannonSelector(player, SelectCannon.WHITELIST_ADD, offPlayer);
                } else
                    userMessages.sendMessage(MessageEnum.ErrorPlayerNotFound, player);
            } else if (args.length >= 3 && (args[1].equalsIgnoreCase("remove"))) {
                OfflinePlayer offPlayer = CannonsUtil.getOfflinePlayer(args[2]);
                if (offPlayer != null && offPlayer.hasPlayedBefore()) {
                    selector.toggleCannonSelector(player, SelectCannon.WHITELIST_REMOVE, offPlayer);
                } else
                    userMessages.sendMessage(MessageEnum.ErrorPlayerNotFound, player);
            } else
                sendMessage(sender, ChatColor.RED + "Usage '/cannons whitelist <add|remove> <NAME>'");
        }
        //toggle sentry target
        else if (args[0].equalsIgnoreCase("target")) {
            if (!player.hasPermission("cannons.player.target")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            // set selection or use toggle as default
            boolean choice = false;
            if (args.length >= 3) {
                choice = Boolean.parseBoolean(args[2]);
                selector.putTarget(player.getUniqueId(), choice);
            }
            // additional range command to select multiple cannons
            int length = 0;
            if (args.length >= 4) {
                try {
                    length = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    length = 0;
                }
            }
            //selection done by a string '/cannons target mob|player|cannon'
            if (args.length >= 2 && (args[1].equalsIgnoreCase("mob"))) {
                if (length > 0)
                    selectCannonsInBox(player, SelectCannon.TARGET_MOB, length);
                else
                    selector.toggleCannonSelector(player, SelectCannon.TARGET_MOB);
            } else if (args.length >= 2 && (args[1].equalsIgnoreCase("player"))) {
                if (length > 0)
                    selectCannonsInBox(player, SelectCannon.TARGET_PLAYER, length);
                else
                    selector.toggleCannonSelector(player, SelectCannon.TARGET_PLAYER);
            } else if (args.length >= 2 && (args[1].equalsIgnoreCase("cannon"))) {
                if (length > 0)
                    selectCannonsInBox(player, SelectCannon.TARGET_CANNON, length);
                else
                    selector.toggleCannonSelector(player, SelectCannon.TARGET_CANNON);
            } else if (args.length >= 2 && (args[1].equalsIgnoreCase("other"))) {
                if (length > 0)
                    selectCannonsInBox(player, SelectCannon.TARGET_OTHER, length);
                else
                    selector.toggleCannonSelector(player, SelectCannon.TARGET_OTHER);
            } else {
                //remove choice for target it the command was invalid
                selector.removeTarget(player.getUniqueId());
                sendMessage(sender, ChatColor.RED + "Usage '/cannons target <mob|player|cannon|other> <true|false> <range>'");
            }
        }
        //get name of cannon
        else if (args[0].equalsIgnoreCase("info")) {
            if (!player.hasPermission("cannons.player.info")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            selector.toggleCannonSelector(player, SelectCannon.INFO);
        }
        //get name of cannon
        else if (args[0].equalsIgnoreCase("dismantle")) {
            if (!player.hasPermission("cannons.player.dismantle") && !player.hasPermission("cannons.admin.dismantle")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            selector.toggleCannonSelector(player, SelectCannon.DISMANTLE);
        }
        //list cannons of this player name
        else if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("cannons.player.list")) {
                plugin.logDebug("Missing permission 'cannons.player.list' for command /cannons " + args[0]);
                return;
            }
            sendMessage(sender, ChatColor.GREEN + "Cannon list for " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + ":");
            for (Cannon cannon : CannonManager.getCannonList().values()) {
                if (cannon.getOwner() != null && cannon.getOwner().equals(player.getUniqueId()))
                    sendMessage(sender, ChatColor.GREEN + "Name:" + ChatColor.GOLD + cannon.getCannonName() + ChatColor.GREEN + " design:" +
                            ChatColor.GOLD + cannon.getCannonDesign().getDesignName() + ChatColor.GREEN + " loc: " + ChatColor.GOLD + cannon.getOffset().toString());
            }
            //show cannon limit
            int buildlimit = plugin.getCannonManager().getCannonBuiltLimit(player);
            if (buildlimit < Integer.MAX_VALUE) {
                int ncannon = plugin.getCannonManager().getNumberOfCannons(player.getUniqueId());
                int newcannons = buildlimit - ncannon;
                if (newcannons > 0)
                    sendMessage(sender, ChatColor.GREEN + "You can build " + ChatColor.GOLD + newcannons + ChatColor.GREEN + " additional cannons");
                else
                    sendMessage(sender, ChatColor.RED + "You reached your maximum number of cannons");
            }
        }
        //cannons reset
        else if (args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("cannons.player.reset")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            // delete all cannon entries for this player
            persistenceDatabase.deleteCannons(player.getUniqueId());
            plugin.getCannonManager().deleteCannons(player.getUniqueId());
            userMessages.sendMessage(MessageEnum.CannonsReseted, player);
        }
        //get blockdata
        else if (args[0].equalsIgnoreCase("blockdata")) {
            if (!player.hasPermission("cannons.player.blockdata")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            selector.toggleCannonSelector(player, SelectCannon.BLOCK_DATA);
        }
        //claim cannons in the surrounding
        else if (args[0].equalsIgnoreCase(CommandList.CLAIM.getCommand())) {
            if (!player.hasPermission(CommandList.CLAIM.getPermission())) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            userMessages.sendMessage(MessageEnum.CmdClaimCannonsStarted, player);
            Cannons.getPlugin().getCannonManager().claimCannonsInBox(player.getLocation(), player.getUniqueId());
            userMessages.sendMessage(MessageEnum.CmdClaimCannonsFinished, player);

        } else { //no help message if it is forbidden for this player
            if (!player.hasPermission("cannons.player.command")) {
                plugin.logDebug(tag + sender.getName() + noPerm + args[0]);
                return;
            }
            // display help
            userMessages.sendMessage(MessageEnum.HelpText, player);
        }
    }

    /**
     * sends a message to the console of the player. Console messages will be striped form color
     *
     * @param sender player or console
     * @param str    message
     */
    private static void sendMessage(CommandSender sender, String str) {
        if (sender == null)
            return;

        //strip color of console messages
        if (!(sender instanceof Player))
            str = ChatColor.stripColor(str);

        sender.sendMessage(str);
    }

    /**
     * @param player player for selecting the cannon
     * @param cmd    select command to perform
     * @param length edge length of of the box
     */
    public static void selectCannonsInBox(Player player, SelectCannon cmd, int length) {
        if (player == null || length <= 0)
            return;

        if (length > 1000)
            length = 1000;

        CannonSelector selector = CannonSelector.getInstance();
        boolean choice = true;
        //buffer the selection because it will be reset after every cannon
        if (selector.containsTarget(player.getUniqueId()))
            choice = selector.getTarget(player.getUniqueId());

        HashSet<Cannon> list = CannonManager.getCannonsInBox(player.getLocation(), length, length, length);
        for (Cannon cannon : list) {
            selector.setSelectedCannon(player, cannon, cmd, choice);
        }
    }

    /**
     * displays the given permission of the player
     *
     * @param sender     command sender
     * @param player     the permission of this player will be checked
     * @param permission permission as string
     */
    private static void displayPermission(CommandSender sender, Player player, String permission) {
        if (player == null || permission == null) return;

        //request permission
        boolean hasPerm = player.hasPermission(permission);
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
     *
     * @param sender     command sender
     * @param permPlayer the permission of this player will be checked
     */
    private static void displayAllPermissions(CommandSender sender, Player permPlayer) {
        sendMessage(sender, ChatColor.GREEN + "Permissions for " + ChatColor.GOLD + permPlayer.getName() + ChatColor.GREEN + ":");
        Cannons plugin = Cannons.getPlugin();
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
        if (newBuildlimit == Integer.MAX_VALUE)
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
        displayPermission(sender, permPlayer, "cannons.admin.blockdata");
    }

    /**
     * displays the given permission of the player
     *
     * @param player     the permission of this player will be checked
     * @param permission permission as string
     */
    private static void displayCommand(Player player, String command, String permission) {
        if (player == null) return;

        if (permission == null || player.hasPermission(permission))
            sendMessage(player, ChatColor.YELLOW + command);
    }


    /**
     * displays all possible commands for the player
     *
     * @param player the permission of this player will be checked
     */
    private static void displayCommands(Player player) {
        List<CommandList> playerCmd = new ArrayList<>();
        List<CommandList> adminCmd = new ArrayList<>();
        for (CommandList cmd : CommandList.values()) {
            if (cmd.isAdminCmd())
                adminCmd.add(cmd);
            else
                playerCmd.add(cmd);
        }
        sendMessage(player, ChatColor.GOLD + "Player commands:" + ChatColor.YELLOW);
        for (CommandList cmd : playerCmd)
            displayCommand(player, cmd.getUsage(), cmd.getPermission());

        sendMessage(player, ChatColor.GOLD + "Admin commands:" + ChatColor.YELLOW);
        for (CommandList cmd : adminCmd)
            displayCommand(player, cmd.getUsage(), cmd.getPermission());
    }
}
