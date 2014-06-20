package at.pavlov.cannons.scheduler;


import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.scheduler.BukkitRunnable;

public class CreateCannon extends BukkitRunnable {

    private final Cannons plugin;
    private Cannon cannon;

    public CreateCannon(Cannons plugin, Cannon cannon){
        this.plugin = plugin;
        this.cannon = cannon;
    }

    @Override
    public void run() {
        plugin.getCannonManager().createCannon(cannon);
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
