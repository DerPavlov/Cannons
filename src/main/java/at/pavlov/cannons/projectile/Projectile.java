package at.pavlov.cannons.projectile;

import java.util.ArrayList;
import java.util.List;

import at.pavlov.cannons.container.SoundHolder;
import at.pavlov.cannons.container.SpawnEntityHolder;
import at.pavlov.cannons.container.SpawnMaterialHolder;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import at.pavlov.cannons.container.ItemHolder;



public class Projectile implements Cloneable{
	private String projectileID;
	private String projectileName;
	private String description;
	private String itemName;
	private ItemHolder loadingItem;
	//list of items or blocks that can represent this this (e.g. redstone dust may for wire when you click a block)
	private List<ItemHolder> alternativeItemList = new ArrayList<ItemHolder>();

	//properties of the cannonball
	private EntityType projectileEntity;
	private boolean projectileOnFire;
	private double velocity;
	private double penetration;
	private double timefuse;
	private double automaticFiringDelay;
	private int automaticFiringMagazineSize;
	private int numberOfBullets;
	private double spreadMultiplier;
	private List<ProjectileProperties> propertyList = new ArrayList<ProjectileProperties>();

	//smokeTrail
	private boolean smokeTrailEnabled;
	private int smokeTrailDistance;
	private BlockData smokeTrailMaterial;
	private double smokeTrailDuration;
	private boolean smokeTrailParticleEnabled;
	private Particle smokeTrailParticleType;
	private int smokeTrailParticleCount;
	private double smokeTrailParticleOffsetX;
	private double smokeTrailParticleOffsetY;
	private double smokeTrailParticleOffsetZ;
	private double smokeTrailParticleSpeed;

	//explosion
	private float explosionPower;
	private boolean explosionPowerDependsOnVelocity;
	private boolean explosionDamage;
	private boolean underwaterDamage;
	private boolean penetrationDamage;
	private double directHitDamage;
	private double playerDamageRange;
	private double playerDamage;
	private double potionRange;
	private double potionDuration;
	private int potionAmplifier;
	private List<PotionEffectType> potionsEffectList = new ArrayList<PotionEffectType>();
	private boolean impactIndicator;

	//cluster
	private boolean clusterExplosionsEnabled;
	private boolean clusterExplosionsInBlocks;
	private int clusterExplosionsAmount;
	private double clusterExplosionsMinDelay;
	private double clusterExplosionsMaxDelay;
	private double clusterExplosionsRadius;
	private double clusterExplosionsPower;

	//placeBlock
	private boolean spawnEnabled;
	private double spawnBlockRadius;
	private double spawnEntityRadius;
	private double spawnVelocity;
	private List<SpawnMaterialHolder> spawnBlocks = new ArrayList<SpawnMaterialHolder>();
	private List<SpawnEntityHolder> spawnEntities = new ArrayList<SpawnEntityHolder>();
	private List<String> spawnProjectiles;

	//spawn Fireworks
	private boolean fireworksEnabled;
	private boolean fireworksFlicker;
	private boolean fireworksTrail;
	private FireworkEffect.Type fireworksType;
	private List<Integer> fireworksColors;
	private List<Integer> fireworksFadeColors;

	//messages
	private boolean impactMessage;

	//sounds
	private SoundHolder soundLoading;
	private SoundHolder soundImpact;
	private SoundHolder soundImpactProtected;
	private SoundHolder soundImpactWater;

	//permissions
	private List<String> permissionLoad = new ArrayList<String>();

	public Projectile(String id)
	{
		this.projectileID = id;
	}

	@Override
	public Projectile clone(){
		try
		{
			// call clone in Object.
			return (Projectile) super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			System.out.println("Cloning not allowed.");
			return this;
		}
	}

	/**
	 * returns true if both the id and data are equivalent of data == -1
	 * @param materialHolder the material of the loaded item
	 * @return true if the materials match
	 */
	public boolean equals(ItemHolder materialHolder)
	{
		return loadingItem.equalsFuzzy(materialHolder);
	}

