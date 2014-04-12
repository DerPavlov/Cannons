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
                plugin.logDebug("block List length: " + list.size());
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
     * @param players the players to be notified
     * @param l center of the sphere
     * @param r radius of the sphere
     * @param mat material of the fake block
     * @param delay delay until the block disappears again
     */
    public void createImitatedSphere(List<String> players, Location l, int r, MaterialHolder mat, int delay)//IMPROVED
    {
        for(String name : players)
        {
            createImitatedSphere(name, l, r, mat, delay);
        }
    }

    /**
     * creates a sphere of fake block and sends it to the given player
     * @param name the player to be notified
     * @param l center of the sphere
     * @param r radius of the sphere
     * @param mat material of the fake block
     * @param delay delay until the block disappears again
     */
    public void createImitatedSphere(String name, Location l, int r, MaterialHolder mat, int delay)//IMPROVED
    {
        Player player = Bukkit.getPlayer(name);
        if(player!=null)
        {
            createImitatedSphere(player, l, r, mat, delay);
        }

    }

    /**
     * creates a sphere of fake block and sends it to the given player
     * @param player the player to be notified
     * @param l center of the sphere
     * @param r radius of the sphere
     * @param mat material of the fake block
     * @param delay delay until the block disappears again
     */
    public void createImitatedSphere(Player player, Location l, int r, MaterialHolder mat, int delay)//IMPROVED
    {

        for(int x = -r; x <=r; x++)
        {
            for(int y = -r; y<=r; y++)
            {
                for(int z = -r; z<=r; z++)
                {
                    Location newL = l.clone().add(x, y, z);
                    if(newL.distance(l)<=r)
                    {
                        sendBlockChangeToPlayer(player, newL, mat, delay);
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
    public void imitateLine(Location loc, Vector direction, int offset,int length, String player,   MaterialHolder material)//TODO
    {
        Player p = Bukkit.getPlayer(player);
        if(p == null)
        {
            Commands.disableImitating(player);
            return;
        }

        BlockIterator iter = new BlockIterator(loc.getWorld(), loc.toVector(), direction, offset, length);

        while (iter.hasNext())
        {
            sendBlockChangeToPlayer(p, iter.next().getLocation(), material, 60);
        }

    }

    /**
     * sends fake block to the given player
     * @param player player to display the blocks
     * @param loc location of the block
     * @param material type of the block
     * @param duration how long to remove the block
     */
    public void sendBlockChangeToPlayer(final Player player, final Location loc, MaterialHolder material, int duration)
    {
        if(loc.getBlock().isEmpty())
        {
            player.sendBlockChange(loc, material.getId(), (byte) material.getData());

            //register block to remove it later
            list.add(new FakeBlockEntry(loc,player,material, duration));

        }
    }







}