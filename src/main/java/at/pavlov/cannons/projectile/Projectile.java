package at.pavlov.cannons.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import at.pavlov.cannons.container.MaterialHolder;



public class Projectile implements Cloneable{
	private String projectileID;
	private String projectileName;
    private String description;
	private String itemName;
	private MaterialHolder loadingItem;
	//list of items or blocks that can represent this this (e.g. redstone dust may for wire when you click a block)
	private List<MaterialHolder> alternativeItemList = new ArrayList<MaterialHolder>();
	
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
	
	//explosion
	private float explosionPower;
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
	
	//placeBlock
	private double blockPlaceRadius;
	private int blockPlaceAmount;
	private double blockPlaceVelocity;
    private double tntFuseTime;
	private List<MaterialHolder> blockPlaceList = new ArrayList<MaterialHolder>();

    //spawnProjectile
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

	//permissions
	private List<String> permissionLoad = new ArrayList<String>();
	
	public Projectile(String id)
	{
		this.projectileID = id;
	}
	
	/**
	 * returns true if both projectiles have the same identifier
	 * @param projectile
	 * @return
	 */
	public boolean equals(Projectile projectile)
	{
		return projectile.getProjectileID().equals(projectileID); 
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
	 * @param id
	 * @param data
	 * @return
	 */
	public boolean equalsFuzzy(int id, int data)
	{
        return id == loadingItem.getId() && (data == loadingItem.getData() || data == -1 || loadingItem.getData() == -1);
    }
	

	/**
	 * returns the ID and Data of the projectile
	 */
	public String toString()
	{
		return loadingItem.getId() + ":" + loadingItem.getData();
	}
	
	/**
	 * returns true if the projectile has this property
	 * @param properties
	 * @return
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
	
	public boolean doesBlockPlace()
	{
		return (blockPlaceAmount > 0 && blockPlaceList != null && !blockPlaceList.isEmpty());
	}
	
	/**
	 * returns true if the player has permission to use that projectile
	 * @param player
	 * @return
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


	public double getBlockPlaceRadius()
	{
		return blockPlaceRadius;
	}


	public void setBlockPlaceRadius(double blockPlaceRadius)
	{
		this.blockPlaceRadius = blockPlaceRadius;
	}


	public int getBlockPlaceAmount()
	{
		return blockPlaceAmount;
	}


	public void setBlockPlaceAmount(int blockPlaceAmount)
	{
		this.blockPlaceAmount = blockPlaceAmount;
	}


	public List<MaterialHolder> getBlockPlaceList()
	{
		return blockPlaceList;
	}


	public void setBlockPlaceList(List<MaterialHolder> blockPlaceList)
	{
		this.blockPlaceList = blockPlaceList;
	}

	public String getProjectileID()
	{
		return projectileID;
	}

	public void setProjectileID(String projectileID)
	{
		this.projectileID = projectileID;
	}

	public MaterialHolder getLoadingItem()
	{
		return loadingItem;
	}

	public void setLoadingItem(MaterialHolder loadingItem)
	{
		this.loadingItem = loadingItem;
	}

	public List<MaterialHolder> getAlternativeItemList()
	{
		return alternativeItemList;
	}

	public void setAlternativeItemList(List<MaterialHolder> alternativeItemList)
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

	public double getBlockPlaceVelocity()
	{
		return blockPlaceVelocity;
	}

	public void setBlockPlaceVelocity(double blockPlaceVelocity)
	{
		this.blockPlaceVelocity = blockPlaceVelocity;
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

    public double getTntFuseTime() {
        return tntFuseTime;
    }

    public void setTntFuseTime(double tntFuseTime) {
        this.tntFuseTime = tntFuseTime;
    }

    public List<String> getSpawnProjectiles() {
        return spawnProjectiles;
    }

    public void setSpawnProjectiles(List<String> spawnProjectiles) {
        this.spawnProjectiles = spawnProjectiles;
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
}
