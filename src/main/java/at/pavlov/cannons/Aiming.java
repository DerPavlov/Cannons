package at.pavlov.cannons;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.container.MovingObject;
import at.pavlov.cannons.event.CannonUseEvent;
import at.pavlov.cannons.listener.Commands;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Aiming {

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

    private final Cannons plugin;
    private final UserMessages userMessages;
    private final Config config;

    private HashMap<String, Cannon> inAimingMode = new HashMap<String, Cannon>();
    private HashSet<String> imitatedEffectsOff = new HashSet<String>();


    /**
     * Constructor
     * @param plugin Cannons main class
     */
	public Aiming(Cannons plugin) {
        this.plugin = plugin;
        this.config = plugin.getMyConfig();
        this.userMessages = plugin.getMyConfig().getUserMessages();
    }

    /**
     * starts the aiming mode scheduler
     */
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

    /**
     * player click interaction with cannon
     * @param cannon operated cannon
     * @param action interaction of player with cannon
     * @param clickedFace which side was clicked (up, down, left, right)
     * @param player operator of the cannon
     * @return message for the player
     */
	public MessageEnum ChangeAngle(Cannon cannon, Action action, BlockFace clickedFace, Player player){
        //fire event
        CannonUseEvent useEvent = new CannonUseEvent(cannon, player, InteractAction.adjustPlayer);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;


		if (action.equals(Action.RIGHT_CLICK_BLOCK )){
			if (config.getToolAutoaim().equalsFuzzy(player.getItemInHand()))
			{
				//aiming mode
				ToggleAimingMode(player, cannon);
			}
			else
			{
				//barrel clicked to change angle
				return DisplayAngle(cannon, clickedFace, player);
			}
		}
		return null;
	}

    /**
     * evaluates the new cannon angle and returns a message for the user
     * @param cannon operated cannon
     * @param clickedFace which side was clicked (up, down, left, right)
     * @param player operator of the cannon
     * @return message for the player
     */
	private MessageEnum DisplayAngle(Cannon cannon, BlockFace clickedFace, Player player)
	{
		CannonDesign design = cannon.getCannonDesign();

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
            if (!player.hasPermission("cannons.player.adjust"))
            {
                return  MessageEnum.PermissionErrorAdjust;
            }
        }

        gunAngles angles;

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
		if (Math.abs(angles.getHorizontal()) >= 1.0)
		{
			if (angles.getHorizontal() >= 0)
			{
				// right 
				if (cannon.getHorizontalAngle() + design.getAngleStepSize() <= design.getMaxHorizontalAngle())
				{
                    //if smaller than minimum -> set to minimum
                    if (cannon.getHorizontalAngle() < design.getMinHorizontalAngle())
                        cannon.setHorizontalAngle(design.getMinHorizontalAngle());
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
                    //if smaller than maximum -> set to maximum
                    if (cannon.getHorizontalAngle() > design.getMaxHorizontalAngle())
                        cannon.setHorizontalAngle(design.getMaxHorizontalAngle());
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
                    //if smaller than minimum -> set to minimum
                    if (cannon.getVerticalAngle() < design.getMinVerticalAngle())
                        cannon.setVerticalAngle(design.getMinVerticalAngle());
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
                    if (cannon.getVerticalAngle() > design.getMaxVerticalAngle())
                        cannon.setVerticalAngle(design.getMaxVerticalAngle());
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


    /**
     * evaluates the difference between actual cannon direction and the given direction
     * @param cannon operated cannon
     * @param loc yaw and pitch will be used
     * @return new cannon aiming direction
     */
	private gunAngles CheckLookingDirection(Cannon cannon, Location loc)
	{
		gunAngles returnValue = new gunAngles(0.0 ,0.0);
		
		//calc vertical angle difference
		returnValue.setVertical(-loc.getPitch() - cannon.getTotalVerticalAngle());
		
		//get yaws of cannon and player
		double cannonYaw = CannonsUtil.directionToYaw(cannon.getCannonDirection());
		double yaw = loc.getYaw();
        double horizontal = yaw - cannonYaw - cannon.getTotalHorizontalAngle();
		
        horizontal = horizontal % 360;
		while(horizontal < -180)
            horizontal = horizontal + 360;
		
		//set horizontal angle
		returnValue.setHorizontal(horizontal);

        plugin.logDebug("Yaw: " + yaw + " cannonYaw: " + horizontal);
		
		return returnValue;
		
	}
	


    /**
     * returns the angle to change by the given block face
     * 0 - right
     * 1 - left
     * 2 - up
     * 3 - down
     * @param clickedFace - click block face on the cannon
     * @param cannonDirection - direction the cannon is facing
     * @param isSneaking - is the player sneaking (will revert all options)
     * @return - angle to change
     */
    private gunAngles CheckBlockFace(BlockFace clickedFace, BlockFace cannonDirection, boolean isSneaking)
	{	
		//check up or down
		if (clickedFace.equals(BlockFace.DOWN)) 
		{
			if (isSneaking)
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
			if (isSneaking)
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
			if (isSneaking)
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
			if (isSneaking)
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
			if (isSneaking)
			{
				return new gunAngles(0.0, -1.0);
			}
			else
			{
				return new gunAngles(0.0, 1.0);
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
        return inAimingMode.get(player.getName());
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
        return player.getLocation().distance(locCannon) <= 4;
    }


    /**
     * updates the auto Aiming direction for player in auto-aiming mode
     */
    void updateAimingMode()
	{
		//player in map change the angle to the angle the player is looking
    	for(Map.Entry<String, Cannon> entry : inAimingMode.entrySet()){
    		Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) return;

    		Cannon cannon = entry.getValue();
    		// only update if since the last update some ticks have past (updateSpeed is in ticks = 50ms)
    		if (System.currentTimeMillis() >= cannon.getLastAimed() + cannon.getCannonDesign().getAngleUpdateSpeed()*50 )
    		{
    			// autoaming or fineadjusting
    			if (config.getToolAutoaim().equalsFuzzy(player.getItemInHand()) && player.isOnline() && cannon.isValid())
        		{
            		MessageEnum message = DisplayAngle(cannon, null, player);
                    //show impact predictor marker
                    impactPredictor(cannon, player);
            		userMessages.displayMessage(player, cannon, message);
        		}		
        		else
        		{
        			//leave aiming Mode
        			MessageEnum message = disableAimingMode(player);
                    userMessages.displayMessage(player, cannon, message);
        		}
    		}	
    	}
	}


    /**
     * switches aming mode for this cannon
     * @param player - player in aiming mode
     * @param cannon - operated cannon
     */
	public void ToggleAimingMode(Player player, Cannon cannon)
	{
		if (inAimingMode.containsKey(player.getName()))
		{
            if (cannon == null)
                cannon = inAimingMode.get(player.getName());

            //this player is already in aiming mode, he might fire the cannon or turn the aiming mode of
		    if (player.isSneaking())
            {
                MessageEnum message = plugin.getFireCannon().playerFiring(cannon, player, InteractAction.fireAutoaim);
                userMessages.displayMessage(player, cannon, message);
            }
            else
            {
                //turn off the aiming mode
                MessageEnum message = disableAimingMode(player);
                userMessages.displayMessage(player, cannon, message);
            }
        }
		else if(cannon != null)
		{
			//check if player has permission to aim
			if (player.hasPermission(cannon.getCannonDesign().getPermissionAutoaim()))
			{
                //check distance before enabling the cannon
                if (distanceCheck(player, cannon))
                {
                    userMessages.displayMessage(player, cannon, MessageEnum.AimingModeEnabled);
                    inAimingMode.put(player.getName(), cannon);
                }
                else
                {
                    userMessages.displayMessage(player, cannon, MessageEnum.AimingModeTooFarAway);
                }

			}
			else
			{
				//no Permission to aim
				userMessages.displayMessage(player, cannon, MessageEnum.PermissionErrorAdjust);
				return;
			}
		}
	}

    /**
     * disables the aiming mode for this player
     * @param player - player in aiming mode
     * @return message for the player
     */
	public MessageEnum disableAimingMode(Player player)
	{		
		if (inAimingMode.containsKey(player.getName()))
		{
			//player in map -> remove
			inAimingMode.remove(player.getName());
            return MessageEnum.AimingModeDisabled;
		}
        return null;
	}
	
	/**
	 * finds the right message for the horizontal angle change
	 * @param cannon
	 * @return
	 */
    private MessageEnum setMessageHorizontal(Cannon cannon, boolean combinedAngle)
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
    private MessageEnum setMessageVertical(Cannon cannon, boolean combinedAngle)
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

    /**
     * show a line where the cannon is aiming
     * @param cannon - operated cannon
     * @param player - player operating the cannon
     */
    public void showAimingVector(Cannon cannon, Player player)
    {
        // Imitation of angle
        if(config.isImitatedAimingEnabled() && isImitatingEnabled(player.getName()))
        {
            plugin.getFakeBlockHandler().imitateLine(player, cannon.getMuzzle(), cannon.getAimingVector(), 0,
                    config.getImitatedAimingLineLength(), config.getImitatedAimingMaterial(), config.getImitatedAimingTime());
        }
    }

    public void toggleImitating(Player player)
    {
        if(!isImitatingEnabled(player.getName()))
        {
            enableImitating(player);
        }
        else
        {
            disableImitating(player);
        }
    }

    public void disableImitating(Player player){
        userMessages.displayMessage(player,MessageEnum.ImitatedEffectsDisabled);
        //it is enabled on default, adding to this list will stop the aiming effect
        imitatedEffectsOff.add(player.getName());
    }

    public boolean isImitatingEnabled(String name){
        //it is enabled on default, adding to this list will stop the aiming effect
        return !imitatedEffectsOff.contains(name);
    }

    public void enableImitating(Player player){
        userMessages.displayMessage(player,MessageEnum.ImitatedEffectsEnabled);
        //it is enabled on default, adding to this list will stop the aiming effect
        imitatedEffectsOff.remove(player.getName());
    }

    /**
     * calculated the impact of the projectile
     * @param cannon the cannon must be loaded with a projectile
     * @return the expected impact location
     */
    public Location impactPredictor(Cannon cannon)
    {
        if (!cannon.isLoaded() || !config.isImitatedPredictorEnabled())
            return null;

        Location muzzle = cannon.getMuzzle();
        Vector vel = cannon.getFiringVector(null, false);

        MovingObject predictor = new MovingObject(muzzle, vel);
        Vector start = muzzle.toVector();


        //make a few iterations until we hit something
        for (int i=0;start.distance(predictor.getLoc()) < config.getImitatedPredictorDistance() && i < config.getImitatedPredictorIterations(); i++)
        {
            //see if we hit something
            Block block = predictor.getLocation().getBlock();
            if (!block.isEmpty())
            {
                predictor.revertProjectileLocation(false);
                return CannonsUtil.findSurface(predictor.getLocation(), predictor.getVel());
            }
            predictor.updateProjectileLocation(false);
        }

        //nothing found
        plugin.logDebug("nothing found");
        return null;
    }

    /**
     * calculated the impact of the projectile and make a sphere with fakeBlocks at the impact for the given player
     * @param cannon the cannon must be loaded with a projectile
     * @param player only this player will see this impact marker blocks
     * @return the expected impact location
     */
    public Location impactPredictor(Cannon cannon, Player player)
    {
        Location surface = impactPredictor(cannon);
        plugin.getFakeBlockHandler().imitatedSphere(player, surface, 1, config.getImitatedPredictorMaterial(), config.getImitatedPredictorTime());
        return surface;
    }
}
