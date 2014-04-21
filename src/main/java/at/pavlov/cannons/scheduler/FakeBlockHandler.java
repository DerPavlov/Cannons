package at.pavlov.cannons.scheduler;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.container.FakeBlockEntry;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.listener.Commands;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class FakeBlockHandler {
    private final Cannons plugin;

    private HashSet<FakeBlockEntry> list = new HashSet<FakeBlockEntry>();


    /**
     * Constructor
     * @param plugin - Cannons instance
     */
    public FakeBlockHandler(Cannons plugin)
    {
        this.plugin = plugin;
    }

    /**
     * starts the scheduler of the teleporter
     */
    public void setupScheduler()
    {
        //changing angles for aiming mode
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            public void run() {
                removeOldBlocks();
            }

        }, 1L, 1L);
    }

    /**
     * removes old blocks form the players vision
     */
    private void removeOldBlocks()
    {
        Iterator<FakeBlockEntry> iter = list.iterator();
        while(iter.hasNext())
        {
            FakeBlockEntry next = iter.next();
            if (next.isExpired())
            {
                //send real block to player
                Player player = next.getPlayerBukkit();
                Location loc = next.getLocation();
                if (player != null && loc != null)
                {
                    player.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
                }

                //remove this entry
                iter.remove();
            }
        }
    }

    /**
     * creates a sphere of fake block and sends it to the given player
     * @param player the player to be notified
     * @param loc center of the sphere
     * @param r radius of the sphere
     * @param mat material of the fake block
     * @param duration delay until the block disappears again in s
     */
    public void imitatedSphere(Player player, Location loc, int r, MaterialHolder mat, double duration)
    {
        if(loc == null || player == null)
            return;

        for(int x = -r; x <=r; x++)
        {
            for(int y = -r; y<=r; y++)
            {
                for(int z = -r; z<=r; z++)
                {
                    Location newL = loc.clone().add(x, y, z);
                    if(newL.distance(loc)<=r)
                    {
                        sendBlockChangeToPlayer(player, newL, mat, duration);
                    }
                }
            }
        }
    }

    /**
     * creates a line of blocks at the give location
     * @param loc starting location of the line
     * @param direction direction of the line
     * @param offset offset from the starting point
     * @param length lenght of the line
     * @param player name of the player
     */
    public void imitateLine(final Player player, Location loc, Vector direction, int offset, int length, MaterialHolder material, double duration)
    {
        if(loc == null || player == null)
            return;

        BlockIterator iter = new BlockIterator(loc.getWorld(), loc.toVector(), direction, offset, length);
        while (iter.hasNext())
        {
            sendBlockChangeToPlayer(player, iter.next().getLocation(), material, duration);
        }

    }

    /**
     * sends fake block to the given player
     * @param player player to display the blocks
     * @param loc location of the block
     * @param material type of the block
     * @param duration how long to remove the block in [s]
     */
    public void sendBlockChangeToPlayer(final Player player, final Location loc, MaterialHolder material, double duration)
    {
        if(loc.getBlock().isEmpty())
        {
            FakeBlockEntry fakeBlockEntry = new FakeBlockEntry(loc,player,(long) (duration*20.0));

            //don't send changes if there is already a block in the list
            if (!list.contains(fakeBlockEntry))
            {
                player.sendBlockChange(loc, material.getId(), (byte) material.getData());
            }
            else
            {
                //renew entry
                list.remove(fakeBlockEntry);
            }


            //register block to remove it later
            list.add(fakeBlockEntry);

        }
    }
}