package at.pavlov.Cannons.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

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
	private int explosionPower;
	private boolean blockDamage;
	private double effectRange;
	private double effectDuration;
	private int effectAmplifier;
	private List<PotionEffect> entityEffects = new ArrayList<PotionEffect>();
	
	//placeBlock
	private double blockPlaceRadius;
	private double blockPlaceAmount;
	private List<MaterialHolder> blockPlaceList = new ArrayList<MaterialHolder>();

	
	private List<String> permissions = new ArrayList<String>();
	
	public Projectile(){

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
		return loadingItem.toString();
	}
	
	public boolean hasProperty(ProjectileProperties properties)
	{
		for (ProjectileProperties propEnum : ProjectileProperties.values())
		{
			if (propEnum == properties)
				return true;
		}
		return false;
	}
	
	/**
	 * returns true if the player has permission to use that projectile
	 * @param player
	 * @return
	 */
	public boolean hasPermission(Player player)
	{
		for (String perm : permissions)
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


	public List<ProjectileProperties> getPropertyList()
	{
		return propertyList;
	}


	public void setPropertyList(List<ProjectileProperties> propertyList)
	{
		this.propertyList = propertyList;
	}


	public int getExplosionPower()
	{
		return explosionPower;
	}


	public void setExplosionPower(int explosionPower)
	{
		this.explosionPower = explosionPower;
	}


	public boolean isBlockDamage()
	{
		return blockDamage;
	}


	public void setBlockDamage(boolean blockDamage)
	{
		this.blockDamage = blockDamage;
	}


	public double getEffectRange()
	{
		return effectRange;
	}


	public void setEffectRange(double effectRange)
	{
		this.effectRange = effectRange;
	}


	public double getEffectDuration()
	{
		return effectDuration;
	}


	public void setEffectDuration(double effectDuration)
	{
		this.effectDuration = effectDuration;
	}


	public int getEffectAmplifier()
	{
		return effectAmplifier;
	}


	public void setEffectAmplifier(int effectAmplifier)
	{
		this.effectAmplifier = effectAmplifier;
	}


	public List<PotionEffect> getEntityEffects()
	{
		return entityEffects;
	}


	public void setEntityEffects(List<PotionEffect> entityEffects)
	{
		this.entityEffects = entityEffects;
	}


	public double getBlockPlaceRadius()
	{
		return blockPlaceRadius;
	}


	public void setBlockPlaceRadius(double blockPlaceRadius)
	{
		this.blockPlaceRadius = blockPlaceRadius;
	}


	public double getBlockPlaceAmount()
	{
		return blockPlaceAmount;
	}


	public void setBlockPlaceAmount(double blockPlaceAmount)
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


	

	

}
