package at.pavlov.Cannons.dao;

import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import com.avaje.ebean.Query;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.cannon.Cannon;

public class PersistenceDatabase
{
	private final Cannons plugin;

	public PersistenceDatabase(Cannons _plugin)
	{
		plugin = _plugin;
	}

	public boolean loadCannons()
	{
		// create a query that returns CannonBean
		Query<CannonBean> query = plugin.getDatabase().find(CannonBean.class);
		List<CannonBean> beans = query.findList();

		// process the result
		if (beans == null || beans.size() == 0)
		{
			// nothing found; list is empty
			return false;
		}
		else
		{
			// found cannons - load them
			for (CannonBean bean : beans)
			{

				//load values for the cannon
				String world = bean.getWorld();
				Vector vect = new Vector(bean.getLocX(), bean.getLocY(), bean.getLocZ());
				BlockFace cannonDirection = BlockFace.valueOf(bean.getCannonDirection());
				String owner = bean.getOwner();

				//make a cannon
				Cannon cannon = new Cannon(plugin.getCannonDesign(bean.getDesignId()), world, vect, cannonDirection, owner);
				// cannon created - load properties
				cannon.setDatabaseId(bean.getId());
				cannon.setCannonName(bean.getName());
				cannon.setLoadedGunpowder(bean.getGunpowder());
				cannon.setProjectileID(bean.getProjectileID());
				cannon.setProjectileData(bean.getProjectileData());
				cannon.setHorizontalAngle(bean.getHorizontalAngle());
				cannon.setVerticalAngle(bean.getVerticalAngle());
				cannon.setValid(bean.isValid());

				//add a cannon to the cannon list
				plugin.createCannon(cannon);
				
				// update sign
				cannon.updateCannonSigns();

			}
			return true;
		}
	}

	/**
	 * save all cannons in the database
	 */
	public void saveAllCannons()
	{

		// get list of all cannons
		List<Cannon> cannonList = plugin.getCannonManager().getCannonList();

		// save all cannon to database
		for (Cannon cannon : cannonList)
		{
			saveCannon(cannon);
		}
	}

	/**
	 * saves this cannon in the database
	 * 
	 * @param cannon
	 */
	private void saveCannon(Cannon cannon)
	{
		// search if the is cannon already stored in the database
		CannonBean bean = plugin.getDatabase().find(CannonBean.class).where().idEq(cannon.getDatabaseId()).findUnique();

		if (bean == null)
		{
			// create a new bean that is managed by bukkit
			bean = plugin.getDatabase().createEntityBean(CannonBean.class);
		}

		// fill the bean with values to store
		// since bukkit manages the bean, we do not need to set
		// the ID property
		bean.setOwner(cannon.getOwner());
		bean.setWorld(cannon.getWorld());
		// save offset
		bean.setLocX(cannon.getOffset().getBlockX());
		bean.setLocY(cannon.getOffset().getBlockY());
		bean.setLocZ(cannon.getOffset().getBlockZ());
		// cannon direction
		bean.setCannonDirection(cannon.getCannonDirection().toString());
		// name
		bean.setName(cannon.getCannonName());
		// amount of gunpowder
		bean.setGunpowder(cannon.getLoadedGunpowder());
		// projectile
		bean.setProjectileID(cannon.getProjectileID());
		bean.setProjectileData(cannon.getProjectileData());
		// angles
		bean.setHorizontalAngle(cannon.getHorizontalAngle());
		bean.setVerticalAngle(cannon.getVerticalAngle());
		// id
		bean.setDesignId(cannon.getDesignID());
		bean.setValid(cannon.isValid());

		// store the bean
		plugin.getDatabase().save(bean);
		cannon.setDatabaseId(bean.getId());

	}

	/**
	 * removes all cannon of this player from the database
	 * 
	 * @param owner
	 */
	public void deleteCannons(String owner)
	{
		// create a query that returns CannonBean
		List<CannonBean> beans = plugin.getDatabase().find(CannonBean.class).where().eq("owner", owner).findList();

		// process the result
		if (beans == null || beans.size() == 0)
		{
			// nothing found; list is empty
			return;
		}
		else
		{
			// found cannons - load them
			for (CannonBean bean : beans)
			{
				plugin.getDatabase().delete(CannonBean.class, bean.getId());
			}
		}
	}

}
