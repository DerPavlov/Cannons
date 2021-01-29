package at.pavlov.cannons.cannon;

import java.util.*;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.container.ItemHolder;
import at.pavlov.cannons.event.CannonUseEvent;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonsUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.util.Vector;

import at.pavlov.cannons.Enum.MessageEnum;
import at.pavlov.cannons.container.SimpleBlock;
import at.pavlov.cannons.utils.InventoryManagement;
import at.pavlov.cannons.projectile.Projectile;
import at.pavlov.cannons.sign.CannonSign;

public class Cannon
{
    // Database id - is -1 until stored in the database. Then it is the id in the
    // database
    private UUID databaseId;

    private String designID;
    private String cannonName;

    // direction the cannon is facing
    private BlockFace cannonDirection;
    // the location is describe by the offset of the cannon and the design
    private Vector offset;
    // world of the cannon
    private UUID world;
    // if the cannon is on a ship, the operation might be limited (e.g smaller angles to adjust the cannon)
    private boolean onShip;
    // with which velocity the canno is moving (set by other plugins)
    private Vector velocity;

    // time the cannon was last time fired
    private long lastFired;
    // time it was last aimed
    private long lastAimed;
    // it was loaded for the last time
    private long lastLoaded;
    // last time the sentry mode solution was updated
    private long lastSentryUpdate;

    // amount of loaded gunpowder
    private int loadedGunpowder;
    // the loaded projectile - can be null
    private Projectile loadedProjectile;
    // the projectile which was loaded previously
    private Projectile lastFiredProjectile;
    private int lastFiredGunpowder;

    // cleaning after firing (clicking with the stick several times
    private double soot;
    // pushing a projectile into the barrel after loading the projectile
    private int projectilePushed;

    // was the cannon fee paid
    private boolean paid;

    // angles
    private double horizontalAngle;
    private double verticalAngle;
    // additional angle if the cannon is mounted e.g. a ship which is facing a different angle
    private double additionalHorizontalAngle;
    private double additionalVerticalAngle;
    // sentry aiming angles the cannon tries to reach
    private double aimingPitch;
    private double aimingYaw;

    // tracking entity
    private UUID sentryEntity;
    // store older targets, so we do not target the same all the time
    private ArrayList<UUID> sentryEntityHistory;
    // how long this entity is targeted by this cannon
    private long sentryTargetingTime;
    // last time loading was tried and failed. Wait some time before trying again
    private long sentryLastLoadingFailed;
    // last time firing failed. Wait some time before trying again
    private long sentryLastFiringFailed;
    // return to default angles after firing
    private boolean sentryHomedAfterFiring;

    //target options for cannon
    private boolean targetMob;
    private boolean targetPlayer;
    private boolean targetCannon;
    private boolean targetOther;

    //observer will see the impact of the target predictor
    //<Player name, remove after showing impact>
    private HashMap<UUID, Boolean> observerMap = new HashMap<UUID, Boolean>();
    //a sentry cannon will not target a whitelisted player
    private HashSet<UUID> whitelist = new HashSet<UUID>();

    // player who has build this cannon
    private UUID owner;
    // designID of the cannon, for different types of cannons - not in use
    private boolean isValid;
    // time point of the last start of the firing sequence (used in combination with isFiring)
    private long lastIgnited;
    // the player which has used the cannon last, important for firing with redstone button
    private UUID lastUser;
    // the last player which was added or removed from the whitelist
    private UUID lastWhitelisted;
    // spread multiplier from the last operator of the cannon
    private double lastPlayerSpreadMultiplier;

    // cannon temperature
    private double tempValue;
    private long tempTimestamp;

    // amount of fired cannonballs with this cannon
    private long firedCannonballs;

    // has the cannon entry changed since it was last saved in the database
    private boolean updated;
    private boolean whitelistUpdated;

    private CannonDesign design;


    // not saved in the database
    // redstone handling event. Last player that pressed the firing button is saved in this list for the next redstone event
    private String firingButtonActivator;


    public Cannon(CannonDesign design, UUID world, Vector cannonOffset, BlockFace cannonDirection, UUID owner)
    {

        this.design = design;
        this.designID = design.getDesignID();
        this.world = world;
        this.offset = cannonOffset;
        this.cannonDirection = cannonDirection;
        this.owner = owner;
        this.isValid = true;
        this.cannonName = null;
        // ignore if there is no fee
        this.paid = design.getEconomyBuildingCost() <= 0;

        //the cannon is not moving
        this.velocity = new Vector(0, 0, 0);

        this.sentryEntity = null;
        sentryEntityHistory = new ArrayList<UUID>();
        sentryTargetingTime = 0;
        sentryLastLoadingFailed = 0;

        this.horizontalAngle = getHomeHorizontalAngle();
        this.verticalAngle = getHomeVerticalAngle();

        this.aimingPitch = 0.0;
        this.aimingYaw = 0.0;

        this.lastPlayerSpreadMultiplier = 1.0;

        // reset
        if (!design.isGunpowderNeeded() || design.isPreloaded())
            // this cannon will start loaded
            this.setLoadedGunpowder(design.getMaxLoadableGunpowderNormal());
        else
            this.setLoadedGunpowder(0);
        if (design.isPreloaded())
            this.setLoadedProjectile(this.getDefaultProjectile(this));
        else
            this.setLoadedProjectile(null);
        lastFiredProjectile = null;
        lastFiredGunpowder = 0;
        this.setSoot(design.getStartingSoot());
        this.setProjectilePushed(design.getProjectilePushing());

        // set temperature
        this.tempValue = 0.0;
        this.tempTimestamp = 0;
        
        this.targetMob = true;
        this.targetPlayer = false;
        this.targetCannon = false;

        this.databaseId = UUID.randomUUID();
        this.updated = true;
        this.whitelistUpdated = true;
    }


    /**
     * returns the location of the location of the cannon
     * @return location of the cannon
     */
    public Location getLocation()
    {
        return design.getAllCannonBlocks(this).get(0);
    }

    /**
     * returns the location of the muzzle
     * @return location of the muzzle
     */
    public Location getMuzzle()
    {
          return design.getMuzzle(this);
    }

    /**
     * returns a random block of the barrel or the cannon if there is no barrel
     * @return location of the barrel block
     */
    public Location getRandomBarrelBlock()
    {
        Random r = new Random();
        List<Location> barrel = design.getBarrelBlocks(this);
        if (barrel.size() > 0)
            return barrel.get(r.nextInt(barrel.size()));
        List<Location> all = design.getAllCannonBlocks(this);
        return all.get(r.nextInt(all.size()));
    }


    /**
     * removes the loaded charge form the chest attached to the cannon, returns true if the ammo was found in the chest
     * @param player - player operating the cannon
     * @param consumesAmmo - if true ammo will be removed from chest inventories
     * @return - true if the cannon has been reloaded. False if there is not enough ammunition
     */
    public MessageEnum reloadFromChests(UUID player, boolean consumesAmmo)
    {
        List<Inventory> invlist = getInventoryList();

        if (!isClean())
            return MessageEnum.ErrorNotCleaned;

        if (isLoading())
            return MessageEnum.ErrorLoadingInProgress;

        if (isFiring())
            return  MessageEnum.ErrorFiringInProgress;

        if (!isProjectilePushed() && isLoaded()){
            this.setProjectilePushed(0);
            return MessageEnum.RamrodPushingProjectileDone;
        }

        if (isProjectilePushed() && isLoaded())
            return MessageEnum.ErrorProjectileAlreadyLoaded;

        //load gunpowder if there is nothing in the barrel
        if (design.isGunpowderConsumption() && design.isGunpowderNeeded() && consumesAmmo)
        {
            //gunpowder will be consumed from the inventory
            //load the maximum gunpowder possible (maximum amount that fits in the cannon or is in the chest)
            int toLoad = design.getMaxLoadableGunpowderNormal() - getLoadedGunpowder();
            if (toLoad > 0)
            {
                ItemStack gunpowder = design.getGunpowderType().toItemStack(toLoad);
                Cannons.getPlugin().logDebug("Amount of chests next to cannon: " + invlist.size());
                gunpowder = InventoryManagement.removeItem(invlist, gunpowder);
                if (gunpowder.getAmount() == 0)
                {
                    //there was enough gunpowder in the chest
                    loadedGunpowder = design.getMaxLoadableGunpowderNormal();
                }
                else
                {
                    //not enough gunpowder, put it back
                    gunpowder.setAmount(toLoad-gunpowder.getAmount());
                    InventoryManagement.addItemInChests(invlist, gunpowder);
                    return MessageEnum.ErrorNoGunpowderInChest;
                }
            }
        }
        else {
            //no ammo consumption - only load if there is less gunpowder then normal in the barrel
            if (getLoadedGunpowder() <= design.getMaxLoadableGunpowderNormal())
                loadedGunpowder = design.getMaxLoadableGunpowderNormal();
        }
        // find a loadable projectile in the chests
        for (Inventory inv : invlist)
        {
            for (ItemStack item : inv.getContents())
            {
                Projectile projectile = ProjectileStorage.getProjectile(this, item);
                if (projectile == null)
                    continue;

                MessageEnum message = CheckPermProjectile(projectile, player);
                if (message == MessageEnum.loadProjectile)
                {
                    // everything went fine, so remove it from the chest remove projectile
                    setLoadedProjectile(projectile);
                    if(design.isProjectileConsumption()&&consumesAmmo)
                    {
                        if (item.getAmount() == 1)
                        {
                            //last item removed
                            inv.removeItem(item);
                        }
                        else
                        {
                            //remove one item
                            item.setAmount(item.getAmount() - 1);
                        }
                    }
                    //push projectile and done
                    setProjectilePushed(0);
                    CannonsUtil.playSound(getMuzzle(), getLoadedProjectile().getSoundLoading());
                    lastLoaded = System.currentTimeMillis();
                    return MessageEnum.loadProjectile;
                }
            }
        }
        return MessageEnum.ErrorNoProjectileInChest;
    }

