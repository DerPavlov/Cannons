package at.pavlov.Cannons;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

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
}
