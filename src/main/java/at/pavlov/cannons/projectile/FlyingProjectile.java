package at.pavlov.cannons.projectile;

import at.pavlov.cannons.Enum.ProjectileCause;
import at.pavlov.cannons.container.MovingObject;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.UUID;


public class FlyingProjectile
{
	private final long spawnTime;
	
	private final UUID entityUID;
    private UUID shooterUID;
    private UUID worldUID;
    private UUID cannonUID;
	private final Projectile projectile;
    private final org.bukkit.projectiles.ProjectileSource source;
    //location of the shooterUID before firing - important for teleporting the player back - observer property
    private final Location playerlocation;
    private Location impactLocation;
    private Location lastSmokeTrailLocation;
    //Important for visual splash effect when the cannonball hits the water surface
    private boolean inWater;
    private boolean wasInWater;
    //if the teleport was already performed
    private boolean teleported;
    //was the projectile fired by a player, redstone or a sentry
    private ProjectileCause projectileCause;

    private MovingObject predictor;



	
	public FlyingProjectile(Projectile projectile, org.bukkit.entity.Projectile projectile_entity, UUID shooterUID, org.bukkit.projectiles.ProjectileSource source, Location playerLoc, UUID cannonId, ProjectileCause projectileCause)
	{
        Validate.notNull(shooterUID, "shooterUID for the projectile can't be null");
        this.entityUID = projectile_entity.getUniqueId();
        this.worldUID = projectile_entity.getWorld().getUID();

        this.wasInWater = this.isInWater();
		this.projectile = projectile;
        this.cannonUID = cannonId;
        this.shooterUID = shooterUID;
        this.playerlocation = playerLoc;
        this.source = source;
        projectile_entity.setShooter(source);
        this.projectileCause = projectileCause;

		this.spawnTime = System.currentTimeMillis();
        this.teleported = false;

        //set location and speed
        Location new_loc = projectile_entity.getLocation();
        predictor = new MovingObject(new_loc, projectile_entity.getVelocity(), projectile.getProjectileEntity());

        this.lastSmokeTrailLocation = new_loc;
    }

    public UUID getShooterUID()
    {
        return shooterUID;
    }

    /*
     * Returns the entity of the flying projectile
     * This is time consuming, the projectile should be cached
     * @return
     */
	public org.bukkit.entity.Projectile getProjectileEntity()
	{
        World world = Bukkit.getWorld(worldUID);
        for (Entity entity : world.getEntitiesByClass(org.bukkit.entity.Projectile.class)) {
            if (entity instanceof org.bukkit.entity.Projectile && entity.getUniqueId().equals(entityUID)) {
                return (org.bukkit.entity.Projectile) entity;
            }
        }
        return null;
	}

	public Projectile getProjectile()
	{
		return projectile;
	}

	public long getSpawnTime()
	{
		return spawnTime;
	}

    public Location getPlayerlocation() {
        return playerlocation;
    }

    /**
     * check if the projectile in in a liquid
     * @return true if the projectile is in a liquid
     */
    private boolean isInWaterCheck(org.bukkit.entity.Projectile projectile_entity)
    {
        if(projectile_entity!=null)
        {
            Block block = projectile_entity.getLocation().getBlock();
            if (block != null)
            {
                return block.isLiquid();
            }
        }
        return false;
    }

    public boolean isInWater() {
        return inWater;
    }

    /**
     * if the projectile has entered the water surface
     * @return true if the projectile has entered the water surface
     */
    public boolean isWaterSurface(org.bukkit.entity.Projectile projectile_entity){
        return !wasInWater&&isInWaterCheck(projectile_entity);
    }

    /**
     * returns if the projectile has entered the water surface and updates also inWater
     * @return true if the projectile has entered water
     */
    public boolean updateWaterSurfaceCheck(org.bukkit.entity.Projectile projectile_entity)
    {
        boolean isSurface = isWaterSurface(projectile_entity);
        inWater = isInWaterCheck(projectile_entity);
        wasInWater = inWater;
        return isSurface;
    }