	/**
	 * returns true if both the id and data are equivalent of data == -1
	 * @param projectileID the file name id of the projectile
	 * @return true if the id matches
	 */
	public boolean equals(String projectileID)
	{
		return this.projectileID.equals(projectileID);
	}



	/**
	 * returns ID, Data, name and lore of the projectile loading item
	 * @return ID, Data, name and lore of the projectile loading item
	 */
	public String toString()
	{
		return loadingItem.toString();
	}

	/**
	 * returns ID and data of the loadingItem
	 * @return ID and data of the loadingItem
	 */
	public String getMaterialInformation() {
		return loadingItem.getType().toString();
	}

	/**
	 * returns true if the projectile has this property
	 * @param properties properties of the projectile
	 * @return true if the projectile has this property
	 */
	public boolean hasProperty(ProjectileProperties properties)
	{
		for (ProjectileProperties propEnum : this.getPropertyList())
		{
			if (propEnum.equals(properties))
				return true;
		}
		return false;
	}

	/**
	 * returns true if the player has permission to use that projectile
	 * @param player who tried to load this projectile
	 * @return true if the player can load this projectile
	 */
	public boolean hasPermission(Player player)
	{
		if (player == null) return true;

		for (String perm : permissionLoad)
		{
			if(!player.hasPermission(perm))
			{
				//missing permission
				return false;
			}
		}
		//player has all permissions
		return true;
	}



