package at.pavlov.cannons.API;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.Enum.MessageEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class CannonsAPI {

    private final Cannons plugin;

    public CannonsAPI(Cannons cannons)
    {
        this.plugin = cannons;
    }

    /**
     * fires the given cannon if the player has enough permissions
     * @param cannon - the cannon to fire
     * @param player - the player how is firing the cannon
     * @param autoreload - if the cannon will autoreload from a chest after firing
     * @param consumesAmmo - if true the cannon will remove ammo from attached chests
     * @return returns a MessagesEnum if the firing was successful or not
     */
    public MessageEnum fireCannon(Cannon cannon, Player player, boolean autoreload, boolean consumesAmmo)
    {
        return plugin.getFireCannon().prepareFire(cannon, player, autoreload, consumesAmmo);
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
    public List<Cannon> getCannonsInSphere(Location center, double sphereRadius)
    {
        return plugin.getCannonManager().getCannonsinSphere(center, sphereRadius);
    }

    /**
     * returns all known cannon in a box around the given location
     * @param center - center of the box
     * @param lengthX - box length in X
     * @param lengthY - box length in Y
     * @param lengthZ - box length in Z
     * @return - list of all cannons in this sphere
     */
    public List<Cannon> getCannonsInBox(Location center, double lengthX, double lengthY, double lengthZ)
    {
        return plugin.getCannonManager().getCannonsInBox(center, lengthX, lengthY, lengthZ);
    }


    public void setCannonAngle(Cannon cannon, double horizontal, double vertical)
    {
        //plugin.getAiming().
    }



}
