package at.pavlov.cannons.Enum;


public enum EntityDataType {
    FUSE_TIME ("Fuse"),
    POTION_EFFECT ("Potion"),
    COLOR ("Color"),
    PARTICLE ("Particle"),
    REAPPLICATION_DELAY ("ReapplicationDelay"),
    RADIUS ("Radius"),
    RADIUS_PER_TICK ("RadiusPerTick"),
    RADIUS_ON_USE ("RadiusOnUse"),
    DURATION ("Duration"),
    DURATION_ON_USE ("DurationOnUse"),
    EFFECTS ("Effects"),
    WAIT_TIME ("WaitTime ");

    private final String str;

    EntityDataType(String str)
    {
        this.str = str;
    }

    public String getString()
    {
        return str;
    }
}
