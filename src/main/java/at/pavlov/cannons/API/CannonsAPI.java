package at.pavlov.cannons.API;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.Enum.MessageEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
     * @param playerUID - player UID searching for the cannon. If there is no cannon he will be the owner. If null no new Cannon can be created.
     * @return - null if there is no cannon, else the cannon
     */
    public Cannon getCannon(Location location, UUID playerUID)
    {
        return plugin.getCannonManager().getCannon(location, playerUID);
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
     * @param playerUID - player UID which operates the cannon
     * @param silent - no messages will be displayed if silent is true
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannons(List<Location> locations, UUID playerUID, boolean silent)
    {
        return plugin.getCannonManager().getCannons(locations, playerUID, silent);
    }

    /**
     * returns all cannons for a list of locations - this will update all cannon locations
     * @param locations - a list of location to search for cannons
     * @param playerUID - player UID which operates the cannon
     * @return - list of all cannons in this sphere
     */
    public HashSet<Cannon> getCannons(List<Location> locations, UUID playerUID)
    {
        return plugin.getCannonManager().getCannons(locations, playerUID, true);
    }

    /**
     * returns the cannon from the storage
     * @param uid UUID of the cannon
     * @return the cannon from the storage
     */
    public Cannon getCannon(UUID uid)
    {
        return plugin.getCannonManager().getCannon(uid);
    }


    public void setCannonAngle(Cannon cannon, double horizontal, double vertical)
    {
        //plugin.getAiming().
    }

    /**
     * removes a cannon from the list
     * @param uid UID of the cannon
     * @param breakCannon the cannon will explode and all cannon blocks will drop
     * @param canExplode if the cannon can explode when loaded with gunpowder
     * @param cause the reason way the cannon was broken
     */
    public void removeCannon(UUID uid, boolean breakCannon, boolean canExplode, BreakCause cause)
    {
        plugin.getCannonManager().removeCannon(uid, breakCannon, canExplode, cause, true);
    }



}
