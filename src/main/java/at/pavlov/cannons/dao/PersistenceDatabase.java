package at.pavlov.cannons.dao;

import java.util.UUID;

import at.pavlov.cannons.Cannons;
import at.pavlov.cannons.cannon.Cannon;

public class PersistenceDatabase
{
	private Cannons plugin;

	public PersistenceDatabase(Cannons _plugin)
	{
		plugin = _plugin;
	}

//    /**
//     * loads all cannons from the database
//     */
//    public void loadCannonsAsync()
//    {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
//            public void run() {
//                loadCannons();
//            }
//        });
//    }

	public void createTables(){
		CreateTableTask createTableTask = new CreateTableTask();
		createTableTask.run();
	}

    /**
     * loads all cannons from the database
     *
     * @return true if loading was successful
     */
	public void loadCannons()
	{
	    if (!plugin.hasConnection()) {
            plugin.logSevere("No connection to database");
            return;
        }
		plugin.getCannonManager().clearCannonList();

	    LoadCannonTask task = new LoadCannonTask();
	    task.runTaskAsynchronously(plugin);
	}

	public void saveAllCannons(boolean async){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		SaveCannonTask saveCannonTask = new SaveCannonTask();
		if (async)
			saveCannonTask.runTaskAsynchronously(plugin);
		else
			saveCannonTask.run();

    }

    public void saveCannon(Cannon cannon){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		SaveCannonTask saveCannonTask = new SaveCannonTask(cannon.getUID());
		saveCannonTask.runTaskAsynchronously(plugin);
	}

    public void deleteCannon(UUID cannon_id){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		DeleteCannonTask deleteCannonTask = new DeleteCannonTask(cannon_id);
		deleteCannonTask.runTaskAsynchronously(plugin);
	}

	public void deleteAllCannons(){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		DeleteCannonTask deleteCannonTask = new DeleteCannonTask();
		deleteCannonTask.runTaskAsynchronously(plugin);
	}

