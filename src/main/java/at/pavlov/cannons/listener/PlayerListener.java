package at.pavlov.cannons.listener;

import at.pavlov.cannons.Aiming;
import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.FireCannon;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.projectile.FlyingProjectile;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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
        if (!aiming.distanceCheck(event.getPlayer(), cannon) && (System.currentTimeMillis() - cannon.getTimestampAimingMode()) > 1000){
            userMessages.sendMessage(MessageEnum.AimingModeTooFarAway, event.getPlayer());
            MessageEnum message = aiming.disableAimingMode(event.getPlayer());
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
    public void BlockPlace(BlockPlaceEvent event) {

        Block block = event.getBlockPlaced();
        Location blockLoc = block.getLocation();

        // setup a new cannon
        cannonManager.getCannon(blockLoc, event.getPlayer().getUniqueId());

        // Place wallsign
        if (event.getBlockPlaced().getBlockData() instanceof WallSign wallSign) {
            // check cannon
            Location loc = event.getBlock().getRelative(wallSign.getFacing().getOppositeFace()).getLocation();
            Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true);
            if (cannon != null) {
                cannon.updateCannonSigns();
            }
        }

        // Place redstonetorch under to the cannon
        if (event.getBlockPlaced().getType() == Material.REDSTONE_TORCH || event.getBlockPlaced().getType() == Material.REDSTONE_WALL_TORCH) {
            // check cannon
            Location loc = event.getBlock().getRelative(BlockFace.UP).getLocation();
            Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true);
            if (cannon != null) {
                // check permissions
                if (!event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone())) {
                    //check if the placed block is in the redstone torch interface
                    if (cannon.isRedstoneTorchInterface(event.getBlock().getLocation())) {
                        userMessages.sendMessage(MessageEnum.PermissionErrorRedstone, event.getPlayer());
                        event.setCancelled(true);
                    }
                }
            }
        }

        // Place redstone wire next to the button
        if (event.getBlockPlaced().getType() == Material.REDSTONE_WIRE) {
            // check cannon
            for (Block b : CannonsUtil.HorizontalSurroundingBlocks(event.getBlock())) {
                Location loc = b.getLocation();
                Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getUniqueId(), true);
                if (cannon == null) {
                    continue;
                }
                // check permissions
                if (event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone())) {
                    continue;
                }

                //check if the placed block is in the redstone wire interface
                if (cannon.isRedstoneWireInterface(event.getBlock().getLocation())) {
                    userMessages.sendMessage(MessageEnum.PermissionErrorRedstone, event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }

        // cancel igniting of the cannon
        if (event.getBlock().getType() == Material.FIRE) {
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
     * Handles event if player interacts with the cannon
     * @param event
     */
	@EventHandler
    public void PlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        final Player player = event.getPlayer();
        Block clickedBlock = getBlock(event.getClickedBlock(), player);
        if (clickedBlock == null){
            return;
        }

        final Location barrel = clickedBlock.getLocation();

        //try if the player has really nothing in his hands, or minecraft is blocking it
        final ItemStack eventitem = Objects.requireNonNullElse(event.getItem(), player.getInventory().getItemInMainHand());

        // find cannon or add it to the list
        final Cannon cannon = cannonManager.getCannon(barrel, player.getUniqueId(), false);

        // ############ select a cannon ####################
        if (isCannonSelect(event, clickedBlock, cannon))
            return;

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

    	if((event.getAction().isRightClick() || event.getAction() == Action.PHYSICAL) && cannon != null) {
            // prevent eggs and snowball from firing when loaded into the gun
            if(config.isCancelItem(eventitem))
                event.setCancelled(true);

            // I used here System.out.println to display the correct color code
            if (event.getItem() != null && event.getItem().getItemMeta() != null && event.getItem().getItemMeta().hasDisplayName()) {
                plugin.logDebug("Cannon interaction with item " + event.getItem());
            }

            // ############ touching a hot cannon will burn you ####################
            handleBurningTouch(cannon, player, event.getBlockFace(), clickedBlock);

            // ############ cooling a hot cannon ####################
            if (isHandleCooling(cannon, eventitem, clickedBlock, event)) {
                return;
            }

            // ############ temperature measurement ################################
            if(isMeasureTemperature(cannon, eventitem, event)) {
                return;
            }

            // ############ set angle ################################
            if (setAngle(cannon, eventitem, clickedBlock, event)) {
                return;
            }

            // ########## Load Projectile ######################
            if (isLoadProjectile(cannon, eventitem, clickedBlock, event)) {
                return;
            }

            // ########## Barrel clicked with gunpowder
            if (isLoadGunpowder(cannon, eventitem, clickedBlock, event)) {
                return;
            }

            // ############ Right click trigger clicked (e.g.torch) ############################
            if (isRightClickTrigger(cannon, clickedBlock, event)) {
                return;
            }

            // ############ Redstone trigger clicked (e.g. button) ############################
            if (isRedstoneTrigger(cannon, clickedBlock, event)) {
                return;
            }

            // ########## Ramrod ###############################
            if (isRamrod(cannon, eventitem, clickedBlock, event)) {
                return;
            }
        }
        //no cannon found - maybe the player has click into the air to stop aiming
        else if(cannon == null && action == Action.RIGHT_CLICK_AIR){
                // stop aiming mode when right clicking in the air
                if (config.getToolAutoaim().equalsFuzzy(eventitem))
                    aiming.aimingMode(player, null, false);
                plugin.getCommandListener().removeCannonSelector(player);
        }
        //fire cannon
        else if(event.getAction().equals(Action.LEFT_CLICK_AIR)) //|| event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            //check if the player is passenger of a projectile, if so he can teleport back by left clicking
            CannonsUtil.teleportBack(plugin.getProjectileManager().getAttachedProjectile(event.getPlayer()));
        	aiming.aimingMode(event.getPlayer(), null, true);
        }
    }

    private boolean isRamrod(Cannon cannon, ItemStack eventitem, Block clickedBlock, PlayerInteractEvent event) {
        if (!config.getToolRamrod().equalsFuzzy(eventitem) || !cannon.isLoadingBlock(clickedBlock.getLocation())) {
            return false;
        }

        plugin.logDebug("Ramrod used");
        event.setCancelled(true);

        final Player player = event.getPlayer();
        if (plugin.getEconomy() != null && !cannon.isPaid()){
            // cannon fee is not paid
            userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
            CannonsUtil.playErrorSound(cannon.getMuzzle());
            return true;
        }

        MessageEnum message = cannon.useRamRod(player);
        userMessages.sendMessage(message, player, cannon);

        final CannonDesign design = cannon.getCannonDesign();
        if (design.isLinkCannonsEnabled() ) {
            int d = design.getLinkCannonsDistance() * 2;
            for (Cannon fcannon : CannonManager.getCannonsInBox(cannon.getLocation(), d, d, d)) {
                if (fcannon.getCannonDesign().equals(cannon.getCannonDesign()) &&  cannon.isAccessLinkingAllowed(fcannon, player))
                    fcannon.useRamRod(player);
            }
        }

        //this will directly fire the cannon after it was loaded
        if (!player.isSneaking() && design.isFireAfterLoading() && cannon.isLoaded() && cannon.isProjectilePushed())
            fireCannon.playerFiring(cannon, player, InteractAction.fireAfterLoading);

        return message != null;
    }

    private boolean isRedstoneTrigger(Cannon cannon, Block clickedBlock, PlayerInteractEvent event) {
        if (!cannon.isRestoneTrigger(clickedBlock.getLocation())) {
            return false;
        }

        plugin.logDebug("interact event: fire redstone trigger");

        final Player player = event.getPlayer();
        if (plugin.getEconomy() != null && !cannon.isPaid()){
            // cannon fee is not paid
            userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
            CannonsUtil.playErrorSound(cannon.getMuzzle());
            return true;
        }

        // do not cancel the event
        cannon.setLastUser(player.getUniqueId());
        return true;
    }

    private boolean isRightClickTrigger(Cannon cannon, Block clickedBlock, PlayerInteractEvent event) {
        if (!cannon.isRightClickTrigger(clickedBlock.getLocation())) {
            return false;
        }

        plugin.logDebug("fire torch");
        event.setCancelled(true);

        final Player player = event.getPlayer();
        if (plugin.getEconomy() != null && !cannon.isPaid()) {
            // cannon fee is not paid
            userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
            CannonsUtil.playErrorSound(cannon.getMuzzle());
            return true;
        }

        MessageEnum message = fireCannon.playerFiring(cannon, player, InteractAction.fireRightClickTigger);
        // display message
        userMessages.sendMessage(message, player, cannon);

        return message != null;
    }

    private boolean isCannonSelect(PlayerInteractEvent event, Block clickedBlock, Cannon cannon) {
        final Player player = event.getPlayer();

        if (!plugin.getCommandListener().isSelectingMode(player)) {
            return false;
        }

        if (plugin.getCommandListener().isBlockSelectingMode(player)){
            plugin.getCommandListener().setSelectedBlock(player, clickedBlock);
            event.setCancelled(true);
            return true;
        }

        else if (cannon != null){
            plugin.getCommandListener().setSelectedCannon(player, cannon);
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    private void handleBurningTouch(Cannon cannon, Player player, BlockFace clickedFace, Block clickedBlock) {
        final CannonDesign design = cannon.getCannonDesign();
        if (!(cannon.getTemperature() > design.getWarningTemperature())) {
            return;
        }

        plugin.logDebug("someone touched a hot cannon");
        userMessages.sendMessage(MessageEnum.HeatManagementBurn, player, cannon);
        if (design.getBurnDamage() > 0)
            player.damage(design.getBurnDamage()*2);
        if (design.getBurnSlowing() > 0)
            PotionEffectType.SLOWNESS.createEffect((int) (design.getBurnSlowing()*20.0), 0).apply(player);

        Location effectLoc = clickedBlock.getRelative(clickedFace).getLocation();
        effectLoc.getWorld().playEffect(effectLoc, Effect.SMOKE, BlockFace.UP);
        effectLoc.getWorld().playSound(effectLoc, Sound.BLOCK_FIRE_EXTINGUISH, 0.1F, 1F);
        CannonsUtil.playSound(effectLoc, design.getSoundHot());
    }

    private boolean isHandleCooling(Cannon cannon, ItemStack eventitem, Block clickedBlock, PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!cannon.getCannonDesign().isCoolingTool(eventitem)) {
            return false;
        }

        event.setCancelled(true);

        if (!cannon.coolCannon(player, clickedBlock.getRelative(event.getBlockFace()).getLocation())) {
            return false;
        }

        plugin.logDebug(player.getName() + " cooled the cannon " + cannon.getCannonName());
        userMessages.sendMessage(MessageEnum.HeatManagementCooling, player, cannon);
        return true;
    }

    private boolean isMeasureTemperature(Cannon cannon, ItemStack eventitem, PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final CannonDesign design = cannon.getCannonDesign();

        if (!config.getToolThermometer().equalsFuzzy(eventitem)) {
            return false;
        }

        if (!player.hasPermission(design.getPermissionThermometer())) {
            plugin.logDebug("Player " + player.getName() + " has no permission " + design.getPermissionThermometer());
            return true;
        }

        plugin.logDebug("measure temperature");
        event.setCancelled(true);
        userMessages.sendMessage(MessageEnum.HeatManagementInfo, player, cannon);
        player.playSound(cannon.getMuzzle(), Sound.BLOCK_ANVIL_LAND, 10f, 1f);
        CannonsUtil.playSound(cannon.getMuzzle(), design.getSoundThermometer());

        return true;
    }

    private boolean setAngle(Cannon cannon, ItemStack eventitem, Block clickedBlock, PlayerInteractEvent event) {
        if ((!config.getToolAdjust().equalsFuzzy(eventitem) && !config.getToolAutoaim().equalsFuzzy(eventitem)) || !cannon.isLoadingBlock(clickedBlock.getLocation())) {
            return false;
        }

        plugin.logDebug("change cannon angle");
        event.setCancelled(true);

        final Player player = event.getPlayer();
        if (plugin.getEconomy() != null && !cannon.isPaid()){
            // cannon fee is not paid
            userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
            CannonsUtil.playErrorSound(cannon.getMuzzle());
            return true;
        }

        MessageEnum message = aiming.changeAngle(cannon, event.getAction(), event.getBlockFace(), player);
        userMessages.sendMessage(message, player, cannon);

        // update Signs
        cannon.updateCannonSigns();

        return message != null;
    }

    private boolean isLoadProjectile(Cannon cannon, ItemStack eventitem, Block clickedBlock, PlayerInteractEvent event) {
        Projectile projectile = ProjectileStorage.getProjectile(cannon, eventitem);
        if (!cannon.isLoadingBlock(clickedBlock.getLocation()) || projectile == null) {
            return false;
        }

        plugin.logDebug("load projectile");
        event.setCancelled(true);
        final Player player = event.getPlayer();
        if (plugin.getEconomy() != null && !cannon.isPaid()){
            // cannon fee is not paid
            userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
            CannonsUtil.playErrorSound(cannon.getMuzzle());
            return true;
        }

        // load projectile
        MessageEnum message = cannon.loadProjectile(projectile, player);
        // display message
        userMessages.sendMessage(message, player, cannon);

        //this will directly fire the cannon after it was loaded
        if (!player.isSneaking() && cannon.getCannonDesign().isFireAfterLoading() && cannon.isLoaded() && cannon.isProjectilePushed())
            fireCannon.playerFiring(cannon, player, InteractAction.fireAfterLoading);

        return message != null;
    }

    private boolean isLoadGunpowder(Cannon cannon, ItemStack eventitem, Block clickedBlock, PlayerInteractEvent event) {
        if (!cannon.isLoadingBlock(clickedBlock.getLocation()) || !cannon.getCannonDesign().getGunpowderType().equalsFuzzy(eventitem)) {
            return false;
        }

        plugin.logDebug("load gunpowder");
        event.setCancelled(true);

        final Player player = event.getPlayer();
        if (plugin.getEconomy() != null && !cannon.isPaid()){
            // cannon fee is not paid
            userMessages.sendMessage(MessageEnum.ErrorNotPaid, player, cannon);
            CannonsUtil.playErrorSound(cannon.getMuzzle());
            return true;
        }

        // load gunpowder
        MessageEnum message = cannon.loadGunpowder(player);

        // display message
        userMessages.sendMessage(message, player, cannon);

        return message != null;
    }

    private static @Nullable Block getBlock(Block eventClickedBlock, Player player) {
        Block clickedBlock = null;
        if (eventClickedBlock != null) {
            return eventClickedBlock;
        }

        // no clicked block - get block player is looking at
        Location location = player.getEyeLocation();
        BlockIterator blocksToAdd = new BlockIterator(location, 0, 5);
        Block block = null;
        while(blocksToAdd.hasNext()) {
            block = blocksToAdd.next();
            if (!block.getType().isAir()){
                clickedBlock = block;
            }
        }

        if (clickedBlock == null) {
            return block;
        }

        return clickedBlock;
    }
}
