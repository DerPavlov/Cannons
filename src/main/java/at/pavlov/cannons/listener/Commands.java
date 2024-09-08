package at.pavlov.cannons.listener;

import at.pavlov.cannons.Aiming;
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
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;
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
import java.util.Locale;

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

    @Subcommand("save")
    @CommandPermission("cannons.admin.reload")
    public static void onSave(CommandSender sender) {
        // save database
        Cannons.getPlugin().getPersistenceDatabase().saveAllCannons(true);
        sendMessage(sender, ChatColor.GREEN + "Cannons database saved ");
    }

    @Subcommand("load")
    @CommandPermission("cannons.admin.reload")
    public static void onLoad(CommandSender sender) {
        // load database
        Cannons.getPlugin().getPersistenceDatabase().loadCannons();
        sendMessage(sender, ChatColor.GREEN + "Cannons database loaded ");
    }

    @Subcommand("reset")
    @Syntax("[all|all_players|PLAYER")
    @CommandPermission("cannons.admin.reset")
    public static void onReset(CommandSender sender, String arg) {
        //try first if there is no player "all" or "all_players"
        OfflinePlayer offall = CannonsUtil.getOfflinePlayer("all");
        OfflinePlayer offallplayers = CannonsUtil.getOfflinePlayer("all_players");

        Cannons plugin = Cannons.getPlugin();
        UserMessages userMessages = plugin.getMyConfig().getUserMessages();
        PersistenceDatabase persistenceDatabase = plugin.getPersistenceDatabase();

        if (arg.equals("all") &&
                (offall == null || !offall.hasPlayedBefore()) ||
                arg.equals("all_players") &&
                        (offallplayers == null || !offallplayers.hasPlayedBefore())) {
            //remove all cannons
            persistenceDatabase.deleteAllCannons();
            plugin.getCannonManager().deleteAllCannons();
            sendMessage(sender, ChatColor.GREEN + "All cannons have been deleted");
            return;
        }

        // delete all cannon entries for this player
        OfflinePlayer offplayer = CannonsUtil.getOfflinePlayer(arg);
        if (offplayer == null || !offplayer.hasPlayedBefore()) {
            sendMessage(sender, ChatColor.RED + "Player " + ChatColor.GOLD + arg + ChatColor.RED + " not found");
            return;
        }

        boolean b1 = plugin.getCannonManager().deleteCannons(offplayer.getUniqueId());
        persistenceDatabase.deleteCannons(offplayer.getUniqueId());
        if (b1) {
            //there was an entry in the list
            sendMessage(sender, ChatColor.GREEN + userMessages.getMessage(MessageEnum.CannonsReseted).replace("PLAYER", arg));
        } else {
            sendMessage(sender, ChatColor.RED + "Player " + ChatColor.GOLD + arg + ChatColor.RED + " has no cannons.");
        }
    }

    @Subcommand("list")
    @Syntax("<PLAYER>")
    @CommandPermission("cannons.admin.list")
    public static void onList(CommandSender sender, @Optional String arg) {
        if (arg != null) {
            //additional player name
            OfflinePlayer offplayer = CannonsUtil.getOfflinePlayer(arg);
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
    }

    @Subcommand("create")
    @Syntax("[DESIGN]")
    @CommandPermission("cannons.admin.create")
    public static void onCreate(Player player, String arg) {
        Cannons plugin = Cannons.getPlugin();
        //check if the design name is valid
        if (!plugin.getDesignStorage().hasDesign(arg)) {
            sendMessage(player, ChatColor.RED + tag + "Design not found Available designs are: " + StringUtils.join(plugin.getDesignStorage().getDesignIds(), ", "));
            return;
        }

        sendMessage(player, ChatColor.GREEN + tag + "Create design: " + ChatColor.GOLD + arg);
        CannonDesign cannonDesign = plugin.getDesignStorage().getDesign(arg);

        Cannon cannon = new Cannon(cannonDesign, player.getWorld().getUID(), player.getLocation().toVector(), BlockFace.NORTH, player.getUniqueId());
        //createCannon(cannon);
        cannon.show();
    }

    @Subcommand("give")
    @Syntax("[PROJECTILE] <amount>")
    @CommandPermission("cannons.admin.give")
    public static void onGive(Player player, String projectileString, @Default("1") int amount) {
        //check if the projectile id is valid
        Projectile projectile = ProjectileStorage.getProjectile(projectileString);
        if (projectile == null) {
            String out = StringUtils.join(ProjectileStorage.getProjectileIds(), ", ");
            sendMessage(player, ChatColor.RED + tag + "Design not found. Available designs are: " + out);
            return;
        }

        sendMessage(player, ChatColor.GREEN + tag + "Give projectile: " + ChatColor.GOLD + projectileString);
        player.getInventory().addItem(projectile.getLoadingItem().toItemStack(amount));
    }

    @Subcommand("permissions")
    @Syntax("<PLAYER>")
    @CommandPermission("cannons.admin.permissions")
    public static void onPermission(CommandSender sender, @Optional String subject) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        //given name in args[1]
        if (subject != null) {
            Player permPlayer = Bukkit.getPlayer(subject);
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
    }

    @Subcommand("build")
    @CommandPermission("cannons.player.command")
    public static void onBuild(Player player) {
        var userMessages = Cannons.getPlugin().getMyConfig().getUserMessages();
        userMessages.sendMessage(MessageEnum.HelpBuild, player);
    }

    @Subcommand("fire")
    @CommandPermission("cannons.player.command")
    public static void onFire(Player player) {
        var userMessages = Cannons.getPlugin().getMyConfig().getUserMessages();
        userMessages.sendMessage(MessageEnum.HelpFire, player);
    }

    @Subcommand("adjust")
    @CommandPermission("cannons.player.command")
    public static void onAdjust(Player player) {
        var userMessages = Cannons.getPlugin().getMyConfig().getUserMessages();
        userMessages.sendMessage(MessageEnum.HelpAdjust, player);
    }

    @Subcommand("commands")
    @CommandPermission("cannons.player.command")
    public static void onDisplayCommand(Player player) {
        displayCommands(player);
    }

    @Subcommand("imitate")
    @CommandCompletion("true|enable|false|disable")
    @CommandPermission("cannons.player.command")
    public static void onImitate(Player player, @Optional String arg) {
        Cannons plugin = Cannons.getPlugin();
        Aiming aiming = plugin.getAiming();
        Config config = plugin.getMyConfig();

        if (!config.isImitatedAimingEnabled()) {
            return;
        }

        if (arg == null) {
            aiming.toggleImitating(player);
            return;
        }

        switch (arg.toLowerCase(Locale.ROOT)) {
            case "true", "enable" -> aiming.enableImitating(player);
            case "false", "disable" -> aiming.disableImitating(player);
        }
    }

    @Subcommand("buy")
    @CommandPermission("cannons.player.build")
    public static void onBuy(Player player) {
        CannonSelector selector = CannonSelector.getInstance();
        selector.toggleBuyCannon(player, SelectCannon.BUY_CANNON);
    }

    @Subcommand("rename")
    @CommandPermission("cannons.player.rename")
    public static void onRename(Player player, String[] args) {
        //TODO: When calling this all default names have a space in it so it won't work as expected
        if (args.length < 2 || args[0] == null || args[1] == null) {
            sendMessage(player, ChatColor.RED + "Usage '/cannons rename <OLD_NAME> <NEW_NAME>'");
            return;
        }

        //selection done by a string '/cannons rename OLD NEW'
        Cannons plugin = Cannons.getPlugin();
        UserMessages userMessages = plugin.getMyConfig().getUserMessages();
        Cannon cannon = CannonManager.getCannon(args[0]);
        if (cannon == null) {
            sendMessage(player, ChatColor.RED + "Cannon not found");
            return;
        }

        MessageEnum message = plugin.getCannonManager().renameCannon(player, cannon, args[1]);
        userMessages.sendMessage(message, player, cannon);
    }

    @Subcommand("observer")
    @Syntax("<off|disable|CANNON_NAME>")
    @CommandPermission("cannons.player.observer")
    public static void onObserver(Player player, String[] args) {
        CannonSelector selector = CannonSelector.getInstance();

        if (args.length < 1) {
            selector.toggleCannonSelector(player, SelectCannon.OBSERVER);
            return;
        }

        Cannons plugin = Cannons.getPlugin();
        UserMessages userMessages = plugin.getMyConfig().getUserMessages();

        if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable") || args[0].equalsIgnoreCase("remove"))
            plugin.getAiming().removeObserverForAllCannons(player);
        else {
            //selection done by a string '/cannons observer CANNON_NAME'
            Cannon cannon = CannonManager.getCannon(args[0]);
            if (cannon != null)
                cannon.toggleObserver(player, false);
            else
                userMessages.sendMessage(MessageEnum.CmdCannonNotFound, player);
        }
        //this never gets called
        //sendMessage(sender, ChatColor.RED + "Usage '/cannons observer' or '/cannons observer <off|disable>' or '/cannons observer <CANNON NAME>'");
    }

    @Subcommand("whitelist")
    @CommandPermission("cannons.player.whitelist")
    public class onWhitelist extends BaseCommand {

        @Default
        public static void help(Player player) {
            sendMessage(player, ChatColor.RED + "Usage '/cannons whitelist <add|remove> <NAME>'");
        }

        @Subcommand("add")
        public static void onAdd(Player player, String subject) {
            OfflinePlayer offPlayer = CannonsUtil.getOfflinePlayer(subject);
            CannonSelector selector = CannonSelector.getInstance();
            UserMessages userMessages = Cannons.getPlugin().getMyConfig().getUserMessages();

            if (offPlayer != null && offPlayer.hasPlayedBefore())
                selector.toggleCannonSelector(player, SelectCannon.WHITELIST_ADD, offPlayer);
            else
                userMessages.sendMessage(MessageEnum.ErrorPlayerNotFound, player);
        }

        @Subcommand("remove")
        public static void onRemove(Player player, String subject) {
            OfflinePlayer offPlayer = CannonsUtil.getOfflinePlayer(subject);
            CannonSelector selector = CannonSelector.getInstance();
            UserMessages userMessages = Cannons.getPlugin().getMyConfig().getUserMessages();

            if (offPlayer != null && offPlayer.hasPlayedBefore()) {
                selector.toggleCannonSelector(player, SelectCannon.WHITELIST_REMOVE, offPlayer);
            } else
                userMessages.sendMessage(MessageEnum.ErrorPlayerNotFound, player);
        }
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

        //################### Player only commands #####################
        if (player == null) {
            plugin.logDebug("This command can only be used by a player");
            return;
        }
        //toggle sentry target
        if (args[0].equalsIgnoreCase("target")) {
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
