package at.pavlov.Cannons;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;

public class Config 
{
	 public Material Cannon_material = Material.WOOL;
	 public ArrayList<Projectile> allowedProjectiles = new ArrayList<Projectile>();
	 //list of all used Materials to build the cannon
	 public ArrayList<Material> usedMaterial =  new ArrayList<Material>();
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
	 public double min_h_angle= -30.0f;
	 public double max_v_angle = 30.0f;
	 public double min_v_angle= -30.0f;
	 public double angle_step = 1.0f;
	 public double angle_deviation = 3.0;
	 public int max_barrel_length = 10;
	 public int min_barrel_length = 2;
	 
	 public boolean enableLimits = false;
	 public int cannonLimitA = 3;
	 public int cannonLimitB = 100;
	 
	 public boolean forceTNTexplosion = true;
	 
	 public int version_number;
	 
	 private UserMessages userMessage;
	 private CannonPlugin plugin;
	 
	 public Config(CannonPlugin plugin)
	 {
		 this.plugin = plugin;
		 allowedProjectiles.add(new Projectile());
		 userMessage = new UserMessages(plugin,this);
	 }

	 
	 public void loadConfig()
	 {
		 plugin.reloadConfig();
		 
		 loadProjectiles(plugin);
		 //Load Cannon material
		 Cannon_material = getMaterial(plugin.getConfig().getString("construction.cannon material"));
		 usedMaterial.add(Cannon_material);
		 usedMaterial.add(Material.STONE_BUTTON);
		 usedMaterial.add(Material.TORCH);
		 //Load other parameter
		 Muzzle_flash = plugin.getConfig().getBoolean("general.muzzleflash");
		 usePlayerName = plugin.getConfig().getBoolean("general.use player name for damage");
		 confusesShooter = plugin.getConfig().getDouble("general.confuses shooter");
		 max_gunpowder =  plugin.getConfig().getInt("general.max gunpowder");
		 gunpowder_depends_on_length =  plugin.getConfig().getBoolean("general.gunpowder varies for length");
		 fireDelay = plugin.getConfig().getInt("general.barrel cooldown time");
		 ignitionDelay = plugin.getConfig().getInt("general.fuse burn time");
		 inventory_take = plugin.getConfig().getBoolean("general.player ammunition consumption");
		 redstone_consumption = plugin.getConfig().getBoolean("general.redstone ammunition consumption");
		 //fire and reload
		 redstone_autoload = plugin.getConfig().getBoolean("fire and reload.redstone autoload");
		 flint_and_steel = plugin.getConfig().getBoolean("fire and reload.fire torch with flint and steel");
		 fireTorch = plugin.getConfig().getBoolean("fire and reload.fire with torch");
		 fireButton = plugin.getConfig().getBoolean("fire and reload.fire with button");
		 //limits
		 enableLimits = plugin.getConfig().getBoolean("amount of built cannons.use limits");
		 cannonLimitA = plugin.getConfig().getInt("amount of built cannons.build limit A");
		 cannonLimitB = plugin.getConfig().getInt("amount of built cannons.build limit B");
		 //angles
		 max_h_angle =  plugin.getConfig().getDouble("angles.max horizontal angle");
		 min_h_angle =  plugin.getConfig().getDouble("angles.min horizontal angle");
		 max_v_angle =  plugin.getConfig().getDouble("angles.max vertical angle");
		 min_v_angle =  plugin.getConfig().getDouble("angles.min vertical angle");
		 angle_step =  plugin.getConfig().getDouble("angles.angle step");
		 angle_deviation =  plugin.getConfig().getDouble("angles.deviation angle");
		 //length
		 max_barrel_length =  plugin.getConfig().getInt("construction.max barrel length");
		 min_barrel_length =  plugin.getConfig().getInt("construction.min barrel length");
		 //protection plugin
		 forceTNTexplosion = plugin.getConfig().getBoolean("protectionplugin.force tnt event");
		
		 //load language
		 if (userMessage.loadLanguage(plugin.getConfig().getString("language"))== false)
		 {
			 plugin.logSevere("Can't load language. Check spelling.");
		 }

		 //save defaults
		 plugin.getConfig().options().copyHeader(true);
		 plugin.getConfig().options().copyDefaults(true);
		 plugin.saveConfig();

	 }
	 
