package at.pavlov.cannons.aim;

import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.cannon.Cannon;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import static at.pavlov.cannons.aim.GunAngles.getGunAngle;

public class GunAnglesWrapper {
    public GunAngles angles;
    public boolean combine;
    public GunAnglesWrapper(GunAngles angles, boolean combine) {
        this.angles = angles;
        this.combine = combine;
    }
}
