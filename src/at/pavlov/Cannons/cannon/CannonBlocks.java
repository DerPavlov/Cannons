package at.pavlov.Cannons.cannon;

import java.util.ArrayList;

import org.bukkit.util.Vector;

import at.pavlov.Cannons.container.SimpleBlock;


public class CannonBlocks
{
	private Vector rotationCenter;													//center off all rotation blocks
    private Vector muzzle;																//center off all muzzle blocks - spawing Vector for snowball
    private ArrayList<SimpleBlock> allCannonBlocks = new ArrayList<SimpleBlock>();
    private ArrayList<Vector> loadingInterface = new ArrayList<Vector>();
    private ArrayList<Vector> chests = new ArrayList<Vector>();
    private ArrayList<Vector> signs = new ArrayList<Vector>();
    private ArrayList<Vector> redstoneTorches = new ArrayList<Vector>();
    private ArrayList<Vector> redstoneWires = new ArrayList<Vector>();
    private ArrayList<Vector> repeater = new ArrayList<Vector>();
    private ArrayList<Vector> redstoneTrigger = new ArrayList<Vector>();
    private ArrayList<Vector> rightClickTrigger = new ArrayList<Vector>();
    private ArrayList<Vector> firingIndicator = new ArrayList<Vector>();
    
    
	public Vector getRotationCenter()
	{
		return rotationCenter;
	}
	public void setRotationCenter(Vector rotationCenter)
	{
		this.rotationCenter = rotationCenter;
	}
	public Vector getMuzzle()
	{
		return muzzle;
	}
	public void setMuzzle(Vector muzzle)
	{
		this.muzzle = muzzle;
	}
	public ArrayList<SimpleBlock> getAllCannonBlocks()
	{
		return allCannonBlocks;
	}
	public void setAllCannonBlocks(ArrayList<SimpleBlock> allCannonBlocks)
	{
		this.allCannonBlocks = allCannonBlocks;
	}
	public ArrayList<Vector> getLoadingInterface()
	{
		return loadingInterface;
	}
	public void setLoadingInterface(ArrayList<Vector> loadingInterface)
	{
		this.loadingInterface = loadingInterface;
	}
	public ArrayList<Vector> getChests()
	{
		return chests;
	}
	public void setChests(ArrayList<Vector> chests)
	{
		this.chests = chests;
	}
	public ArrayList<Vector> getSigns()
	{
		return signs;
	}
	public void setSigns(ArrayList<Vector> signs)
	{
		this.signs = signs;
	}
	public ArrayList<Vector> getRedstoneTorches()
	{
		return redstoneTorches;
	}
	public void setRedstoneTorches(ArrayList<Vector> redstoneTorches)
	{
		this.redstoneTorches = redstoneTorches;
	}
	public ArrayList<Vector> getRedstoneWires()
	{
		return redstoneWires;
	}
	public void setRedstoneWires(ArrayList<Vector> redstoneWires)
	{
		this.redstoneWires = redstoneWires;
	}
	public ArrayList<Vector> getRepeater()
	{
		return repeater;
	}
	public void setRepeater(ArrayList<Vector> repeater)
	{
		this.repeater = repeater;
	}
	public ArrayList<Vector> getRedstoneTrigger()
	{
		return redstoneTrigger;
	}
	public void setRedstoneTrigger(ArrayList<Vector> redstoneTrigger)
	{
		this.redstoneTrigger = redstoneTrigger;
	}
	public ArrayList<Vector> getRightClickTrigger()
	{
		return rightClickTrigger;
	}
	public void setRightClickTrigger(ArrayList<Vector> rightClickTrigger)
	{
		this.rightClickTrigger = rightClickTrigger;
	}
	public ArrayList<Vector> getFiringIndicator()
	{
		return firingIndicator;
	}
	public void setFiringIndicator(ArrayList<Vector> firingIndicator)
	{
		this.firingIndicator = firingIndicator;
	}
    
}