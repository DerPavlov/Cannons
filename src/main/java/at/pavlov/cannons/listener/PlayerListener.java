package at.pavlov.cannons.listener;

import at.pavlov.cannons.Enum.InteractAction;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
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
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;

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
    public void PlayerMove(PlayerMoveEvent event)
    {
        // only active if the player is in aiming mode
        Cannon cannon =  aiming.getCannonInAimingMode(event.getPlayer());
        if (!aiming.distanceCheck(event.getPlayer(), cannon))
        {
            userMessages.displayMessage(event.getPlayer(), MessageEnum.AimingModeTooFarAway);
            MessageEnum message = aiming.disableAimingMode(event.getPlayer(), cannon);
            userMessages.displayMessage(event.getPlayer(), message);
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

        Cannon cannon = cannonManager.getCannon(blockLoc, event.getPlayer().getName());

        // check if it is a cannon
        if (cannon != null)
        {
            // data =-1 means no data check, all buckets are allowed
            Projectile projectile = plugin.getProjectile(cannon, event.getBucket().getId(), -1);
            if (projectile != null) event.setCancelled(true);
        }
    }

    /**
     * Create a cannon if the building process is finished Deletes a projectile
     * if loaded Checks for redstone torches if built
     *
     * @param event
     */
    @EventHandler
    public void BlockPlace(BlockPlaceEvent event)
    {

        Block block = event.getBlockPlaced();
        Location blockLoc = block.getLocation();

        // setup a new cannon
        cannonManager.getCannon(blockLoc, event.getPlayer().getName());

        // delete placed projectile or gunpowder if clicked against the barrel
        if (event.getBlockAgainst() != null)
        {
            Location barrel = event.getBlockAgainst().getLocation();

            // check if block is cannonblock
            Cannon cannon = cannonManager.getCannon(barrel, event.getPlayer().getName(), true);
            if (cannon != null)
            {
                // delete projectile
                Projectile projectile = plugin.getProjectile(cannon, block.getTypeId(), block.getData());
                if (projectile != null && cannon.getCannonDesign().canLoad(projectile))
                {
                    // check if the placed block is not part of the cannon
                    if (!cannon.isCannonBlock(event.getBlock()))
                    {
                        event.setCancelled(true);
                    }
                }
                // delete gunpowder block
                if (cannon.getCannonDesign().getGunpowderType().equalsFuzzy(event.getBlock()))
                {
                    // check if the placed block is not part of the cannon
                    if (!cannon.isCannonBlock(event.getBlock()))
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }

        // Place redstonetorch under to the cannon
        if (event.getBlockPlaced().getType() == Material.REDSTONE_TORCH_ON || event.getBlockPlaced().getType() == Material.REDSTONE_TORCH_OFF)
        {
            // check cannon
            Location loc = event.getBlock().getRelative(BlockFace.UP).getLocation();
            Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getName(), true);
            if (cannon != null)
            {
                // check permissions
                if (!event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()))
                {
                    //check if the placed block is in the redstone torch interface
                    if (cannon.isRedstoneTorchInterface(event.getBlock().getLocation()))
                    {
                        userMessages.displayMessage(event.getPlayer(), MessageEnum.PermissionErrorRedstone);
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
                Cannon cannon = cannonManager.getCannon(loc, event.getPlayer().getName(), true);
                if (cannon != null)
                {
                    // check permissions
                    if (!event.getPlayer().hasPermission(cannon.getCannonDesign().getPermissionRedstone()))
                    {
                        //check if the placed block is in the redstone wire interface
                        if (cannon.isRedstoneWireInterface(event.getBlock().getLocation()))
                        {
                            userMessages.displayMessage(event.getPlayer(), MessageEnum.PermissionErrorRedstone);
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
            Location loc = event.getBlockAgainst().getLocation();
            if (cannonManager.getCannon(loc, event.getPlayer().getName(), true) != null)
            {
                event.setCancelled(true);
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

        plugin.logDebug("Redstone event was fired by " + block.getType());

        // ##########  redstone torch fire
        // off because it turn form off to on
        if (block.getType() == Material.REDSTONE_TORCH_ON)
        {
            // go one block up and check this is a cannon
            Cannon cannon = cannonManager.getCannon(block.getRelative(BlockFace.UP).getLocation(), null);

            if (cannon != null)
            {
                // there is cannon next to the torch - check if the torch is
                // place right

                plugin.logDebug("redstone torch");
                if (cannon.isRedstoneTorchInterface(block.getLocation()))
                {
                    MessageEnum message = fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
                    plugin.logDebug("fire cannon returned: " + message.getString());
                }
            }
        }

        // ##########  redstone wire fire
        if (block.getType() == Material.REDSTONE_WIRE)
        {
            // block is powered
            if (block.isBlockPowered())
            {
                // check all block next to this if there is a cannon
                for (Block b : CannonsUtil.HorizontalSurroundingBlocks(block))
                {
                    Cannon cannon = cannonManager.getCannon(b.getLocation(), null);
                    if (cannon != null)
                    {
                        // there is cannon next to the wire - check if the wire
                        // is place right

                        plugin.logDebug("redstone wire ");
                        if (cannon.isRedstoneWireInterface(block.getLocation()))
                        {
                            MessageEnum message = fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
                            plugin.logDebug("fire cannon returned: " + message.getString());
                        }
                    }
                }
            }
        }

        // ##########  redstone repeater and comparator fire
        if (block.getType() == Material.DIODE_BLOCK_OFF || block.getType() == Material.REDSTONE_COMPARATOR_OFF)
        {
            // check all block next to this if there is a cannon
            for (Block b : CannonsUtil.HorizontalSurroundingBlocks(block))
            {
                Cannon cannon = cannonManager.getCannon(b.getLocation(), null);
                if (cannon != null)
                {
                    // there is cannon next to the wire - check if the wire
                    // is place right
                    plugin.logDebug("redstone repeater ");
                    if (cannon.isRedstoneRepeaterInterface(block.getLocation()))
                    {
                        MessageEnum message = fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
                        plugin.logDebug("fire cannon returned: " + message.getString());
                    }

                }
            }
        }


        // ##########  fire with Button
        Cannon cannon = cannonManager.getCannon(event.getBlock().getLocation(), null);
        if (cannon != null)
        {
            //check if the button is a loading firing interface of the cannon
            if (cannon.isRestoneTrigger(event.getBlock().getLocation()))
            {

                //get the user of the cannon
                Player player = null;
                if (cannon.getLastUser() != null)
                    player = Bukkit.getPlayer(cannon.getLastUser());
                //reset user
                cannon.setLastUser("");
                if (player == null)
                    return;

                plugin.logDebug("Redfire with button by " + player.getName());

                MessageEnum message = fireCannon.playerFiring(cannon, player, InteractAction.fireButton);
                userMessages.displayMessage(player, cannon, message);
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
    	if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
        {
            Block clickedBlock;
            if(event.getClickedBlock() == null)
            {
                // no clicked block - get block player is looking at
                clickedBlock = event.getPlayer().getTargetBlock(null, 4);
            }
            else
            {
                clickedBlock = event.getClickedBlock();
            }
            final Player player = event.getPlayer();
            final Location barrel = clickedBlock.getLocation();

            // find cannon or add it to the list
            final Cannon cannon = cannonManager.getCannon(barrel, player.getName(), true);

            //no cannon found - maybe the player has click into the air to stop aiming
            if(cannon == null)
            {
                // all other actions will stop aiming mode
                if(action == Action.RIGHT_CLICK_AIR)
                {
                    aiming.aimingMode(event.getPlayer(), null, false);
                }
                return;
            }
            else event.setCancelled(true);//prevents player from using redstone blocks, such as lever, button, if player clicked cannon by right click

            // get cannon design
            final CannonDesign design = cannon.getCannonDesign();

            // prevent eggs and snowball from firing when loaded into the gun
            /*if(config.isCancelItem(player.getItemInHand()))
            {
                event.setCancelled(true);
            }*/


            plugin.logDebug("player interact event fired");


            // ############ touching a hot cannon will burn you ####################
            if(cannon.getTemperature() > design.getWarningTemperature())
            {
                plugin.logDebug("someone touched a hot cannon");
                userMessages.displayMessage(player, cannon, MessageEnum.HeatManagementBurn);
                if (design.getBurnDamage() > 0)
                    player.damage(design.getBurnDamage()*2);
                if (design.getBurnSlowing() > 0)
                    PotionEffectType.SLOW.createEffect((int) (design.getBurnSlowing()*20.0), 0).apply(player);

                BlockFace clickedFace = event.getBlockFace();

                Location effectLoc = clickedBlock.getRelative(clickedFace).getLocation();
                effectLoc.getWorld().playEffect(effectLoc, Effect.SMOKE, BlockFace.UP);
                effectLoc.getWorld().playSound(effectLoc, Sound.FIZZ, 0.1F, 1F);
            }


            // ############ cooling a hot cannon ####################
            if(design.isCoolingTool(player.getItemInHand()))
            {
                plugin.logDebug(player.getName() + " cooled the cannon " + cannon.getCannonName());
                userMessages.displayMessage(player, cannon, MessageEnum.HeatManagementCooling);

                cannon.coolCannon(player, clickedBlock.getRelative(event.getBlockFace()).getLocation());

                event.setCancelled(true);
            }


            // ############ temperature measurement ################################
            if(config.getToolThermometer().equalsFuzzy(player.getItemInHand()))
            {
                if (player.hasPermission(design.getPermissionThermometer()))
                {
                    plugin.logDebug("measure temperature");
                    userMessages.displayMessage(player, cannon, MessageEnum.HeatManagementInfo);
                    player.playSound(cannon.getMuzzle(), Sound.ANVIL_LAND, 10f, 1f);
                }
                else
                {
                    plugin.logDebug("Player " + player.getName() + " has no permission " + design.getPermissionThermometer());
                }
            }


            // ############ set angle ################################
            if((config.getToolAdjust().equalsFuzzy(player.getItemInHand()) || config.getToolAutoaim().equalsFuzzy(player.getItemInHand())) && cannon.isLoadingBlock(clickedBlock.getLocation()))
            {
                plugin.logDebug("change cannon angle");


                MessageEnum message = aiming.changeAngle(cannon, event.getAction(), event.getBlockFace(), player);
                userMessages.displayMessage(player, cannon, message);

                aiming.showAimingVector(cannon, player);

                // update Signs
                cannon.updateCannonSigns();

                if(message != null)
                {
                    if(message.isError())
                        CannonsUtil.playErrorSound(player);
                    return;
                }
            }

            // ########## Load Projectile ######################
            Projectile projectile = plugin.getProjectile(cannon, event.getItem());
            if (cannon.isLoadingBlock(clickedBlock.getLocation()) && projectile != null) {
                plugin.logDebug("load projectile");

                // load projectile
                MessageEnum message = cannon.loadProjectile(projectile, player);
                // display message
                userMessages.displayMessage(player, cannon, message);

                if(message !=null)
                {
                	if(message.isError())
                        CannonsUtil.playErrorSound(player);
                    return;
                }
            }


            // ########## Barrel clicked with gunpowder
            if(cannon.isLoadingBlock(clickedBlock.getLocation()) && design.getGunpowderType().equalsFuzzy(event.getItem()))
            {
                plugin.logDebug("load gunpowder");

                // load gunpowder
                MessageEnum message = cannon.loadGunpowder(player);

                // display message
                userMessages.displayMessage(player, cannon, message);

                if(message != null)
                {
                	if(message.isError())
                        CannonsUtil.playErrorSound(player);
                    return;
                }
            }


            // ############ Torch clicked ############################
            if(cannon.isRightClickTrigger(clickedBlock.getLocation()))
            {
                plugin.logDebug("fire torch");

                MessageEnum message = fireCannon.playerFiring(cannon, player, InteractAction.fireTorch);

                // display message
                userMessages.displayMessage(player, cannon, message);

                if(message!=null)
                {
                	if(message.isError())
                        CannonsUtil.playErrorSound(player);
                    return;
                }
            }


            // ############ Button clicked ############################
            if(cannon.isRestoneTrigger(clickedBlock.getLocation()))
            {
                plugin.logDebug("interact event: fire button");
                cannon.setLastUser(player.getName());

                return;
            }


            // ########## Ramrod ###############################
            if(config.getToolRamrod().equalsFuzzy(player.getItemInHand()) && cannon.isLoadingBlock(clickedBlock.getLocation()))
            {
                plugin.logDebug("Ramrod used");
                MessageEnum message = cannon.useRamRod(player);
                userMessages.displayMessage(player, cannon, message);

                if(message!=null)
                {
                    if(message.isError())
                        CannonsUtil.playErrorSound(player);
                    return;
                }

            }

        }

        //fire cannon
        else if(event.getAction().equals(Action.LEFT_CLICK_AIR)) //|| event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
        	aiming.aimingMode(event.getPlayer(), null, true);
        }
    }

}
