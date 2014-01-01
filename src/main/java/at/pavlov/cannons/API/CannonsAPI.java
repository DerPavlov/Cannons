package at.pavlov.cannons.API;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.config.MessageEnum;
import org.bukkit.entity.Player;

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

}
