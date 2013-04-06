package at.pavlov.Cannons.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import at.pavlov.Cannons.Cannons;
import at.pavlov.Cannons.projectile.Projectile;

public class ProjectileStorage
{
	Cannons plugin;
	List<Projectile> projectileList = new ArrayList<Projectile>();
	
	public ProjectileStorage(Cannons plugin)
	{
		this.plugin = plugin;
	}
	
	public Projectile getProjectile(ItemStack item)
	{
		return getProjectile(item.getTypeId(), item.getData().getData());
	}
	
	public Projectile getProjectile(int id, int data)
	{
		for (Projectile projectile : projectileList)
		{
			if (projectile.equalsFuzzy(id, data))
				return projectile;
		}
		return null;
	}
	
	public void loadProjectiles()
	{
		
	}
	
	
	

}
