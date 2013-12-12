package at.pavlov.cannons.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.projectile.ProjectileProperties;
import at.pavlov.cannons.utils.CannonsUtil;

public class ProjectileStorage
{
	Cannons plugin;


    private static List<Projectile> projectileList = new ArrayList<Projectile>();
	
	public ProjectileStorage(Cannons plugin)
	{
		this.plugin = plugin;
		projectileList = new ArrayList<Projectile>();
	}
	
	/**
	 * returns the projectile that can be loaded with this item. If data=-1 the data is ignored
	 * @param item
	 * @return
	 */
	public static Projectile getProjectile(Cannon cannon, ItemStack item)
	{
		if (item == null) return null;
		return getProjectile(cannon, item.getTypeId(), item.getData().getData());
	}
	
	/**
	 * returns the projectiles that can be loaded int the cannon with this id and data. If data=-1 the data is ignored
	 * @param id
	 * @param data
	 * @return
	 */
	public static Projectile getProjectile(Cannon cannon, int id, int data)
	{
		for (Projectile projectile : projectileList)
		{
            if (cannon.getCannonDesign().canLoad(projectile) && projectile.equalsFuzzy(id, data))
				return projectile;
		}
		return null;
	}
	
	/**
	 * loads all projectile designs from the disk or copys the defaults if there is no design
	 */
	public void loadProjectiles()
	{
		plugin.logInfo("Loading projectile configs");
		
		//clear old list
		this.projectileList.clear();
		
		//load defaults if there no projectile folder
		// check if design folder is empty or does not exist
		if (CannonsUtil.isFolderEmpty(getPath()))
		{
			// the folder is empty, copy defaults
			plugin.logInfo("No projectiles loaded - loading default projectiles");
			copyDefaultProjectiles();
		}
		
		//get list of all files in /projectiles/
		ArrayList<String> projectileFileList = getProjectilesFiles();
		
		// stop if there are no files found
		if (projectileFileList == null || projectileFileList.size() == 0)
			return;

		for (String file : projectileFileList)
		{
			//load .yml
			Projectile projectile = loadYml(file);

			plugin.logDebug("load projectile " + file + " item " + projectile.getLoadingItem().toString());
			// add to the list if valid
			if (projectile != null)
				projectileList.add(projectile);
		}	
	}
	
