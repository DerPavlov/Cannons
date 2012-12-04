package at.pavlov.Cannons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class UserMessages {
	private FileConfiguration customLanguage = null;
	private File customLanguageFile = null;

	
	public String BarreltoHot;
	
	public String NoProjectile;
	public String NoSulphur;
	public String NoFlintAndSteel;
	public String MaximumGunpowderLoaded;
	public String ProjectileAlreadyLoaded;
	public String FireGun;
	
	public String enableAimingMode;
	public String disableAimingMode;
	
	public String settingCombinedAngle;
	public String settingVerticalAngleUp;
	public String settingVerticalAngleDown;
	public String settingHorizontalAngleRight;
	public String settingHorizontalAngleLeft;
	
	public String loadProjectile;
	public String loadGunpowder;
	public String twoTorches;
	public String tooManyGuns;
	
	public String cannonBuilt;
	public String cannonDestroyed;
	
	public String ErrorPermRestoneTorch;
	public String ErrorPermFire;
	public String ErrorPermLoad;
	public String ErrorPermAdjust;
	public String ErrorPermissionProjectile;
	
	public String HelpText;
	public String HelpBuild;
	public String HelpFire;
	public String HelpAdjust;
	
	
	private Config config;
	private CannonPlugin plugin;
	
	public UserMessages(CannonPlugin plugin, Config config){
		this.config = config;
		this.plugin = plugin;
		loadLanguage("english");
	}
		 
	public boolean loadLanguage(String language){
		if (language.equalsIgnoreCase("english")){
			loadEnglish();
		}			
		else if (language.equalsIgnoreCase("german")){
			loadGerman();
		}
		else if (language.equalsIgnoreCase("custom")){
			loadCustom("CustomLanguage");
		}
		else {
			plugin.logSevere("Language not found. Loading english language.");
			loadEnglish();
			return false;
		}
		return true;
	}
	
	private void loadEnglish(){
		BarreltoHot = ChatColor.RED + "The Barrel is too hot and must cool down";
		NoProjectile = ChatColor.RED + "No Projectile loaded. Load something!";
		NoFlintAndSteel =ChatColor.RED + "You need Flint and Steel to fire";
		NoSulphur = ChatColor.RED + "No gunpowder. Please load the barrel with gunpowder.";
		MaximumGunpowderLoaded = ChatColor.YELLOW + "Maximum gunpowder of " + ChatColor.GOLD + "GUNPOWDER " + ChatColor.YELLOW + "already loaded. More will destroyed the barrel.";
		ProjectileAlreadyLoaded = ChatColor.YELLOW + "Projectile " + ChatColor.GOLD + "PROJECTILE " + ChatColor.YELLOW + "and " + ChatColor.GOLD + "GUNPOWDER " + ChatColor.YELLOW + "gunpowder already loaded. Fire the damn gun before you blow yourself up.";
		FireGun = ChatColor.YELLOW + "FIRE!";
		
		enableAimingMode = ChatColor.GREEN + "Entering aiming mode";
		disableAimingMode = ChatColor.GREEN + "Leaving aiming mode";
		
		settingCombinedAngle = ChatColor.YELLOW + "New angle " + ChatColor.GOLD + "HDEGREE° " + ChatColor.YELLOW + "horizontal and " + ChatColor.GOLD + "VDEGREE° " + ChatColor.YELLOW + "vertical";
		settingVerticalAngleUp = ChatColor.YELLOW + "Setting vertical angle to " + ChatColor.GOLD + "VDEGREE°";
		settingVerticalAngleDown = ChatColor.YELLOW + "Setting vertical angle to " + ChatColor.GOLD + "-VDEGREE°";
		settingHorizontalAngleRight = ChatColor.YELLOW + "Aiming " + ChatColor.GOLD + "HDEGREE° " + ChatColor.YELLOW + "to the " + ChatColor.GOLD + "right";
		settingHorizontalAngleLeft = ChatColor.YELLOW + "Aiming " + ChatColor.GOLD + "HDEGREE° " + ChatColor.YELLOW + "to the " + ChatColor.GOLD + "left";
		
		loadProjectile = ChatColor.YELLOW + "Projectile " + ChatColor.GOLD + "PROJECTILE " + ChatColor.YELLOW + "has been loaded. \nFire if ready.";
		loadGunpowder = ChatColor.GOLD +  "GUNPOWDERkg " + ChatColor.YELLOW + "of gunpowder loaded.";
		
		twoTorches = ChatColor.RED + "You can't place two torches on one cannon";
		tooManyGuns = ChatColor.RED + "You cant build more than " + ChatColor.GOLD + "CANNONS gun(s)";
		
		cannonBuilt = ChatColor.GREEN + "You have created a cannon";
		cannonDestroyed = ChatColor.RED + "One of your cannons has been destroyed";
		
		ErrorPermRestoneTorch = ChatColor.RED + "You are not allowed to control cannons with Redstone";
		ErrorPermFire = ChatColor.RED + "You have no idea how to fire this cannon.";
		ErrorPermLoad = ChatColor.RED + "No permission to load this gun.";
		ErrorPermAdjust = ChatColor.RED + "You have no idea how to adjust this cannon.";
		ErrorPermissionProjectile  = ChatColor.RED + "No permission for this projectile.";
		
		HelpText = "\n Cannons plugin \n " + 
				ChatColor.RED + "/cannons build " + ChatColor.GREEN + " - How to build a cannon" + "\n " +
				ChatColor.RED + "/cannons fire " + ChatColor.GREEN + " - How to load and fire a cannon" + "\n " +
				ChatColor.RED + "/cannons adjust " + ChatColor.GREEN + " - A small guide aim with a cannon";
		HelpBuild = "\n How to build a cannon: \n " +
				"To make the barrel place " + config.min_barrel_length + "-" + config.max_barrel_length + " " + config.Cannon_material + " Blocks in x or z direction.\n " +
				"Place a Button on each end of the barrel \n " +
				"Finish it by placing a torch on the first block of the barrel. \n " +
				"To check if the cannon works right click the torch and you will\nget a message.";
		HelpFire = "\n How to load and fire: \n "+
				"1) Load the barrel with Sulphur by right clicking a few times \n" + "with sulphur. \n " +
				"2) Right click with a projectile block (e.g. " + config.allowedProjectiles.get(0).material + ").\n "+
				"3) When you have done everthing right right click the torch.";
		HelpAdjust = "\n You missed the target? Learn now how to aim: \n "+
				"Right clicking with empty hands will increase the angle. \n "+
				"Shift + right click will decrease the angle. \n "+
				"Clicking on the top of the barrel will change the vertical angle.\n " +
				"Clicking on the side will change the horizontal angle.";
		
	}
	
	private void loadGerman(){
		BarreltoHot = ChatColor.RED + "Das Rohr ist zu heiss";
		NoProjectile = ChatColor.RED + "Kein Kanonkugel im Rohr. Lade einen Block!";
		NoSulphur = ChatColor.RED + "Kein Schwarzpuvler. Lade zuerst die Treibladung!";
		NoFlintAndSteel =ChatColor.RED + "Du brauchst ein Feuerzeug zum Feuern";
		MaximumGunpowderLoaded = ChatColor.YELLOW + "Du hast bereits " + ChatColor.GOLD + "GUNPOWDERkg " + ChatColor.YELLOW + "Schwarzpuvler geladen.";
		ProjectileAlreadyLoaded = ChatColor.YELLOW + "Es wurde bereits eine Kanonenkugel aus " + ChatColor.GOLD + "PROJECTILE " + ChatColor.YELLOW + "und " + ChatColor.GOLD + "GUNPOWDERkg " + ChatColor.YELLOW + "Pulver geladen.  \nFeuer die Kanone endlich ab!";
		FireGun = ChatColor.YELLOW + "FEUER!";
		
		enableAimingMode = ChatColor.GREEN + "Zielmodus aktiviert";
		disableAimingMode = ChatColor.RED + "Zielmodus deaktiviert";
		
		settingCombinedAngle = ChatColor.YELLOW + "Neuer Winkel " + ChatColor.GOLD + "HDEGREE° " + ChatColor.YELLOW + "horizontal and " + ChatColor.GOLD + "VDEGREE° " + ChatColor.YELLOW + "vertikal";
		settingVerticalAngleUp = ChatColor.YELLOW + "Neuer vertikaler Winkel " + ChatColor.GOLD + "VDEGREE°";
		settingVerticalAngleDown = ChatColor.YELLOW + "Neuer vertikaler Winkel " + ChatColor.GOLD + "-VDEGREE°";
		settingHorizontalAngleRight = ChatColor.YELLOW + "Justiere die Kanone " + ChatColor.GOLD + "HDEGREE° " + ChatColor.YELLOW + "nach " + ChatColor.GOLD + "rechts";
		settingHorizontalAngleLeft = ChatColor.YELLOW + "Justiere die Kanone " + ChatColor.GOLD + "HDEGREE° " + ChatColor.YELLOW + "nach " + ChatColor.GOLD + "links";
		
		loadProjectile = ChatColor.YELLOW + "Eine Kanonenkugel aus " + ChatColor.GOLD + "PROJECTILE " + ChatColor.YELLOW + "wurde geladen.";
		loadGunpowder = ChatColor.GOLD + "GUNPOWDERkg " + ChatColor.YELLOW + "Schwarzpuvler geladen";
		twoTorches = ChatColor.RED + "Zwei Fackeln an einer Kanone sind unmöglich.";
		tooManyGuns = ChatColor.RED + "Du darfst nicht mehr als " + ChatColor.GOLD + "CANNONS Kanone(n) " + ChatColor.YELLOW + "bauen.";
		
		cannonBuilt = ChatColor.GREEN + "Kanone wurde fertiggestellt";
		cannonDestroyed = ChatColor.RED + "Eine deiner Kanonen wurde zerstört";
		
		ErrorPermRestoneTorch = ChatColor.RED + "Nicht genügend Rechte um eine Kanone mit Redstone zu steuern.";
		ErrorPermFire = ChatColor.RED + "Du weisst leider nicht wie man eine Kanonen abfeuert.";
		ErrorPermLoad = ChatColor.RED + "Du würdest dich beim Laden der Kanone nur verletzen.";
		ErrorPermAdjust = ChatColor.RED + "Das Zielen mit Kanonen liegt dir nicht im Blut.";
		ErrorPermissionProjectile  = ChatColor.RED + "Keine Rechte für dieses Projektil.";
		
		HelpText = "\n Cannons Plugin " + "\n"  + 
				ChatColor.RED + "/cannons build " + ChatColor.GREEN + " - Wie baue ich eine Kanone" + "\n " +
				ChatColor.RED + "/cannons fire " + ChatColor.GREEN + " - Wie lade und feuere ich?" + "\n " +
				ChatColor.RED + "/cannons adjust " + ChatColor.GREEN + " - Wie kann ich diese Kanone ausrichten?";
		HelpBuild = "\n Bauanleitung für Kanonen: \n " +
				"Für das Rohr platziere " + config.min_barrel_length + "-" + config.max_barrel_length + " " + config.Cannon_material + " Blöcke in x oder z Richtung.\n " +
				"Damit die Kanone erkannt platziere einen Schalter an beiden Enden des Rohrs und eine Fackel auf dem ersten Block. \n " +
				"Wenn du die Fackel rechts anklickst und eine Meldung bekommst\n " + "ist alles richtig gebaut.";
		HelpFire = "\n Wie lade ich eine Kanone? Weiterlesen und mehr erfahren:\n "+
				"1) Wähle Scharzpuvlver aus und mache mehrere Rechtsklicks auf das Rohr.\n" +
				"2) Ein Rechtsklick mit dem Projektil (z.B. " + config.allowedProjectiles.get(0).material + ") um die\n " + "Kanonenkugel zu laden. \n "+
				"3) Hast du alles richtig gemacht kannst du die Kanone mit\n" + "Rechtsklick auf die Fackel abfeuern.";
		HelpAdjust = "\n Ziel verfehlt? Lese und lerne wie man zielt: \n "+
				"Ein Rechtsklick mit einer leeren Hand erhöht den Winkel. \n "+
				"Shift + Rechtsklick auf das Rohr verringert den Winkel.\n "+
				"Klicke oben oder unten um den verticalen Winkel zu ändern. \n "+
				"Die Seiten des Rohrs verändern den horizontalen Winkel.";

	}

	/*
	@SuppressWarnings("unused")
	private void loadFrench(){
		BarreltoHot = "Le canon est trop chaud et doit refroidir";
		NoProjectile ="Le canon n'est pas chargé, veuillez y mettre un projectile !";
		NoSulphur = "Pas de poudre. Mettez en à l'intérieur .";
		NoFlintAndSteel ="You need Flint and Steel to fire";
		MaxiumGunpowderLoaded1 = "Maximum gunpowder of";
		MaxiumGunpowderLoaded2 = "already loaded. More will destroyed the barrel.";
		ProjectileAlreadyLoaded1 = "Projectile";
		ProjectileAlreadyLoaded2 = "gunpowder already loaded. Fire the damn gun before you blow yourself up.";
		//MaxiumGunpowderLoaded = "Maximum de poudre chargée atteind. Encore un peu et le canon explosera.";
		//ProjectileAlreadyLoaded = "Projectile déjà chargé. Veuillez faire feu avant d'exploser";
		FireGun = "FEU !";
		settingVerticalAngle1 = "Angle verticale réglé à";
		settingVerticalAngle2 = "degré(s)";
		settingHorizontalAngle1 = "Angle horizontale régléà ";
		settingHorizontalAngle2 = "degré(s)";
		loadProjectile1 = "Projectile";
		loadProjectile2 = "a été charhé. Faites feu si vous êtes prêt.";
		loadGunpowder = "Kg de pourdre chargé(s).";
		twoTorches = "Vous ne pouvez pas placer deux torches sur un canon !";
		tooManyGuns1 = "You cant build more than";
		tooManyGuns2 = "guns";
		
		cannonBuilt = "You have created a cannon";
		cannonDestroyed = "One of your cannons has been destroyed";
		
		ErrorPermRestoneTorch = "Vous n'avez pas les autorisations pour activer le canon avec de la redstone";
		ErrorPermFire = "Vous ne savez tirer avec un canon.";
		ErrorPermLoad = "Vous n'avez pas les autorisations pour charger un canon.";
		ErrorPermAdjust = "Vous ne savez pas ajuster l'angle de tir sur un canon.";
		
		HelpText = "\n Cannons plugin " + "\n " + 
				ChatColor.RED + "/cannons build " + ChatColor.GREEN + " - Comment frabriquer un canon" + "\n " +
				ChatColor.RED + "/cannons fire " + ChatColor.GREEN + " - Comment charger un canon" + "\n " +
				ChatColor.RED + "/cannons adjust " + ChatColor.GREEN + " - un petit guide avec le Plugins Cannons";
		HelpBuild = "\n Comment fabriquer un canon: \n " +
				"Pour faire un canon, placer " + config.min_barrel_length + "-" + config.max_barrel_length + " blocs de " + config.Cannon_material + " dans les directions X ou Z.\n" +
				"Placer un Bouton à chaque extrémité du canon \n " +
				"Finir en plaçant une torche sur le premier bloc du canon. \n " +
				"Pour vérifier si le canon fonctionne un clic droit sur la torche et un méssage vous l'indiquera. ";
		HelpFire = "\n Comment charger et faire feu: \n "+
				"1) Charger le canon avec en faisant un clique droit \n " + "avec la poudre en main. \n " +
				"2) Clic droit avec un block de (e.g. " + config.allowedProjectiles.get(0).material + ") pour charger un projectile.\n "+
				"3) Une fois fait, clique droit à la main sur la toche.";
		HelpAdjust = "\n Vous avez raté la cible? Apprenez à viser: \n "+
				"Clic droit avec les mains vides va augmenter l'angle. \n "+
				"Sneak + clic droit avec la main sur le canon augmente l'angle. \n "+
				"En cliquant sur le dessus du canon modifie l'angle verticale.\n " +
				"En cliquant sur le côté du canon modifie l'angle horizontale.\n ";

	}
	
	@SuppressWarnings("unused")
	private void loadPtBr(){
		BarreltoHot = "O barril esta muito quente, espere resfriar.";
		NoProjectile= "Sem municao. Recarregue!";
		NoSulphur= "Sem polvora. Coloque polvora no canhao";
		NoFlintAndSteel= "Voce precisa de 'Flint and Steel' para atirar";
		MaxiumGunpowderLoaded1 = "Maximum gunpowder of";
		MaxiumGunpowderLoaded2 = "already loaded. More will destroyed the barrel.";
		ProjectileAlreadyLoaded1 = "Projectile";
		ProjectileAlreadyLoaded2 = "gunpowder already loaded. Fire the damn gun before you blow yourself up.";
		//MaxiumGunpowderLoaded= "Ha muita polvora. Se houver mais o barril estourara.";
		//ProjectileAlreadyLoaded= "Projetil ja carregado. Descarregue o canhao.";
		FireGun= "FOGO!";
		settingVerticalAngle1= "Ajustando o angulo vertical para";
		settingVerticalAngle2= "grau(s)";
		settingHorizontalAngle1= "Ajustando o angulo horizontal para";
		settingHorizontalAngle2= "grau(s)";
		loadProjectile1= "Projetil";
		loadProjectile2= "foi carregado. Atire o canhao.";
		loadGunpowder= "kg de Polvora carregado(s).";
		twoTorches= "Voce nao pode colocar duas tochas em um unico canhao.";
		tooManyGuns1= "Voce nao pode criar mais que";
		tooManyGuns2= "Canhoes";
		cannonBuilt= "Voce criou um canhao.";
		cannonDestroyed= "Um de seus canhoes foi destruido.";
		ErrorPermRestoneTorch= "Voce nao esta permitido para usar 'Redstone' nos canhoes.";
		ErrorPermFire= "Voce nao pode atirar.";
		ErrorPermLoad= "Voce nao pode recarregar o canhao.";
		ErrorPermAdjust= "Voce nao pode ajustar o angulo.";
		HelpText = "\n Cannons plugin \n " + 
				ChatColor.RED + "/cannons build " + ChatColor.GREEN + " - How to build a cannon" + "\n " +
				ChatColor.RED + "/cannons fire " + ChatColor.GREEN + " - How to load and fire a cannon" + "\n " +
				ChatColor.RED + "/cannons adjust " + ChatColor.GREEN + " - A small guide aim with a cannon";
		HelpBuild = "\n How to build a cannon: \n " +
				"To make the barrel place " + config.min_barrel_length + "-" + config.max_barrel_length + " " + config.Cannon_material + " Blocks in x or z direction.\n " +
				"Place a Button on each end of the barrel \n " +
				"Finish it by placing a torch on the first block of the barrel. \n " +
				"To check if the cannon works right click the torch and you will\nget a message.";
		HelpFire = "\n How to load and fire: \n "+
				"1) Load the barrel with Sulphur by right clicking a few times \n" + "with sulphur. \n " +
				"2) Right click with a projectile block (e.g. " + config.allowedProjectiles.get(0).material + ").\n "+
				"3) When you have done everthing right right click the torch.";
		HelpAdjust = "\n You missed the target? Learn now how to aim: \n "+
				"Right clicking with empty hands will increase the angle. \n "+
				"Shift + right click will decrease the angle. \n "+
				"Clicking on the top of the barrel will change the vertical angle.\n " +
				"Clicking on the side will change the horizontal angle.";
		}
	 */
	
	
	private void loadCustom(String filename)
	{
		reloadcustomLanguage(filename);
		customLanguage.options().copyDefaults(true);
		savecustomLanguage();
		
		BarreltoHot = getEntry("BarreltoHot");
		NoProjectile = getEntry("NoProjectile");
		NoSulphur = getEntry("NoSulphur");
		NoFlintAndSteel = getEntry("NoFlintAndSteel");
		MaximumGunpowderLoaded = getEntry("MaximumGunpowderLoaded");
		ProjectileAlreadyLoaded = getEntry("ProjectileAlreadyLoaded");
		FireGun = getEntry("FireGun");
		
		enableAimingMode = getEntry("enableAimingMode");
		disableAimingMode = getEntry("disableAimingMode");
		
		settingCombinedAngle = getEntry("settingCombinedAngle");
		settingVerticalAngleUp = getEntry("settingVerticalAngleUp");
		settingVerticalAngleDown = getEntry("settingVerticalAngleDown");
		settingHorizontalAngleRight = getEntry("settingHorizontalAngleRight");
		settingHorizontalAngleLeft = getEntry("settingHorizontalAngleLeft");
		
		loadProjectile = getEntry("loadProjectile");
		loadGunpowder = getEntry("loadGunpowder");
		twoTorches = getEntry("twoTorches");
		tooManyGuns = getEntry("tooManyGuns");
		cannonBuilt = getEntry("cannonBuilt");
		cannonDestroyed = getEntry("cannonDestroyed");
		
		ErrorPermRestoneTorch = getEntry("ErrorPermRestoneTorch");
		ErrorPermFire = getEntry("ErrorPermFire");
		ErrorPermLoad = getEntry("ErrorPermLoad");
		ErrorPermAdjust = getEntry("ErrorPermAdjust");
		
		HelpText = getEntry("HelpText");
		HelpBuild = getEntry("HelpBuild");
		HelpFire = getEntry("HelpFire");
		HelpAdjust = getEntry("HelpAdjust");	
	}
		
	public String getEntry(String key)
	{
		String entry = customLanguage.getString(key);
		//replace red color
		String replace = "" + ChatColor.RED;
		entry = splitString(entry, "ChatColor.RED ", replace);
		entry = splitString(entry, "RED ", replace);
		//replace green color
		replace = "" + ChatColor.GREEN;
		entry = splitString(entry, "ChatColor.GREEN ", replace);
		entry = splitString(entry, "GREEN ", replace);
		//replace yellow color
		replace = "" + ChatColor.YELLOW;
		entry = splitString(entry, "ChatColor.YELLOW ", replace);
		entry = splitString(entry, "YELLOW ", replace);
		//replace gold color
		replace = "" + ChatColor.GOLD;
		entry = splitString(entry, "ChatColor.GOLD ", replace);
		entry = splitString(entry, "GOLD ", replace);
		
		//replace Projectile expample
		replace = "" + config.allowedProjectiles.get(0).material;
		entry = splitString(entry, "ALLOWED_PROJECTILE", replace);
		//replace min barrel length
		replace = "" + config.min_barrel_length;
		entry = splitString(entry, "MIN_BARREL_LENGTH", replace);
		//replace max barrel length
		replace = "" + config.max_barrel_length;
		entry = splitString(entry, "MAX_BARREL_LENGTH", replace);
		//replace cannon material
		replace = "" + config.Cannon_material;
		entry = splitString(entry, "CANNON_MATERIAL", replace);
		//replace new line
		replace = "\n ";
		entry = splitString(entry, "NEWLINE " , replace);
		return entry;
	}


	public String splitString(String entry, String split, String replace)
	{
		if (entry == null) 
		{
			plugin.logSevere("Wrong entry in the lanugagefile");
			return null;
		}
		
		String tempEntry[] = entry.split(split);
		if (tempEntry.length > 0)
		{
			for(int x=1 ; x < tempEntry.length ; x++) 
			{
				tempEntry[0] += replace + tempEntry[x];
			}
			return tempEntry[0];
		}
		return null;
	}


	private void reloadcustomLanguage(String filename) 
	{
	    if (customLanguageFile == null) 
	    {
	    	customLanguageFile = new File(getDataFolder(), filename + ".yml");
	    }
	    customLanguage = YamlConfiguration.loadConfiguration(customLanguageFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = plugin.getResource(filename + ".yml");
	    if (defConfigStream != null) 
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customLanguage.setDefaults(defConfig);
	    }
	}


	private String getDataFolder() 
	{
		return "plugins/Cannons/";
	}


	private void savecustomLanguage()
	{
	    if (customLanguage == null || customLanguageFile == null) 
	    {
	    return;
	    }
	    try 
	    {
	        customLanguage.save(customLanguageFile);
	    } catch (IOException ex) 
	    {
	        plugin.logSevere("Could not save config to " + customLanguageFile);
	    }
	}
	
	
	//############# getMaximumGunpowderLoaded ##########################################
	public String getMaximumGunpowderLoaded(int gunpowder)
	{		
		return splitString(MaximumGunpowderLoaded, "GUNPOWDER", Integer.toString(gunpowder));
	}
	
	//############# getProjectileAlreadyLoaded ##########################################
	public String getProjectileAlreadyLoaded(int gunpowder, Material Projectile)
	{		
		String string = splitString(ProjectileAlreadyLoaded, "GUNPOWDER", Integer.toString(gunpowder));
		return splitString(string, "PROJECTILE", Projectile.toString());
	}
	
	//############# getSettingCombinedAngle ##########################################
	public String getSettingCombinedAngle(int hAngle, int vAngle)
	{		
		String string = splitString(settingCombinedAngle, "HDEGREE", Integer.toString(hAngle));
		return splitString(string, "VDEGREE", Integer.toString(vAngle));
	}
	
	
	//############# getSettingVerticalAngle ##########################################
	public String getSettingVerticalAngle(int angle)
	{		
		if (angle >= 0)
		{
			//UP
			return splitString(settingVerticalAngleUp, "VDEGREE", Integer.toString(angle));
		}
		else
		{
			//DOWN
			return splitString(settingVerticalAngleDown, "VDEGREE", Integer.toString(-angle));
		}
	}
	
	//############# getSettingHorizontalAngle ##########################################
	public String getSettingHorizontalAngle(int angle)
	{		
		if (angle >= 0)
		{
			//Right
			return splitString(settingHorizontalAngleRight, "HDEGREE", Integer.toString(angle));
		}
		else
		{
			//Left
			return splitString(settingHorizontalAngleLeft, "HDEGREE", Integer.toString(-angle));
		}
	}
	
	//############# getloadProjectile ##########################################
	public String getloadProjectile(Material Projectile)
	{		
		return splitString(loadProjectile, "PROJECTILE", Projectile.toString());
	}
	
	//############# getloadGunpowder ##########################################
	public String getloadGunpowder(int gunpowder)
	{		
		return splitString(loadGunpowder, "GUNPOWDER", Integer.toString(gunpowder));
	}
	
	//############# getTooManyGuns ##########################################
	public String getTooManyGuns(int cannons)
	{		
		return splitString(tooManyGuns, "CANNONS", Integer.toString(cannons));
	}
}
