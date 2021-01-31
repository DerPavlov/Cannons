package at.pavlov.cannons.projectile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.pavlov.cannons.container.SoundHolder;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.utils.CannonsUtil;

public class ProjectileStorage
{
	private final Cannons plugin;


	private static List<Projectile> projectileList;

	public ProjectileStorage(Cannons plugin)
	{
		this.plugin = plugin;
		projectileList = new ArrayList<Projectile>();
	}

	/**
	 * returns a list of all projectiles names
	 * @return list of all projectiles names
	 */
	public static ArrayList<String> getProjectileIds(){
		ArrayList<String> list = new ArrayList<String>();
		for (Projectile proj : projectileList){
			list.add(proj.getProjectileId());
		}
		return list;
	}

	/**
	 * returns the projectile that can be loaded with this item. If data=-1 the data is ignored
	 * @param item ItemStack of projectile
	 * @return true if there is a projectile with this material
	 */
	public static Projectile getProjectile(Cannon cannon, ItemStack item)
	{
		ItemHolder materialHolder = new ItemHolder(item);
		return getProjectile(cannon, materialHolder);
	}

	/**
	 * returns the projectiles that can be loaded in the cannon with this id and data. If data=-1 the data is ignored
	 * @param materialHolder material of the projectile
	 * @return true if there is a projectile with this material
	 */
	public static Projectile getProjectile(Cannon cannon, ItemHolder materialHolder)
	{
		for (Projectile projectile : projectileList)
		{
			if (cannon.getCannonDesign().canLoad(projectile) && !projectile.getLoadingItem().equals(Material.AIR) && projectile.equals(materialHolder))
				return projectile;
		}
		return null;
	}


	/**
	 * returns the projectiles that can be loaded in the cannon by projectile id (file name)
	 * @param projectileId id of the projectile
	 * @return true if there is a projectile with this id
	 */
	public static Projectile getProjectile(Cannon cannon, String projectileId)
	{
		for (Projectile projectile : projectileList)
		{
			if (cannon.getCannonDesign().canLoad(projectile) && projectile.equals(projectileId))
				return projectile;
		}
		return null;
	}

	/**
	 * returns the projectiles for the given id (file name)
	 * @param projectileId id of the projectile
	 * @return true if there is a projectile with this id
	 */
	public static Projectile getProjectile(String projectileId)
	{
		for (Projectile projectile : projectileList)
		{
			if (projectile.equals(projectileId))
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
		projectileList.clear();

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
			if (projectile != null) {
				plugin.logDebug("load projectile " + file + " item " + projectile.getLoadingItem().toString());
				projectileList.add(projectile);
			}
		}
	}