    /**
     * removes cooling item form the chest attached to the cannon, returns true if it was enough to cool down the cannon
     * @return - true if the cannon has been cooled down
     */
    public boolean automaticCoolingFromChest()
    {

        List<Inventory> invlist = getInventoryList();

        //cooling items will be consumed from the inventory
        int toCool = (int) Math.ceil((this.getTemperature() - design.getWarningTemperature())/design.getCoolingAmount());
        ItemStack item = new ItemStack(Material.AIR, toCool);

        if (toCool <= 0)
            return true;

        //do this for every cooling item
        for (ItemHolder mat : design.getItemCooling())
        {
            item = mat.toItemStack(item.getAmount());
            item = InventoryManagement.removeItem(invlist, item);


            int usedItems= toCool - item.getAmount();
            this.setTemperature(this.getTemperature()-usedItems*design.getCoolingAmount());

            //put used items back to the chest (not if the item is AIR)
            ItemStack itemUsed = design.getCoolingToolUsed(item);
            itemUsed.setAmount(usedItems);
            if (!itemUsed.getType().equals(Material.AIR))
                InventoryManagement.addItemInChests(invlist, itemUsed);

            //if all items have been removed we are done
            if (item.getAmount() == 0)
                return true;
        }
        return false;
    }


    /**
     * returns the inventories of all attached chests
     * @return - list of inventory
     */
    List<Inventory> getInventoryList()
    {
        //get the inventories of all attached chests
        List<Inventory> invlist = new ArrayList<Inventory>();
        for (Location loc : getCannonDesign().getChestsAndSigns(this))
        {
            // check if block is a chest
            invlist = InventoryManagement.getInventories(loc.getBlock(), invlist);
        }
        return invlist;
    }


    /**
     * loads Gunpowder in a cannon
     * @param amountToLoad - number of items which are loaded into the cannon
     */
    MessageEnum loadGunpowder(int amountToLoad)
    {
        //this cannon does not need gunpowder
        if (!design.isGunpowderNeeded())
            return MessageEnum.ErrorNoGunpowderNeeded;
        // this cannon needs to be cleaned first
        if (!isClean())
            return MessageEnum.ErrorNotCleaned;
        if (isLoading())
            return MessageEnum.ErrorLoadingInProgress;
        if (isFiring())
            return MessageEnum.ErrorFiringInProgress;
        //projectile pushing necessary
        if (isLoaded()&&!isProjectilePushed())
            return MessageEnum.ErrorNotPushed;
        // projectile already loaded
        if (isLoaded())
            return MessageEnum.ErrorProjectileAlreadyLoaded;
        // maximum gunpowder already loaded
        if (getLoadedGunpowder() >= design.getMaxLoadableGunpowderOverloaded())
            return MessageEnum.ErrorMaximumGunpowderLoaded;

        //load the maximum gunpowder
        setLoadedGunpowder(getLoadedGunpowder() + amountToLoad);

        // update Signs
        updateCannonSigns();
        
        if (getLoadedGunpowder() > design.getMaxLoadableGunpowderOverloaded())
            setLoadedGunpowder(design.getMaxLoadableGunpowderOverloaded());


        //Overloading is enabled
        if(design.isOverloadingEnabled())
        {
        	if(!design.isOverloadingRealMode())
        	{
                if(design.getMaxLoadableGunpowderNormal()<getLoadedGunpowder())
                	return MessageEnum.loadOverloadedGunpowder;
                else if(design.getMaxLoadableGunpowderNormal()==getLoadedGunpowder())
                	return MessageEnum.loadGunpowderNormalLimit;
        	}
        	else if(design.getMaxLoadableGunpowderNormal()<getLoadedGunpowder())
                return MessageEnum.loadOverloadedGunpowder;

        }
        return MessageEnum.loadGunpowder;
    }


    /**
     * checks the permission of a player before loading gunpowder in the cannon. Designed for player operation
     * @param player - the player which is loading the cannon
     */
    public MessageEnum loadGunpowder(Player player)
    {

        //fire event
        CannonUseEvent useEvent = new CannonUseEvent(this, player.getUniqueId(), InteractAction.loadGunpowder);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;


        //save the amount of gunpowder we loaded in the cannon
        int gunpowder = 0;
        int maximumLoadableNormal = design.getMaxLoadableGunpowderNormal() - getLoadedGunpowder();
        int maximumLoadableAbsolute = design.getMaxLoadableGunpowderOverloaded() - getLoadedGunpowder();

        //check if the player has permissions for this cannon
        MessageEnum returnVal = CheckPermGunpowder(player);

        //the player seems to have all rights to load the cannon.
        if (returnVal.equals(MessageEnum.loadGunpowder))
        {
            //if the player is sneaking the maximum gunpowder is loaded, but at least 1
            if(player.isSneaking()) {
                    gunpowder = player.getInventory().getItemInMainHand().getAmount();
                    if (maximumLoadableNormal < gunpowder) gunpowder = maximumLoadableNormal;
                    else if (maximumLoadableAbsolute < gunpowder) gunpowder = 1;
            }
            if (gunpowder <= 0)
                gunpowder = 1;
            if (design.isAutoloadChargeWhenLoadingProjectile()) {
                //get the amount of gunpowder that can be maximal loaded
                if(player.getInventory().contains(design.getGunpowderType().getType(), maximumLoadableNormal))
                    gunpowder = maximumLoadableNormal;
                else
                    gunpowder = 0;
                System.out.println("[Cannons] gunpowder: " + gunpowder);
            }

            //load the gunpowder
            returnVal = loadGunpowder(gunpowder);
        }

        // the cannon was loaded with gunpowder - lets get it form the player
        switch(returnVal)
        {
	        case loadGunpowder:
	        {
	        	// take item from the player
                CannonsUtil.playSound(getMuzzle(), design.getSoundGunpowderLoading());
	            if (design.isGunpowderConsumption()&&!design.isAmmoInfiniteForPlayer())
                    if (design.isGunpowderConsumption()&&!design.isAmmoInfiniteForPlayer())
                        InventoryManagement.removeItem(player.getInventory(), design.getGunpowderType().toItemStack(gunpowder));
                    else
	                    InventoryManagement.takeFromPlayerHand(player, gunpowder);
	        	break;
	        }
	        case loadGunpowderNormalLimit:
	        {
                CannonsUtil.playSound(getMuzzle(), design.getSoundGunpowderLoading());
	            if (design.isGunpowderConsumption()&&!design.isAmmoInfiniteForPlayer())
                    if (design.isGunpowderConsumption()&&!design.isAmmoInfiniteForPlayer())
                        InventoryManagement.removeItem(player.getInventory(), design.getGunpowderType().toItemStack(gunpowder));
                    else
	                    InventoryManagement.takeFromPlayerHand(player, gunpowder);
	        	break;
	        }
	        case loadOverloadedGunpowder:
	        {
                CannonsUtil.playSound(getMuzzle(), design.getSoundGunpowderOverloading());
	            if (design.isGunpowderConsumption()&&!design.isAmmoInfiniteForPlayer())
	                InventoryManagement.takeFromPlayerHand(player, gunpowder);
	        	break;
	        }
	        default:
	        {
	            CannonsUtil.playErrorSound(getMuzzle());
	        }
        }
        return returnVal;

    }

    /**
     * load the projectile in the cannon and checks permissions. Designed for player operation
     * @param player - who is loading the cannon
     * @return - a message which can be displayed
     */
    public MessageEnum loadProjectile(Projectile projectile, Player player)
    {
        //fire event
        CannonUseEvent useEvent = new CannonUseEvent(this, player.getUniqueId(), InteractAction.loadProjectile);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;

        if (projectile == null) return null;

        MessageEnum returnVal;

        // autoload gunpowder
        if (design.isAutoloadChargeWhenLoadingProjectile()) {
            returnVal = loadGunpowder(player);
            //return error if loading of gunpowder was not successful
            if (!(returnVal.equals(MessageEnum.loadGunpowder)|| returnVal.equals(MessageEnum.loadGunpowderNormalLimit) || returnVal.equals(MessageEnum.loadOverloadedGunpowder)))
                return returnVal;
        }

        returnVal = CheckPermProjectile(projectile, player);

        // check if loading of projectile was successful
        if (returnVal.equals(MessageEnum.loadProjectile) || returnVal.equals(MessageEnum.loadGunpowderAndProjectile))
        {
            // load projectile
            setLoadedProjectile(projectile);
            CannonsUtil.playSound(getMuzzle(), projectile.getSoundLoading());

            // remove from player
            if (design.isProjectileConsumption()&&!design.isAmmoInfiniteForPlayer())
                InventoryManagement.takeFromPlayerHand(player,1);

            // update Signs
            updateCannonSigns();
        }
        else
        {
            //projectile not loaded
            CannonsUtil.playErrorSound(getMuzzle());
        }


        return returnVal;
    }

