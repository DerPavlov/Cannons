package at.pavlov.Cannons;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CannonData {
	 public Location location;
	 public BlockFace face;
	 public int barrel_length;
	 public long LastFired; 
	 public int gunpowder;
	 public Material projectile;
	 public int horizontal_angle;
	 public int vertical_angle;
	 public Player builder;
	 public boolean isValid;
	 public ArrayList<Location> CannonBlocks = new ArrayList<Location>();
	 
	 public CannonData(){
		 CannonBlocks = new ArrayList<Location>();
		 isValid = true;
	 }
	
	 public void addBlock(Location loc){
		 CannonBlocks.add(loc);
	 }
	 
	 public Vector getFiringVector(Config config)
	 {
		 //get projectile
		 Projectile proj = config.getProjectile(projectile);
		 
		//set direction of the snowball
 		Vector vect = new Vector(1f,0f,0f);
 		Random r = new Random();
 		//1.0 for min_barrel_length and gets smaller
 		double shrinkFactor =  Math.exp(- 2.0 * ((double) barrel_length - config.min_barrel_length)/(config.max_barrel_length - config.min_barrel_length));
 		
 		double deviation = r.nextGaussian() * config.angle_deviation * shrinkFactor;
 		if (proj.canisterShot) deviation += r.nextGaussian() * proj.spreadCanisterShot;
 		double horizontal =  Math.sin((horizontal_angle + deviation) * Math.PI/180);
 		
 		deviation = r.nextGaussian() * config.angle_deviation *shrinkFactor;
 		if (proj.canisterShot) deviation += r.nextGaussian() * proj.spreadCanisterShot;
 		double vertical = Math.sin((vertical_angle + deviation)  * Math.PI/180);
 		
 		if (face == BlockFace.WEST) 
 		{
 			 vect = new Vector( -1.0f, vertical, -horizontal);
 		}
 		else if (face == BlockFace.NORTH) 
 		{
 			 vect = new Vector(horizontal , vertical, -1.0f);
 		}
 		else if (face == BlockFace.EAST) 
 		{
 			vect = new Vector( 1.0f, vertical, horizontal);
 		}
 		else if (face == BlockFace.SOUTH) 
 		{
 			 vect = new Vector(-horizontal , vertical,1.0f);
 		}
 		
 		double multi = proj.max_speed  * gunpowder / config.max_gunpowder;
 		if (multi < 0.1) multi=0.1;
 		
 		return vect.multiply(multi);
	 }
}
