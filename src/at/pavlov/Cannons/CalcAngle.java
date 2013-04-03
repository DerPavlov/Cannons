package at.pavlov.Cannons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.cannon.CannonData;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.UserMessages;


public class CalcAngle {
	
	Cannons plugin;
	UserMessages userMessages;
	Config config;
	
	HashMap<Player, CannonData> inAimingMode = new HashMap<Player, CannonData>();
	
	private class gunAngles
	{
		private double horizontal;
		private double vertical;
		
		public gunAngles(double horizontal, double vertical)
		{
			this.setHorizontal(horizontal);
			this.setVertical(vertical);
		}

		public double getHorizontal() {
			return horizontal;
		}

		public void setHorizontal(double horizontal) {
			this.horizontal = horizontal;
		}

		public double getVertical() {
			return vertical;
		}

		public void setVertical(double vertical) {
			this.vertical = vertical;
		}
	}
	
	
	//##################### Constructor ##############################
	public CalcAngle(Cannons plugin, UserMessages userMessages, Config config)
	{
		this.plugin = plugin;
		this.userMessages = userMessages;
		this.config = config;
		
		inAimingMode = new HashMap<Player, CannonData>();

	}
	
	//##################### InitAimingMode ##############################
	public void initAimingMode()
	{
		//changing angles for aiming mode
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() 
		{
			public void run() 
			    {
			    	updateAimingMode();
			    }
		}, 5L, 5L);	
	}
	
	//################# ChangeAngle #######################################################
	public void ChangeAngle(CannonData cannon_loc, Action action, BlockFace clickedFace, Player player){
		if (action.equals(Action.RIGHT_CLICK_BLOCK )){
			if (player.getItemInHand().getType() == Material.WATCH)
			{
				//aiming mode
				ToggleAimingMode(player, cannon_loc);
			}
			else
			{
				//barrel clicked to change angle
				DisplayAngle(cannon_loc, action, clickedFace, player);
			}
		}
	}
	
	//################# DisplayAngle #######################################################
	private void DisplayAngle(CannonData cannon_loc, Action action, BlockFace clickedFace, Player player)
	{
		gunAngles angles = new gunAngles(0.0, 0.0);
		//both horizontal and vertical angle will be displayed in one message
		boolean combine = false;
		//angle changed
		boolean hasChanged = false;
		
		if (player.hasPermission("cannons.player.adjust") == false)
		{
			player.sendMessage(userMessages.ErrorPermAdjust);
			return;
		}
			

		
		//barrel clicked to change angle
		if (player.getItemInHand().getType() == Material.WATCH)
		{
			//aiming mode
			angles = CheckLookingDirection(cannon_loc, player.getLocation().getDirection());
			combine = true;
		}
		else
		{
			//barrel clicked to change angle
			angles = CheckBlockFace(clickedFace, cannon_loc.face, player.isSneaking());
			combine = false;
		}
		 
		//Check angles
		if (Math.abs(angles.getHorizontal()) >= 1.0)
		{
			if (angles.getHorizontal() >= 0)
			{
				// right 
				if (cannon_loc.horizontal_angle + config.angle_step <= config.max_h_angle)
				{
					cannon_loc.horizontal_angle += config.angle_step;
					hasChanged = true;
					if (combine == false) 
					{
						player.sendMessage(userMessages.getSettingHorizontalAngle(cannon_loc.horizontal_angle));
					}
				}
			}
			else
			{
				// left 
				if (cannon_loc.horizontal_angle - config.angle_step >= config.min_h_angle)
				{
					cannon_loc.horizontal_angle -= config.angle_step;
					hasChanged = true;
					if (combine == false) 
					{
						player.sendMessage(userMessages.getSettingHorizontalAngle(cannon_loc.horizontal_angle));
					}
				}
			}
		}
		
		if (Math.abs(angles.getVertical()) >= 1.0)
		{
			if (angles.getVertical() >= 0.0)
			{
				// up
				if (cannon_loc.vertical_angle + config.angle_step <= config.max_v_angle)
				{
					cannon_loc.vertical_angle+= config.angle_step;
					hasChanged = true;
					if (combine == false) 
					{
						player.sendMessage(userMessages.getSettingVerticalAngle(cannon_loc.vertical_angle));
					}
				}
			}
			else
			{
				// down
				if (cannon_loc.vertical_angle - config.angle_step >= config.min_v_angle)
				{
					cannon_loc.vertical_angle -= config.angle_step;
					hasChanged = true;
					if (combine == false) 
					{
						player.sendMessage(userMessages.getSettingVerticalAngle(cannon_loc.vertical_angle));
					}		
				}
			}
		}
		
		//display the combined messages with both angles
		if (combine == true && hasChanged == true)
		{
			player.sendMessage(userMessages.getSettingCombinedAngle(cannon_loc.horizontal_angle, cannon_loc.vertical_angle));
		}
	}
	
	
	
	
	//############## CheckLookingDirection   ################################
	private gunAngles CheckLookingDirection(CannonData cannon, Vector lookingDirection)
	{	
		gunAngles returnValue = new gunAngles(0.0 ,0.0);
		
		//calc vertical angle difference
		returnValue.setVertical((lookingDirection.getY() - Math.sin(cannon.vertical_angle * Math.PI / 180)) * 180 / Math.PI);

		
		//calc horizontal angle difference
		if (cannon.face == BlockFace.NORTH) 
		{
			//vect = new Vector( -1.0f, 0, 0);
			returnValue.setHorizontal( (-lookingDirection.getZ() - Math.sin(cannon.horizontal_angle * Math.PI / 180) ) * 180 / Math.PI);
		}
		else if (cannon.face == BlockFace.EAST) 
		{
			//vect = new Vector(0 , 0, -1.0f);
			returnValue.setHorizontal( (lookingDirection.getX() - Math.sin(cannon.horizontal_angle * Math.PI / 180) ) * 180 / Math.PI);
		}
		else if (cannon.face == BlockFace.SOUTH) 
		{
			//vect = new Vector( 1.0f, 0, 0);
			returnValue.setHorizontal( (lookingDirection.getZ() - Math.sin(cannon.horizontal_angle * Math.PI / 180) ) * 180 / Math.PI);
		}
		else if (cannon.face == BlockFace.WEST) 
		{
			//vect = new Vector(0, 0, 1.0f);
			
			returnValue.setHorizontal( (-lookingDirection.getX() - Math.sin(cannon.horizontal_angle * Math.PI / 180) ) * 180 / Math.PI);
		}
		
		
		return returnValue;
		
	}
	

	
	//############## CheckBlockFace   ################################
	//0 - right
	//1 - left
	//2 - up
	//3 - down
	public gunAngles CheckBlockFace(BlockFace clickedFace, BlockFace cannonDirection, boolean isSneaking)
	{	
		//check up or down
		if (clickedFace.equals(BlockFace.DOWN)) 
		{
			if (isSneaking == true)
			{
				return new gunAngles(0.0, 1.0);
			}
			else
			{
				return new gunAngles(0.0, -1.0);
			}
		}
		if (clickedFace.equals(BlockFace.UP)) 
		{
			if (isSneaking == true)
			{
				return new gunAngles(0.0, -1.0);
			}
			else
			{
				return new gunAngles(0.0, 1.0);
			}
		}
		//check left 
		BlockFace rightFace = getRightBlockFace(cannonDirection);
		if (clickedFace.equals(rightFace.getOppositeFace())) 
		{
			if (isSneaking == true)
			{
				return new gunAngles(1.0, 0.0);
			}
			else
			{
				return new gunAngles(-1.0, 0.0);
			}
		}
		//check right
		if (clickedFace.equals(rightFace)) 
		{
			if (isSneaking == true)
			{
				return new gunAngles(-1.0, 0.0);
			}
			else
			{
				return new gunAngles(1.0, 0.0);
			}
		}
		//check front or back
		if (clickedFace.equals(cannonDirection) || clickedFace.equals(cannonDirection.getOppositeFace()) ) 
		{
			if (isSneaking == true)
			{
				return new gunAngles(-1.0, 0.0);
			}
			else
			{
				return new gunAngles(1.0, 0.0);
			}
		}
		
		return new gunAngles(0.0, 0.0);
	}
	
	//############## getRightBlockFace   ################################
	private BlockFace getRightBlockFace(BlockFace face)
	{
		if (face == BlockFace.NORTH) return BlockFace.EAST;
		if (face == BlockFace.EAST) return BlockFace.SOUTH;
		if (face == BlockFace.SOUTH) return BlockFace.WEST;
		if (face == BlockFace.WEST) return BlockFace.NORTH;
		return BlockFace.UP;
	}
	
	//############## PlayerMove ################################
	public void PlayerMove(Player player)
	{
		if (inAimingMode.containsKey(player) == true)
		{	
			//check if player if far away from the cannon
			CannonData cannon = inAimingMode.get(player);
			//go the back of the cannon
			Location locCannon = cannon.firingLocation.getBlock().getRelative(cannon.face.getOppositeFace(), cannon.barrel_length).getLocation();
			if (player.getLocation().distance(locCannon) > 5)
			{
				//cancel aiming mode if too far away
				disableAimingMode(player);
			}
		}
		
		
	}
	
	//############## updateAimingMode   ################################
	public void updateAimingMode()
	{
		//player in map change the angle to the angle the player is looking
    	for(Map.Entry<Player, CannonData> entry : inAimingMode.entrySet()){
    		Player player = entry.getKey();
    		CannonData cannon = entry.getValue();
    		if (player.getItemInHand().getType() == Material.WATCH && player.isOnline() == true && cannon.isValid == true)
    		{
        		DisplayAngle(cannon, null, null, player);   
    		}		
    		else
    		{
    			//leave aiming Mode
    			disableAimingMode(player);
    		}
    	}
	}
	
	
	//############## ToggleAimingMode   ################################
	public void ToggleAimingMode(Player player, CannonData cannon)
	{
		
		if (inAimingMode.containsKey(player) == true)
		{
			//player in map -> remove
			disableAimingMode(player);
		}
		else
		{
			//check if player has permission to aim
			if (player.hasPermission("cannons.player.adjust") == true)
			{
				player.sendMessage(userMessages.enableAimingMode);
				inAimingMode.put(player, cannon);
			}
			else
			{
				//no Permission to aim
				player.sendMessage(userMessages.ErrorPermAdjust);
				return;
			}
		}
	}
	
	//############## disableAimingMode   ################################
	public void disableAimingMode(Player player)
	{		
		if (inAimingMode.containsKey(player) == true)
		{
			//player in map -> remove
			player.sendMessage(userMessages.disableAimingMode);
			inAimingMode.remove(player);
		}
	}
	
}
