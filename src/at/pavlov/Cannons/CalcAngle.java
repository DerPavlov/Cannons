package at.pavlov.Cannons;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import at.pavlov.Cannons.cannon.Cannon;
import at.pavlov.Cannons.cannon.CannonDesign;
import at.pavlov.Cannons.config.Config;
import at.pavlov.Cannons.config.MessageEnum;
import at.pavlov.Cannons.config.UserMessages;
import at.pavlov.Cannons.utils.CannonsUtil;


public class CalcAngle {
	
	Cannons plugin;
	UserMessages userMessages;
	Config config;
	
	HashMap<Player, Cannon> inAimingMode = new HashMap<Player, Cannon>();
	
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
		
		inAimingMode = new HashMap<Player, Cannon>();

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
	public void ChangeAngle(Cannon cannon_loc, Action action, BlockFace clickedFace, Player player){
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
	private void DisplayAngle(Cannon cannon, Action action, BlockFace clickedFace, Player player)
	{
		CannonDesign design = plugin.getDesignStorage().getDesign(cannon);
		
		gunAngles angles = new gunAngles(0.0, 0.0);
		//both horizontal and vertical angle will be displayed in one message
		boolean combine = false;
		//angle changed
		boolean hasChanged = false;
		//message Enum
		MessageEnum message = null;
		
		if (player.hasPermission("cannons.player.adjust") == false)
		{
			userMessages.displayMessage(player, MessageEnum.PermissionErrorAdjust, cannon);
			return;
		}
			

		
		//barrel clicked to change angle
		if (player.getItemInHand().getType() == Material.WATCH)
		{
			//aiming mode
			angles = CheckLookingDirection(cannon, player.getLocation().getDirection());
			combine = true;
		}
		else
		{
			//barrel clicked to change angle
			angles = CheckBlockFace(clickedFace, cannon.getCannonDirection(), player.isSneaking());
			combine = false;
		}
		 
		//Check angles
		if (Math.abs(angles.getHorizontal()) >= 1.0)
		{
			if (angles.getHorizontal() >= 0)
			{
				// right 
				if (cannon.getHorizontalAngle() + design.getAngleStepSize() <= design.getMaxHorizontalAngle())
				{
					cannon.setHorizontalAngle(cannon.getHorizontalAngle() + design.getAngleStepSize());
					hasChanged = true;
					message = setMessageHorizontal(cannon, combine);
				}
			}
			else
			{
				// left 
				if (cannon.getHorizontalAngle() - design.getAngleStepSize() >= design.getMinHorizontalAngle())
				{
					cannon.setHorizontalAngle(cannon.getHorizontalAngle() - design.getAngleStepSize());
					hasChanged = true;
					message = setMessageHorizontal(cannon, combine);
				}
			}
		}
		
		if (Math.abs(angles.getVertical()) >= 1.0)
		{
			if (angles.getVertical() >= 0.0)
			{
				// up
				if (cannon.getVerticalAngle() + design.getAngleStepSize() <= design.getMaxVerticalAngle())
				{
					cannon.setVerticalAngle(cannon.getVerticalAngle() + design.getAngleStepSize());
					hasChanged = true;
					message = setMessageVertical(cannon, combine);
				}
			}
			else
			{
				// down
				if (cannon.getVerticalAngle() - design.getAngleStepSize() >= design.getMaxVerticalAngle())
				{
					cannon.setVerticalAngle(cannon.getVerticalAngle() - design.getAngleStepSize());
					hasChanged = true;
					message = setMessageVertical(cannon, combine);
				}
			}
		}
		
		//display message only if the angle has changed
		if (hasChanged)
			userMessages.displayMessage(player, message, cannon);
	}
	
	
	
	
	//############## CheckLookingDirection   ################################
	private gunAngles CheckLookingDirection(Cannon cannon, Vector lookingDirection)
	{	
		gunAngles returnValue = new gunAngles(0.0 ,0.0);
		
		//calc vertical angle difference
		returnValue.setVertical((lookingDirection.getY() - Math.sin(cannon.getVerticalAngle() * Math.PI / 180)) * 180 / Math.PI);

		
		//calc horizontal angle difference
		if (cannon.getCannonDirection().equals(BlockFace.NORTH)) 
		{
			//vect = new Vector( -1.0f, 0, 0);
			returnValue.setHorizontal( (-lookingDirection.getZ() - Math.sin(cannon.getHorizontalAngle() * Math.PI / 180) ) * 180 / Math.PI);
		}
		else if (cannon.getCannonDirection().equals(BlockFace.EAST)) 
		{
			//vect = new Vector(0 , 0, -1.0f);
			returnValue.setHorizontal( (lookingDirection.getX() - Math.sin(cannon.getHorizontalAngle() * Math.PI / 180) ) * 180 / Math.PI);
		}
		else if (cannon.getCannonDirection().equals(BlockFace.SOUTH)) 
		{
			//vect = new Vector( 1.0f, 0, 0);
			returnValue.setHorizontal( (lookingDirection.getZ() - Math.sin(cannon.getHorizontalAngle() * Math.PI / 180) ) * 180 / Math.PI);
		}
		else if (cannon.getCannonDirection().equals(BlockFace.WEST)) 
		{
			//vect = new Vector(0, 0, 1.0f);
			
			returnValue.setHorizontal( (-lookingDirection.getX() - Math.sin(cannon.getHorizontalAngle() * Math.PI / 180) ) * 180 / Math.PI);
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
		BlockFace rightFace = CannonsUtil.roatateFace(cannonDirection);
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
	

	
	//############## PlayerMove ################################
	public void PlayerMove(Player player)
	{
		
		
		if (inAimingMode.containsKey(player) == true)
		{	
			//check if player if far away from the cannon
			Cannon cannon = inAimingMode.get(player);
			CannonDesign design = plugin.getCannonDesign(cannon);
			//go to trigger location
			Location locCannon = design.getFiringTrigger(cannon);
			if (player.getLocation().distance(locCannon) > 2)
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
    	for(Map.Entry<Player, Cannon> entry : inAimingMode.entrySet()){
    		Player player = entry.getKey();
    		Cannon cannon = entry.getValue();
    		if (player.getItemInHand().getType() == Material.WATCH && player.isOnline() == true && cannon.isValid() == true)
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
	public void ToggleAimingMode(Player player, Cannon cannon)
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
				userMessages.displayMessage(player, MessageEnum.AimingModeEnabled, cannon);
				inAimingMode.put(player, cannon);
			}
			else
			{
				//no Permission to aim
				userMessages.displayMessage(player, MessageEnum.PermissionErrorAdjust, cannon);
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
			userMessages.displayMessage(player, MessageEnum.AimingModeDisabled, null);
			inAimingMode.remove(player);
		}
	}
	
	/**
	 * finds the right message for the horizontal angle change
	 * @param cannon
	 * @return
	 */
	public MessageEnum setMessageHorizontal(Cannon cannon, boolean combinedAngle)
	{
		if (combinedAngle)
			return MessageEnum.SettingCombinedAngle;
		//correct some angle messages
		if (cannon.getHorizontalAngle() > 0)
		{
			//aiming to the right
			return MessageEnum.SettingHorizontalAngleRight;
		}
		else
		{
			//aiming to the left
			return MessageEnum.SettingHorizontalAngleLeft;
		}
	}
	
	/**
	 * finds the right message for the vertical angle change
	 * @param cannon
	 * @return
	 */
	public MessageEnum setMessageVertical(Cannon cannon, boolean combinedAngle)
	{
		if (combinedAngle)
			return MessageEnum.SettingCombinedAngle;
		if (cannon.getVerticalAngle() > 0)
		{
			//aiming to the down
			return MessageEnum.SettingVerticalAngleUp;
		}
		else
		{
			//aiming to the up
			return MessageEnum.SettingVerticalAngleDown;
		}
	}
	
}
