package at.pavlov.cannons.API;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.config.MessageEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CannonsAPI {

    Cannons plugin;

    public CannonsAPI(Cannons cannons)
    {
        this.plugin = cannons;
    }

    /**
     * fires the given cannon if the player has enough permissions
     * @param cannon - the cannon to fire
     * @param player - the player how is firing the cannon
     * @param autoreload - if the cannon will autoreload from a chest after firing
     * @return returns a MessagesEnum if the firing was successful or not
     */
    public MessageEnum fireCannon(Cannon cannon, Player player, boolean autoreload)
    {
        return plugin.getFireCannon().prepareFire(cannon, player, autoreload);
    }


    /**
     * returns the cannon on the given location
     * @param location - location of a cannon block
     * @param player - player searching for the cannon. If there is no cannon he will be the owner. If null no new Cannon can be created.
     * @return - null if there is no cannon, else the cannon
     */
    public Cannon getCannon(Location location, Player player)
    {
        return plugin.getCannonManager().getCannon(location, player.getName());
    }

    /**
     * returns all known cannon in a sphere around the given location
     * @param center - center of the box
     * @param sphereRadius - radius of the sphere in blocks
     * @return - list of all cannons in this sphere
     */
    public List<Cannon> getCannons(Location center, double sphereRadius)
    {
        return plugin.getCannonManager().getCannons(center, sphereRadius);
    }



}