	public String getItemName()
	{
		return itemName;
	}


	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}


	public String getProjectileName()
	{
		return projectileName;
	}


	public void setProjectileName(String projectileName)
	{
		this.projectileName = projectileName;
	}


	public double getVelocity()
	{
		return velocity;
	}


	public void setVelocity(double velocity)
	{
		this.velocity = velocity;
	}


	public double getPenetration()
	{
		return penetration;
	}


	public void setPenetration(double penetration)
	{
		this.penetration = penetration;
	}


	public double getTimefuse()
	{
		return timefuse;
	}


	public void setTimefuse(double timefuse)
	{
		this.timefuse = timefuse;
	}


	public int getNumberOfBullets()
	{
		return numberOfBullets;
	}


	public void setNumberOfBullets(int numberOfBullets)
	{
		this.numberOfBullets = numberOfBullets;
	}


	public double getSpreadMultiplier()
	{
		return spreadMultiplier;
	}


	public void setSpreadMultiplier(double spreadMultiplier)
	{
		this.spreadMultiplier = spreadMultiplier;
	}


	List<ProjectileProperties> getPropertyList()
	{
		return propertyList;
	}


	public void setPropertyList(List<ProjectileProperties> propertyList)
	{
		this.propertyList = propertyList;
	}


	public float getExplosionPower()
	{
		return explosionPower;
	}


	public void setExplosionPower(float explosionPower)
	{
		this.explosionPower = explosionPower;
	}


	public double getPotionRange()
	{
		return potionRange;
	}


	public void setPotionRange(double potionRange)
	{
		this.potionRange = potionRange;
	}


	public double getPotionDuration()
	{
		return potionDuration;
	}


	public void setPotionDuration(double potionDuration)
	{
		this.potionDuration = potionDuration;
	}


	public int getPotionAmplifier()
	{
		return potionAmplifier;
	}


	public void setPotionAmplifier(int potionAmplifier)
	{
		this.potionAmplifier = potionAmplifier;
	}


	public List<PotionEffectType> getPotionsEffectList()
	{
		return potionsEffectList;
	}


	public void setPotionsEffectList(List<PotionEffectType> potionsEffectList)
	{
		this.potionsEffectList = potionsEffectList;
	}

	public String getProjectileId()
	{
		return projectileID;
	}

	public void setProjectileID(String projectileID)
	{
		this.projectileID = projectileID;
	}

	public ItemHolder getLoadingItem()
	{
		return loadingItem;
	}

	public void setLoadingItem(ItemHolder loadingItem)
	{
		this.loadingItem = loadingItem;
	}

	public List<ItemHolder> getAlternativeItemList()
	{
		return alternativeItemList;
	}

	public void setAlternativeItemList(List<ItemHolder> alternativeItemList)
	{
		this.alternativeItemList = alternativeItemList;
	}

	public boolean getExplosionDamage()
	{
		return explosionDamage;
	}

	public void setExplosionDamage(boolean explosionDamage)
	{
		this.explosionDamage = explosionDamage;
	}

	public boolean getPenetrationDamage()
	{
		return penetrationDamage;
	}

	public void setPenetrationDamage(boolean penetrationDamage)
	{
		this.penetrationDamage = penetrationDamage;
	}

	public List<String> getPermissionLoad()
	{
		return permissionLoad;
	}

	public void setPermissionLoad(List<String> permissionLoad)
	{
		this.permissionLoad = permissionLoad;
	}

	public double getDirectHitDamage() {
		return directHitDamage;
	}

	public void setDirectHitDamage(double directHitDamage) {
		this.directHitDamage = directHitDamage;
	}

	public double getPlayerDamageRange() {
		return playerDamageRange;
	}

	public void setPlayerDamageRange(double playerDamageRange) {
		this.playerDamageRange = playerDamageRange;
	}

	public double getPlayerDamage() {
		return playerDamage;
	}

	public void setPlayerDamage(double playerDamage) {
		this.playerDamage = playerDamage;
	}

	public boolean isImpactMessage() {
		return impactMessage;
	}

	public void setImpactMessage(boolean impactMessage) {
		this.impactMessage = impactMessage;
	}

	public boolean isUnderwaterDamage() {
		return underwaterDamage;
	}

	public void setUnderwaterDamage(boolean underwaterDamage) {
		this.underwaterDamage = underwaterDamage;
	}

	public boolean isFireworksFlicker() {
		return fireworksFlicker;
	}

	public void setFireworksFlicker(boolean fireworksFlicker) {
		this.fireworksFlicker = fireworksFlicker;
	}

	public FireworkEffect.Type getFireworksType() {
		return fireworksType;
	}

	public void setFireworksType(FireworkEffect.Type fireworksType) {
		this.fireworksType = fireworksType;
	}

	public List<Integer> getFireworksColors() {
		return fireworksColors;
	}

	public void setFireworksColors(List<Integer> fireworksColors) {
		this.fireworksColors = fireworksColors;
	}

	public List<Integer> getFireworksFadeColors() {
		return fireworksFadeColors;
	}

	public void setFireworksFadeColors(List<Integer> fireworksFadeColors) {
		this.fireworksFadeColors = fireworksFadeColors;
	}

	public boolean isFireworksTrail() {
		return fireworksTrail;
	}

	public void setFireworksTrail(boolean fireworksTrail) {
		this.fireworksTrail = fireworksTrail;
	}

	public double getAutomaticFiringDelay() {
		return automaticFiringDelay;
	}

	public void setAutomaticFiringDelay(double automaticFiringDelay) {
		this.automaticFiringDelay = automaticFiringDelay;
	}

	public int getAutomaticFiringMagazineSize() {
		return automaticFiringMagazineSize;
	}

	public void setAutomaticFiringMagazineSize(int automaticFiringMagazineSize) {
		this.automaticFiringMagazineSize = automaticFiringMagazineSize;
	}

	public boolean isFireworksEnabled() {
		return fireworksEnabled;
	}

	public void setFireworksEnabled(boolean fireworksEnabled) {
		this.fireworksEnabled = fireworksEnabled;
	}

	public EntityType getProjectileEntity() {
		return projectileEntity;
	}

	public void setProjectileEntity(EntityType projectileEntity) {
		this.projectileEntity = projectileEntity;
	}

	public boolean isProjectileOnFire() {
		return projectileOnFire;
	}

	public void setProjectileOnFire(boolean projectileOnFire) {
		this.projectileOnFire = projectileOnFire;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isExplosionPowerDependsOnVelocity()
	{
		return explosionPowerDependsOnVelocity;
	}


	public void setExplosionPowerDependsOnVelocity(boolean explosionPowerDependsOnVelocity)
	{
		this.explosionPowerDependsOnVelocity = explosionPowerDependsOnVelocity;
	}

	public boolean isClusterExplosionsEnabled() {
		return clusterExplosionsEnabled;
	}

	public void setClusterExplosionsEnabled(boolean clusterExplosionsEnabled) {
		this.clusterExplosionsEnabled = clusterExplosionsEnabled;
	}

	public int getClusterExplosionsAmount() {
		return clusterExplosionsAmount;
	}

	public void setClusterExplosionsAmount(int clusterExplosionsAmount) {
		this.clusterExplosionsAmount = clusterExplosionsAmount;
	}

	public double getClusterExplosionsRadius() {
		return clusterExplosionsRadius;
	}

	public void setClusterExplosionsRadius(double clusterExplosionsRadius) {
		this.clusterExplosionsRadius = clusterExplosionsRadius;
	}

	public boolean isClusterExplosionsInBlocks() {
		return clusterExplosionsInBlocks;
	}

	public void setClusterExplosionsInBlocks(boolean clusterExplosionsInBlocks) {
		this.clusterExplosionsInBlocks = clusterExplosionsInBlocks;
	}

	public double getClusterExplosionsMinDelay() {
		return clusterExplosionsMinDelay;
	}

	public void setClusterExplosionsMinDelay(double clusterExplosionsMinDelay) {
		this.clusterExplosionsMinDelay = clusterExplosionsMinDelay;
	}

	public double getClusterExplosionsMaxDelay() {
		return clusterExplosionsMaxDelay;
	}

	public void setClusterExplosionsMaxDelay(double clusterExplosionsMaxDelay) {
		this.clusterExplosionsMaxDelay = clusterExplosionsMaxDelay;
	}

	public double getClusterExplosionsPower() {
		return clusterExplosionsPower;
	}

	public void setClusterExplosionsPower(double clusterExplosionsPower) {
		this.clusterExplosionsPower = clusterExplosionsPower;
	}

	public boolean isSpawnEnabled() {
		return spawnEnabled;
	}

	public void setSpawnEnabled(boolean spawnEnabled) {
		this.spawnEnabled = spawnEnabled;
	}

	public double getSpawnVelocity() {
		return spawnVelocity;
	}

	public void setSpawnVelocity(double spawnVelocity) {
		this.spawnVelocity = spawnVelocity;
	}

	public List<SpawnMaterialHolder> getSpawnBlocks() {
		return spawnBlocks;
	}

	public void setSpawnBlocks(List<SpawnMaterialHolder> spawnBlocks) {
		this.spawnBlocks = spawnBlocks;
	}

	public List<SpawnEntityHolder> getSpawnEntities() {
		return spawnEntities;
	}

	public void setSpawnEntities(List<SpawnEntityHolder> spawnEntities) {
		this.spawnEntities = spawnEntities;
	}

	public List<String> getSpawnProjectiles() {
		return spawnProjectiles;
	}

	public void setSpawnProjectiles(List<String> spawnProjectiles) {
		this.spawnProjectiles = spawnProjectiles;
	}

	public double getSpawnEntityRadius() {
		return spawnEntityRadius;
	}

	public void setSpawnEntityRadius(double spawnEntityRadius) {
		this.spawnEntityRadius = spawnEntityRadius;
	}

	public double getSpawnBlockRadius() {
		return spawnBlockRadius;
	}

	public void setSpawnBlockRadius(double spawnBlockRadius) {
		this.spawnBlockRadius = spawnBlockRadius;
	}

	public SoundHolder getSoundLoading() {
		return soundLoading;
	}

	public void setSoundLoading(SoundHolder soundLoading) {
		this.soundLoading = soundLoading;
	}

	public SoundHolder getSoundImpact() {
		return soundImpact;
	}

	public void setSoundImpact(SoundHolder soundImpact) {
		this.soundImpact = soundImpact;
	}

	public SoundHolder getSoundImpactWater() {
		return soundImpactWater;
	}

	public void setSoundImpactWater(SoundHolder soundImpactWater) {
		this.soundImpactWater = soundImpactWater;
	}

	public SoundHolder getSoundImpactProtected() {
		return soundImpactProtected;
	}

	public void setSoundImpactProtected(SoundHolder soundImpactProtected) {
		this.soundImpactProtected = soundImpactProtected;
	}

	public boolean isImpactIndicator() {
		return impactIndicator;
	}

	public void setImpactIndicator(boolean impactIndicator) {
		this.impactIndicator = impactIndicator;
	}

	public boolean isSmokeTrailEnabled() {
		return smokeTrailEnabled;
	}

	public void setSmokeTrailEnabled(boolean smokeTrailEnabled) {
		this.smokeTrailEnabled = smokeTrailEnabled;
	}

	public BlockData getSmokeTrailMaterial() {
		return smokeTrailMaterial;
	}

	public void setSmokeTrailMaterial(BlockData smokeTrailMaterial) {
		this.smokeTrailMaterial = smokeTrailMaterial;
	}

	public double getSmokeTrailDuration() {
		return smokeTrailDuration;
	}

	public void setSmokeTrailDuration(double smokeTrailDuration) {
		this.smokeTrailDuration = smokeTrailDuration;
	}

	public int getSmokeTrailDistance() {
		return smokeTrailDistance;
	}

	public void setSmokeTrailDistance(int smokeTrailDistance) {
		this.smokeTrailDistance = smokeTrailDistance;
	}

	public void setSmokeTrailParticleEnabled(boolean smokeTrailParticleEnabled) {
		this.smokeTrailParticleEnabled = smokeTrailParticleEnabled;
	}

	public boolean isSmokeTrailParticleEnabled() { return smokeTrailParticleEnabled; }

	public void setSmokeTrailParticleType(Particle smokeTrailParticleType) {
		this.smokeTrailParticleType = smokeTrailParticleType;
	}

	public Particle getSmokeTrailParticleType() { return smokeTrailParticleType; }

	public void setSmokeTrailParticleCount(int smokeTrailParticleCount) {
		this.smokeTrailParticleCount = smokeTrailParticleCount;
	}

	public int getSmokeTrailParticleCount() { return smokeTrailParticleCount; }

	public void setSmokeTrailParticleOffsetX(double smokeTrailParticleOffsetX) {
		this.smokeTrailParticleOffsetX = smokeTrailParticleOffsetX;
	}

	public double getSmokeTrailParticleOffsetX() { return smokeTrailParticleOffsetX; }

	public void setSmokeTrailParticleOffsetY(double smokeTrailParticleOffsetY) {
		this.smokeTrailParticleOffsetY = smokeTrailParticleOffsetY;
	}

	public double getSmokeTrailParticleOffsetY() { return smokeTrailParticleOffsetY; }

	public void setSmokeTrailParticleOffsetZ(double smokeTrailParticleOffsetZ) {
		this.smokeTrailParticleOffsetZ = smokeTrailParticleOffsetZ;
	}

	public double getSmokeTrailParticleOffsetZ() { return smokeTrailParticleOffsetZ; }

	public void setSmokeTrailParticleSpeed(double smokeTrailParticleSpeed) {
		this.smokeTrailParticleSpeed = smokeTrailParticleSpeed;
	}

	public double getSmokeTrailParticleSpeed() { return smokeTrailParticleSpeed; }

}
