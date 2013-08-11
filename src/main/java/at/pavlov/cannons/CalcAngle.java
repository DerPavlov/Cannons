package at.pavlov.cannons;

import java.util.HashMap;
import java.util.Map;

import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.MessageEnum;
import at.pavlov.cannons.config.UserMessages;


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
		}, 1L, 1L);	
	}
	
	//################# ChangeAngle #######################################################
	public MessageEnum ChangeAngle(Cannon cannon_loc, Action action, BlockFace clickedFace, Player player){
		if (action.equals(Action.RIGHT_CLICK_BLOCK )){
			if (config.getToolAutoaim().equalsFuzzy(player.getItemInHand()))
			{
				//aiming mode
				ToggleAimingMode(player, cannon_loc);
			}
			else
			{
				//barrel clicked to change angle
				return DisplayAngle(cannon_loc, clickedFace, player);
			}
		}
		return null;
	}
	
	//################# DisplayAngle #######################################################
	private MessageEnum DisplayAngle(Cannon cannon, BlockFace clickedFace, Player player)
	{
		CannonDesign design = plugin.getDesignStorage().getDesign(cannon);
		
		gunAngles angles = new gunAngles(0.0, 0.0);
		//both horizontal and vertical angle will be displayed in one message
		boolean combine;
		//angle changed
		boolean hasChanged = false;
		//message Enum
		MessageEnum message = null;

        if (player != null)
        {
            //if the player is not the owner of this gun
            if (!cannon.getOwner().equals(player.getName())  && design.isAccessForOwnerOnly())
            {
                return MessageEnum.ErrorNotTheOwner;
            }
            //if the player has the permission to adjust this gun
            if (player.hasPermission("cannons.player.adjust") == false)
            {
                return  MessageEnum.PermissionErrorAdjust;
            }
        }

		//barrel clicked to change angle
		if (config.getToolAutoaim().equalsFuzzy(player.getItemInHand()))
		{
			//aiming mode
			angles = CheckLookingDirection(cannon, player.getLocation());
			combine = true;
		}
		else
		{
			//barrel clicked to change angle
			angles = CheckBlockFace(clickedFace, cannon.getCannonDirection(), player.isSneaking());
			combine = false;
		}
		
		//Check angles
		if (Math.abs(angles.getHorizontal()) >= design.getAngleStepSize())
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
				if (cannon.getVerticalAngle() - design.getAngleStepSize() >= design.getMinVerticalAngle())
				{
					cannon.setVerticalAngle(cannon.getVerticalAngle() - design.getAngleStepSize());
					hasChanged = true;
					message = setMessageVertical(cannon, combine);
				}
			}
		}
		
		//update the time
		cannon.setLastAimed(System.currentTimeMillis());
		
		//display message only if the angle has changed
		if (hasChanged)
			return message;
		else
			return null;
	}
	
	
	
	
	//############## CheckLookingDirection   ################################
	private gunAngles CheckLookingDirection(Cannon cannon, Location loc)
	{	
		CannonDesign design = cannon.getCannonDesign();
		
		gunAngles returnValue = new gunAngles(0.0 ,0.0);
		
		//calc vertical angle difference
		returnValue.setVertical(-loc.getPitch() - (cannon.getVerticalAngle() + design.getDefaultVerticalAngle()));
		
		//get yaws of cannon and player
		double cannonYaw = CannonsUtil.directionToYaw(cannon.getCannonDirection());
		double playerYaw = loc.getYaw();
		
		//make the player yaw prettier so it is 0<yaw<360
		playerYaw = playerYaw % 360;
		while(playerYaw < 0)
			playerYaw = playerYaw + 360;
		
		//set horizontal angle
		returnValue.setHorizontal(playerYaw - cannonYaw - cannon.getHorizontalAngle());	
		
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

    /**
     * returns the cannon of the player if he is in aiming mode
     * @param player the player who is in aiming mode
     * @return the cannon which is in aiming mode by the given player
     */
    public Cannon getCannonInAimingMode(Player player)
    {
        //return the cannon of the player if he is in aiming mode
        return inAimingMode.get(player);
    }

	
	/**
	 * if the player is not near the cannon
	 * @param player The player which has moved
     * @return false if the player is too far away
	 */
	public boolean distanceCheck(Player player, Cannon cannon)
	{
        // no cannon? then exit
        if (cannon == null)
            return true;

        //check if player if far away from the cannon
        CannonDesign design = plugin.getCannonDesign(cannon);
		//go to trigger location
		Location locCannon = design.getFiringTrigger(cannon);
		if (player.getLocation().distance(locCannon) > 4)
		{
			//cancel aiming mode if too far away
			return false;
        }
        return true;
	}
	
	//############## updateAimingMode   ################################
	public void updateAimingMode()
	{
		//player in map change the angle to the angle the player is looking
    	for(Map.Entry<Player, Cannon> entry : inAimingMode.entrySet()){
    		Player player = entry.getKey();
    		Cannon cannon = entry.getValue();
    		// only update if since the last update some ticks have past (updateSpeed is in ticks = 50ms)
    		if (System.currentTimeMillis() >= cannon.getLastAimed() + cannon.getCannonDesign().getAngleUpdateSpeed()*50 )
    		{
    			// autoaming or fineadjusting
    			if (config.getToolAutoaim().equalsFuzzy(player.getItemInHand()) && player.isOnline() == true && cannon.isValid() == true)
        		{
            		MessageEnum message = DisplayAngle(cannon, null, player);
            		userMessages.displayMessage(player, message, cannon);
        		}		
        		else
        		{
        			//leave aiming Mode
        			MessageEnum message = disableAimingMode(player);
                    userMessages.displayMessage(player, message, cannon);
        		}
    		}	
    	}
	}

	
	
	//############## ToggleAimingMode   ################################
	public void ToggleAimingMode(Player player, Cannon cannon)
	{
		
		if (inAimingMode.containsKey(player) == true)
		{
			//player in map -> remove
			MessageEnum message = disableAimingMode(player);
            userMessages.displayMessage(player, message, cannon);
        }
		else
		{
			//check if player has permission to aim
			if (player.hasPermission("cannons.player.adjust") == true)
			{
                //check distance before enabling the cannon
                if (distanceCheck(player, cannon) == true)
                {
                    userMessages.displayMessage(player, MessageEnum.AimingModeEnabled, cannon);
                    inAimingMode.put(player, cannon);
                }
                else
                {
                    userMessages.displayMessage(player, MessageEnum.AimingModeTooFarAway, cannon);
                }

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
	public MessageEnum disableAimingMode(Player player)
	{		
		if (inAimingMode.containsKey(player) == true)
		{
			//player in map -> remove
			inAimingMode.remove(player);
            return MessageEnum.AimingModeDisabled;
		}
        return null;
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
