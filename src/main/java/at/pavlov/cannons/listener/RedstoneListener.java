package at.pavlov.cannons.listener;

import at.pavlov.cannons.Aiming;
import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.FireCannon;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneListener implements Listener {
    private final UserMessages userMessages;
    private final Cannons plugin;
    private final CannonManager cannonManager;
    private final FireCannon fireCannon;

    public RedstoneListener(Cannons plugin) {
        this.plugin = plugin;
        this.userMessages = this.plugin.getMyConfig().getUserMessages();
        this.cannonManager = this.plugin.getCannonManager();
        this.fireCannon = this.plugin.getFireCannon();
    }

    /**
     * handles redstone events (torch, wire, repeater, button
     * @param event - BlockRedstoneEvent
     */
    @EventHandler
    public void RedstoneEvent(BlockRedstoneEvent event) {
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
                    fireCannon.redstoneFiring(cannon, InteractAction.fireRedstone);
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
}
