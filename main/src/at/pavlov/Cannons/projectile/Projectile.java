package at.pavlov.Cannons.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import at.pavlov.Cannons.container.MaterialHolder;



public class Projectile {
	private String projectileID;
	private String projectileName;
	private String itemName;
	private MaterialHolder loadingItem;
	//list of items or blocks that can represent this this (e.g. redstone dust may for wire when you click a block)
	private List<MaterialHolder> alternativeItemList = new ArrayList<MaterialHolder>();
	
	//properties of the cannonball
	private double velocity;	
	private double penetration;
	private double timefuse;
	private int numberOfBullets;
	private double spreadMultiplier;
	private List<ProjectileProperties> propertyList = new ArrayList<ProjectileProperties>();
	
	//explosion
	private float explosionPower;
	private boolean explosionDamage;
	private boolean penetrationDamage;
	private double potionRange;
	private double potionDuration;
	private int potionAmplifier;
	private List<PotionEffectType> potionsEffectList = new ArrayList<PotionEffectType>();
	
	//placeBlock
	private double blockPlaceRadius;
	private int blockPlaceAmount;
	private double blockPlaceVelocity;
	private List<MaterialHolder> blockPlaceList = new ArrayList<MaterialHolder>();

	
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
	
	/**
	 * returns true if both the id and data are equivalent of data == -1
	 * @param id
	 * @param data
	 * @return
	 */
	public boolean equalsFuzzy(int id, int data)
	{
		if (id == loadingItem.getId())
		{
			return (data == loadingItem.getData() || data == -1 || loadingItem.getData() == -1);
		}
		return false;
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
				System.out.println("perm " + perm);
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


	public List<ProjectileProperties> getPropertyList()
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


	

	

}
