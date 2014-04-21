package at.pavlov.cannons.cannon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.container.MaterialHolder;
import at.pavlov.cannons.event.CannonUseEvent;
import at.pavlov.cannons.Enum.InteractAction;
import at.pavlov.cannons.listener.Commands;
import at.pavlov.cannons.projectile.ProjectileStorage;
import at.pavlov.cannons.utils.CannonsUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
    private String world;
    // if the cannon is on a ship, the operation might be limited (e.g smaller angles to adjust the cannon)
    private boolean onShip;

    // time the cannon was last time fired
    private long lastFired;
    // time it was last aimed
    private long lastAimed;

    // amount of loaded gunpowder
    private int loadedGunpowder;
    // the loaded projectile - can be null
    private Projectile loadedProjectile;

    // cleaning after firing (clicking with the stick several times
    private double soot;
    // pushing a projectile into the barrel after loading the projectile
    private int projectilePushed;

    // angles
    private double horizontalAngle;
    private double verticalAngle;
    // additional angle if the cannon is mounted e.g. a ship which is facing a different angle
    private double additionalHorizontalAngle;
    private double additionalVerticalAngle;

    // player who has build this cannon
    private String owner;
    // designID of the cannon, for different types of cannons - not in use
    private boolean isValid;
    // true if the cannon if firing
    private boolean isFiring;
    //the player which has used the cannon last, important for firing with redstone button
    private String lastUser;

    //cannon temperature
    private double tempValue;
    private long tempTimestamp;

    private CannonDesign design;


    // not saved in the database
    // redstone handling event. Last player that pressed the firing button is saved in this list for the next redstone event
    private String firingButtonActivator;


    public Cannon(CannonDesign design, String world, Vector cannonOffset, BlockFace cannonDirection, String owner)
    {

        this.design = design;
        this.designID = design.getDesignID();
        this.world = world;
        this.offset = cannonOffset;
        this.cannonDirection = cannonDirection;
        this.owner = owner;
        this.isValid = true;

        this.horizontalAngle = (design.getMaxHorizontalAngle()+design.getMinHorizontalAngle())/2.0;
        this.verticalAngle = (design.getMaxVerticalAngle()+design.getMinVerticalAngle())/2.0;

        // reset
        this.setLoadedGunpowder(0);
        this.setLoadedProjectile(null);
        this.setSoot(0.0);
        this.setProjectilePushed(design.getProjectilePushing());

        this.databaseId = UUID.randomUUID();
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
     * @return
     */
    public Location getMuzzle()
    {
          return design.getMuzzle(this);
    }


    /**
     * removes the loaded charge form the chest attached to the cannon, returns true if the ammo was found in the chest
     * @param player - player operating the cannon
     * @param consumesAmmo - if true ammo will be removed from chest inventories
     * @return - true if the cannon has been reloaded. False if there is not enough ammunition
     */
    public boolean reloadFromChests(Player player, boolean consumesAmmo)
    {
        List<Inventory> invlist = getInventoryList();

        //clean the cannon
        //setSoot(0.0);

        //load gunpowder
        if (design.isGunpowderConsumption()&&consumesAmmo)
        {

            //gunpowder will be consumed from the inventory
            //load the maximum gunpowder possible (maximum amount that fits in the cannon or is in the chest)
            int toLoad = design.getMaxLoadableGunpowder() - getLoadedGunpowder();
            ItemStack gunpowder = design.getGunpowderType().toItemStack(toLoad);
            gunpowder = InventoryManagement.removeItem(invlist, gunpowder);
            if (gunpowder.getAmount() == 0)
            {
                //there was enough gunpowder in the chest
                loadedGunpowder = design.getMaxLoadableGunpowder();
            }
            else
            {
                //not enough gunpowder, put it back
                gunpowder.setAmount(toLoad-gunpowder.getAmount());
                InventoryManagement.addItemInChests(invlist, gunpowder);
            }
        }
        else
        {
            loadedGunpowder = design.getMaxLoadableGunpowder();
        }




        // find a loadable projectile in the chests
        for (Inventory inv : invlist)
        {
            for (ItemStack item : inv.getContents())
            {
                //try to load it and see what happens
                Projectile projectile = ProjectileStorage.getProjectile(this, item);
                if (projectile == null)
                    continue;

                MessageEnum message = CheckPermProjectile(projectile, player);
                if (message == MessageEnum.loadProjectile)
                {
                    // everything went fine, so remove it from the chest remove projectile
                    loadedProjectile = projectile;

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
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * removes cooling item form the chest attached to the cannon, returns true if it was enough to cool down the cannon
     * @param player - player operating the cannon
     * @return - true if the cannon has been cooled down
     */
    public boolean automaticCoolingFromChest(Player player)
    {

        List<Inventory> invlist = getInventoryList();

        //cooling items will be consumed from the inventory
        int toCool = (int) Math.ceil((this.getTemperature() - design.getWarningTemperature())/design.getCoolingAmount());
        ItemStack item = new ItemStack(Material.AIR, toCool);

        if (toCool <= 0)
            return true;

        //do this for every cooling item
        for (MaterialHolder mat : design.getItemCooling())
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
        // this cannon needs to be cleaned first
        if (!isClean())
            return MessageEnum.ErrorNotCleaned;
        //projectile pushing necessary
        if (isLoaded()&&!isProjectilePushed())
            return MessageEnum.ErrorNotPushed;
        // projectile already loaded
        if (isLoaded()&&isProjectilePushed())
            return MessageEnum.ErrorProjectileAlreadyLoaded;
        // maximum gunpowder already loaded
        if (getLoadedGunpowder() >= design.getMaxLoadableGunpowder())
            return MessageEnum.ErrorMaximumGunpowderLoaded;

        //load the maximum gunpowder
        setLoadedGunpowder(getLoadedGunpowder() + amountToLoad);

        if (getLoadedGunpowder() > design.getMaxLoadableGunpowder())
            setLoadedGunpowder(design.getMaxLoadableGunpowder());

        // update Signs
        updateCannonSigns();

        return MessageEnum.loadGunpowder;
    }


    /**
     * checks the permission of a player before loading gunpowder in the cannon. Designed for player operation
     * @param player - the player which is loading the cannon
     */
    public MessageEnum loadGunpowder(Player player)
    {

        //fire event
        CannonUseEvent useEvent = new CannonUseEvent(this, player, InteractAction.loadGunpowder);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;


        //save the amount of gunpowder we loaded in the cannon
        int gunpowder = 0;
        int maximumLoadable = design.getMaxLoadableGunpowder() - getLoadedGunpowder();

        //check if the player has permissions for this cannon
        MessageEnum returnVal = CheckPermGunpowder(player);

        //the player seems to have all rights to load the cannon.
        if (returnVal.equals(MessageEnum.loadGunpowder))
        {
            //if the player is sneaking the maximum gunpowder is loaded, but at least 1
            if (player.isSneaking())
            {
                //get the amount of gunpowder that can be maximal loaded
                gunpowder = player.getItemInHand().getAmount();
                if (gunpowder > maximumLoadable)
                    gunpowder = maximumLoadable;
            }
            if (gunpowder <= 0)
                gunpowder = 1;

            //load the gunpowder
            returnVal = loadGunpowder(gunpowder);
        }

        // the cannon was loaded with gunpowder - lets get it form the player
        if (returnVal.equals(MessageEnum.loadGunpowder))
        {
            // take item from the player
            if (design.isGunpowderConsumption()&&!design.isAmmoInfiniteForPlayer())
                InventoryManagement.takeFromPlayerHand(player, gunpowder);
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
        CannonUseEvent useEvent = new CannonUseEvent(this, player, InteractAction.loadProjectile);
        Bukkit.getServer().getPluginManager().callEvent(useEvent);

        if (useEvent.isCancelled())
            return null;

        if (projectile == null) return null;

        MessageEnum returnVal = CheckPermProjectile(projectile, player);

        // check if loading of projectile was successful
        if (returnVal.equals(MessageEnum.loadProjectile))
        {
            //if the player is not the owner of this gun
            if (player != null && !this.getOwner().equals(player.getName()) && design.isAccessForOwnerOnly())
                return MessageEnum.ErrorNotTheOwner;
            // already loaded with a projectile
            if (isLoaded())
                return MessageEnum.ErrorProjectileAlreadyLoaded;
            // is cannon cleaned with ramrod?
            if (!isClean())
                return MessageEnum.ErrorNotCleaned;
            // no gunpowder loaded
            if (getLoadedGunpowder() == 0)
                return MessageEnum.ErrorNoGunpowder;

            // load projectile
            loadedProjectile = projectile;

            // remove from player
            if (design.isProjectileConsumption()&&!design.isAmmoInfiniteForPlayer())
                InventoryManagement.takeFromPlayerHand(player,1);

            // update Signs
            updateCannonSigns();
        }
        return returnVal;
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
            if (!this.getOwner().equals(player.getName()) && design.isAccessForOwnerOnly())
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
     * @param player - whose permissions are checked
     * @return true if the player and cannons can load the projectile
     */
    private MessageEnum CheckPermProjectile(Projectile projectile, Player player)
    {
        if (player != null)
        {
            // no permission to load
            if (!player.hasPermission(design.getPermissionLoad()))
                return MessageEnum.PermissionErrorLoad;
        }
        // no permission for this projectile
        if (!projectile.hasPermission(player))
            return MessageEnum.PermissionErrorProjectile;
        // loading successful
        return MessageEnum.loadProjectile;
    }

    /**
     * a ramrod is used to clean the barrel before loading gunpowder and to push the projectile into the barrel
     * @param player player using the ramrod tool (null will bypass permission check)
     * @return message for the player
     */
    public MessageEnum useRamRod(Player player)
    {
        //no permission to use this tool
        if (player!=null && !player.hasPermission(design.getPermissionRamrod()))
            return MessageEnum.PermissionErrorRamrod;
        //if the player is not the owner of this gun
        if (player!=null &&!this.getOwner().equals(player.getName()) && design.isAccessForOwnerOnly())
            return MessageEnum.ErrorNotTheOwner;
        //if the barrel is dirty clean it
        if (!isClean())
        {
            cleanCannon(1);
            if (isClean())
                return MessageEnum.RamrodCleaningDone;
            else
                return MessageEnum.RamrodCleaning;
        }
        //if clean load the gunpowder
        if (isClean()&&!isLoadedWithGunpowder())
        {
            cleanCannon(1);
            return MessageEnum.ErrorNoGunpowder;
        }
        //if no projectile
        if (isLoadedWithGunpowder()&&!isLoaded())
            return MessageEnum.ErrorNoProjectile;
        //if the projectile is loaded
        if (isLoaded() && !isProjectilePushed())
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
     * is cannon loaded return true
     *
     * @return - true if there is a projectile in the cannon
     */
    public boolean isLoaded()
    {
        return (loadedProjectile != null);
    }

    /**
     * removes gunpowder and the projectile. Items are drop at the cannonball firing point
     */
    private void dropCharge()
    {
        removeCharge();
        if (loadedGunpowder > 0)
        {
            ItemStack powder = design.getGunpowderType().toItemStack(loadedGunpowder);
            getWorldBukkit().dropItemNaturally(design.getMuzzle(this), powder);
        }

        // can't drop Air
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
        //delete charge for human gunner
        this.setLoadedGunpowder(0);
        this.setLoadedProjectile(null);

        //update Sign
        this.updateCannonSigns();
    }

    /**
     * removes the sign text and charge of the cannon after destruction
     */
    public MessageEnum destroyCannon(boolean breakBlocks, BreakCause cause)
    {
        // update cannon signs the last time
        isValid = false;
        updateCannonSigns();

        // drop charge
        dropCharge();

        if (breakBlocks)
            breakAllCannonBlocks();

        // return message
        switch (cause)
        {
            case Overheating:
                return MessageEnum.HeatManagementOverheated;
            case Other:
                return null;
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
            wBlock.setType(cBlock.getMaterial());
            wBlock.setData((byte) cBlock.getData());
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
                wBlock.setData((byte) 0, false);
            }
        }

        //remove all
        for (SimpleBlock cBlock : design.getAllCannonBlocks(this.getCannonDirection()))
        {
            Block wBlock = cBlock.toLocation(getWorldBukkit(), offset).getBlock();

            if (wBlock.getType() != Material.AIR)
            {
                wBlock.setType(Material.AIR);
                wBlock.setData((byte) 0, false);
            }
        }
    }


    /**
     * breaks all cannon blocks of the cannon
     */
    void breakAllCannonBlocks()
    {
        List<Location> locList = design.getAllCannonBlocks(this);

        Bukkit.getWorld(world).createExplosion(locList.get(0), 2);
        //Bukkit.getWorld(world).createExplosion(locList.get(r.nextInt(locList.size())),getLoadedGunpowder()/design.getMaxLoadableGunpowder()*4,true);
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
        for (SimpleBlock designBlock : design.getAllCannonBlocks(cannonDirection))
        {
            if (designBlock.compareBlockAndLocFuzzy(block, offset))
            {
                return true;
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
            System.out.println("destructable: " + loc);
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
        if (loc.getBlock().getType() != Material.WALL_SIGN) return false;

        CannonBlocks cannonBlocks  = this.getCannonDesign().getCannonBlockMap().get(this.getCannonDirection());
        if (cannonBlocks != null)
        {
            for (SimpleBlock cannonblock : cannonBlocks.getChestsAndSigns())
            {
                // compare location
                if (cannonblock.toLocation(this.getWorldBukkit(),this.offset).equals(loc))
                {
                    Block block = loc.getBlock();
                    //compare and data
                    //only the two lower bits of the bytes are important for the direction (delays are not interessting here)
                    if (cannonblock.getData() == block.getData() || block.getData() == -1 || cannonblock.getData() == -1 )
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
                    //only the two lower bits of the bytes are important for the direction (delays are not interessting here)
                    if (cannonblock.getData() == block.getData() %4 || block.getData() == -1 || cannonblock.getData() == -1 )
                        return true;
                }
            }
        }
        return false;
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
    public MessageEnum checkRedstonePermission(String player)
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
            if (b == Material.REDSTONE_TORCH_ON || b == Material.REDSTONE_TORCH_OFF)
            {
                removeRedstone();
                return MessageEnum.PermissionErrorRedstone;
            }
        }

        // wire
        for (Location loc : design.getRedstoneWireAndRepeater(this))
        {
            Material b = loc.getBlock().getType();
            if (b == Material.REDSTONE_WIRE || b == Material.DIODE || b == Material.DIODE_BLOCK_ON || b == Material.DIODE_BLOCK_OFF)
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
            if (block.getType() == Material.REDSTONE_TORCH_ON || block.getType() == Material.REDSTONE_TORCH_OFF)
            {
                block.breakNaturally();
            }
        }

        // wires and repeater
        for (Location loc : design.getRedstoneWireAndRepeater(this))
        {
            Block block = loc.getBlock();
            if (block.getType() == Material.REDSTONE_WIRE || block.getType() == Material.DIODE_BLOCK_ON || block.getType() == Material.DIODE_BLOCK_OFF)
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
     * Checks the cannon if the actual temperature might destroy the cannon
     * @return true if the cannon will explode
     */
    public boolean checkHeatManagement()
    {
        double tempCannon = this.getTemperature();
        double tempCritical = design.getCriticalTemperature();
        double tempMax = design.getMaximumTemperature();
        double explodingProbability = 0.0;

        if (tempCannon > tempCritical)
        {
            //no exploding chance for temperature < critical, 100% chance for > maximum
            explodingProbability = Math.pow((tempCannon-tempCritical)/(tempMax-tempCritical),3);
            //play some effects for a hot barrel
            this.playBarrelSmokeEffect((int)(explodingProbability*20.0+1));
        }

        Random r = new Random();
        return r.nextDouble()<explodingProbability;
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
            effectLoc.getWorld().playSound(effectLoc, Sound.FIZZ, 1, 1);
        }
    }

    /**
     * cools down a cannon by using the item in hand of a player
     * @param player player using the cannon
     */
    public void coolCannon(Player player)
    {
        int toCool = (int) Math.ceil((this.getTemperature() - design.getWarningTemperature())/design.getCoolingAmount());

        //if the player is sneaking the maximum gunpowder is loaded, but at least 1
        int amount = 1;
        if (player.isSneaking())
        {
            //get the amount of gunpowder that can be maximal loaded
            amount = player.getItemInHand().getAmount();
            if (amount > toCool)
                amount = toCool;
        }

        setTemperature(getTemperature()-design.getCoolingAmount()*amount);

        ItemStack newItem = design.getCoolingToolUsed(player.getItemInHand());
        //remove only one item if the material is AIR else replace the item (e.g. water bucket with a bucket)
        if (newItem.getType().equals(Material.AIR))
            InventoryManagement.takeFromPlayerHand(player, 1);
        else
            player.setItemInHand(newItem);

        return;
    }

    /**
     * cools down a cannon by using the item in hand of a player
     * @param player player using the cannon
     * @param effectLoc location of the smoke effects
     */
    public void coolCannon(Player player, Location effectLoc)
    {
        coolCannon(player);

        if (effectLoc !=null && getTemperature() > design.getWarningTemperature())
        {
            effectLoc.getWorld().playEffect(effectLoc, Effect.SMOKE, BlockFace.UP);
            effectLoc.getWorld().playSound(effectLoc, Sound.FIZZ, 1, 1);
        }
        return;
    }


    /**
     * return the firing vector of the cannon. The spread depends on the cannon, the projectile and the player
     * @param player
     * @return
     */
    public Vector getFiringVector(Player player)
    {
        // get projectile
        // set direction of the snowball
        Vector vect = new Vector(1f, 0f, 0f);
        Random r = new Random();

        double playerSpreadMultiplier = getPlayerSpreadMultiplier(player);

        double deviation = r.nextGaussian() * design.getSpreadOfCannon() * loadedProjectile.getSpreadMultiplier()*playerSpreadMultiplier;
        double azi = (getTotalHorizontalAngle() + deviation) * Math.PI / 180;

        deviation = r.nextGaussian() * design.getSpreadOfCannon() * loadedProjectile.getSpreadMultiplier()*playerSpreadMultiplier;
        double polar = (-getTotalVerticalAngle() + 90.0 + deviation)* Math.PI / 180;

        double hx = Math.sin(polar)*Math.sin(azi);
        double hy = Math.sin(polar)*Math.cos(azi);
        double hz = Math.cos(polar);

        if (cannonDirection.equals(BlockFace.WEST))
        {
            vect = new Vector(-hy, hz, -hx);
        }
        else if (cannonDirection.equals(BlockFace.NORTH))
        {
            vect = new Vector(hx, hz, -hy);
        }
        else if (cannonDirection.equals(BlockFace.EAST))
        {
            vect = new Vector(hy, hz, hx);
        }
        else if (cannonDirection.equals(BlockFace.SOUTH))
        {
            vect = new Vector(-hx, hz, hy);
        }

        double multi = getCannonballVelocity();
        if (multi < 0.1) multi = 0.1;

        return vect.multiply(multi);
    }

    public Vector getAimingVector()
    {
        Vector vect = new Vector(0.0f, 0.0f, 0.0f);
        double azi = (getTotalHorizontalAngle() ) * Math.PI / 180;
        double polar = (-getTotalVerticalAngle() + 90.0)* Math.PI / 180;

        double hx = Math.sin(polar)*Math.sin(azi);
        double hy = Math.sin(polar)*Math.cos(azi);
        double hz = Math.cos(polar);

        if (cannonDirection.equals(BlockFace.WEST))
        {
            vect = new Vector(-hy, hz, -hx);
        }
        else if (cannonDirection.equals(BlockFace.NORTH))
        {
            vect = new Vector(hx, hz, -hy);
        }
        else if (cannonDirection.equals(BlockFace.EAST))
        {
            vect = new Vector(hy, hz, hx);
        }
        else if (cannonDirection.equals(BlockFace.SOUTH))
        {
            vect = new Vector(-hx, hz, hy);
        }

        double multi = getCannonballVelocity();
        if (multi < 0.1) multi = 0.1;

        return vect.multiply(multi);

    }

    /**
     * etracts the spreadMultiplier from the permissions
     * @param player
     * @return
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
     * loaded gunpowder the dependency on the gunpowder is (1-2^(-4*loaded/max))
     *
     * @return
     */
    double getCannonballVelocity()
    {
        if (loadedProjectile == null || design == null) return 0.0;
        return loadedProjectile.getVelocity() * design.getMultiplierVelocity() * (1 - Math.pow(2, -4 * loadedGunpowder / design.getMaxLoadableGunpowder()));
    }

    /**
     * @return true if the cannons has a sign
     */
    public boolean hasCannonSign()
    {
        // search all possible sign locations
        for (Location signLoc : design.getChestsAndSigns(this))
        {
            if (signLoc.getBlock().getTypeId() == Material.WALL_SIGN.getId())
                return true;
        }
        return false;
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
     *
     * @param block
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

        sign.update(true);
    }

    /**
     * returns the strings for the sign
     *
     * @param index
     * @return
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
                if (owner == null) owner = "missing Owner";
                return owner;
            case 2 :
                // loaded Gunpowder/Projectile
                if (loadedProjectile != null) return "p: " + loadedGunpowder + " c: " + loadedProjectile.toString();
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
    public boolean equals(Object obj)
    {
        return this.getID().equals(((Cannon) obj).getID());
    }

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

    public UUID getID()
    {
        return databaseId;
    }

    public void setID(UUID ID)
    {
        this.databaseId = ID;
    }

    public String getDesignID()
    {
        return designID;
    }

    public void setDesignID(String designID)
    {
        this.designID = designID;
    }

    public String getCannonName()
    {
        return cannonName;
    }

    public void setCannonName(String name)
    {
        this.cannonName = name;
    }

    public BlockFace getCannonDirection()
    {
        return cannonDirection;
    }

    public void setCannonDirection(BlockFace cannonDirection)
    {
        this.cannonDirection = cannonDirection;
    }

    public String getWorld()
    {
        return world;
    }

    public void setWorld(String world)
    {
        this.world = world;
    }

    public long getLastFired()
    {
        return lastFired;
    }

    public void setLastFired(long lastFired)
    {
        this.lastFired = lastFired;
    }

    public int getLoadedGunpowder()
    {
        return loadedGunpowder;
    }

    public void setLoadedGunpowder(int loadedGunpowder)
    {
        this.loadedGunpowder = loadedGunpowder;
    }

    public double getHorizontalAngle()
    {
        return horizontalAngle;
    }

    /**
     * sets a new horizontal angle of the cannon. The angle is limited by the design
     * @param horizontalAngle - new vertical angle
     */
    public void setHorizontalAngle(double horizontalAngle)
    {
        this.horizontalAngle = horizontalAngle;

        //the angle should not exceed the limits - if the cannon is on a ship, the max/min angles are smaller
        double maxHorizontal = (isOnShip())?design.getMaxHorizontalAngleOnShip():design.getMaxHorizontalAngle();
        if (this.horizontalAngle > maxHorizontal)
            this.horizontalAngle = maxHorizontal;
        double minHorizontal = (isOnShip())?design.getMinHorizontalAngleOnShip():design.getMinHorizontalAngle();
        if (this.horizontalAngle < minHorizontal)
            this.horizontalAngle = minHorizontal;
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
        double maxVertical = (isOnShip())?design.getMaxVerticalAngleOnShip():design.getMaxVerticalAngle();
        if (this.verticalAngle > maxVertical)
            this.verticalAngle = maxVertical;
        double minVertical = (isOnShip())?design.getMinVerticalAngleOnShip():design.getMinVerticalAngle();
        if (this.verticalAngle < minVertical)
            this.verticalAngle = minVertical;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(boolean isValid)
    {
        this.isValid = isValid;
    }

    public Vector getOffset()
    {
        return offset;
    }

    public void setOffset(Vector offset)
    {
        this.offset = offset;
    }

    public void setCannonDesign(CannonDesign design)
    {
        this.design = design;
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

    public String getLastUser() {
        return lastUser;
    }

    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }

    public boolean isFiring() {
        return isFiring;
    }

    public void setFiring(boolean firing) {
        isFiring = firing;
    }

    /**
     * returns the ambient temperature for the cannon in celsius
     * @return ambient temperature for the cannon in celsius
     */
    public double getAmbientTemperature()
    {
        return (Math.sqrt(this.design.getMuzzle(this).getBlock().getTemperature())-0.5)*60;
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
        return (update) ? this.getTemperature():this.tempValue;
    }

    /**
     * sets the temperature of the cannon to the given value
     * @param temperature - temperature of the cannon
     */
    public void setTemperature(double temperature) {
        this.tempTimestamp = System.currentTimeMillis();
        this.tempValue = temperature;
    }

    public long getTemperatureTimeStamp()
    {
        return tempTimestamp;
    }

    public void setTemperatureTimeStamp(long temperatureTimeStamp)
    {
        this.tempTimestamp = temperatureTimeStamp;
    }


    public boolean isClean()
    {
        return getSoot()<1;
    }

    public boolean isLoadedWithGunpowder()
    {
        return loadedGunpowder!=0;
    }

    public double getSoot() {
        return soot;
    }

    public void setSoot(double soot) {
        this.soot = (soot>0)?soot:0;
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


    public boolean isOnShip() {
        return onShip;
    }

    public void setOnShip(boolean onShip) {
        this.onShip = onShip;
    }


}