	/**
	 * get all projectile file names form /projectiles
	 * @return list of all projectiles files
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
			if (listOfFiles == null)
			{
				plugin.logSevere("Projectile folder empty");
				return projectileList;
			}

			for (File listOfFile : listOfFiles) {
				if (listOfFile.isFile()) {
					ymlFile = listOfFile.getName();
					if (ymlFile.endsWith(".yml") || ymlFile.endsWith(".yaml")) {
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

		File projectileFile = new File(getPath() + ymlFile);
		FileConfiguration projectileConfig = YamlConfiguration.loadConfiguration(projectileFile);

		//load it from the disk

		//general
		projectile.setProjectileName(projectileConfig.getString("general.projectileName", "noProjectileName"));
		projectile.setDescription(projectileConfig.getString("general.description", "no description for this projectile"));
		projectile.setItemName(projectileConfig.getString("general.itemName", "noItemName"));
		projectile.setLoadingItem(new ItemHolder(projectileConfig.getString("general.loadingItem", "minecraft:cobblestone")));
		projectile.setAlternativeItemList(CannonsUtil.toItemHolderList(projectileConfig.getStringList("general.alternativeId")));

		//cannonball
		projectile.setProjectileEntity(getProjectileEntity(projectileConfig.getString("cannonball.entityType", "SNOWBALL")));
		projectile.setProjectileOnFire(projectileConfig.getBoolean("cannonball.isOnFire", false));
		projectile.setVelocity(projectileConfig.getDouble("cannonball.velocity", 1.0));
		projectile.setPenetration(projectileConfig.getDouble("cannonball.penetration", 0.0));
		projectile.setPenetrationDamage(projectileConfig.getBoolean("cannonball.doesPenetrationDamage", true));
		projectile.setTimefuse(projectileConfig.getDouble("cannonball.timefuse", 0.0));
		projectile.setAutomaticFiringDelay(projectileConfig.getDouble("cannonball.automaticFiringDelay", 1.0));
		projectile.setAutomaticFiringMagazineSize(projectileConfig.getInt("cannonball.automaticFiringMagazineSize", 1));
		projectile.setNumberOfBullets(projectileConfig.getInt("cannonball.numberOfBullets", 1));
		projectile.setSpreadMultiplier(projectileConfig.getDouble("cannonball.spreadMultiplier", 1.0));
		projectile.setPropertyList(toProperties(projectileConfig.getStringList("cannonball.properties")));

		//smokeTrail
		projectile.setSmokeTrailEnabled(projectileConfig.getBoolean("smokeTrail.enabled", false));
		projectile.setSmokeTrailDistance(projectileConfig.getInt("smokeTrail.distance", 10));
		projectile.setSmokeTrailMaterial(Bukkit.createBlockData(projectileConfig.getString("smokeTrail.material", "minecraft:cobweb")));
		projectile.setSmokeTrailDuration(projectileConfig.getDouble("smokeTrail.duration", 20.0));
		projectile.setSmokeTrailParticleEnabled(projectileConfig.getBoolean("smokeTrail.particles.enabled", false));
		projectile.setSmokeTrailParticleType(Particle.valueOf(projectileConfig.getString("smokeTrail.particles.type", "CAMPFIRE_SIGNAL_SMOKE")));
		projectile.setSmokeTrailParticleCount(projectileConfig.getInt("smokeTrail.particles.count", 1));
		projectile.setSmokeTrailParticleOffsetX(projectileConfig.getDouble("smokeTrail.particles.x_offset", 0.0));
		projectile.setSmokeTrailParticleOffsetY(projectileConfig.getDouble("smokeTrail.particles.y_offset", 0.0));
		projectile.setSmokeTrailParticleOffsetZ(projectileConfig.getDouble("smokeTrail.particles.z_offset", 0.0));
		projectile.setSmokeTrailParticleSpeed(projectileConfig.getDouble("smokeTrail.particles.speed", 0.0));

		//explosion
		projectile.setExplosionPower((float) projectileConfig.getDouble("explosion.explosionPower", 2.));
		projectile.setExplosionPowerDependsOnVelocity(projectileConfig.getBoolean("explosion.explosionPowerDependsOnVelocity", true));
		projectile.setExplosionDamage(projectileConfig.getBoolean("explosion.doesExplosionDamage", true));
		projectile.setUnderwaterDamage(projectileConfig.getBoolean("explosion.doesUnderwaterExplosion", false));
		projectile.setDirectHitDamage(projectileConfig.getDouble("explosion.directHitDamage", 5.0));
		projectile.setPlayerDamageRange(projectileConfig.getDouble("explosion.playerDamageRange", 3.0));
		projectile.setPlayerDamage(projectileConfig.getDouble("explosion.playerDamage", 5.0));
		projectile.setPotionRange(projectileConfig.getDouble("explosion.potionRange", 1.0));
		projectile.setPotionDuration(projectileConfig.getDouble("explosion.potionDuration", 1.0));
		projectile.setPotionAmplifier(projectileConfig.getInt("explosion.potionAmplifier", 0));
		projectile.setPotionsEffectList(toPotionEffect(projectileConfig.getStringList("explosion.potionEffects")));
		projectile.setImpactIndicator(projectileConfig.getBoolean("explosion.impactIndicator", true));

		//cluster
		projectile.setClusterExplosionsEnabled(projectileConfig.getBoolean("clusterExplosion.enabled", false));
		projectile.setClusterExplosionsInBlocks(projectileConfig.getBoolean("clusterExplosion.explosionInBlocks", false));
		projectile.setClusterExplosionsAmount(projectileConfig.getInt("clusterExplosion.amount", 5));
		projectile.setClusterExplosionsMinDelay(projectileConfig.getDouble("clusterExplosion.minDelay", 0.0));
		projectile.setClusterExplosionsMaxDelay(projectileConfig.getDouble("clusterExplosion.maxDelay", 5.0));
		projectile.setClusterExplosionsRadius(projectileConfig.getDouble("clusterExplosion.radius", 5.0));
		projectile.setClusterExplosionsPower(projectileConfig.getDouble("clusterExplosion.power", 5.0));

		//spawnOnExplosion
		projectile.setSpawnEnabled(projectileConfig.getBoolean("spawnOnExplosion.enabled", false));
		projectile.setSpawnBlockRadius(projectileConfig.getDouble("spawnOnExplosion.blockRadius", 1.0));
		projectile.setSpawnEntityRadius(projectileConfig.getDouble("spawnOnExplosion.entityRadius", 2.0));
		projectile.setSpawnVelocity(projectileConfig.getDouble("spawnOnExplosion.velocity", 0.1));
		projectile.setSpawnBlocks(CannonsUtil.toSpawnMaterialHolderList(projectileConfig.getStringList("spawnOnExplosion.block")));
		projectile.setSpawnEntities(CannonsUtil.toSpawnEntityHolderList(projectileConfig.getStringList("spawnOnExplosion.entity")));
		projectile.setSpawnProjectiles(projectileConfig.getStringList("spawnOnExplosion.projectiles"));

		//spawnFireworks
		projectile.setFireworksEnabled(projectileConfig.getBoolean("spawnFireworks.enabled", false));
		projectile.setFireworksFlicker(projectileConfig.getBoolean("spawnFireworks.flicker",false));
		projectile.setFireworksTrail(projectileConfig.getBoolean("spawnFireworks.trail",false));
		projectile.setFireworksType(getFireworksType(projectileConfig.getString("spawnFireworks.type", "BALL")));
		projectile.setFireworksColors(toColor(projectileConfig.getStringList("spawnFireworks.colors")));
		projectile.setFireworksFadeColors(toColor(projectileConfig.getStringList("spawnFireworks.fadeColors")));

		//messages
		projectile.setImpactMessage(projectileConfig.getBoolean("messages.hasImpactMessage", false));

		//sounds
		projectile.setSoundLoading(new SoundHolder(projectileConfig.getString("sounds.loading", "BLOCK_STONE_PLACE:5:0.5")));
		projectile.setSoundImpact(new SoundHolder(projectileConfig.getString("sounds.impact", "ENTITY_GENERIC_EXPLODE:10:0.5")));
		projectile.setSoundImpactProtected(new SoundHolder(projectileConfig.getString("sounds.impactProtected", "ENTITY_GENERIC_EXPLODE:10:0.5")));
		projectile.setSoundImpactWater(new SoundHolder(projectileConfig.getString("sounds.impactWater", "ENTITY_GENERIC_SPLASH:10:0.3")));

		//loadPermissions
		projectile.setPermissionLoad(projectileConfig.getStringList("loadPermission"));

		return projectile;
	}


	/**
	 * copys the default designs from the .jar to the disk
	 */
	private void copyDefaultProjectiles()
	{
		copyFile("canistershot");
		copyFile("tnt");
		copyFile("cobblestone");
		copyFile("diamond");

		copyFile("enderpearl");

		copyFile("firework1");
		copyFile("firework2");
		copyFile("firework3");
		copyFile("firework4");
	}

