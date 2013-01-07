package at.pavlov.Cannons.dao;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.avaje.ebean.Query;

import at.pavlov.Cannons.Cannons;

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
					plugin.logSevere("World for cannon not found");
				}
				else
				{
					Location loc = new Location(world, bean.getLocX(), bean.getLocY(), bean.getLocZ());
					// find the cannon to this block
					CannonData cannonData = plugin.getCannonList().find_cannon(loc, null);

					if (cannonData != null)
					{
						// cannon found - load properties
						cannonData.id = bean.getId();
						cannonData.name = bean.getName();
						cannonData.owner = bean.getOwner();
						cannonData.gunpowder = bean.getGunpowder();
						cannonData.projectileID = bean.getProjectileID();
						cannonData.projectileData = bean.getProjectileData();
						cannonData.horizontal_angle = bean.getHorizontalAngle();
						cannonData.vertical_angle = bean.getVerticalAngle();
						cannonData.designId = bean.getDesignId();
						cannonData.isValid = bean.isValid();
					}
					else
					{
						// no cannon found at this position
						plugin.logSevere("Unable to find cannon at " + loc.toString());
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
		List<CannonData> cannonList = plugin.getCannonList().getCannonList();

		// save all cannon to database
		for (CannonData cannon : cannonList)
		{
			saveCannon(cannon);
		}
		
		// if the obsoleteList is empty there are no cannon to remove
		if (obsoleteIdList == null || obsoleteIdList.size() == 0) return; 
		
		//remove all row with the id
		for (Object id : obsoleteIdList)
		{
			plugin.getDatabase().delete(CannonBean.class, id);
		}
		
		
	}

	private void saveCannon(CannonData cannon)
	{
		//search if the is cannon already stored in the database
		CannonBean bean = plugin.getDatabase().find(CannonBean.class).where().idEq(cannon.id).findUnique();
		
		if (bean == null)
		{
			// create a new bean that is managed by bukkit
			bean = plugin.getDatabase().createEntityBean(CannonBean.class);
			// fill the bean with values to store
			// since bukkit manages the bean, we do not need to set
			// the ID property
			bean.setOwner(cannon.owner);
			bean.setWorld(cannon.location.getWorld().getName());
			// get one Cannonblock
			if (cannon.CannonBlocks.size() > 0)
			{
				bean.setLocX(cannon.CannonBlocks.get(0).getBlockX());
				bean.setLocY(cannon.CannonBlocks.get(0).getBlockY());
				bean.setLocZ(cannon.CannonBlocks.get(0).getBlockZ());
			}
			else
			{
				bean.setLocX(cannon.location.getBlockX());
				bean.setLocY(cannon.location.getBlockY());
				bean.setLocZ(cannon.location.getBlockZ());
			}
		}
		
		bean.setName(cannon.name);
		bean.setGunpowder(cannon.gunpowder);
		bean.setProjectileID(cannon.projectileID);
		bean.setProjectileData(cannon.projectileData);
		bean.setHorizontalAngle(cannon.horizontal_angle);
		bean.setVerticalAngle(cannon.vertical_angle);
		bean.setDesignId(cannon.designId);
		bean.setValid(cannon.isValid);

		// store the bean
		plugin.getDatabase().save(bean);
		cannon.id = bean.getId();
		
		//this cannons is not obsolete - remove it from the list
		removeObsoleteID(cannon.id);
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

}
