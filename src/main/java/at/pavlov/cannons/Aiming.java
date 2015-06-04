package at.pavlov.cannons;

import at.pavlov.cannons.Enum.FakeBlockType;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.cannon.Cannon;
import at.pavlov.cannons.cannon.CannonDesign;
import at.pavlov.cannons.cannon.CannonManager;
import at.pavlov.cannons.config.Config;
import at.pavlov.cannons.config.UserMessages;
import at.pavlov.cannons.container.MovingObject;
import at.pavlov.cannons.container.Target;
import at.pavlov.cannons.event.CannonUseEvent;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import java.util.*;


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

    //<Player,cannon name>
    private HashMap<UUID, UUID> inAimingMode = new HashMap<UUID, UUID>();
	//<Cannon>
	private HashSet<UUID> sentryCannons = new HashSet<UUID>();
    //<Player>
    private HashSet<UUID> imitatedEffectsOff = new HashSet<UUID>();

    //<cannon uid, timespamp>
    private HashMap<UUID, Long> lastAimed = new HashMap<UUID, Long>();


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
                //long startTime = System.nanoTime();
                updateAimingMode();
                updateImpactPredictor();
                updateSentryMode();
                //plugin.logDebug("Time update aiming: " + new DecimalFormat("0.00").format((System.nanoTime() - startTime)/1000000.0) + "ms");
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
	public MessageEnum changeAngle(Cannon cannon, Action action, BlockFace clickedFace, Player player){
        //fire event
        CannonUseEvent useEvent = new CannonUseEvent(cannon, player.getUniqueId(), InteractAction.adjustPlayer);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;


		if (action.equals(Action.RIGHT_CLICK_BLOCK )){
			if (config.getToolAutoaim().equalsFuzzy(player.getItemInHand()))
			{
				//aiming mode
				aimingMode(player, cannon, false);
			}
			else
			{
				//barrel clicked to change angle
				return updateAngle(player, cannon, clickedFace, InteractAction.adjustPlayer);
			}
		}
		return null;
	}

    /**
     * evaluates the new cannon angle and returns a message for the user
     * @param player operator of the cannon
     * @param cannon operated cannon
     * @param clickedFace which side was clicked (up, down, left, right)
     * @return message for the player
     */
	private MessageEnum updateAngle(Player player, Cannon cannon, BlockFace clickedFace, InteractAction action) {
        if (cannon == null)
            return null;

        CannonDesign design = cannon.getCannonDesign();
        boolean isSentry = design.isSentry();

		//both horizontal and vertical angle will be displayed in one message
		boolean combine;
		//angle changed
		boolean hasChanged = false;
		//message Enum
		MessageEnum message = null;

        if (player != null)
        {
            //if the player is not the owner of this gun
            if (!cannon.getOwner().equals(player.getUniqueId())  && design.isAccessForOwnerOnly())
                return MessageEnum.ErrorNotTheOwner;
            //if the player has the permission to adjust this gun
            if (!player.hasPermission(cannon.getCannonDesign().getPermissionAdjust()))
                return  MessageEnum.PermissionErrorAdjust;
        }

        gunAngles angles;

        if (action == InteractAction.adjustSentry && isSentry){
            // sentry mode
            if (cannon.isChunkLoaded()) {
                angles = getGunAngle(cannon, cannon.getAimingYaw(), cannon.getAimingPitch());
                combine = true;
            }
            else{
                plugin.logDebug("chunk not loaded. ignore cannon: " + cannon.getLocation());
                return null;
            }
        }
		else if (action == InteractAction.adjustAutoaim && player != null && inAimingMode.containsKey(player.getUniqueId()))
		{
			//aiming mode only if player is sneaking
            if (player.isSneaking())
            {
                angles = getGunAngle(cannon, player.getLocation().getYaw(), player.getLocation().getPitch());
                combine = true;
            }
            else
                return null;
		}
		else
		{
            //barrel clicked to change angle
			if (player!=null) {
				angles = CheckBlockFace(clickedFace, cannon.getCannonDirection(), player.isSneaking());
				//register impact predictor
				cannon.addObserver(player, true);
                combine = false;
			}
			else
			    return null;
		}

		//Check angles
		if (Math.abs(angles.getHorizontal()) >= 1.0)
		{
			if (angles.getHorizontal() >= 0)
			{
				// right 
				if (cannon.getHorizontalAngle() + design.getAngleStepSize() <= cannon.getMaxHorizontalAngle())
				{
                    //if smaller than minimum -> set to minimum
                    if (cannon.getHorizontalAngle() < cannon.getMinHorizontalAngle())
                        cannon.setHorizontalAngle(cannon.getMinHorizontalAngle());
                    cannon.setHorizontalAngle(cannon.getHorizontalAngle() + design.getAngleStepSize());
					hasChanged = true;
					message = setMessageHorizontal(cannon, combine);
				}
			}
			else
			{
				// left 
				if (cannon.getHorizontalAngle() - design.getAngleStepSize() >= cannon.getMinHorizontalAngle())
				{
                    //if smaller than maximum -> set to maximum
                    if (cannon.getHorizontalAngle() > cannon.getMaxHorizontalAngle())
                        cannon.setHorizontalAngle(cannon.getMaxHorizontalAngle());
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
				if (cannon.getVerticalAngle() + design.getAngleStepSize() <= cannon.getMaxVerticalAngle())
				{
                    //if smaller than minimum -> set to minimum
                    if (cannon.getVerticalAngle() < cannon.getMinVerticalAngle())
                        cannon.setVerticalAngle(cannon.getMinVerticalAngle());
					cannon.setVerticalAngle(cannon.getVerticalAngle() + design.getAngleStepSize());
					hasChanged = true;
					message = setMessageVertical(cannon, combine);
				}
			}
			else
			{
				// down
				if (cannon.getVerticalAngle() - design.getAngleStepSize() >= cannon.getMinVerticalAngle())
				{
                    if (cannon.getVerticalAngle() > cannon.getMaxVerticalAngle())
                        cannon.setVerticalAngle(cannon.getMaxVerticalAngle());
					cannon.setVerticalAngle(cannon.getVerticalAngle() - design.getAngleStepSize());
					hasChanged = true;
					message = setMessageVertical(cannon, combine);
				}
			}
		}
		
		//update the time
		cannon.setLastAimed(System.currentTimeMillis());
        //show aiming vector in front of the cannon
        showAimingVector(cannon, player);
		
		//display message only if the angle has changed
		if (hasChanged) {

            //player.getWorld().playSound(cannon.getMuzzle(), Sound.IRONGOLEM_WALK, 1f, 0.5f);
            CannonsUtil.playSound(cannon.getMuzzle(),design.getSoundAdjust());
            //predict impact marker
            updateLastAimed(cannon);
			if (cannon.getCannonDesign().isAngleUpdateMessage())
            	return message;
			else
				return null;
        }
		else
            //no change in angle
            return null;
	}


    /**
     * evaluates the difference between actual cannon direction and the given direction
     * @param cannon operated cannon
     * @param yaw yaw of the direction to aim
     * @param pitch pitch of the direction to aim
     * @return new cannon aiming direction
     */
	private gunAngles getGunAngle(Cannon cannon, double yaw, double pitch)
	{
        double horizontal = yaw - CannonsUtil.directionToYaw(cannon.getCannonDirection()) - cannon.getTotalHorizontalAngle();
        horizontal = horizontal % 360;
		while(horizontal < -180)
            horizontal = horizontal + 360;

		return new gunAngles(horizontal, -pitch - cannon.getTotalVerticalAngle());
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
        if (clickedFace == null || cannonDirection == null)
            return new gunAngles(0.0, 0.0);


		//check up or down
		if (clickedFace.equals(BlockFace.DOWN)) 
		{
			if (isSneaking)
				return new gunAngles(0.0, 1.0);
			else
				return new gunAngles(0.0, -1.0);
		}
		if (clickedFace.equals(BlockFace.UP)) 
		{
			if (isSneaking)
				return new gunAngles(0.0, -1.0);
			else
				return new gunAngles(0.0, 1.0);
		}
		//check left 
		BlockFace rightFace = CannonsUtil.roatateFace(cannonDirection);
		if (clickedFace.equals(rightFace.getOppositeFace())) 
		{
			if (isSneaking)
				return new gunAngles(1.0, 0.0);
			else
				return new gunAngles(-1.0, 0.0);
		}
		//check right
		if (clickedFace.equals(rightFace)) 
		{
			if (isSneaking)
				return new gunAngles(-1.0, 0.0);
			else
				return new gunAngles(1.0, 0.0);
		}
		//check front or back
		if (clickedFace.equals(cannonDirection) || clickedFace.equals(cannonDirection.getOppositeFace()) ) 
		{
			if (isSneaking)
				return new gunAngles(0.0, -1.0);
			else
				return new gunAngles(0.0, 1.0);
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
        if (player == null)
            return null;
        //return the cannon of the player if he is in aiming mode
        return CannonManager.getCannon(inAimingMode.get(player.getUniqueId()));
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
        //if there is no trigger - set the muzzle a location
        if (locCannon == null)
            locCannon = cannon.getMuzzle();
        if (cannon.getMuzzle() == null)
        {
            plugin.logSevere("cannon design " + cannon.getCannonDesign() + " has no muzzle location");
            return false;
        }

        return player.getLocation().distance(locCannon) <= 4;
    }


    /**
     * updates the auto Aiming direction for player in auto-aiming mode
     */
    private void updateAimingMode()
	{
		//player in map change the angle to the angle the player is looking
    	Iterator<Map.Entry<UUID, UUID>> iter = inAimingMode.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry<UUID, UUID> entry = iter.next();
    		Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                iter.remove();
                continue;
            }


            //find the cannon with this id
    		Cannon cannon = CannonManager.getCannon(entry.getValue());
            if (cannon == null) {
                iter.remove();
                continue;
            }

    		// only update if since the last update some ticks have past (updateSpeed is in ticks = 50ms)
    		if (System.currentTimeMillis() >= cannon.getLastAimed() + cannon.getCannonDesign().getAngleUpdateSpeed())
    		{
    			// autoaming or fineadjusting
    			if (distanceCheck(player, cannon) && player.isOnline() && cannon.isValid())
        		{
            		MessageEnum message = updateAngle(player, cannon, null, InteractAction.adjustAutoaim);
            		userMessages.sendMessage(message, player, cannon);
        		}		
        		else
        		{
        			//leave aiming Mode
        			MessageEnum message = disableAimingMode(player, cannon);
                    userMessages.sendMessage(message, player, cannon);
        		}
    		}	
    	}
	}

    private void updateSentryMode(){
        Iterator<UUID> iter = sentryCannons.iterator();
        while(iter.hasNext()) {
            Cannon cannon = CannonManager.getCannon(iter.next());
            if (cannon == null) {
                //this cannon does not exist
                iter.remove();
                continue;
            }
            // calculate a firing solution
            if (cannon.isChunkLoaded() && System.currentTimeMillis() > cannon.getLastSentryUpdate() + cannon.getCannonDesign().getSentryUpdateTime()) {
                cannon.setLastSentryUpdate(System.currentTimeMillis());

                HashMap<UUID, Target> targets = CannonsUtil.getNearbyTargets(cannon.getMuzzle(), cannon.getCannonDesign().getSentryMinRange(), cannon.getCannonDesign().getSentryMaxRange());
                //old target - is this still valid?
                if (cannon.hasSentryEntity()) {
                    if (System.currentTimeMillis() > cannon.getSentryTargetingTime() + cannon.getCannonDesign().getSentrySwapTime() || !targets.containsKey(cannon.getSentryEntity())) {
                        cannon.setSentryEntity(null);
                    }
                    else{
                        //hopefully we can aim at this target
						Target target = targets.get(cannon.getSentryEntity());
                        if (!canFindTargetSolution(cannon, target.getLocation(), target.getVelocity())){
                            cannon.setSentryEntity(null);
                        }
                        //is it still an entity
                        else{
                            if (!targets.containsKey(cannon.getSentryEntity()))
                                cannon.setSentryEntity(null);
                        }
                    }
                }

                //find a suitable target
                if (!cannon.hasSentryEntity()){
                    ArrayList<Target> possibleTargets = new ArrayList<Target>();
                    for (Target t : targets.values()) {
                        // todo now it is just players, for testing
                        if (t.getType() == EntityType.PLAYER) {
                            if (canFindTargetSolution(cannon, t.getLocation(), t.getVelocity())){
                                possibleTargets.add(t);
							}
                        }
                    }
                    //so we have some targets
                    if (possibleTargets.size()>0) {
                        for (Target t : possibleTargets) {
                            //select one target
                            if (!cannon.wasSentryTarget(t.getUniqueId())) {
                                cannon.setSentryEntity(t.getUniqueId());
                                break;
                            }
                        }
                        if (!cannon.hasSentryEntity())
                            cannon.setSentryEntity(possibleTargets.get(0).getUniqueId());
                    }
                }

                //find target solution at target
                if (cannon.getSentryEntity() != null){
                    Target target = targets.get(cannon.getSentryEntity());
					plugin.logDebug("targeting: " + target.getName());

                    // find exact solution for the cannon
                    if (calculateTargetSolution(cannon, target.getLocation(), target.getVelocity())){
                        cannon.setSentryEntity(target.getUniqueId());
                    }
                    else {
                        //no exact solution found for this target. So skip it and try it again in the next run
                        plugin.logDebug("no solution found");
                        cannon.setSentryEntity(null);
                        cannon.setLastSentryUpdate(System.currentTimeMillis() - cannon.getCannonDesign().getSentryUpdateTime());
                    }
                }
            }

            //aim at the found solution
            double oldHAngle = cannon.getHorizontalAngle();
            double oldVAngle = cannon.getVerticalAngle();
            // only update if since the last update some ticks have past (updateSpeed is in ticks = 50ms)
            if (System.currentTimeMillis() >= cannon.getLastAimed() + cannon.getCannonDesign().getAngleUpdateSpeed()) {
                // autoaming or fineadjusting
                if (cannon.isValid()) {
                    updateAngle(null, cannon, null, InteractAction.adjustSentry);
                    //no change in angle - ready to fire
                    if (oldHAngle == cannon.getHorizontalAngle() && oldVAngle == cannon.getVerticalAngle()) {
                        //load from chest
                        if (!cannon.isLoaded() && System.currentTimeMillis() > cannon.getSentryLastLoadingFailed() + 2000) {
                            MessageEnum messageEnum = cannon.reloadFromChests(cannon.getOwner(), !cannon.getCannonDesign().isAmmoInfiniteForRedstone());
                            if (messageEnum.isError()) {
                                cannon.setSentryLastLoadingFailed(System.currentTimeMillis());
                                CannonsUtil.playErrorSound(cannon.getMuzzle());
                                plugin.logDebug("Sentry " + cannon.getCannonName() + " loading message: " + messageEnum);
                            }
                            else
                                cannon.setSentryLastLoadingFailed(System.currentTimeMillis()-2000);
                        }
                        if (cannon.isReadyToFire() && cannon.getSentryEntity() != null) {
                            MessageEnum messageEnum = plugin.getFireCannon().sentryFiring(cannon);
                            if (messageEnum != null)
                                plugin.logDebug("Sentry " + cannon.getCannonName() + " firing message: " + messageEnum);
                        }
                    }
                }
            }
        }
    }

    /**
     * find a possible solution to fire the cannon - this is just an estimation
     * @param cannon the cannon which is operated
     * @param target lcoation of the target
	 * @param targetVelocity how fast the target is moving
     * @return true if the cannon can fire on this target
     */
    private boolean canFindTargetSolution(Cannon cannon, Location target, Vector targetVelocity){
        if (!cannon.getWorld().equals(target.getWorld().getUID()))
            return false;

        //plugin.logDebug("old target " + target);
        Location newTarget = target.clone();
//        double distance = target.distance(cannon.getMuzzle());
//        double velocity = cannon.getCannonballVelocity();
//        if (velocity > 0) {
//            double time = distance / velocity;
//            //indirect fire needs longer
//            if (cannon.getCannonDesign().isSentryIndirectFire())
//                time *= 3.0;
//            time += (cannon.getCannonDesign().getFuseBurnTime()+cannon.getCannonDesign().getSentryUpdateTime())/20.0;
//            //plugin.logDebug("time: " + time);
//            //plugin.logDebug("distance: " + distance);
//            plugin.logDebug("velocity: " + targetVelocity);
//
//            newTarget.add(targetVelocity.multiply(time));
//        }
        //plugin.logDebug("new target " + newTarget);

        Vector direction = newTarget.toVector().subtract(cannon.getMuzzle().toVector());
        double yaw = CannonsUtil.vectorToYaw(direction);
        double pitch = CannonsUtil.vectorToPitch(direction);
        //can the cannon fire on this player
        if (cannon.canAimYaw(yaw)) {
			cannon.setAimingYaw(yaw);
			cannon.setAimingPitch(pitch);
            return true;
        }
        return false;
    }

    /**
     * find exact solution to fire the cannon
     * @param cannon the cannon which is operated
     * @param target lcoation of the target
     * @param targetVelocity how fast the target is moving
     * @return true if a solution was found
     */
    private boolean calculateTargetSolution(Cannon cannon, Location target, Vector targetVelocity){
        canFindTargetSolution(cannon, target, targetVelocity);
        //todo fancy algorithm for aiming
        plugin.logDebug("calculate Target solution ");
        for (int i=0; i<60; i++){
            Vector fvector = CannonsUtil.directionToVector(cannon.getAimingYaw(), cannon.getAimingPitch(), cannon.getCannonballVelocity());
            double diffY = simulateShot(fvector, cannon.getMuzzle(), target);
            plugin.logDebug("diff " + diffY);
            if (!cannon.getCannonDesign().isSentryIndirectFire()) {
                if (diffY < 0) {
                    cannon.setAimingPitch(cannon.getAimingPitch()-cannon.getCannonDesign().getAngleStepSize());
                    plugin.logDebug("new aiming pitch " + cannon.getAimingPitch());
                }
                else
                    return true;
            }
        }

        return false;
    }

    private double simulateShot(Vector vector, Location muzzle, Location target){
        MovingObject cannonball = new MovingObject(muzzle, vector);
        double target_distance = Math.sqrt(Math.pow(target.getX() - muzzle.getX(), 2)+Math.pow(target.getZ()-muzzle.getZ(),2));
        Vector oldLoc = null;
        for (int i=0; i<100; i++){
            cannonball.updateProjectileLocation(false);
            Vector cLoc = cannonball.getLoc();
            if (Math.sqrt(Math.pow(cLoc.getX() - muzzle.getX(), 2)+Math.pow(cLoc.getZ()-muzzle.getZ(),2)) > target_distance) {
                //calculate intersection
                plugin.logDebug("oldLoc " + oldLoc);
                if (oldLoc==null)
                    return cLoc.getY() - target.getY();
                Vector vel = cannonball.getVel().clone();
                double dist1 = vel.getX()+vel.getY()+vel.getZ();
                double dist2 = oldLoc.distance(target.toVector());
                Vector inter = oldLoc.add(vel.multiply(dist2 /dist1));
                plugin.logDebug("inter " + inter);
                return inter.getY() - target.getY();
            }
            oldLoc = cLoc.clone();
        }
        return -1000000000000.0;
    }


    /**
     * switches aming mode for this cannon
     * @param player - player in aiming mode
     * @param cannon - operated cannon
     */
	public void aimingMode(Player player, Cannon cannon, boolean fire)
	{
        if (player == null)
            return;

        boolean isAimingMode = inAimingMode.containsKey(player.getUniqueId());
		if (isAimingMode)
		{
            if (cannon == null)
                cannon = getCannonInAimingMode(player);

            //this player is already in aiming mode, he might fire the cannon or turn the aiming mode off
		    if (fire)
            {
                MessageEnum message = plugin.getFireCannon().playerFiring(cannon, player, InteractAction.fireAutoaim);
                userMessages.sendMessage(message, player, cannon);
            }
            else
            {
                //turn off the aiming mode
                MessageEnum message = disableAimingMode(player, cannon);
                userMessages.sendMessage(message, player, cannon);
            }
        }
        //enable aiming mode. Sentry cannons can't be opertated by players
		else if(cannon != null && !cannon.getCannonDesign().isSentry())
		{
			//check if player has permission to aim
			if (player.hasPermission(cannon.getCannonDesign().getPermissionAutoaim()))
			{
                //check distance before enabling the cannon
                if (distanceCheck(player, cannon))
                {
                    MessageEnum message = enableAimingMode(player, cannon);
                    userMessages.sendMessage(message, player, cannon);
                }
                else
                {
                    userMessages.sendMessage(MessageEnum.AimingModeTooFarAway, player, cannon);
                }

			}
			else
			{
				//no Permission to aim
				userMessages.sendMessage(MessageEnum.PermissionErrorAdjust, player, cannon);
			}
		}
	}

    /**
     * enable the aiming mode
     * @param player player how operates the cannon
     * @param cannon the cannon in aiming mode
     * @return message for the user
     */
    public MessageEnum enableAimingMode(Player player, Cannon cannon)
    {
        if (player == null)
            return null;

        //sentry can't be in aiming mode
        if (cannon == null || cannon.getCannonDesign().isSentry())
            return null;

        if (!player.hasPermission(cannon.getCannonDesign().getPermissionAutoaim()))
            return MessageEnum.PermissionErrorAutoaim;

        inAimingMode.put(player.getUniqueId(), cannon.getUID());

		cannon.addObserver(player, false);
		CannonsUtil.playSound(player.getEyeLocation(), cannon.getCannonDesign().getSoundEnableAimingMode());

        return MessageEnum.AimingModeEnabled;

    }


    /**
     * disables the aiming mode for this player
     * @param player - player in aiming mode
     * @return message for the player
     */
    public MessageEnum disableAimingMode(Player player)
    {
        //player.playSound(player.getEyeLocation(), Sound.MINECART_BASE, 0.25f, 0.75f);
        Cannon cannon = getCannonInAimingMode(player);
        if (cannon!=null)
            CannonsUtil.playSound(player.getEyeLocation(), cannon.getCannonDesign().getSoundDisableAimingMode());
        return disableAimingMode(player, cannon);
    }

        /**
         * disables the aiming mode for this player
         * @param player player in aiming mode
         * @param cannon operated cannon
         * @return message for the player
         */
	public MessageEnum disableAimingMode(Player player, Cannon cannon)
	{
        if (player == null)
            return null;

		if (inAimingMode.containsKey(player.getUniqueId()))
		{
			//player in map -> remove
			inAimingMode.remove(player.getUniqueId());

            if (cannon!=null)
            {
                cannon.removeObserver(player);
            }

            return MessageEnum.AimingModeDisabled;
		}
        return null;
	}

    /**
     * set a new aiming target for the given cannon
     * @param cannon operated cannon
     * @param loc new yaw and pitch angles
     */
    public void setAimingTarget(Cannon cannon, Location loc)
    {
        if (cannon == null)
            return;

        cannon.setAimingYaw(loc.getPitch());
        cannon.setAimingPitch(loc.getYaw());
    }
	
	/**
	 * finds the right message for the horizontal angle change
	 * @param cannon operated cannon
	 * @return message from the cannon
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
	 * @param cannon operated cannon
	 * @return message for the player
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
        if (player == null || cannon == null)
            return;

        // Imitation of angle
        if(config.isImitatedAimingEnabled() && isImitatingEnabled(player.getUniqueId()))
        {
            plugin.getFakeBlockHandler().imitateLine(player, cannon.getMuzzle(), cannon.getAimingVector(), 0,
                    config.getImitatedAimingLineLength(), config.getImitatedAimingMaterial(), FakeBlockType.AIMING, config.getImitatedAimingTime());
        }
    }

    public void toggleImitating(Player player)
    {
        if(!isImitatingEnabled(player.getUniqueId()))
        {
            enableImitating(player);
        }
        else
        {
            disableImitating(player);
        }
    }

    public void disableImitating(Player player){
        userMessages.sendMessage(MessageEnum.ImitatedEffectsDisabled, player);
        //it is enabled on default, adding to this list will stop the aiming effect
        imitatedEffectsOff.add(player.getUniqueId());
    }

    public boolean isImitatingEnabled(UUID playerUID){
        //it is enabled on default, adding to this list will stop the aiming effect
        return !imitatedEffectsOff.contains(playerUID);
    }

    public void enableImitating(Player player){
        userMessages.sendMessage(MessageEnum.ImitatedEffectsEnabled, player);
        //it is enabled on default, adding to this list will stop the aiming effect
        imitatedEffectsOff.remove(player.getUniqueId());
    }

    /**
     * updates the last time usage of the cannon
     * @param cannon operated cannon
     */
    public void updateLastAimed(Cannon cannon)
    {
        lastAimed.put(cannon.getUID(), System.currentTimeMillis());
    }

    /**
     * removes this cannon from the list of last time usage
     * @param cannon operated cannon
     */
    public void removeCannon(Cannon cannon)
    {
        lastAimed.remove(cannon.getUID());
    }

    /**
     * removes all entries of this player in this class
     * @param player player to remove
     */
    public void removePlayer(Player player)
    {
        disableAimingMode(player);
    }

    /**
     * removes the observer entry for this player in all cannons
     * @param player this player will be removed from the lists
     */
    public void removeObserverForAllCannons(Player player)
    {
        for (Cannon cannon : CannonManager.getCannonList().values())
        {
            cannon.removeObserver(player);
        }
        userMessages.sendMessage(MessageEnum.CannonObserverRemoved, player);

    }

    /**
     * calculated the impact of the projectile
     * @param cannon the cannon must be loaded with a projectile
     * @return the expected impact location
     */
    public Location impactPredictor(Cannon cannon)
    {
        if (cannon == null || cannon.getCannonballVelocity()<0.01 || !config.isImitatedPredictorEnabled() || !cannon.getCannonDesign().isPredictorEnabled())
            return null;

        Location muzzle = cannon.getMuzzle();
        Vector vel = cannon.getFiringVector(false, false);

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
        plugin.logDebug("impact predictor could not find the impact");
        return null;
    }

    /**
     *  impact effects will be only be shown if the cannon is not adjusted (aiming) for a while
     */
    public void updateImpactPredictor()
    {
        Iterator<Map.Entry<UUID, Long>> iter = lastAimed.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<UUID, Long> last = iter.next();
            Cannon cannon = CannonManager.getCannon(last.getKey());
            if (cannon == null) {
                iter.remove();
                continue;
            }

            CannonDesign design = cannon.getCannonDesign();
            if (last.getValue()+design.getPredictorDelay() < System.currentTimeMillis())
            {
                //reset the aiming so we have the do the next update after the update time
                last.setValue(last.getValue() - design.getPredictorDelay() + design.getPredictorUpdate());

                //find all the watching players
                HashMap<UUID, Boolean> nameList = cannon.getObserverMap();
                if ( nameList.isEmpty())
                {
                    //remove wrong entries and cannon with no observer (we don't need to update them)
                    iter.remove();
                    continue;
                }

                Location impact = impactPredictor(cannon);
                Iterator<Map.Entry<UUID, Boolean>> entry = nameList.entrySet().iterator();
                while(entry.hasNext())
                {
                    Map.Entry<UUID, Boolean> nextName = entry.next();
                    Player player = Bukkit.getPlayer(nextName.getKey());
                    //show impact to the player
                    if (player != null && impact != null && plugin.getFakeBlockHandler().belowMaxLimit(player, impact)) {
                        plugin.getFakeBlockHandler().imitatedSphere(player, impact, 1, config.getImitatedPredictorMaterial(), FakeBlockType.IMPACT_PREDICTOR, config.getImitatedPredictorTime());
                    }
                    //remove entry if there removeEntry enabled, or player is offline
                    if (nextName.getValue() || player == null)
                    {
                        plugin.logDebug("remove " + nextName.getKey() + " from observerlist");
                        entry.remove();
                    }
                }
            }
        }
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
        plugin.getFakeBlockHandler().imitatedSphere(player, surface, 1, config.getImitatedPredictorMaterial(), FakeBlockType.IMPACT_PREDICTOR, config.getImitatedPredictorTime());
        return surface;
    }

    /**
     * adds the cannon to the list of sentry guns. This cannons will be operate on its own.
     * @param cannonId cannon to add
     */
    public void addSentryCannon(UUID cannonId){
        if (cannonId!=null){
            sentryCannons.add(cannonId);
        }
    }

    /**
     * removes the cannon from the list of sentry guns. This cannons will be operate on its own.
     * @param cannonId cannon to add
     */
    public void removeSentryCannon(UUID cannonId){
        if (cannonId!=null){
            sentryCannons.remove(cannonId);
        }
    }

    /**
     * calculates the new solution for the sentry cannons
     * @param cannon sentry cannon
     * @param target target of the cannon
     * @return angles for the cannon
     */
    private gunAngles calctSentrySolution(Cannon cannon, Location target){
        return new gunAngles(0., 0.);
    }
}
