package at.pavlov.cannons.listener;

import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.container.DeathCause;
import at.pavlov.cannons.projectile.FlyingProjectile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import at.pavlov.cannons.Aiming;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.FireCannon;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener
{
    private final Config config;
    private final UserMessages userMessages;
    private final Cannons plugin;
    private final CannonManager cannonManager;
    private final FireCannon fireCannon;
    private final Aiming aiming;

    public PlayerListener(Cannons plugin)
    {
        this.plugin = plugin;
        this.config = this.plugin.getMyConfig();
        this.userMessages = this.plugin.getMyConfig().getUserMessages();
        this.cannonManager = this.plugin.getCannonManager();
        this.fireCannon = this.plugin.getFireCannon();
        this.aiming = this.plugin.getAiming();
    }

    @EventHandler
    public void PlayerDeath(PlayerDeathEvent event)
    {
        UUID killedUID = event.getEntity().getUniqueId();
        if (plugin.getExplosion().wasAffectedByCannons(event.getEntity())){
            //DeathCause cause = plugin.getExplosion().getDeathCause(killedUID);
            plugin.getExplosion().removeKilledPlayer(killedUID);

            Player shooter = null;
//            if (cause.getShooterUID() != null)
//                shooter = Bukkit.getPlayer(cause.getShooterUID());
//            Cannon cannon = plugin.getCannon(cause.getCannonUID());
            FlyingProjectile c = plugin.getExplosion().getCurrentCannonball();
            Cannon cannon = CannonManager.getCannon(c.getCannonUID());
            String message = userMessages.getDeathMessage(killedUID, c.getShooterUID(), cannon, c.getProjectile());
            if (message != null && !message.equals(" "))
                event.setDeathMessage(message);
        }
    }

    @EventHandler
    public void PlayerMove(PlayerMoveEvent event)
    {
        // only active if the player is in aiming mode
        Cannon cannon =  aiming.getCannonInAimingMode(event.getPlayer());
        if (!aiming.distanceCheck(event.getPlayer(), cannon)) {
            userMessages.sendMessage(MessageEnum.AimingModeTooFarAway, event.getPlayer());
            MessageEnum message = aiming.disableAimingMode(event.getPlayer(), cannon);
            userMessages.sendMessage(message, event.getPlayer());
        }
    }
    /*
    * remove Player from auto aiming list
    * @param event - PlayerQuitEvent
    */
    @EventHandler
    public void LogoutEvent(PlayerQuitEvent event)
    {
        aiming.removePlayer(event.getPlayer());
    }

    /**
     * cancels the event if the player click a cannon with water
     * @param event - PlayerBucketEmptyEvent
     */
    @EventHandler
    public void PlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {
        // if player loads a lava/water bucket in the cannon
        Location blockLoc = event.getBlockClicked().getLocation();

        Cannon cannon = cannonManager.getCannon(blockLoc, event.getPlayer().getUniqueId());

        // check if it is a cannon
        if (cannon != null)
        {
            // data =-1 means no data check, all buckets are allowed
            Projectile projectile = plugin.getProjectile(cannon, event.getItemStack());
            if (projectile != null) event.setCancelled(true);
        }
    }

    /**
     * Create a cannon if the building process is finished Deletes a projectile
     * if loaded Checks for redstone torches if built
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void BlockPlace(BlockPlaceEvent event)
    {

        Block block = event.getBlockPlaced();
        Location blockLoc = block.getLocation();

        // setup a new cannon
        cannonManager.getCannon(blockLoc, event.getPlayer().getUniqueId());

//        // delete placed projectile or gunpowder if clicked against the barrel
//        if (event.getBlockAgainst() != null)
//        {
//            Location barrel = event.getBlockAgainst().getLocation();
//
//            // check if block is cannonblock
//            Cannon cannon = cannonManager.getCannon(barrel, event.getPlayer().getUniqueId(), true);
//            if (cannon != null)
//            {
//                // delete projectile
//
//                Projectile projectile = plugin.getProjectile(cannon, event.getItemInHand());
//                if (projectile != null && cannon.getCannonDesign().canLoad(projectile))
//                {
//                    // check if the placed block is not part of the cannon
//                    if (!cannon.isCannonBlock(event.getBlock()))
//                    {
//                        event.setCancelled(true);
//                    }
//                }
//                // delete gunpowder block
//                if (cannon.getCannonDesign().getGunpowderType().equalsFuzzy(event.getBlock()))
//                {
//                    // check if the placed block is not part of the cannon
//                    if (!cannon.isCannonBlock(event.getBlock()))
//                    {
//                        event.setCancelled(true);
//                    }
//                }
//            }
//        }

        // Place wallsign
        if (event.getBlockPlaced().getBlockData() instanceof WallSign)
        {
            WallSign wallSign = (WallSign) event.getBlockPlaced().getBlockData();
            // check cannon
            Location loc = event.getBlock().getRelative(wallSign.getFacing().getOppositeFace()).getLocation();
            Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true);
            if (cannon != null)
            {
                cannon.updateCannonSigns();
            }
        }

        // Place redstonetorch under to the cannon
        if (event.getBlockPlaced().getType() == Material.REDSTONE_TORCH || event.getBlockPlaced().getType() == Material.REDSTONE_WALL_TORCH)
        {
            // check cannon
            Location loc = event.getBlock().getRelative(BlockFace.UP).getLocation();
            Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true);
            if (cannon != null)
            {
                // check permissions
                if (!event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()))
                {
                    //check if the placed block is in the redstone torch interface
                    if (cannon.isRedstoneTorchInterface(event.getBlock().getLocation()))
                    {
                        userMessages.sendMessage(MessageEnum.PermissionErrorRedstone, event.getPlayer());
                        event.setCancelled(true);
                    }
                }
            }
        }

        // Place redstone wire next to the button
        if (event.getBlockPlaced().getType() == Material.REDSTONE_WIRE)
        {
            // check cannon
            for (Block b : CannonsUtil.HorizontalSurroundingBlocks(event.getBlock()))
            {
                Location loc = b.getLocation();
                Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true);
                if (cannon != null)
                {
                    // check permissions
                    if (!event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()))
                    {
                        //check if the placed block is in the redstone wire interface
                        if (cannon.isRedstoneWireInterface(event.getBlock().getLocation()))
                        {
                            userMessages.sendMessage(MessageEnum.PermissionErrorRedstone, event.getPlayer());
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        // cancel igniting of the cannon
        if (event.getBlock().getType() == Material.FIRE)
        {
            // check cannon
            if (event.getBlockAgainst() != null) {
                Location loc = event.getBlockAgainst().getLocation();
                if (cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true) != null) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * handles redstone events (torch, wire, repeater, button
     * @param event - BlockRedstoneEvent
     */
    @EventHandler
    public void RedstoneEvent(BlockRedstoneEvent event)
    {
        Block block = event.getBlock();
        if (block == null) return;

        // ##########  redstone torch fire
        // off because it turn form off to on
        if ((block.getType() == Material.REDSTONE_TORCH || block.getType() == Material.REDSTONE_WALL_TORCH) && event.getNewCurrent() > event.getOldCurrent())
        {
            // go one block up and check this is a cannon
            Cannon cannon = cannonManager.getCannon(block.getRelative(BlockFace.UP).getLocation(), null);

            if (cannon != null)
            {
                // there is cannon next to the torch - check if the torch is
                // place right
                if (cannon.isRedstoneTorchInterface(block.getLocation()))
                {
                    MessageEnum message = fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
                }
            }
        }

        // ##########  redstone wire fire
        if (block.getType() == Material.REDSTONE_WIRE && event.getNewCurrent() > event.getOldCurrent())
        {
            // check all block next to this if there is a cannon
            for (Block b : CannonsUtil.HorizontalSurroundingBlocks(block))
            {
                Cannon cannon = cannonManager.getCannon(b.getLocation(), null);
                if (cannon != null)
                {
                    // there is cannon next to the wire - check if the wire
                    // is place right
                    if (cannon.isRedstoneWireInterface(block.getLocation()))
                    {
                        MessageEnum message = fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
                    }
                }

            }
        }

        // ##########  redstone repeater and comparator fire
        if ((block.getType() == Material.REPEATER || block.getType() == Material.COMPARATOR) && event.getNewCurrent() > event.getOldCurrent())
        {
            // check all block next to this if there is a cannon
            for (Block b : CannonsUtil.HorizontalSurroundingBlocks(block))
            {
                Cannon cannon = cannonManager.getCannon(b.getLocation(), null);
                if (cannon != null)
                {
                    // there is cannon next to the wire - check if the wire
                    // is place right
                    if (cannon.isRedstoneRepeaterInterface(block.getLocation()))
                    {
                        MessageEnum message = fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
                    }

                }
            }
        }


        // ##########  fire with redstone trigger ######
        Cannon cannon = cannonManager.getCannon(event.getBlock().getLocation(), null);
        if (cannon != null)
        {
            //check if this is a redstone trigger of the cannon (e.g. button)
            if (cannon.isRestoneTrigger(event.getBlock().getLocation()))
            {
                //get the user of the cannon
                Player player;
                if (cannon.getLastUser() != null)
                    player = Bukkit.getPlayer(cannon.getLastUser());
                else
                    //no last cannon user
                    return;

                if (cannon.getLastUser() == null || player == null)
                    return;
                //reset user
                cannon.setLastUser(null);

                plugin.logDebug("Redfire with button by " + player.getName());

                MessageEnum message = fireCannon.playerFiring(cannon, player, InteractAction.fireRedstoneTrigger);
                userMessages.sendMessage(message, player, cannon);
            }
        }


    }



    /**
     * Handles event if player interacts with the cannon
     * @param event
     */
	@EventHandler
    public void PlayerInteract(PlayerInteractEvent event)
    {
        Action action = event.getAction();

        Block clickedBlock = null;
        if(event.getClickedBlock() == null)
        {
            // no clicked block - get block player is looking at
            Location location = event.getPlayer().getEyeLocation();
            BlockIterator blocksToAdd = new BlockIterator(location, 0, 5);
            Block block = null;
            while(blocksToAdd.hasNext()) {
                block = blocksToAdd.next();
                if (block.getType() != Material.AIR){
                    clickedBlock = block;
                }
            }
            if (clickedBlock == null) {
                clickedBlock = block;
            }
        }
        else
        {
            clickedBlock = event.getClickedBlock();
        }

        if (clickedBlock == null){
            return;
        }

        final Player player = event.getPlayer();
        final Location barrel = clickedBlock.getLocation();

        //if try if the player has really nothing in his hands, or minecraft is blocking it
        final ItemStack eventitem;
        if (event.getItem() == null) {
            eventitem = player.getInventory().getItemInMainHand();
        }
        else{
            eventitem = event.getItem();
        }

        // find cannon or add it to the list
        final Cannon cannon = cannonManager.getCannon(barrel, player.getUniqueId(), false);

        // ############ select a cannon ####################
        if(plugin.getCommandListener().isSelectingMode(player))
        {
            if (plugin.getCommandListener().isBlockSelectingMode(player)){
                plugin.getCommandListener().setSelectedBlock(player, clickedBlock);
                event.setCancelled(true);
                return;
            }
            else if (cannon != null){
                plugin.getCommandListener().setSelectedCannon(player, cannon);
                event.setCancelled(true);
                return;
            }
        }

    	if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.PHYSICAL) && event.getHand() == EquipmentSlot.HAND && cannon != null)
        {
            // get cannon design
            final CannonDesign design = cannon.getCannonDesign();

            // prevent eggs and snowball from firing when loaded into the gun
            if(config.isCancelItem(eventitem))
                event.setCancelled(true);

            // I used here System.out.println to display the correct color code
            if (plugin.isDebugMode() && event.getItem() != null && event.getItem().getItemMeta() != null && event.getItem().getItemMeta().hasDisplayName()) {
                System.out.println("Cannon interaction with item " + event.getItem());
            }

            // ############ touching a hot cannon will burn you ####################
            if(cannon.getTemperature() > design.getWarningTemperature())
            {
                plugin.logDebug("someone touched a hot cannon");
                userMessages.sendMessage(MessageEnum.HeatManagementBurn, player, cannon);
                if (design.getBurnDamage() > 0)
                    player.damage(design.getBurnDamage()*2);
                if (design.getBurnSlowing() > 0)
                    PotionEffectType.SLOW.createEffect((int) (design.getBurnSlowing()*20.0), 0).apply(player);

                BlockFace clickedFace = event.getBlockFace();

                Location effectLoc = clickedBlock.getRelative(clickedFace).getLocation();
                effectLoc.getWorld().playEffect(effectLoc, Effect.SMOKE, BlockFace.UP);
                //ffectLoc.getWorld().playSound(effectLoc, Sound.FIZZ, 0.1F, 1F);
                CannonsUtil.playSound(effectLoc, design.getSoundHot());
            }


            // ############ cooling a hot cannon ####################
            if(design.isCoolingTool(eventitem))
            {
                event.setCancelled(true);
                if (cannon.coolCannon(player, clickedBlock.getRelative(event.getBlockFace()).getLocation())) {
                    plugin.logDebug(player.getName() + " cooled the cannon " + cannon.getCannonName());
                    userMessages.sendMessage(MessageEnum.HeatManagementCooling, player, cannon);
                }
            }


            // ############ temperature measurement ################################
            if(config.getToolThermometer().equalsFuzzy(eventitem))
            {
                if (player.hasPermission(design.getPermissionThermometer()))
                {
                    plugin.logDebug("measure temperature");
                    event.setCancelled(true);
                    userMessages.sendMessage(MessageEnum.HeatManagementInfo, player, cannon);
                    //player.playSound(cannon.getMuzzle(), Sound.ANVIL_LAND, 10f, 1f);
                    CannonsUtil.playSound(cannon.getMuzzle(), design.getSoundThermometer());
                }
                else
                {
                    plugin.logDebug("Player " + player.getName() + " has no permission " + design.getPermissionThermometer());
                }
            }


            // ############ set angle ################################
            if((config.getToolAdjust().equalsFuzzy(eventitem) || config.getToolAutoaim().equalsFuzzy(eventitem)) && cannon.isLoadingBlock(clickedBlock.getLocation()))
            {
                plugin.logDebug("change cannon angle");
                event.setCancelled(true);

                if (plugin.getEconomy() != null && !cannon.isPaid()){
                    // cannon fee is not paid
                    userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
                    CannonsUtil.playErrorSound(cannon.getMuzzle());
                    return;
                }

                MessageEnum message = aiming.changeAngle(cannon, event.getAction(), event.getBlockFace(), player);
                userMessages.sendMessage(message, player, cannon);

                // update Signs
                cannon.updateCannonSigns();

                if(message!=null)
                    return;
            }

            // ########## Load Projectile ######################
            Projectile projectile = plugin.getProjectile(cannon, eventitem);
            if (cannon.isLoadingBlock(clickedBlock.getLocation()) && projectile != null) {
                plugin.logDebug("load projectile");
                event.setCancelled(true);

                if (plugin.getEconomy() != null && !cannon.isPaid()){
                    // cannon fee is not paid
                    userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
                    CannonsUtil.playErrorSound(cannon.getMuzzle());
                    return;
                }

                // load projectile
                MessageEnum message = cannon.loadProjectile(projectile, player);
                // display message
                userMessages.sendMessage(message, player, cannon);

                //this will directly fire the cannon after it was loaded
                if (!player.isSneaking() && design.isFireAfterLoading() && cannon.isLoaded() && cannon.isProjectilePushed())
                    fireCannon.playerFiring(cannon, player, InteractAction.fireAfterLoading);

                if(message!=null)
                    return;
            }


            // ########## Barrel clicked with gunpowder
            if(cannon.isLoadingBlock(clickedBlock.getLocation()) && design.getGunpowderType().equalsFuzzy(eventitem))
            {
                plugin.logDebug("load gunpowder");
                event.setCancelled(true);

                if (plugin.getEconomy() != null && !cannon.isPaid()){
                    // cannon fee is not paid
                    userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
                    CannonsUtil.playErrorSound(cannon.getMuzzle());
                    return;
                }

                // load gunpowder
                MessageEnum message = cannon.loadGunpowder(player);

                // display message
                userMessages.sendMessage(message, player, cannon);

                if(message!=null)
                    return;
            }


            // ############ Right click trigger clicked (e.g.torch) ############################
            if(cannon.isRightClickTrigger(clickedBlock.getLocation()))
            {
                plugin.logDebug("fire torch");
                event.setCancelled(true);

                if (plugin.getEconomy() != null && !cannon.isPaid()){
                    // cannon fee is not paid
                    userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
                    CannonsUtil.playErrorSound(cannon.getMuzzle());
                    return;
                }

                MessageEnum message = fireCannon.playerFiring(cannon, player, InteractAction.fireRightClickTigger);
                // display message
                userMessages.sendMessage(message, player, cannon);

                if(message!=null)
                    return;
            }


            // ############ Redstone trigger clicked (e.g. button) ############################
            if(cannon.isRestoneTrigger(clickedBlock.getLocation()))
            {
                plugin.logDebug("interact event: fire redstone trigger");

                if (plugin.getEconomy() != null && !cannon.isPaid()){
                    // cannon fee is not paid
                    userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
                    CannonsUtil.playErrorSound(cannon.getMuzzle());
                    return;
                }

                // do not cancel the event
                cannon.setLastUser(player.getUniqueId());

                return;
            }


            // ########## Ramrod ###############################
            if(config.getToolRamrod().equalsFuzzy(eventitem) && cannon.isLoadingBlock(clickedBlock.getLocation()))
            {
                plugin.logDebug("Ramrod used");
                event.setCancelled(true);

                if (plugin.getEconomy() != null && !cannon.isPaid()){
                    // cannon fee is not paid
                    userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
                    CannonsUtil.playErrorSound(cannon.getMuzzle());
                    return;
                }

                MessageEnum message = cannon.useRamRod(player);
                userMessages.sendMessage(message, player, cannon);

                // todo clean multiple cannons in the vicinity
                if (design.isLinkCannonsEnabled() ) {
                    int d = design.getLinkCannonsDistance() * 2;
                    for (Cannon fcannon : CannonManager.getCannonsInBox(cannon.getLocation(), d, d, d)) {
                        if (fcannon.getCannonDesign().equals(cannon.getCannonDesign()) &&  (!cannon.getCannonDesign().isAccessForOwnerOnly() || fcannon.getOwner() == player.getUniqueId()))
                        fcannon.useRamRod(player);
                    }
                }

                //this will directly fire the cannon after it was loaded
                if (!player.isSneaking() && design.isFireAfterLoading() && cannon.isLoaded() && cannon.isProjectilePushed())
                    fireCannon.playerFiring(cannon, player, InteractAction.fireAfterLoading);

                if(message!=null)
                    return;
            }
        }
        //no cannon found - maybe the player has click into the air to stop aiming
        else if(cannon == null && action == Action.RIGHT_CLICK_AIR && event.getHand() == EquipmentSlot.HAND){
                // stop aiming mode when right clicking in the air
                if (config.getToolAutoaim().equalsFuzzy(eventitem))
                    aiming.aimingMode(player, null, false);
                plugin.getCommandListener().removeCannonSelector(player);
        }
        //fire cannon
        else if(event.getAction().equals(Action.LEFT_CLICK_AIR) && event.getHand() == EquipmentSlot.HAND) //|| event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            //check if the player is passenger of a projectile, if so he can teleport back by left clicking
            CannonsUtil.teleportBack(plugin.getProjectileManager().getAttachedProjectile(event.getPlayer()));
        	aiming.aimingMode(event.getPlayer(), null, true);
        }
    }

}