	/**
	 * get all projectile file names form /projectiles
	 * @return
	 */
	private ArrayList<String> getProjectilesFiles()
	{
		ArrayList<String> projectileList = new ArrayList<String>();

		try
		{
			// check plugin/cannons/designs for .yml and .schematic files
			String ymlFile;
			File folder = new File(getPath());

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++)
			{
				if (listOfFiles[i].isFile())
				{
					ymlFile = listOfFiles[i].getName();
					if (ymlFile.endsWith(".yml") || ymlFile.endsWith(".yaml"))
					{
						// there is a shematic file and a .yml file
						projectileList.add(ymlFile);
					}
				}
			}
		}
		catch (Exception e)
		{
			plugin.logSevere("Error while checking yml and schematic " + e);
		}
		return projectileList;
	}
	
	/**
	 * loads the config for one cannon from the .yml file
	 * @param ymlFile
	 *            of the cannon config file
	 */
	private Projectile loadYml(String ymlFile)
	{
		//create a new projectile
		String id = CannonsUtil.removeExtension(ymlFile);
		Projectile projectile = new Projectile(id);
		// load .yml file

		File projectileFile = new File(getPath() + ymlFile);;
		FileConfiguration projectileConfig = YamlConfiguration.loadConfiguration(projectileFile);
		
		//load it from the disk
		
		//general
		projectile.setProjectileName(projectileConfig.getString("general.projectileName", "noProjectileName"));
		projectile.setItemName(projectileConfig.getString("general.itemName", "noItemName"));
		projectile.setLoadingItem(new MaterialHolder(projectileConfig.getString("general.loadingItem", "1:0")));	
		projectile.setAlternativeItemList(CannonsUtil.toMaterialHolderList(projectileConfig.getStringList("general.alternativeId")));
		
		//cannonball
		projectile.setVelocity(projectileConfig.getDouble("cannonball.velocity", 1.0));
		projectile.setPenetration(projectileConfig.getDouble("cannonball.penetration", 0.0));
		projectile.setPenetrationDamage(projectileConfig.getBoolean("cannonball.doesPenetrationDamage", true));
		projectile.setTimefuse(projectileConfig.getDouble("cannonball.timefuse", 0.0));
		projectile.setNumberOfBullets(projectileConfig.getInt("cannonball.numberOfBullets", 1));
		projectile.setSpreadMultiplier(projectileConfig.getDouble("cannonball.spreadMultiplier", 1.0));
		projectile.setPropertyList(toProperties(projectileConfig.getStringList("cannonball.properties")));
		
		//explosion
		projectile.setExplosionPower(projectileConfig.getInt("explosion.explosionPower", 2));
		projectile.setExplosionDamage(projectileConfig.getBoolean("explosion.doesExplosionDamage", true));
        projectile.setUnderwaterDamage(projectileConfig.getBoolean("explosion.doesUnderwaterExplosion", false));
        projectile.setDirectHitDamage(projectileConfig.getDouble("explosion.directHitDamage", 5.0));
        projectile.setPlayerDamageRange(projectileConfig.getDouble("explosion.playerDamageRange", 3.0));
        projectile.setPlayerDamage(projectileConfig.getDouble("explosion.playerDamage", 5.0));
		projectile.setPotionRange(projectileConfig.getDouble("explosion.potionRange", 1.0));
		projectile.setPotionDuration(projectileConfig.getDouble("explosion.potionDuration", 1.0));
		projectile.setPotionAmplifier(projectileConfig.getInt("explosion.potionAmplifier", 0));
		projectile.setPotionsEffectList(toPotionEffect(projectileConfig.getStringList("explosion.potionEffects")));

		//placeBlock
		projectile.setBlockPlaceRadius(projectileConfig.getDouble("placeBlock.radius", 3.0));
		projectile.setBlockPlaceAmount(projectileConfig.getInt("placeBlock.amount", 3));
		projectile.setBlockPlaceVelocity(projectileConfig.getDouble("placeBlock.velocity", 0.1));
		projectile.setBlockPlaceList(CannonsUtil.toMaterialHolderList(projectileConfig.getStringList("placeBlock.material")));

        //messages
        projectile.setImpactMessage(projectileConfig.getBoolean("messages.hasImpactMessage", false));

		//loadPermissions
		projectile.setPermissionLoad(projectileConfig.getStringList("loadPermission"));
		
		return projectile;
	}
	
	
	/**
	 * copys the default designs from the .jar to the disk
	 */
	private void copyDefaultProjectiles()
	{
		File cobblestoneYmlFile = new File(plugin.getDataFolder(), "projectiles/cobblestone.yml");
		if (!cobblestoneYmlFile.exists())
		{
			cobblestoneYmlFile.getParentFile().mkdirs();
			CannonsUtil.copyFile(plugin.getResource("projectiles/cobblestone.yml"), cobblestoneYmlFile);
		}
        File enderpearlYmlFile = new File(plugin.getDataFolder(), "projectiles/enderpearl.yml");
        if (!enderpearlYmlFile.exists())
        {
            enderpearlYmlFile.getParentFile().mkdirs();
            CannonsUtil.copyFile(plugin.getResource("projectiles/enderpearl.yml"), enderpearlYmlFile);
        }
	}
	
	
	/**
	 * returns the path of the projectiles folder
	 * @return
	 */
	private String getPath()
	{
		// Directory path here
		return "plugins/Cannons/projectiles/";
	}
	

	/**
	 * returns a PotionEffectTypeList from a list of strings
	 * @param stringList
	 * @return
	 */
	private List<PotionEffectType> toPotionEffect(List<String> stringList)
	{
		List<PotionEffectType> effectList = new ArrayList<PotionEffectType>();
		
		for (String str : stringList)
		{
			PotionEffectType effect = PotionEffectType.getByName(str);
			if (effect != null)
				effectList.add(effect);
		}
		return effectList;
	}
	
	/**
	 * returns a PotionEffectTypeList from a list of strings
	 * @param stringList
	 * @return
	 */
	private List<ProjectileProperties> toProperties(List<String> stringList)
	{
		List<ProjectileProperties> projectileList = new ArrayList<ProjectileProperties>();
		
		for (String str : stringList)
		{
			ProjectileProperties projectile = ProjectileProperties.getByName(str);
			if (projectile != null)
				projectileList.add(projectile);
		}
		return projectileList;
	}

}