    /**
     * Returns the default projectile for this cannon (first entry).
     * @return default Projectile
     */
    public Projectile getDefaultProjectile(Cannon cannon){
        if (this.getCannonDesign().getAllowedProjectiles().size() > 0)
            return ProjectileStorage.getProjectile(cannon, this.getCannonDesign().getAllowedProjectiles().get(0));
        return null;
    }

    /**
     * Check if cannons can be loaded with gunpowder by the player
     *
     * @param player - check permissions of this player
     * @return - true if the cannon can be loaded
     */
    private MessageEnum CheckPermGunpowder(Player player)
    {

        if (player != null)
        {
            //if the player is not the owner of this gun
            if (design.isAccessForOwnerOnly() && this.getOwner()!=null && !this.getOwner().equals(player.getUniqueId()))
                return MessageEnum.ErrorNotTheOwner;
            // player can't load cannon
            if (!player.hasPermission(design.getPermissionLoad()))
                return MessageEnum.PermissionErrorLoad;
        }
        // loading successful
        return MessageEnum.loadGunpowder;
    }

    /**
     * Check the if the cannons can be loaded
     *
     * @param playerUid - whose permissions are checked
     * @return true if the player and cannons can load the projectile
     */
    private MessageEnum CheckPermProjectile(Projectile projectile, UUID playerUid) {
        return CheckPermProjectile(projectile, Bukkit.getPlayer(playerUid));
    }

    /**
     * Check the if the cannons can be loaded
     *
     * @param player - whose permissions are checked
     * @return true if the player and cannons can load the projectile
     */
    private MessageEnum CheckPermProjectile(Projectile projectile, Player player)
    {
        if (player != null)
        {
            //if the player is not the owner of this gun
            if (this.getOwner()!=null && !this.getOwner().equals(player.getUniqueId()) && design.isAccessForOwnerOnly())
                return MessageEnum.ErrorNotTheOwner;
            // no permission for this projectile
            if (!projectile.hasPermission(player))
                return MessageEnum.PermissionErrorProjectile;
        }
        // no gunpowder loaded
        if (!isGunpowderLoaded())
            return MessageEnum.ErrorNoGunpowder;
        if (isLoading())
            return MessageEnum.ErrorLoadingInProgress;
        if (isFiring())
            return MessageEnum.ErrorFiringInProgress;
        // already loaded with a projectile
        if (isLoaded())
            return MessageEnum.ErrorProjectileAlreadyLoaded;
        // is cannon cleaned with ramrod?
        if (!isClean())
            return MessageEnum.ErrorNotCleaned;

        // loading successful
        if (design.isAutoloadChargeWhenLoadingProjectile())
            return MessageEnum.loadGunpowderAndProjectile;
        return MessageEnum.loadProjectile;
    }

    /**
     * Permission check and usage for ram rod
     * @param player player using the ramrod tool (null will bypass permission check)
     * @return message for the player
     */
    private MessageEnum useRamRodInteral(Player player)
    {
        //no permission to use this tool
        if (player!=null && !player.hasPermission(design.getPermissionRamrod()))
            return MessageEnum.PermissionErrorRamrod;
        //if the player is not the owner of this gun
        if (player!=null && this.getOwner()!=null && !this.getOwner().equals(player.getUniqueId()) && design.isAccessForOwnerOnly())
            return MessageEnum.ErrorNotTheOwner;
        if (isLoading())
            return MessageEnum.ErrorLoadingInProgress;
        if (isFiring())
            return MessageEnum.ErrorFiringInProgress;
        //if the barrel is dirty clean it
        if (!isClean())
        {
            cleanCannon(1);
            if (isClean())
                return MessageEnum.RamrodCleaningDone;
            else
                return MessageEnum.RamrodCleaning;
        }
        //if clean show message that cleaning is done
        if (isClean() && !isGunpowderLoaded())
        {
            cleanCannon(1);
            return MessageEnum.RamrodCleaningDone;
        }
        //if no projectile
        if (!isLoaded())
            return MessageEnum.ErrorNoProjectile;
        //if the projectile is loaded
        if (!isProjectilePushed())
        {
            pushProjectile(1);
            if (isProjectilePushed())
                return MessageEnum.RamrodPushingProjectileDone;
            else
                return MessageEnum.RamrodPushingProjectile;
        }
        //if projectile is in place
        if (isLoaded() && isProjectilePushed())
            return MessageEnum.ErrorProjectileAlreadyLoaded;

        //no matching case found
        return null;

    }

    /**
     * a ramrod is used to clean the barrel before loading gunpowder and to push the projectile into the barrel
     * @param player player using the ramrod tool (null will bypass permission check)
     * @return message for the player
     */
	public MessageEnum useRamRod(Player player)
    {
        MessageEnum message = useRamRodInteral(player);
        if(message != null)
        {
            if(message.isError()) CannonsUtil.playErrorSound(getMuzzle());
            else switch(message)
            {
                case RamrodCleaning:
                {
                    CannonsUtil.playSound(getMuzzle(), design.getSoundRamrodCleaning());
                    break;
                }
                case RamrodCleaningDone:
                {
                    CannonsUtil.playSound(getMuzzle(), design.getSoundRamrodCleaningDone());
                    break;
                }
                case RamrodPushingProjectile:
                {
                    CannonsUtil.playSound(getMuzzle(), design.getSoundRamrodPushing());
                    break;
                }
                case RamrodPushingProjectileDone:
                {
                    CannonsUtil.playSound(getMuzzle(), design.getSoundRamrodPushingDone());
                    break;
                }
                default:
                    CannonsUtil.playErrorSound(getMuzzle());
            }
        }
        return message;
    }

    /**
     * is cannon loaded return true
     * @return - true if the cannon is loaded with a projectile and gunpowder
     */
    public boolean isLoaded()
    {
        return isProjectileLoaded()&&isGunpowderLoaded()&&!isLoading();
    }

    /**
     * is the cannon loaded with a projectile
     * @return - true if there is a projectile in the cannon
     */
    public boolean isProjectileLoaded()
    {
        return (loadedProjectile != null);
    }

    /**
     * returns true if the cannon has at least 1 gunpowder loaded
     * @return true if loaded with gunpowder
     */
    public boolean isGunpowderLoaded()
    {
        return getLoadedGunpowder()>0 || !design.isGunpowderNeeded();
    }

    /**
     * removes gunpowder and the projectile. Items are drop at the cannonball firing point
     */
    private void dropCharge()
    {
        //drop gunpowder
        if (loadedGunpowder > 0 && design.isGunpowderNeeded())
        {
            ItemStack powder = design.getGunpowderType().toItemStack(loadedGunpowder);
            getWorldBukkit().dropItemNaturally(design.getMuzzle(this), powder);
        }

        // drop projectile
        if (isLoaded())
        {
            getWorldBukkit().dropItemNaturally(design.getMuzzle(this), loadedProjectile.getLoadingItem().toItemStack(1));
        }
        removeCharge();

    }

    /**
     * removes the gunpowder and projectile loaded in the cannon
     */
    public void removeCharge()
    {
        lastFiredProjectile = this.getLoadedProjectile();
        lastFiredGunpowder = this.getLoadedGunpowder();
        //delete charge for human gunner
        if (design.isGunpowderNeeded())
            this.setLoadedGunpowder(0);
        this.setLoadedProjectile(null);

        //update Sign
        this.updateCannonSigns();
    }

    /**
     * removes the sign text and charge of the cannon after destruction
     * @param breakBlocks break all cannon block naturally
     * @param canExplode if the cannon can explode when loaded with gunpoweder
     * @param cause cause of the cannon destruction
     */
    public MessageEnum destroyCannon(boolean breakBlocks, boolean canExplode, BreakCause cause)
    {
        // update cannon signs the last time
        isValid = false;
        updateCannonSigns();

        if (breakBlocks)
            breakAllCannonBlocks();

        //loaded cannon can exploded (80% chance)
        if (canExplode && design.getExplodingLoadedCannons() > 0 && getLoadedGunpowder() > 0 && Math.random() > 0.2)
        {
            double power = getLoadedGunpowder()/design.getMaxLoadableGunpowderNormal()*design.getExplodingLoadedCannons();
            World world = getWorldBukkit();
            if (world != null)
                //todo fix overheating
                world.createExplosion(getRandomBarrelBlock(),(float) power);
        }
        // drop charge
        else
            dropCharge();

        // return message
        switch (cause)
        {
            case Overheating:
                return MessageEnum.HeatManagementOverheated;
            case Other:
                return null;
            case Dismantling:
                return MessageEnum.CannonDismantled;
            default:
                return MessageEnum.CannonDestroyed;
        }
    }

    /**
     * this will force the cannon to show up at this location - all blocks will be overwritten
     */
    public void show()
    {
        for (SimpleBlock cBlock : design.getAllCannonBlocks(this.getCannonDirection()))
        {
            Block wBlock = cBlock.toLocation(getWorldBukkit(), offset).getBlock();
            //todo check show
            wBlock.setBlockData(cBlock.getBlockData());
            //wBlock.setBlockData(cBlock);
        }
    }

