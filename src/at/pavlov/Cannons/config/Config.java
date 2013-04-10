package at.pavlov.Cannons.config;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.container.MaterialHolder;
import at.pavlov.Cannons.projectile.Projectile;

/**
 * 
 * @author Peter
 * 
 */



public class Config
{
	public String CannonMaterialName = "WOOL";
	public int CannonMaterialId = 35;
	public int CannonMaterialData = 15;
	public ArrayList<Projectile> allowedProjectiles = new ArrayList<Projectile>();
	// list of all used Materials to build the cannon
	public ArrayList<MaterialHolder> usedMaterial = new ArrayList<MaterialHolder>();
	public boolean Muzzle_flash = true;
	public boolean usePlayerName = true;
	public int Muzzle_displ = 5;
	public int max_gunpowder = 10;
	public boolean gunpowder_depends_on_length = true;
	public double fireDelay = 5;
	public double ignitionDelay = 1;
	public double confusesShooter = 10;
	public boolean inventory_take = false;
	public boolean redstone_consumption = true;

	public boolean redstone_autoload = true;
	public boolean flint_and_steel = false;
	public boolean fireTorch = true;
	public boolean fireButton = false;

	public double max_h_angle = 30.0f;
	public double min_h_angle = -30.0f;
	public double max_v_angle = 30.0f;
	public double min_v_angle = -30.0f;
	public double angle_step = 1.0f;
	public double angle_deviation = 3.0;
	public int max_barrel_length = 10;
	public int min_barrel_length = 2;

	public boolean enableLimits = false;
	public int cannonLimitA = 3;
	public int cannonLimitB = 100;

	public int version_number;

	private UserMessages userMessage;
	private Cannons plugin;
	private DesignStorage designStorage;
	private ProjectileStorage projectileStorage;

	public Config(Cannons plugin)
	{
		this.plugin = plugin;
		allowedProjectiles.add(new Projectile());
		userMessage = new UserMessages(this.plugin);
		designStorage = new DesignStorage(this.plugin);
		projectileStorage = new ProjectileStorage(this.plugin);
	}

	public void loadConfig()
	{
		plugin.reloadConfig();
		projectileStorage.loadProjectiles();
		designStorage.loadCannonDesigns();
		userMessage.loadLanguage();
		
		
		// Load Cannon material
		CannonMaterialName = plugin.getConfig().getString("construction.cannon material name", "cannon material name missing");
		CannonMaterialId = plugin.getConfig().getInt("construction.cannon material ID", 35);
		CannonMaterialData = plugin.getConfig().getInt("construction.cannon material data", 15);

		usedMaterial.add(new MaterialHolder(CannonMaterialId, CannonMaterialData));
		usedMaterial.add(new MaterialHolder(Material.STONE_BUTTON.getId(), -1));
		usedMaterial.add(new MaterialHolder(Material.TORCH.getId(), 5));
		// Load other parameter
		Muzzle_flash = plugin.getConfig().getBoolean("general.muzzleflash", true);
		usePlayerName = plugin.getConfig().getBoolean("general.use player name for damage", false);
		confusesShooter = plugin.getConfig().getDouble("general.confuses shooter", 3.0);
		max_gunpowder = plugin.getConfig().getInt("general.max gunpowder", 5);
		gunpowder_depends_on_length = plugin.getConfig().getBoolean("general.gunpowder varies for length", true);
		fireDelay = plugin.getConfig().getInt("general.barrel cooldown time", 3);
		ignitionDelay = plugin.getConfig().getInt("general.fuse burn time", 3);
		inventory_take = plugin.getConfig().getBoolean("general.player ammunition consumption", true);
		redstone_consumption = plugin.getConfig().getBoolean("general.redstone ammunition consumption", true);

		// fire and reload
		redstone_autoload = plugin.getConfig().getBoolean("fire and reload.redstone autoload", true);
		flint_and_steel = plugin.getConfig().getBoolean("fire and reload.fire torch with flint and steel", false);
		fireTorch = plugin.getConfig().getBoolean("fire and reload.fire with torch", true);
		fireButton = plugin.getConfig().getBoolean("fire and reload.fire with button", true);
		// limits
		enableLimits = plugin.getConfig().getBoolean("amount of built cannons.use limits", true);
		cannonLimitA = plugin.getConfig().getInt("amount of built cannons.build limit A", 1);
		cannonLimitB = plugin.getConfig().getInt("amount of built cannons.build limit B", 10);
		// angles
		max_h_angle = plugin.getConfig().getDouble("angles.max horizontal angle", 30.0);
		min_h_angle = plugin.getConfig().getDouble("angles.min horizontal angle", 30.0);
		max_v_angle = plugin.getConfig().getDouble("angles.max vertical angle", 30.0);
		min_v_angle = plugin.getConfig().getDouble("angles.min vertical angle", 30.0);
		angle_step = plugin.getConfig().getDouble("angles.angle step", 1.0);
		angle_deviation = plugin.getConfig().getDouble("angles.deviation angle", 1.0);
		// length
		max_barrel_length = plugin.getConfig().getInt("construction.max barrel length", 5);
		min_barrel_length = plugin.getConfig().getInt("construction.min barrel length", 2);


		// save defaults
		plugin.getConfig().options().copyHeader(true);
		plugin.getConfig().options().copyDefaults(true);
		plugin.saveConfig();

	}



	

	// ################### getUserMessage ################################
	public UserMessages getUserMessages()
	{
		return userMessage;
	}

	/**
	 * check if the block is the same type as the barrel
	 * @param block
	 * @return
	 */
	public boolean isCannonBarrel(Block block)
	{
		if (block.getTypeId() == CannonMaterialId)
		{
			//negative values mean all data values are allowed
			if (CannonMaterialData < 0 || block.getData() == CannonMaterialData) { return true; }
		}
		return false;
	}


	/**
	 * checks if this block is one of these the cannon is build. Checks all blocks
	 * @param block
	 * @return
	 */
	public boolean isCannonBlock(Block block)
	{
		Iterator<MaterialHolder> iter = usedMaterial.iterator();
		while (iter.hasNext())
		{
			MaterialHolder next = iter.next();
			if (block.getTypeId() == next.getId())
			{
				//negative values mean all data values are allowed
				if (next.getData() < 0 || block.getData() == next.getData()) { return true; }
			}
		}
		return false;
	}

	public DesignStorage getDesignStorage()
	{
		return designStorage;
	}

	public ProjectileStorage getProjectileStorage()
	{
		return projectileStorage;
	}

	public void setProjectileStorage(ProjectileStorage projectileStorage)
	{
		this.projectileStorage = projectileStorage;
	}
	
	

}