	 private void loadProjectiles(CannonPlugin plugin)
	 {
		 //Load Projectiles
		 allowedProjectiles.clear();
		 List<String> list_material = plugin.getConfig().getStringList("projectiles");
		 if (list_material != null)
		 {
	         Iterator<String> iter = list_material.iterator();
	         while (iter.hasNext()) 
	         {	      
	        	 String next = iter.next();
	        	 String nextMaterial = getMaterial(next).toString();
	        	 if (nextMaterial != "AIR")
	        	 { 
	        		 allowedProjectiles.add(loadProjectileData(plugin, next));
	        	 }
	        	 else 
	    		 {
	    			 plugin.logSevere("Wrong name in list");
	    		 }
	         }
		 }
		 else 
		 {
			 plugin.logSevere("No Projectile found.");
		 }
	 }
	 
	 private Projectile loadProjectileData(CannonPlugin plugin, String next)
	 {
		 // get data of projectile
		 Projectile projectile = new Projectile();
		
		 projectile.material = getMaterial(next);

		 projectile.max_speed =  plugin.getConfig().getDouble(next + "." + "max speed");
		 projectile.player_damage =  plugin.getConfig().getDouble(next + "." + "player damage");
		 //cannonball
		 projectile.cannonball =  plugin.getConfig().getBoolean(next + "." + "cannonball.cannonball");
		 projectile.explosion_power =  plugin.getConfig().getDouble(next + "." + "cannonball.explosion power") ;
		 projectile.penetration =  plugin.getConfig().getDouble(next + "." + "cannonball.penetration");
		 projectile.timefuse =  plugin.getConfig().getDouble(next + "." + "cannonball.timefuse");
		 //canister shot
		 projectile.canisterShot =  plugin.getConfig().getBoolean(next + "." + "canistershot.canister shot");
		 projectile.spreadCanisterShot =  plugin.getConfig().getDouble(next + "." + "canistershot.spread");
		 projectile.amountCanisterShot =  plugin.getConfig().getInt(next + "." + "canistershot.amount of bullets");
		 //placeBlock
		 projectile.placeBlock =  plugin.getConfig().getBoolean(next + "." + "placeBlock.enabled");
		 projectile.placeBlockRadius =  plugin.getConfig().getDouble(next + "." + "placeBlock.radius");
		 projectile.placeBlockAmount =  plugin.getConfig().getInt(next + "." + "placeBlock.amount");
		 projectile.placeBlockMaterial = getMaterial(plugin.getConfig().getString(next + "." + "placeBlock.material"));
		 //effects
		 projectile.effectDuration =  plugin.getConfig().getDouble(next + "." + "effects.effect duration");
		 projectile.superBreaker =  plugin.getConfig().getBoolean(next + "." + "effects.super breaker");
		 projectile.incendiary =  plugin.getConfig().getBoolean(next + "." + "effects.incendiary");
		 projectile.blindness =  plugin.getConfig().getBoolean(next + "." + "effects.blindness");
		 projectile.poison =  plugin.getConfig().getBoolean(next + "." + "effects.poison");
		 projectile.slowness =  plugin.getConfig().getBoolean(next + "." + "effects.slowness");
		 projectile.slowDigging =  plugin.getConfig().getBoolean(next + "." + "effects.slowDigging");
		 projectile.weakness =  plugin.getConfig().getBoolean(next + "." + "effects.weakness");
		 projectile.confusion =  plugin.getConfig().getBoolean(next + "." + "effects.confusion");
		 projectile.hunger =  plugin.getConfig().getBoolean(next + "." + "effects.hunger");
		 projectile.teleport =  plugin.getConfig().getBoolean(next + "." + "effects.teleport to impact");
		 
		 if ((projectile.cannonball == false && projectile.canisterShot == false) || projectile.max_speed <= 0.1)
		 {
			plugin.logSevere("No proporties of " + projectile.material + " found. Check if both names are written in the same way.");
		 }
		 
		 return projectile;
	 }
	 
	 //################# getMaterial ############################
	 private Material getMaterial(String input)
	 {
		 if (input == null) return Material.AIR;
				 
		 Material material = null;
		 try 
	     {
			 material = Material.getMaterial(Integer.parseInt(input));
	     } 
	     catch (NumberFormatException ex) 
	     {
	    	 material = Material.getMaterial(input.toUpperCase());
	     }
		 //replace invalid entries
		 if (material == null)
		 {	 plugin.logSevere("Invalid entry " + input);
			 material = Material.AIR;
		 }
		 return material;
	 }
	 
	 
	 //################# getProjectile ############################
	 public Projectile getProjectile(Material material)
	 {
		 Iterator<Projectile> iter = allowedProjectiles.iterator();
		 while (iter.hasNext())
		 {
			 Projectile next =  iter.next();
			 if (next.material ==  material)
			 {
				 return next;
			 }
		 }
		 return null;
	 }
	 
	 //################### getUserMessage ################################
	 public UserMessages getUserMessages()
	 {
		return userMessage;
	 }
	 
}
