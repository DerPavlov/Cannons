package at.pavlov.Cannons.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotEmpty;


@Entity
@Table(name ="cannonlist")
public class CannonBean
{
	@Id 
	private int id;

	private String name;
	
	private String builder;

	@NotEmpty
	private String world;
	private int locX;
	private int locY;
	private int locZ;
	private int gunpowder;
	private int projectileID;
	private int projectileData;
	private double horizontalAngle;
	private double verticalAngle;
	private boolean Valid;
	
	
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getWorld()
	{
		return world;
	}
	public void setWorld(String world)
	{
		this.world = world;
	}
	public int getLocX()
	{
		return locX;
	}
	public void setLocX(int locX)
	{
		this.locX = locX;
	}
	public int getLocY()
	{
		return locY;
	}
	public void setLocY(int locY)
	{
		this.locY = locY;
	}
	public int getLocZ()
	{
		return locZ;
	}
	public void setLocZ(int locZ)
	{
		this.locZ = locZ;
	}
	public int getGunpowder()
	{
		return gunpowder;
	}
	public void setGunpowder(int gunpowder)
	{
		this.gunpowder = gunpowder;
	}
	public int getProjectileID()
	{
		return projectileID;
	}
	public void setProjectileID(int projectileID)
	{
		this.projectileID = projectileID;
	}
	public int getProjectileData()
	{
		return projectileData;
	}
	public void setProjectileData(int projectileData)
	{
		this.projectileData = projectileData;
	}
	public double getHorizontalAngle()
	{
		return horizontalAngle;
	}
	public void setHorizontalAngle(double horizontalAngle)
	{
		this.horizontalAngle = horizontalAngle;
	}

	public double getVerticalAngle()
	{
		return verticalAngle;
	}
	public void setVerticalAngle(double verticalAngle)
	{
		this.verticalAngle = verticalAngle;
	}
	public String getBuilder()
	{
		return builder;
	}
	public void setBuilder(String builder)
	{
		this.builder = builder;
	}
	public boolean isValid()
	{
		return Valid;
	}
	public void setValid(boolean valid)
	{
		Valid = valid;
	}
	
	
	
}
