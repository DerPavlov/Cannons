package at.pavlov.Cannons;

import org.bukkit.Material;

public class Projectile {
	public Material material;
	
	public boolean cannonball;
	public double explosion_power;
	public double player_damage;
	public double penetration;
	public double timefuse;
	public double max_speed;
	
	public boolean canisterShot;
	public double spreadCanisterShot;
	public int amountCanisterShot;

	public boolean placeBlock;
	public double placeBlockRadius;
	public int placeBlockAmount;
	public Material placeBlockMaterial;
	
	public double effectDuration;
	public boolean superBreaker;
	public boolean incendiary;
	public boolean blindness;
	public boolean poison;
	public boolean slowness;
	public boolean slowDigging;
	public boolean weakness;
	public boolean confusion;
	public boolean hunger;
	public boolean teleport;
	
	public Projectile(){
		material = Material.COBBLESTONE;
		
		cannonball = true;
		explosion_power = 4;
		player_damage = 5;
		penetration = 1;
		timefuse = 0;
		max_speed = 5;
		
		canisterShot = false;
		spreadCanisterShot = 5;
		amountCanisterShot = 30;
		
		placeBlock = false;
		placeBlockRadius = 2;
		placeBlockAmount = 10;
		placeBlockMaterial = Material.AIR;

		effectDuration = 5;
		superBreaker = false;
		incendiary = false;
		blindness = false;
		poison = false;
		slowness = false;
		slowDigging = false;
		weakness = false;
		confusion = false;
		hunger = false;
		teleport = false;
	}
}
