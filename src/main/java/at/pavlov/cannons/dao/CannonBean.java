package at.pavlov.cannons.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import org.bukkit.block.BlockFace;

import java.util.UUID;


@Entity
@Table(name ="cannonlist_2_2")
public class CannonBean
{
	@Id
	private UUID id;

    @Length(max=20)
	private String name;

    @Length(max=20)
	private String owner;

	@NotEmpty
    @Length(max=20)
	private String world;

    @Length(max=20)
	private String cannonDirection;
	private int locX;
	private int locY;
	private int locZ;
    private double soot;
	private int gunpowder;
	private int projectileID;
	private int projectileData;
    private int projectilePushed;
    private double cannonTemperature;
    private long cannonTemperatureTimestamp;
	private double horizontalAngle;
	private double verticalAngle;

    @Length(max=20)
	private String designId;
	private boolean Valid;

    private long firedCannonballs;
	
	
	
	public UUID getId()
	{
		return id;
	}
	public void setId(UUID id)
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
	public boolean isValid()
	{
		return Valid;
	}
	public void setValid(boolean valid)
	{
		Valid = valid;
	}
	public String getOwner()
	{
		return owner;
	}
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	public String getCannonDirection()
	{
		return cannonDirection;
	}
	public void setCannonDirection(String cannonDirection)
	{
		this.cannonDirection = cannonDirection;
	}
	public String getDesignId()
	{
		return designId;
	}
	public void setDesignId(String designId)
	{
		this.designId = designId;
	}
    public double getCannonTemperature() {
        return cannonTemperature;
    }
    public void setCannonTemperature(double cannonTemperature) {
        this.cannonTemperature = cannonTemperature;
    }
    public int getProjectilePushed() {
        return projectilePushed;
    }
    public void setProjectilePushed(int projectilePushed) {
        this.projectilePushed = projectilePushed;
    }
    public double getSoot() {
        return soot;
    }
    public void setSoot(double soot) {
        this.soot = soot;
    }
    public long getCannonTemperatureTimestamp() {
        return cannonTemperatureTimestamp;
    }
    public void setCannonTemperatureTimestamp(long cannonTemperatureTimestamp) {
        this.cannonTemperatureTimestamp = cannonTemperatureTimestamp;
    }

    public long getFiredCannonballs() {
        return firedCannonballs;
    }

    public void setFiredCannonballs(long firedCannonballs) {
        this.firedCannonballs = firedCannonballs;
    }
}
