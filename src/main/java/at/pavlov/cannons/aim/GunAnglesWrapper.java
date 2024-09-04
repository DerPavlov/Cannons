package at.pavlov.cannons.aim;

public class GunAnglesWrapper {
    public GunAngles angles;
    public boolean combine;
    public GunAnglesWrapper(GunAngles angles, boolean combine) {
        this.angles = angles;
        this.combine = combine;
    }
}