    public boolean wasInWater() {
        return wasInWater;
    }

    public void setWasInWater(boolean wasInWater) {
        this.wasInWater = wasInWater;
    }

    /**
     * searches for the projectile and checks if the projectile is still alive and valid
     *
     * @return returns false if the projectile entity is null
     */
    public boolean isValid()
    {
        return isValid(getProjectileEntity());
    }

    /**
     * if the projectile is still alive and valid
     * a projectile is valid if it has an entity, is not below -64 and younger than 1h (60*60*1000)
     * @return returns false if the projectile entity is null
     */
    public boolean isValid(org.bukkit.entity.Projectile projectile_entity)
    {
        return (projectile_entity != null && projectile_entity.getLocation().getBlockY() > -64 && System.currentTimeMillis() < getSpawnTime() + 3600000);
    }

    /**
     * updated the location and speed of the projectile to the expected values
     */
    public void update()
    {
        predictor.updateProjectileLocation(isInWater());
    }

    /**
     * revert update of the location
     */
    public void revertUpdate()
    {
        predictor.revertProjectileLocation(isInWater());
    }

    /**
     * returns the calculated location of the projectile
     * @return the location where the projectile should be
     */
    public Location getExpectedLocation()
    {
        return predictor.getLocation();
    }

    /**
     * returns actual location of the projectile
     * @return momentary position of the projectile
     */
    public Location getActualLocation(org.bukkit.entity.Projectile projectile_entity)
    {
        return projectile_entity.getLocation();
    }

    /**
     * returns the distance of the projectile location to the calculated location
     * @return distance of the projectile location to the calculated location
     */
    public double distanceToProjectile(org.bukkit.entity.Projectile projectile_entity)
    {
        return projectile_entity.getLocation().toVector().distance(predictor.getLoc());
    }

    /**
     * teleports the projectile to the predicted location
     */
    public void teleportToPrediction(org.bukkit.entity.Projectile projectile_entity)
    {
        if (projectile_entity == null)
            return;
        projectile_entity.teleport(predictor.getLocation());
        projectile_entity.setVelocity(predictor.getVel());
    }

    /**
     * teleports the projectile to the given location
     * @param loc target location
     * @param vel velocity of the projectile
     */
    public void teleport(Location loc, Vector vel)
    {
        this.predictor.setLocation(loc);
        this.predictor.setVel(vel);
        teleportToPrediction(getProjectileEntity());
    }

    @Override
    public int hashCode() {
        //compare projectile entities
        return entityUID.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        //equal if the projectile entities are equal
        FlyingProjectile obj2 = (FlyingProjectile) obj;
        return this.getUID().equals(obj2.getUID());
    }

    public UUID getUID()
    {
        return entityUID;
    }

    public Location getImpactLocation() {
        return impactLocation;
    }

    public void setImpactLocation(Location impactLocation) {
        this.impactLocation = impactLocation;
    }

    public UUID getCannonUID() {
        return cannonUID;
    }

    public void setCannonUID(UUID cannonUID) {
        this.cannonUID = cannonUID;
    }

    public org.bukkit.projectiles.ProjectileSource getSource() {
        return source;
    }

    public UUID getWorldUID()
    {
        return worldUID;
    }

    public World getWorld()
    {
        return Bukkit.getWorld(worldUID);
    }

    public boolean isTeleported() {
        return teleported;
    }

    public void setTeleported(boolean teleported) {
        this.teleported = teleported;
    }

    public Location getLastSmokeTrailLocation() {
        return lastSmokeTrailLocation;
    }

    public void setLastSmokeTrailLocation(Location lastSmokeTrailLocation) {
        this.lastSmokeTrailLocation = lastSmokeTrailLocation;
    }

    public ProjectileCause getProjectileCause() {
        return projectileCause;
    }
}
