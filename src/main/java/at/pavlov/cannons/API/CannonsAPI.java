package at.pavlov.cannons.API;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.Enum.MessageEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

public class CannonsAPI {

    private final Cannons plugin;

    public CannonsAPI(Cannons cannons)
    {
        this.plugin = cannons;
    }

    /**
     * fires the given cannon
     * @param cannon the cannon to fire
     * @param player the player how is firing the cannon. Player:null will skip permission check.
     * @param autoreload if the cannon will autoreload from a chest after firing
     * @param consumesAmmo if true the cannon will remove ammo from attached chests
     * @return returns a MessagesEnum if the firing was successful or not
     */
    public MessageEnum fireCannon(Cannon cannon, Player player, boolean autoreload, boolean consumesAmmo, InteractAction interaction)
    {
        return plugin.getFireCannon().fire(cannon, player, autoreload, consumesAmmo, interaction);
    }

    /**
     * fires the given cannon. Default cannon design settings for players are used.
     * @param cannon the cannon to fire
     * @param player the player how is firing the cannon
     * @return returns a MessagesEnum if the firing was successful or not
     */
    public MessageEnum playerFiring(Cannon cannon, Player player, InteractAction interaction)
    {
        return plugin.getFireCannon().playerFiring(cannon, player, interaction);
    }

    /**
     * fires the given cannon. Default cannon design settings for redstone are used.
     * @param cannon the cannon to fire
     * @return returns a MessagesEnum if the firing was successful or not
     */
    public MessageEnum redstoneFiring(Cannon cannon, InteractAction interaction)
    {
        return plugin.getFireCannon().redstoneFiring(cannon, interaction);
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
    public HashSet<Cannon> getCannonsInSphere(Location center, double sphereRadius)
    {
        return plugin.getCannonManager().getCannonsInSphere(center, sphereRadius);
    }

    /**
     * returns all known cannon in a box around the given location
     * @param center - center of the box
     * @param lengthX - box length in X
     * @param lengthY - box length in Y
     * @param lengthZ - box length in Z
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannonsInBox(Location center, double lengthX, double lengthY, double lengthZ)
    {
        return plugin.getCannonManager().getCannonsInBox(center, lengthX, lengthY, lengthZ);
    }

    /**
     * returns all cannons for a list of locations - this will update all cannon locations
     * @param locations - a list of location to search for cannons
     * @param player - player which operates the cannon
     * @param silent - no messages will be displayed if silent is true
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannons(List<Location> locations, Player player, boolean silent)
    {
        return plugin.getCannonManager().getCannons(locations, player.getName(), silent);
    }

    /**
     * returns all cannons for a list of locations - this will update all cannon locations
     * @param locations - a list of location to search for cannons
     * @param player - player which operates the cannon
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannons(List<Location> locations, Player player)
    {
        return plugin.getCannonManager().getCannons(locations, player.getName(), true);
    }


    public void setCannonAngle(Cannon cannon, double horizontal, double vertical)
    {
        //plugin.getAiming().
    }



}