	/**
	 * copies the yml file from the .jar to the projectile folder
	 * @param filename - name of the .yml file
	 */
	private void copyFile(String filename)
	{
		File YmlFile = new File(plugin.getDataFolder(), "projectiles/" + filename + ".yml");

		YmlFile.getParentFile().mkdirs();
		if (!YmlFile.exists())
		{
			CannonsUtil.copyFile(plugin.getResource("projectiles/" + filename + ".yml"), YmlFile);
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
			else
				plugin.logSevere("No potion effect found with the name: " + str);
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
			else
				plugin.logSevere("No projectile property with the name: " + str + " found");
		}
		return projectileList;
	}



	/**
	 * returns a list of colors in RGB integer format from a list of strings in hex format
	 * @param stringList
	 * @return
	 */
	private List<Integer> toColor(List<String> stringList)
	{
		List<Integer> colorList = new ArrayList<Integer>();

		for (String str : stringList)
		{
			try
			{
				Integer color = Integer.parseInt(str,16);
				colorList.add(color);
			}
			catch (Exception ex)
			{
				plugin.logSevere(str + " is not a hexadecimal number");
			}
		}
		return colorList;
	}

	/**
	 * returns the projectile with the matching ID
	 * @param str
	 * @return
	 */
	public Projectile getByName(String str)
	{
		for (Projectile projectile : projectileList)
		{
			if (projectile.getProjectileId().equalsIgnoreCase(str))
				return projectile;
		}
		return null;
	}

	/**
	 * converts a string into a firework effect
	 * @param str - name of the effect
	 * @return fittiong firework effect
	 */
	FireworkEffect.Type getFireworksType(String str)
	{
		try
		{
			return FireworkEffect.Type.valueOf(str);
		}
		catch(Exception ex)
		{
			plugin.logDebug(str + " is not a valid fireworks type. BALL was used instead.");
			return FireworkEffect.Type.BALL;
		}
	}

	/**
	 * returns converts a string into a firework effect
	 * @param str - name of the effect
	 * @return fittiong firework effect
	 */
	EntityType getProjectileEntity(String str)
	{
		try
		{
			return EntityType.valueOf(str.toUpperCase());
		}
		catch(Exception ex)
		{
			plugin.logSevere(str + " is not a valid entity type. SNOWBALL was used instead.");
			return EntityType.SNOWBALL;
		}
	}

}
