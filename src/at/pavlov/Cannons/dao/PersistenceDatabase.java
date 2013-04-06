package at.pavlov.Cannons.dao;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.avaje.ebean.Query;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.cannon.Cannon;

public class PersistenceDatabase
{
	private final Cannons plugin;
	private List<Object> obsoleteIdList;

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

				World world = Bukkit.getWorld(bean.getWorld());
				if (world == null)
				{
					plugin.logSevere("World '" + bean.getWorld() + "' for cannon not found");
				}
				else
				{
					Location loc = new Location(world, bean.getLocX(), bean.getLocY(), bean.getLocZ());
					
					// find the cannon to this block
					Cannon cannon = plugin.getCannonManager().getCannon(loc, bean.getOwner());

					if (cannon != null)
					{
						// cannon found - load properties
						cannon.setDatabaseId(bean.getId());
						cannon.setCannonName(bean.getName());
						cannon.setOwner(bean.getOwner());
						cannon.setLoadedGunpowder(bean.getGunpowder());
						cannon.setProjectileID(bean.getProjectileID());
						cannon.setProjectileData(bean.getProjectileData());
						cannon.setHorizontalAngle(bean.getHorizontalAngle());
						cannon.setVerticalAngle(bean.getVerticalAngle());
						cannon.setDesignID(bean.getDesignId());
						cannon.setValid(bean.isValid());
						cannon.setCannonDesign(plugin.getCannonDesign(cannon));
						
						//update sign
						cannon.updateCannonSigns();
					}
					else
					{
						// no cannon found at this position
						plugin.logInfo("Unable to find cannon at x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
					}
				}

			}
			return true;
		}
	}

	/**
	 * save all Cannons to the Database
	 */

	public void saveAllCannons()
	{
		//get the list of all id's to remove obsolete cannons later
		obsoleteIdList = plugin.getDatabase().find(CannonBean.class).findIds();
		
		// get list of all cannons
		List<Cannon> cannonList = plugin.getCannonManager().getCannonList();

		// save all cannon to database
		for (Cannon cannon : cannonList)
		{
			saveCannon(cannon);
		}
		
		// if the obsoleteList is empty there are no cannon to remove
		if (obsoleteIdList == null || obsoleteIdList.size() == 0) return; 
		
		//deactivated, since the cannon can be every if signs are used
		//remove all row with the id
		//for (Object id : obsoleteIdList)
		//{
		//	plugin.getDatabase().delete(CannonBean.class, id);
		//}
		
		
	}

	private void saveCannon(Cannon cannon)
	{
		//search if the is cannon already stored in the database
		CannonBean bean = plugin.getDatabase().find(CannonBean.class).where().idEq(cannon.getDatabaseId()).findUnique();
		
		if (bean == null)
		{
			// create a new bean that is managed by bukkit
			bean = plugin.getDatabase().createEntityBean(CannonBean.class);
			// fill the bean with values to store
			// since bukkit manages the bean, we do not need to set
			// the ID property
			bean.setOwner(cannon.getOwner());
			bean.setWorld(cannon.getWorld());
			// save offset
			bean.setLocX(cannon.getOffset().getBlockX());
			bean.setLocY(cannon.getOffset().getBlockY());
			bean.setLocZ(cannon.getOffset().getBlockZ());
		
		}
		
		bean.setName(cannon.getCannonName());
		bean.setGunpowder(cannon.getLoadedGunpowder());
		bean.setProjectileID(cannon.getProjectileID());
		bean.setProjectileData(cannon.getProjectileData());
		bean.setHorizontalAngle(cannon.getHorizontalAngle());
		bean.setVerticalAngle(cannon.getVerticalAngle());
		bean.setDesignId(cannon.getDesignID());
		bean.setValid(cannon.isValid());

		// store the bean
		plugin.getDatabase().save(bean);
		cannon.setDatabaseId(bean.getId());
		
		//this cannons is not obsolete - remove it from the list
		removeObsoleteID(cannon.getDatabaseId());
	}
	
	private void removeObsoleteID(int id)
	{
		if (obsoleteIdList == null || obsoleteIdList.size() == 0) return; 
		
		Iterator<Object> iter = obsoleteIdList.iterator();
		while(iter.hasNext())
		{
			if (iter.next().equals(id))
			{
				iter.remove();
			}
		}
	}
	
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