	public void deleteCannons(UUID player_id){
		if (!plugin.hasConnection()) {
			plugin.logSevere("No connection to database");
			return;
		}
		DeleteCannonTask deleteCannonTask = new DeleteCannonTask(player_id, true);
		deleteCannonTask.runTaskAsynchronously(plugin);
	}

//    /**
//     * save all cannons in the database
//     */
//    public void saveAllCannonsAsync()
//    {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
//            public void run() {
//                saveAllCannons();
//            }
//        });
//    }
//
//
//	/**
//	 * save all cannons in the database
//	 */
//	public void saveAllCannons()
//	{
//
//		// get list of all cannons
//		ConcurrentHashMap<UUID, Cannon> cannonList = CannonManager.getCannonList();
//
//        if (plugin == null || plugin.getDatabase() == null)
//            return;
//
//        plugin.getDatabase().beginTransaction();
//        // save all cannon to database
//        for (Cannon cannon : cannonList.values())
//        {
//            boolean noError = saveCannon(cannon);
//            if (!noError)
//            {
//                //if a error occurs while save the cannon, stop it
//                plugin.getDatabase().endTransaction();
//                return;
//            }
//        }
//        plugin.getDatabase().commitTransaction();
//        plugin.getDatabase().endTransaction();
//	}
//
//    /**
//     * saves this cannon in the database
//     * @param cannon the cannon to store
//     */
//    public void saveCannonAsync(Cannon cannon)
//    {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new DelayedTask(cannon) {
//            public void run(Object object) {
//                Cannon cannon = (Cannon) object;
//                saveCannon(cannon);
//            }
//        });
//    }
//
//	/**
//	 * saves this cannon in the database
//	 * @param cannon the cannon to store
//	 */
//    private boolean saveCannon(Cannon cannon)
//	{
//        //don't save cannons which are not valid anymore
//        if (!cannon.isValid())
//            return true;
//
//
//		try
//		{
//			// search if the is cannon already stored in the database
//			//CannonBean bean = plugin.getDatabase().find(CannonBean.class).where().idEq(cannon.getID()).findUnique();
//            CannonBean bean = plugin.getDatabase().find(CannonBean.class, cannon.getUID());
//
//			if (bean == null)
//			{
//				plugin.logDebug("creating new database entry");
//				// create a new bean that is managed by bukkit
//				bean = plugin.getDatabase().createEntityBean(CannonBean.class);
//
//				bean.setId(cannon.getUID());
//			}
//			else
//			{
//				plugin.logDebug("saving cannon: " + cannon.getUID());
//			}
//
//            if (cannon.getOwner() == null){
//                plugin.logDebug("Owner of cannon is null");
//                return false;
//            }
//
//
//			// fill the bean with values to store
//			// since bukkit manages the bean, we do not need to set
//			// the ID property
//			bean.setOwner(cannon.getOwner());
//			bean.setWorld(cannon.getWorld());
//			// save offset
//			bean.setLocX(cannon.getOffset().getBlockX());
//			bean.setLocY(cannon.getOffset().getBlockY());
//			bean.setLocZ(cannon.getOffset().getBlockZ());
//			// cannon direction
//			bean.setCannonDirection(cannon.getCannonDirection().toString());
//			// name
//			bean.setName(cannon.getCannonName());
//            // must the barrel be clean with the ramrod
//            bean.setSoot(cannon.getSoot());
//			// amount of gunpowder
//			bean.setGunpowder(cannon.getLoadedGunpowder());
//
//			// load projectile
//			// if no projectile is found, set it to air
//			Projectile projectile = cannon.getLoadedProjectile();
//			if (projectile != null)
//			{
//				bean.setProjectileID(projectile.getProjectileId());
//			}
//            else
//            {
//                bean.setProjectileID("none");
//            }
//            //is the projectile already pushed in the barrel
//            bean.setProjectilePushed(cannon.getProjectilePushed());
//
//			// angles
//			bean.setHorizontalAngle(cannon.getHorizontalAngle());
//			bean.setVerticalAngle(cannon.getVerticalAngle());
//			// id
//			bean.setDesignId(cannon.getDesignID());
//            //temperature
//            bean.setCannonTemperature(cannon.getTemperature(false));
//            bean.setCannonTemperatureTimestamp(cannon.getTemperatureTimeStamp());
//            //load fired cannonballs
//            bean.setFiredCannonballs(cannon.getFiredCannonballs());
//
//            //save targets
//            bean.setTargetMob(cannon.isTargetMob());
//            bean.setTargetPlayer(cannon.isTargetPlayer());
//            bean.setTargetCannon(cannon.isTargetCannon());
//
//            //save paid fee
//            bean.setPaid(cannon.isPaid());
//
//            List<WhitelistBean> whitelist = new ArrayList<WhitelistBean>();
//            for (UUID player : cannon.getWhitelist()) {
//                WhitelistBean white = new WhitelistBean();
//                white.setPlayer(player);
//                whitelist.add(white);
//            }
//            bean.setWhitelist(whitelist);
//
//
//			// store the bean
//			plugin.getDatabase().save(bean);
//			//cannon.setID(bean.getId());
//			return true;
//		}
//		catch (Exception e)
//		{
//			plugin.logDebug("can't save to database. " + e);
//			return false;
//		}
//	}
//
//    /**
//     * removes all cannon of this player from the database
//     * @param owner the unique id of the owner of the cannon
//     *
//     */
//    public void deleteCannonsAsync(UUID owner)
//    {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new DelayedTask(owner) {
//            public void run(Object object) {
//                UUID owner = (UUID) object;
//                deleteCannons(owner);
//            }
//        });
//    }
//
//	/**
//	 * removes all cannon of this player from the database
//	 * @param owner the unique id of the owner of the cannon
//     * @return returns true is there is an entry of this player in the database
//	 */
//	private boolean deleteCannons(UUID owner)
//	{
//		// create a query that returns CannonBean
//		List<CannonBean> beans = plugin.getDatabase().find(CannonBean.class).where().eq("owner", owner).findList();
//
//
//        // process the result
//        if (beans == null || beans.size() == 0)
//        {
//            // nothing found; list is empty
//            return false;
//        }
//        else
//        {
//            plugin.getDatabase().beginTransaction();
//            try
//            {
//                // found cannons - load them
//                for (CannonBean bean : beans)
//                {
//                    plugin.getDatabase().delete(CannonBean.class, bean.getId());
//                }
//                plugin.getDatabase().commitTransaction();
//
//            }
//            finally {
//                plugin.getDatabase().endTransaction();
//            }
//            return true;
//        }
//	}
//
//    /**
//     * removes this cannon from the database
//     * @param cannonID ID of the cannon to delete
//     */
//    public void deleteCannonAsync(UUID cannonID)
//    {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new DelayedTask(cannonID) {
//            public void run(Object object) {
//                UUID cannonID = (UUID) object;
//                deleteCannon(cannonID);
//            }
//        });
//    }
//
//	/**
//	 * removes this cannon from the database
//	 * @param cannonID id of the cannon to delete
//	 */
//    private void deleteCannon(UUID cannonID)
//	{
//        CannonBean bean = plugin.getDatabase().find(CannonBean.class, cannonID);
//		// if the database id is null, it is not saved in the database
//		if (bean != null)
//        {
//            plugin.logDebug("removing cannon " + cannonID.toString());
//            plugin.getDatabase().delete(bean);
//        }
//	}
//
//    /**
//     * removes all cannons from the database
//     */
//    public void deleteAllCannonsAsync()
//    {
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
//            public void run() {
//                deleteAllCannons();
//            }
//        });
//    }
//
//    /**
//     * removes all cannons from the database
//     */
//    private void deleteAllCannons()
//    {
//        List<CannonBean> cbean = plugin.getDatabase().find(CannonBean.class).findList();
//        // if the database id is null, it is not saved in the database
//        if (cbean != null)
//        {
//            plugin.getDatabase().delete(cbean);
//        }
//    }

}