    /**
     * this will force the cannon blocks to become AIR
     */
    public void hide()
    {
        //remove only attachable block
        for (SimpleBlock cBlock : design.getAllCannonBlocks(this.getCannonDirection()))
        {
            Block wBlock = cBlock.toLocation(getWorldBukkit(), offset).getBlock();
            //if that block is not loaded
            if (wBlock == null) return;

            if (wBlock.getState() instanceof Attachable)
            {
                //System.out.println("hide " + wBlock.getType());
                wBlock.setType(Material.AIR);
                //wBlock.setData((byte) 0, false);
            }
        }

        //remove all
        for (SimpleBlock cBlock : design.getAllCannonBlocks(this.getCannonDirection()))
        {
            Block wBlock = cBlock.toLocation(getWorldBukkit(), offset).getBlock();

            if (wBlock.getType() != Material.AIR)
            {
                wBlock.setType(Material.AIR);
               // wBlock.setData((byte) 0, false);
            }
        }
    }


    /**
     * breaks all cannon blocks of the cannon
     */
    private void breakAllCannonBlocks()
    {
        List<Location> locList = design.getAllCannonBlocks(this);
        for (Location loc : locList)
        {
            loc.getBlock().breakNaturally();
        }
    }


    /**
     * returns true if this block is a block of the cannon
     * @param block - block to check
     * @return - true if it is part of this cannon
     */
    public boolean isCannonBlock(Block block)
    {
        if (getWorld().equals(block.getWorld().getUID())){
            for (SimpleBlock designBlock : design.getAllCannonBlocks(cannonDirection))
            {
                if (designBlock.compareMaterialAndLoc(block, offset))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * return true if this block can be destroyed, false if it is protected
     * @param block - location of the block
     * @return - true if the block can be destroyed
     */
    public boolean isDestructibleBlock(Location block)
    {
        for (Location loc : design.getDestructibleBlocks(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this block is a part of the loading interface - default is
     * the barrel the barrel
     *
     * @param block
     * @return
     */
    public boolean isLoadingBlock(Location block)
    {
        for (Location loc : design.getLoadingInterface(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * return true if this location where the torch interacts with the cannon
     *
     * @param block
     * @return
     */
    public boolean isChestInterface(Location block)
    {
        for (Location loc : design.getChestsAndSigns(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this location where the torch interacts with the cannon
     * does not check the ID
     *
     * @param loc
     * @return
     */
    public boolean isCannonSign(Location loc)
    {
        if (!(loc.getBlock().getBlockData() instanceof WallSign)) {
            return false;
        }

        CannonBlocks cannonBlocks  = this.getCannonDesign().getCannonBlockMap().get(this.getCannonDirection());
        if (cannonBlocks != null)
        {
            for (SimpleBlock cannonblock : cannonBlocks.getChestsAndSigns())
            {
                // compare location
                if (cannonblock.toLocation(this.getWorldBukkit(),this.offset).equals(loc))
                {
                    //Block block = loc.getBlock();
                    //compare and data
                    //only the two lower bits of the bytes are important for the direction (delays are not interessting here)
                    //todo check facing
                    //if (cannonblock.getData() == block.getData() || block.getData() == -1 || cannonblock.getData() == -1 )
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * return true if this is a right click trigger block
     *
     * @param block
     * @return
     */
    public boolean isRightClickTrigger(Location block)
    {
        for (Location loc : design.getRightClickTrigger(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this is a redstone trigger block
     *
     * @param block
     * @return
     */
    public boolean isRestoneTrigger(Location block)
    {
        for (Location loc : design.getRedstoneTrigger(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this location where the torch interacts with the cannon
     *
     * @param block
     * @return
     */
    public boolean isRedstoneTorchInterface(Location block)
    {
        for (Location loc : design.getRedstoneTorches(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this location where the torch interacts with the cannon
     *
     * @param block
     * @return
     */
    public boolean isRedstoneWireInterface(Location block)
    {
        for (Location loc : design.getRedstoneWireAndRepeater(this))
        {
            if (loc.equals(block))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * return true if this location where the torch interacts with the cannon
     * does not check the ID
     *
     * @param loc
     * @return
     */
    public boolean isRedstoneRepeaterInterface(Location loc)
    {
        CannonBlocks cannonBlocks  = this.getCannonDesign().getCannonBlockMap().get(this.getCannonDirection());
        if (cannonBlocks != null)
        {
            for (SimpleBlock cannonblock : cannonBlocks.getRedstoneWiresAndRepeater())
            {
                // compare location
                if (cannonblock.toLocation(this.getWorldBukkit(),this.offset).equals(loc))
                {
                    Block block = loc.getBlock();
                    //compare and data
                    //todo check facing
                    //only the two lower bits of the bytes are important for the direction (delays are not interessting here)
                    //if (cannonblock.getData() == block.getData() %4 || block.getData() == -1 || cannonblock.getData() == -1 )
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * returns true if the sentry is in automatic mode, false if in manual mode
     * @return true if sentry is in automatic mode
     */
    public boolean isSentryAutomatic(){
        for (Location loc : design.getBarrelBlocks(this)){
            if (loc.getBlock().isBlockIndirectlyPowered())
                return false;
        }
        return true;
    }

    /**
     * returns the first block of the cannon
     * @return - first block of the cannon
     */
    public Location getFirstCannonBlock()
    {
        return design.getAllCannonBlocks(cannonDirection).get(0).toLocation(getWorldBukkit(), offset);

    }

    /**
     * returns true if the player has the permission to place redstone near a cannon.
     * player = null will also return true
     * @param player player operating the cannon
     * @return
     */
    public MessageEnum checkRedstonePermission(UUID player)
    {
        Player playerBukkit = null;
        if (player != null) playerBukkit = Bukkit.getPlayer(player);
        return checkRedstonePermission(playerBukkit);
    }

    /**
     * checks if the player has permission to use the cannon with redstone
     * @return message for the player
     */
    MessageEnum checkRedstonePermission(Player player)
    {
        // the player is null means he is offline -> automatic handling like
        // database check
        if (player == null) return MessageEnum.CannonCreated;
        // if the player has the permission to use redstone return
        if (player.hasPermission(design.getPermissionRedstone())) return MessageEnum.CannonCreated;

        // torch
        for (Location loc : design.getRedstoneTorches(this))
        {
            Material b = loc.getBlock().getType();
            if (b == Material.REDSTONE_TORCH )
            {
                removeRedstone();
                return MessageEnum.PermissionErrorRedstone;
            }
        }

        // wire
        for (Location loc : design.getRedstoneWireAndRepeater(this))
        {
            Material b = loc.getBlock().getType();
            if (b == Material.REDSTONE_WIRE || b == Material.REPEATER || b == Material.COMPARATOR)
            {
                removeRedstone();
                return MessageEnum.PermissionErrorRedstone;
            }
        }

        // no redstone wiring found
        return MessageEnum.CannonCreated;
    }

    /**
     * break all redstone connections to this cannon
     */
    private void removeRedstone()
    {
        // torches
        for (Location loc : design.getRedstoneTorches(this))
        {
            Block block = loc.getBlock();
            if (block.getType() == Material.REDSTONE_TORCH)
            {
                block.breakNaturally();
            }
        }

        // wires and repeater
        for (Location loc : design.getRedstoneWireAndRepeater(this))
        {
            Block block = loc.getBlock();
            if (block.getType() == Material.REDSTONE_WIRE || block.getType() == Material.REPEATER || block.getType() == Material.COMPARATOR)
            {
                block.breakNaturally();
            }
        }
    }

    /**
     * updates the location of the cannon
     * @param moved - how far the cannon has been moved
     */
    public void move(Vector moved)
    {
        offset.add(moved);
        this.hasUpdated();
    }

    /**
     * updates the rotation of the cannon
     * @param center - center of the rotation
     * @param angle - how far the cannon is rotated in degree (90, 180, 270, -90)
     */
    public void rotate(Vector center, int angle)
    {
        if (angle == 0)
            return;

        double dAngle =  angle*Math.PI/180;

        center = new Vector (center.getBlockX(), center.getBlockY(), center.getBlockZ());

        Vector diffToCenter = offset.clone().subtract(center);

        double newX = diffToCenter.getX()*Math.cos(dAngle) - diffToCenter.getZ()*Math.sin(dAngle);
        double newZ = diffToCenter.getX()*Math.sin(dAngle) + diffToCenter.getZ()*Math.cos(dAngle);

        offset = new Vector(Math.round(center.getX()+newX), offset.getBlockY(), Math.round(center.getZ()+newZ));

        //rotate blockface
        if (angle > 0)
        {
            for (int i = 0; i<=angle%90; i++)
                cannonDirection = CannonsUtil.roatateFace(cannonDirection);
        }
        else
        {
            for (int i = 0; i<=(-angle)%90; i++)
                cannonDirection = CannonsUtil.roatateFaceOpposite(cannonDirection);
        }
        this.hasUpdated();

    }

    /**
     * updates the rotation of the cannon by rotating it 90 to the right
     * @param center - center of the rotation
     */
    public void rotateRight(Vector center)
    {
        this.rotate(center, 90);
    }

    /**
     * updates the rotation of the cannon by rotating it 90 to the left
     * @param center - center of the rotation
     */
    public void rotateLeft(Vector center)
    {
        this.rotate(center, -90);
    }

    /**
     * updates the rotation of the cannon by rotating it 180
     * @param center - center of the rotation
     */
    public void rotateFlip(Vector center)
    {
        this.rotate(center, 180);
    }

    /**
     * get the change for a barrel explosion due to overheating
     * @return chance for a barrel explosion
     */
    public double getOverheatingChance()
    {
        if (!design.isHeatManagementEnabled())
            return 0.0;

        double tempCannon = this.getTemperature();
        double tempCritical = design.getCriticalTemperature();
        double tempMax = design.getMaximumTemperature();
        double explodingProbability = 0.0;

        if (tempCannon > tempCritical)
        {
            //no exploding chance for temperature < critical, 100% chance for > maximum
            explodingProbability = Math.pow((tempCannon-tempCritical)/(tempMax-tempCritical),3);
        }
        return explodingProbability;
    }

    /**
     * Checks the cannon if the actual temperature might destroy the cannon
     * @return true if the cannon will explode
     */
    public boolean checkHeatManagement()
    {
        double explodingProbability = getOverheatingChance();
        //play some effects for a hot barrel
        if (getTemperature() > design.getCriticalTemperature())
            this.playBarrelSmokeEffect((int)(explodingProbability*20.0+1));
        return Math.random()<explodingProbability;
    }

    /**
     * plays the given effect on random locations of the barrel
     * @param amount - number of effects
     */
    void playBarrelSmokeEffect(int amount)
    {
        if (amount <= 0)
            return;

        Random r = new Random();
        List<Location> barrelList = design.getBarrelBlocks(this);

        //if the barrel list is 0 something is completely odd
        int max = barrelList.size();
        if (max < 0)
            return;

        Location effectLoc;
        BlockFace face;

        for (int i=0; i<amount; i++)
        {
            //grab a random face and find a block for them the adjacent block is AIR
            face = CannonsUtil.randomBlockFaceNoDown();
            int j = 0;
            do
            {
                i++;
                effectLoc = barrelList.get(r.nextInt(max)).getBlock().getRelative(face).getLocation();
            } while (i<4 && effectLoc.getBlock().getType() != Material.AIR);

            effectLoc.getWorld().playEffect(effectLoc, Effect.SMOKE, face);
            //effectLoc.getWorld().playSound(effectLoc, Sound.FIZZ, 1, 1);
            CannonsUtil.playSound(effectLoc, design.getSoundHot());
        }
    }

    /**
     * cools down a cannon by using the item in hand of a player
     * @param player player using the cannon
     */
    public boolean coolCannon(Player player)
    {
        int toCool = (int) Math.ceil((this.getTemperature() - design.getWarningTemperature())/design.getCoolingAmount());
        if (toCool <= 0)
            return false;

        //if the player is sneaking the maximum gunpowder is loaded, but at least 1
        int amount = 1;
        if (player.isSneaking())
        {
            //get the amount of gunpowder that can be maximal loaded
            amount = player.getInventory().getItemInMainHand().getAmount();
            if (amount > toCool)
                amount = toCool;
        }

        setTemperature(getTemperature()-design.getCoolingAmount()*amount);

        ItemStack newItem = design.getCoolingToolUsed(player.getInventory().getItemInMainHand());
        //remove only one item if the material is AIR else replace the item (e.g. water bucket with a bucket)
        if (newItem.getType().equals(Material.AIR))
            InventoryManagement.takeFromPlayerHand(player, 1);
        else
            player.getInventory().setItemInMainHand(newItem);

        return true;
    }

    /**
     * cools down a cannon by using the item in hand of a player
     * @param player player using the cannon
     * @param effectLoc location of the smoke effects
     */
    public boolean coolCannon(Player player, Location effectLoc)
    {
        boolean cooled = coolCannon(player);
        if (cooled && effectLoc !=null && getTemperature() > design.getWarningTemperature())
        {
            effectLoc.getWorld().playEffect(effectLoc, Effect.SMOKE, BlockFace.UP);
            //effectLoc.getWorld().playSound(effectLoc, Sound.FIZZ, 1, 1);
            CannonsUtil.playSound(effectLoc, design.getSoundCool());
        }
        return cooled;
    }


    /**
     * return the firing vector of the cannon. The spread depends on the cannon, the projectile and the player
     * @param addSpread if there is spread added to the firing vector
     * @param usePlayerSpread if additional spread of the player will be added
     * @return firing vector
     */
    public Vector getFiringVector(boolean addSpread, boolean usePlayerSpread)
    {
        if (lastFiredProjectile == null && loadedProjectile == null)
            return new Vector(0, 0, 0);
        Projectile projectile = loadedProjectile;
        if (projectile == null)
            projectile = lastFiredProjectile;

        Random r = new Random();

        double playerSpread = 1.0;
        if (usePlayerSpread)
            playerSpread = getLastPlayerSpreadMultiplier();

        final double spread = design.getSpreadOfCannon() * projectile.getSpreadMultiplier()*playerSpread;
        double deviation = 0.0;

        if (addSpread)
            deviation = r.nextGaussian() * spread;
        double h = (getTotalHorizontalAngle() + deviation + CannonsUtil.directionToYaw(cannonDirection));

        if (addSpread)
            deviation = r.nextGaussian() * spread;
        double v = (-getTotalVerticalAngle() + deviation);

        double multi = getCannonballVelocity();
        if (multi < 0.1) multi = 0.1;

        double randomness = 1.0;
        if (addSpread)
            randomness = (1.0 + r.nextGaussian()*spread/180.0);
        return CannonsUtil.directionToVector(h, v, multi*randomness);
    }

    /**
     * returns the vector the cannon is currently aiming
     * @return vector the cannon is aiming
     */
    public Vector getAimingVector()
    {
        double multi = getCannonballVelocity();
        if (multi < 0.1)
            multi = 0.1;

        return CannonsUtil.directionToVector(getTotalHorizontalAngle()  + CannonsUtil.directionToYaw(cannonDirection), -getTotalVerticalAngle(), multi);
    }

    /**
     * returns the vector the cannon is currently targeting
     * @return targeting vector
     */
    public Vector getTargetVector()
    {
        double multi = getCannonballVelocity();
        if (multi < 0.1)
            multi = 0.1;

        return CannonsUtil.directionToVector(getAimingYaw(), getAimingPitch(), multi);
    }

    /**
     * etracts the spreadMultiplier from the permissions
     * @param player player operating the cannon
     * @return spread multiplier
     */
    private double getPlayerSpreadMultiplier(Player player)
    {
        if (player == null) return 1.0;


        // only if the permissions system is enabled. If there are no permissions, everything is true.
        if (!player.hasPermission( this.getCannonDesign().getPermissionSpreadMultiplier() + "." + Integer.MAX_VALUE))
        {
            // search if there is a valid entry
            for (int i = 1; i <= 10; i++)
            {
                if (player.hasPermission( this.getCannonDesign().getPermissionSpreadMultiplier() + "." + i))
                {
                    return i/10.0;
                }
            }

        }

        //using default value
        return 1.0;
    }

    /**
     * returns the speed of the cannonball depending on the cannon, projectile,
     * @return the velocity of the load projectile, 0 if nothing is loaded
     */
    public double getCannonballVelocity()
    {
        if ((loadedProjectile == null && lastFiredProjectile == null) || design == null)
            return 0.0;

        int loadableGunpowder = design.getMaxLoadableGunpowderNormal();
        if (loadableGunpowder <= 0)
            loadableGunpowder = 1;

        if (loadedProjectile == null)
            return lastFiredProjectile.getVelocity() * design.getMultiplierVelocity() * (1 - Math.pow(2, -4 * lastFiredGunpowder / loadableGunpowder));
        else
            return loadedProjectile.getVelocity() * design.getMultiplierVelocity() * (1 - Math.pow(2, -4 * loadedGunpowder / loadableGunpowder));
    }

    /**
     * @return true if the cannons has a sign
     */
    public boolean hasCannonSign()
    {
        // search all possible sign locations
        for (Location signLoc : design.getChestsAndSigns(this))
        {
            if (signLoc.getBlock().getBlockData() instanceof WallSign)
                return true;
        }
        return false;
    }

    /**
     * @return the number of signs on a cannon
     */
    public int getNumberCannonSigns()
    {
        // search all possible sign locations
        int i = 0;
        for (Location signLoc : design.getChestsAndSigns(this))
        {
            if (signLoc.getBlock().getBlockData() instanceof WallSign)
                i++;
        }
        return i;
    }

    /**
     * updates all signs that are attached to a cannon
     */
    public void updateCannonSigns()
    {
        // update all possible sign locations
        for (Location signLoc : design.getChestsAndSigns(this))
        {
            //check blocktype and orientation before updating sign.
            if (isCannonSign(signLoc))
                updateSign(signLoc.getBlock());
        }
    }

    /**
     * updates the selected sign
     * @param block sign block
     */
    private void updateSign(Block block)
    {
        Sign sign = (Sign) block.getState();
        if (isValid)
        {
            // Cannon name in the first line
            sign.setLine(0, getSignString(0));
            // Cannon owner in the second
            sign.setLine(1, getSignString(1));
            // loaded Gunpowder/Projectile
            sign.setLine(2, getSignString(2));
            // angles
            sign.setLine(3, getSignString(3));
        }
        else
        {
            // Cannon name in the first line
            sign.setLine(0, "this cannon is");
            // Cannon owner in the second
            sign.setLine(1, "damaged");
            // loaded Gunpowder/Projectile
            sign.setLine(2, "");
            // angles
            sign.setLine(3, "");
        }
        sign.setEditable(false);
        sign.update(true);
    }

    /**
     * returns the strings for the sign
     * @param index line on sign
     * @return line on the sign
     */
    public String getSignString(int index)
    {

        switch (index)
        {

            case 0 :
                // Cannon name in the first line
                if (cannonName == null) cannonName = "missing Name";
                return cannonName;
            case 1 :
                // Cannon owner in the second
                if (owner == null)
                    return "missing Owner";
                OfflinePlayer bPlayer = Bukkit.getOfflinePlayer(owner);
                if (bPlayer == null || !bPlayer.hasPlayedBefore())
                    return "not found";
                return bPlayer.getName();
            case 2 :
                // loaded Gunpowder/Projectile
                if (loadedProjectile != null) return "p: " + loadedGunpowder + " c: " + loadedProjectile.getMaterialInformation();
                else return "p: " + loadedGunpowder + " c: " + "0:0";
            case 3 :
                // angles
                return horizontalAngle + "/" + verticalAngle;
        }
        return "missing";
    }




    /**
     * returns the name of the cannon written on the sign
     *
     * @return
     */
    private String getLineOfCannonSigns(int line)
    {
        String lineStr = "";
        // goto the first cannon sign
        for (Location signLoc : design.getChestsAndSigns(this))
        {
            lineStr = CannonSign.getLineOfThisSign(signLoc.getBlock(), line);
            // if something is found return it
            if (lineStr != null && !lineStr.equals(""))
            {
                return lineStr;
            }
        }

        return lineStr;
    }

    /**
     * returns the cannon name that is written on a cannon sign
     *
     * @return
     */
    public String getCannonNameFromSign()
    {
        return getLineOfCannonSigns(0);
    }

    /**
     * returns the cannon owner that is written on a cannon sign
     *
     * @return
     */
    public String getOwnerFromSign()
    {
        return getLineOfCannonSigns(1);
    }

    /**
     * returns true if cannon design for this cannon is found
     *
     * @param cannonDesign
     * @return
     */
    public boolean equals(CannonDesign cannonDesign)
    {
        if (designID.equals(cannonDesign.getDesignID())) return true;
        return false;
    }

    /**
     *
     * @param obj - object to compare
     * @return true if both cannons are equal
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Cannon) {
            Cannon obj2 = (Cannon) obj;
            return this.getUID().equals(obj2.getUID());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return databaseId.hashCode();
    }

    /**
     * get bukkit world
     *
     * @return
     */
    public World getWorldBukkit()
    {
        if (this.world != null)
        {
            World bukkitWorld = Bukkit.getWorld(this.world);
            if (bukkitWorld == null)
                System.out.println("[Cannons] Can't find world: " + world);
            return Bukkit.getWorld(this.world);
            // return new Location(bukkitWorld, )
        }
        return null;
    }

    public UUID getUID()
    {
        return databaseId;
    }

    public void setUID(UUID ID)
    {
        this.databaseId = ID;
        this.hasUpdated();
    }

    public String getDesignID()
    {
        return designID;
    }

    public void setDesignID(String designID)
    {
        this.designID = designID;
        this.hasUpdated();
    }

    public String getCannonName()
    {
        return cannonName;
    }

    public void setCannonName(String name)
    {
        this.cannonName = name;
        this.hasUpdated();
    }

    public BlockFace getCannonDirection()
    {
        return cannonDirection;
    }

    public void setCannonDirection(BlockFace cannonDirection)
    {
        this.cannonDirection = cannonDirection;
        this.hasUpdated();
    }

    public UUID getWorld()
    {
        return world;
    }

    public void setWorld(UUID world)
    {
        this.world = world;
        this.hasUpdated();
    }

    public long getLastFired()
    {
        return lastFired;
    }

    public void setLastFired(long lastFired)
    {
        this.lastFired = lastFired;
        this.hasUpdated();
    }

    public int getLoadedGunpowder()
    {
        if (loadedGunpowder<design.getMaxLoadableGunpowderNormal() && !design.isGunpowderNeeded())
            design.getMaxLoadableGunpowderNormal();

        return loadedGunpowder;
    }

    public void setLoadedGunpowder(int loadedGunpowder)
    {
        this.loadedGunpowder = loadedGunpowder;
        this.hasUpdated();
    }

    public double getHorizontalAngle(){
        return horizontalAngle;
    }

    /**
     * sets a new horizontal angle of the cannon. The angle is limited by the design
     * @param horizontalAngle - new vertical angle
     */
    public void setHorizontalAngle(double horizontalAngle){
        this.horizontalAngle = horizontalAngle;

        //the angle should not exceed the limits - if the cannon is on a ship, the max/min angles are smaller
        double maxHorizontal = getMaxHorizontalAngle();
        if (this.horizontalAngle > maxHorizontal)
            this.horizontalAngle = maxHorizontal;
        double minHorizontal = getMinHorizontalAngle();
        if (this.horizontalAngle < minHorizontal)
            this.horizontalAngle = minHorizontal;
        this.hasUpdated();
    }

    /**
     * returns the maximum horizontal angle, depending if the cannon is on a ship or not
     * @retun the maximum horizontal angle
     */
    public double getMaxHorizontalAngle(){
        return (isOnShip())?design.getMaxHorizontalAngleOnShip():design.getMaxHorizontalAngleNormal();
    }

    /**
     * returns the minimum horizontal angle, depending if the cannon is on a ship or not
     * @retun the minimum horizontal angle
     */
    public double getMinHorizontalAngle() {
        return (isOnShip())?design.getMinHorizontalAngleOnShip():design.getMinHorizontalAngleNormal();
    }

    public double getVerticalAngle()
    {
        return verticalAngle;
    }

    /**
     * sets a new vertical angle of the cannon. The angle is limited by the design
     * @param verticalAngle - new vertical angle
     */
    public void setVerticalAngle(double verticalAngle)
    {
        this.verticalAngle = verticalAngle;
        //the angle should not exceed the limits - if the cannon is on a ship, the max/min angles are smaller
        double maxVertical = getMaxVerticalAngle();
        if (this.verticalAngle > maxVertical)
            this.verticalAngle = maxVertical;
        double minVertical = getMinVerticalAngle();
        if (this.verticalAngle < minVertical)
            this.verticalAngle = minVertical;
        this.hasUpdated();
    }

    /**
     * returns the maximum vertical angle, depending if the cannon is on a ship or not
     * @return returns the maximum vertical angle
     */
    public double getMaxVerticalAngle()
    {
        return (isOnShip())?design.getMaxVerticalAngleOnShip():design.getMaxVerticalAngleNormal();
    }

    /**
     * returns the minimum vertical angle, depending if the cannon is on a ship or not
     * @return returns the minimum vertical angle
     */
    public double getMinVerticalAngle()
    {
        return (isOnShip())?design.getMinVerticalAngleOnShip():design.getMinVerticalAngleNormal();
    }


    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(UUID owner)
    {
        this.owner = owner;
        this.hasUpdated();
    }

    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(boolean isValid)
    {
        this.isValid = isValid;
        this.hasUpdated();
    }

    public Vector getOffset()
    {
        return offset;
    }

    public void setOffset(Vector offset)
    {
        this.offset = offset;
        this.hasUpdated();
    }

    public void setCannonDesign(CannonDesign design)
    {
        this.design = design;
        this.hasUpdated();
    }

    public CannonDesign getCannonDesign()
    {
        return this.design;
    }

    public Projectile getLoadedProjectile()
    {
        return loadedProjectile;
    }

    public void setLoadedProjectile(Projectile loadedProjectile)
    {
        this.loadedProjectile = loadedProjectile;
        this.hasUpdated();
    }

    public String getFiringButtonActivator()
    {
        return firingButtonActivator;
    }

    public void setFiringButtonActivator(String firingButtonActivator)
    {
        this.firingButtonActivator = firingButtonActivator;
    }

    public long getLastAimed()
    {
        return lastAimed;
    }

    public void setLastAimed(long lastAimed)
    {
        this.lastAimed = lastAimed;
    }

    public UUID getLastUser()
    {
        return lastUser;
    }

    public void setLastUser(UUID lastUser)
    {
        this.lastUser = lastUser;
        if(design.isLastUserBecomesOwner())
            this.setOwner(lastUser);
    }

    public boolean isFiring()
    {
        //check if firing is finished and not reseted (after server restart)
        Projectile projectile = getLoadedProjectile();
        //delayTime is the time how long the firing should take
        long delayTime = (long) (design.getFuseBurnTime()*1000.);
        if (projectile != null)
            delayTime += (long) (((projectile.getAutomaticFiringMagazineSize()-1)*projectile.getAutomaticFiringDelay())*1000.0);

        return (lastIgnited + delayTime) >= System.currentTimeMillis();
    }

    public boolean finishedFiringAndLoading()
    {
        //check if firing is finished and not reseted (after server restart)
        Projectile projectile = getLoadedProjectile();
        //delayTime is the time how long the firing should take
        long delayTime = (long) ((design.getFuseBurnTime() + design.getLoadTime())*1000.);
        if (projectile != null)
            delayTime += (long) (((projectile.getAutomaticFiringMagazineSize()-1)*projectile.getAutomaticFiringDelay() + design.getLoadTime())*1000.0);

        return (lastIgnited + delayTime) < System.currentTimeMillis();
    }

    public void setFiring() {
        lastIgnited = System.currentTimeMillis();
        this.hasUpdated();
    }

    public boolean isLoading()
    {
        //delayTime is the time how long the loading should take
        long delayTime = (long) (design.getLoadTime()*1000.0);
        return (lastLoaded + delayTime) > System.currentTimeMillis();
    }

    /**
     * returns the ambient temperature for the cannon in celsius
     * @return ambient temperature for the cannon in celsius
     */
    public double getAmbientTemperature()
    {
        return (Math.sqrt(getMuzzle().getBlock().getTemperature())-0.5)*60;
    }

    /**
     * returns the temperature of the cannon
     * @return cannon temperature
     */
    public double getTemperature() {
        //barrel temperature - minus ambient temperature + exponential decay
        double timePassed = (System.currentTimeMillis() - this.tempTimestamp)/1000.0;
        double decay = Math.exp(-timePassed/design.getCoolingCoefficient());
        double ambient = getAmbientTemperature();
        tempValue = ambient + (tempValue - ambient)*decay;
        this.tempTimestamp = System.currentTimeMillis();

        return tempValue;
    }

    public double getTemperature(boolean update)
    {
        return (update ? this.getTemperature():this.tempValue);
    }

    /**
     * sets the temperature of the cannon to the given value
     * @param temperature - temperature of the cannon
     */
    public void setTemperature(double temperature) {
        this.tempTimestamp = System.currentTimeMillis();
        this.tempValue = temperature;
        this.hasUpdated();
    }

    public long getTemperatureTimeStamp()
    {
        return tempTimestamp;
    }

    public void setTemperatureTimeStamp(long temperatureTimeStamp)
    {
        this.tempTimestamp = temperatureTimeStamp;
        this.hasUpdated();
    }

    public boolean isOverheated(){
        return getTemperature() > design.getCriticalTemperature();
    }

    /**
     * checks if the cannon will be overheated after firing the loaded charge
     * @return true if the cannon reaches the crital limit
     */
    public boolean isOverheatedAfterFiring(){
        return getTemperature() + design.getHeatIncreasePerGunpowder()*getLoadedGunpowder() > design.getCriticalTemperature();
    }

    /**
     * if the cannon can be fired or if it is too hot
     * @return true if the cannon can be loaded
     */
    public boolean isReadyToFire(){
        return isLoaded() && !isOverheatedAfterFiring() && !isFiring() && isClean() && !barrelTooHot() && isProjectilePushed() && finishedFiringAndLoading();
    }

    /**
     * if the barrel is still cooling down from the last time fired
     * @return true if the barrel it too hot
     */
    public boolean barrelTooHot(){
        return getLastFired() + design.getBarrelCooldownTime()*1000 >= System.currentTimeMillis();
    }

    public boolean isClean()
    {
        return getSoot()<1;
    }

    public double getSoot() {
        return soot;
    }

    public void setSoot(double soot) {
        this.soot = (soot>0)?soot:0;
        this.hasUpdated();
    }

    /**
     * reduces the soot of the cannon by the given amount
     * @param amount soot to reduce
     */
    public void cleanCannon(int amount){
        setSoot(getSoot()-amount);
    }

    public int getProjectilePushed() {
        return projectilePushed;
    }

    public void setProjectilePushed(int projectilePushed) {
        this.projectilePushed = (projectilePushed>0)?projectilePushed:0;
        this.hasUpdated();
    }

    /**
     * is the Projectile in place and done
     * @return if the projectile is ready to fire
     */
    public boolean isProjectilePushed(){
        return (getProjectilePushed() == 0);
    }

    /**
     * pushes the projectile to the gunpowder
     * @param amount how often the projectile is pushed
     */
    public void pushProjectile(int amount){
        setProjectilePushed(getProjectilePushed()-amount);
    }


    public double getAdditionalHorizontalAngle() {
        return additionalHorizontalAngle;
    }

    public void setAdditionalHorizontalAngle(double additionalHorizontalAngle) {
        this.additionalHorizontalAngle = additionalHorizontalAngle;
    }

    public double getAdditionalVerticalAngle() {
        return additionalVerticalAngle;
    }

    public void setAdditionalVerticalAngle(double additionalVerticalAngle) {
        this.additionalVerticalAngle = additionalVerticalAngle;
    }

    /**
     * the total angle is sum of the cannon angle and the location where it is mounted
     * @return sum of all angles
     */
    public double getTotalHorizontalAngle(){
        return this.horizontalAngle + this.additionalHorizontalAngle;
    }

    /**
     * the total angle is sum of the cannon angle, its design and the location where it is mounted
     * @return sum of all angles
     */
    public double getTotalVerticalAngle(){
        return design.getDefaultVerticalAngle() + this.verticalAngle + this.additionalVerticalAngle;
    }

    /**
     * get the default horizontal home position of the cannon
     * @return default horizontal home position
     */
    public double getHomeHorizontalAngle(){
        return (design.getMaxHorizontalAngleNormal()+design.getMinHorizontalAngleNormal())/2.0;
    }

    /**
     * get the default vertical home position of the cannon
     * @return default vertical home position
     */
    public double getHomeVerticalAngle(){
        return (design.getMaxVerticalAngleNormal()+design.getMinVerticalAngleNormal())/2.0;
    }

    /**
     * get the default horizontal home position of the cannon
     * @return default horizontal home position
     */
    public double getHomeYaw(){
        return getHomeHorizontalAngle() + CannonsUtil.directionToYaw(cannonDirection);
    }

    /**
     * get the default vertical home position of the cannon
     * @return default vertical home position
     */
    public double getHomePitch(){
        return getHomeVerticalAngle();
    }

    /**
     * if the cannon has the target in sight and angles are set correctly
     * @return true if aiminig is finished
     */
    public boolean targetInSight(){
        return Math.abs(getAimingYaw() - getHorizontalYaw()) < design.getAngleStepSize() &&  Math.abs(getAimingPitch() - getVerticalPitch()) < design.getAngleStepSize();
    }

    /**
     * whenever the cannon can aim in this direction or not
     * @param yaw horizontal angle
     * @return true if it can aim this direction
     */
    public boolean canAimYaw(double yaw){
        double horizontal = yaw - CannonsUtil.directionToYaw(getCannonDirection()) - this.additionalHorizontalAngle;

        horizontal = horizontal % 360;
        while(horizontal < -180)
            horizontal = horizontal + 360;
        return (horizontal > getMinHorizontalAngle() && horizontal < getMaxHorizontalAngle());
    }

    /**
     * whenever the cannon can aim in this direction or not
     * @param pitch vertical angle
     * @return true if it can aim this direction
     */
    public boolean canAimPitch(double pitch){
        double vertical = -pitch - design.getDefaultVerticalAngle() - this.additionalVerticalAngle;
        return (vertical > getMinVerticalAngle() && vertical < getMaxVerticalAngle());
    }

    public double verticalAngleToPitch(double vertical){
        return -vertical - design.getDefaultVerticalAngle() - this.additionalVerticalAngle;
    }

    public double getMaxVerticalPitch(){
        return verticalAngleToPitch(getMaxVerticalAngle());
    }

    public double getMinVerticalPitch(){
        return verticalAngleToPitch(getMinVerticalAngle());
    }

    public double getVerticalPitch(){
        return verticalAngleToPitch(getVerticalAngle());
    }

    public double horizontalAngleToYaw(double horizontal){
        double yaw = horizontal + this.additionalHorizontalAngle + CannonsUtil.directionToYaw(getCannonDirection()) ;

        yaw = yaw % 360;
        while(yaw < -180)
            yaw = yaw + 360;
        while(yaw > 180)
            yaw = yaw - 360;
        return yaw;
    }

    public double getMaxHorizontalYaw(){
        return horizontalAngleToYaw(getMaxHorizontalAngle());
    }

    public double getMinHorizontalYaw(){
        return horizontalAngleToYaw(getMinHorizontalAngle());
    }

    public double getHorizontalYaw(){
        return horizontalAngleToYaw(getHorizontalAngle());
    }

    public boolean isOnShip() {
        return onShip;
    }

    public void setOnShip(boolean onShip) {
        this.onShip = onShip;
        this.hasUpdated();
    }

    public double getAimingPitch() {
        return aimingPitch;
    }

    public void setAimingPitch(double aimingPitch) {
        this.aimingPitch = aimingPitch;
        this.hasUpdated();
    }

    public double getAimingYaw() {
        return aimingYaw;
    }

    public void setAimingYaw(double aimingYaw) {
        this.aimingYaw = aimingYaw;
        this.hasUpdated();
    }

    public HashMap<UUID, Boolean> getObserverMap() {
        return observerMap;
    }

    /**
     * add the player as observer for this cannon
     * @param player player will be added as observer
     * @param removeAfterShowing if true, the observer only works once
     * @return message for the player
     */
    public MessageEnum addObserver(Player player, boolean removeAfterShowing)
    {
        Validate.notNull(player, "player must not be null");

        //permission check
        if (!player.hasPermission(design.getPermissionObserver()))
            return MessageEnum.PermissionErrorObserver;

        //the player might have an entry which allows unlimited observing (e.g. observer)
        //removeAfterShowing == true is weaker
        if (observerMap.get(player.getUniqueId()) == null || observerMap.get(player.getUniqueId()))
            observerMap.put(player.getUniqueId(), removeAfterShowing);
        return MessageEnum.CannonObserverAdded;
    }

    /**
     * removes the player as observer
     * @param player player will be removed as observer
     * @return message for the player
     */
    public MessageEnum removeObserver(Player player)
    {
        Validate.notNull(player, "player must not be null");

        observerMap.remove(player.getUniqueId());
        return MessageEnum.CannonObserverRemoved;
    }

    /**
     * toogles the player as observer for this cannon
     * @param player player will be added as observer
     * @param removeAfterShowing if true, the observer only works once
     * @return message for the player
     */
    public MessageEnum toggleObserver(Player player, boolean removeAfterShowing)
    {
        Validate.notNull(player, "player must not be null");

        if (observerMap.containsKey(player.getUniqueId()))
            return removeObserver(player);
        else
            return addObserver(player, removeAfterShowing);

    }

    /**
     * gets the chance of cannon explosion due to overloading of gunpowder
     * @return chance of explosion
     */
    public double getOverloadingExplosionChance()
    {
        if(design.isOverloadingEnabled())
        {
            double tempInc;
            if(design.isOverloadingDependsOfTemperature())
                tempInc = tempValue/design.getMaximumTemperature();
            else
                tempInc = 1;

            int saferGunpowder;
            if(design.isOverloadingRealMode())
                saferGunpowder = 0;
            else
                saferGunpowder = design.getMaxLoadableGunpowderNormal();

            //prevent negative values
            int gunpowder = loadedGunpowder - saferGunpowder;
            if (gunpowder < 0)
                gunpowder = 0;
            double chance  = tempInc * design.getOverloadingChangeInc()*Math.pow(gunpowder*design.getOverloadingChanceOfExplosionPerGunpowder(), design.getOverloadingExponent());
            return (chance <= 0) ? 0.0:chance;
        }
        else
            return 0.0;
    }

    /**
     * Calculating if cannon might to explode
     * @return true if explosion chance was more then random number
     */
	public boolean isExplodedDueOverloading()
	{
        double chance = getOverloadingExplosionChance();
        //Cannons.getPlugin().logDebug("Chance of explosion (overloading) = " + design.getOverloadingChangeInc() + " * ((" + loadedGunpowder + " ( may to be - " + design.getMaxLoadableGunpowder_Normal() + ")) * " + design.getOverloadingChanceOfExplosionPerGunpowder() + ") ^ " + design.getOverloadingExponent() + " (may to be multiplied by " + tempValue + " / " + design.getMaximumTemperature() + " = " + chance);
        if(Math.random()<chance)
            return true;
        return false;
    }

    public long getFiredCannonballs() {
        return firedCannonballs;
    }

    public void setFiredCannonballs(long firedCannonballs) {
        this.firedCannonballs = firedCannonballs;
        this.hasUpdated();
    }

    public void incrementFiredCannonballs(){
        this.firedCannonballs++;
        this.hasUpdated();
    }

    public double getLastPlayerSpreadMultiplier() {
        return lastPlayerSpreadMultiplier;
    }

    public void setLastPlayerSpreadMultiplier(Player player) {
        this.lastPlayerSpreadMultiplier = getPlayerSpreadMultiplier(player);
    }

    public void resetLastPlayerSpreadMultiplier(){
        this.lastPlayerSpreadMultiplier = 1.0;
    }

    public long getLastSentryUpdate() {
        return lastSentryUpdate;
    }

    public void setLastSentryUpdate(long lastSentryUpdate) {
        this.lastSentryUpdate = lastSentryUpdate;
    }

    public boolean isChunkLoaded(){
        Chunk chunk = getLocation().getChunk();
        return chunk != null && chunk.isLoaded();
    }

    public UUID getSentryEntity() {
        return sentryEntity;
    }

    public boolean hasSentryEntity(){
        return sentryEntity != null;
    }

    /**
     * Set this as new sentry target and add it to the list of targeted entities
     * @param sentryTarget
     */
    public void setSentryTarget(UUID sentryTarget) {
        this.sentryEntity = sentryTarget;
        if (sentryTarget != null){
            setSentryTargetingTime(System.currentTimeMillis());
            //store only 5
            if (sentryEntityHistory.size() > 5)
                sentryEntityHistory.remove(0);
            sentryEntityHistory.add(sentryTarget);
        }
    }

    /**
     * was this entity targeted in the last time
     * @param entityId ID of the entity
     * @return true if it was target
     */
    public boolean wasSentryTarget(UUID entityId){
        return entityId != null && sentryEntityHistory.contains(entityId);
    }

    public long getSentryTargetingTime() {
        return sentryTargetingTime;
    }

    private void setSentryTargetingTime(long sentryTargetingTime) {
        this.sentryTargetingTime = sentryTargetingTime;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public long getSentryLastLoadingFailed() {
        return sentryLastLoadingFailed;
    }

    public void setSentryLastLoadingFailed(long sentryLastLoadingFailed) {
        this.sentryLastLoadingFailed = sentryLastLoadingFailed;
    }

    public Projectile getLastFiredProjectile() {
        return lastFiredProjectile;
    }

    public int getLastFiredGunpowder() {
        return lastFiredGunpowder;
    }

    public long getLastLoaded() {
        return lastLoaded;
    }

    public void setLastLoaded(long lastLoaded) {
        this.lastLoaded = lastLoaded;
    }

    public long getSentryLastFiringFailed() {
        return sentryLastFiringFailed;
    }

    public void setSentryLastFiringFailed(long sentryLastFiringFailed) {
        this.sentryLastFiringFailed = sentryLastFiringFailed;
    }

    public HashSet<UUID> getWhitelist() {
        return whitelist;
    }

    public void addWhitelistPlayer(UUID playerUID){
        setLastWhitelisted(playerUID);
        whitelist.add(playerUID);
        this.hasWhitelistUpdated();
    }

    public void removeWhitelistPlayer(UUID playerUID){
        setLastWhitelisted(playerUID);
        whitelist.remove(playerUID);
        this.hasWhitelistUpdated();
    }

    public boolean isWhitelisted(UUID playerUID){
        return whitelist.contains(playerUID);
    }

    public UUID getLastWhitelisted() {
        return lastWhitelisted;
    }

    public void setLastWhitelisted(UUID lastWhitelisted) {
        this.lastWhitelisted = lastWhitelisted;
    }

    public boolean isTargetMob() {
        return targetMob;
    }

    public void setTargetMob(boolean targetMob) {
        this.targetMob = targetMob;
        this.hasUpdated();
    }

    public void toggleTargetMob(){
        setTargetMob(!this.targetMob);
    }

    public boolean isTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(boolean targetPlayer) {
        this.targetPlayer = targetPlayer;
        this.hasUpdated();
    }

    public void toggleTargetPlayer(){
        setTargetPlayer(!this.targetPlayer);
    }

    public boolean isTargetCannon() {
        return targetCannon;
    }

    public void setTargetCannon(boolean targetCannon) {
        this.targetCannon = targetCannon;
        this.hasUpdated();
    }

    public void toggleTargetCannon(){
        setTargetCannon(!this.targetCannon);
    }

    public boolean isTargetOther() {
        return targetOther;
    }

    public void setTargetOther(boolean targetOther) {
        this.targetOther = targetOther;
        this.hasUpdated();
    }

    public void toggleTargetOther(){
        setTargetOther(!this.targetOther);
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
        this.hasUpdated();
    }

    public void boughtByPlayer(UUID playerID){
        setPaid(true);
        setOwner(playerID);
        whitelist.clear();
        whitelist.add(playerID);
    }

    public EntityType getProjectileEntityType(){
        if (loadedProjectile != null){
            return loadedProjectile.getProjectileEntity();
        }
        if (lastFiredProjectile != null){
            return lastFiredProjectile.getProjectileEntity();
        }
        return EntityType.SNOWBALL;
    }

    public boolean isSentryHomedAfterFiring() {
        return sentryHomedAfterFiring;
    }

    public void setSentryHomedAfterFiring(boolean sentryHomedAfterFiring) {
        this.sentryHomedAfterFiring = sentryHomedAfterFiring;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void hasUpdated() {
        this.updated = true;
    }

    public void setUpdated(boolean updated){
        this.updated = updated;
    }

    public boolean isWhitelistUpdated() {
        return whitelistUpdated;
    }

    public void hasWhitelistUpdated() {
        this.whitelistUpdated = true;
    }

    public void setWhitelistUpdated(boolean whitelistUpdated) {
        this.whitelistUpdated = whitelistUpdated;
    }
}
