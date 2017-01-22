package at.pavlov.cannons.Enum;


public enum ProjectileCause {

    //Error Messages
    PlayerFired("Player fired"),
    RedstoneFired("Redstone fired"),
    SentryFired("Sentry fired"),
    SpawnedProjectile("Spawned projectile"),
    DeflectedProjectile("Deflected projectile"),
    UnknownFired("fired unknown cause");

    private final String str;

    ProjectileCause(String str)
    {
        this.str = str;
    }

    public String getString()
    {
        return str;
    }

}
