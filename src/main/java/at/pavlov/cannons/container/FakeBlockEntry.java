package at.pavlov.cannons.container;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FakeBlockEntry implements Cloneable{
    private int locX;
    private int locY;
    private int locZ;
    private String world;

    private long startTime;
    //how long the block stays in ticks
    private long duration;

    private String player;

    public FakeBlockEntry(Location loc, Player player, long duration)
    {
        this.locX = loc.getBlockX();
        this.locY = loc.getBlockY();
        this.locZ = loc.getBlockZ();
        this.world = loc.getWorld().getName();

        this.player = player.getName();

        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }


    public int getLocX() {
        return locX;
    }

    public void setLocX(int locX) {
        this.locX = locX;
    }

    public int getLocY() {
        return locY;
    }

    public void setLocY(int locY) {
        this.locY = locY;
    }

    public int getLocZ() {
        return locZ;
    }

    public void setLocxZ(int locZ) {
        this.locZ = locZ;
    }

    public String getWorld() {
        return world;
    }

    public World getWorldBukkit() {
        return Bukkit.getWorld(getWorld());
    }

    public Location getLocation(){
        World world = getWorldBukkit();
        if(world != null)
            return new Location(world,getLocX(),getLocY(),getLocZ());
        else
            return null;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isExpired(){
        return (System.currentTimeMillis() > getStartTime() + getDuration()*20);
    }

    public String getPlayer() {
        return player;
    }

    public Player getPlayerBukkit() {
        return Bukkit.getPlayer(getPlayer());
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    @Override
    public int hashCode() {
        int hash = 3;

        hash = 19 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.locX) ^ (Double.doubleToLongBits(this.locX) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.locY) ^ (Double.doubleToLongBits(this.locY) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.locZ) ^ (Double.doubleToLongBits(this.locZ) >>> 32));
        hash = 19 * hash + player.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        FakeBlockEntry obj2 = (FakeBlockEntry) obj;
        return this.locX == obj2.getLocX() && this.locY == obj2.getLocY() && this.locZ == obj2.getLocZ() && this.world.equals(obj2.getWorld()) && this.player.equals(obj2.getPlayer());
    }
}
