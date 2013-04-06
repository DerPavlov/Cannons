package at.pavlov.Cannons.cannon;

import java.util.ArrayList;

import org.bukkit.util.Vector;

import at.pavlov.Cannons.container.SimpleBlock;


public class CannonBlocks
{
	private Vector rotationCenter;														//center off all rotation blocks
    private Vector muzzle;																//center off all muzzle blocks - spawing Vector for snowball
    private ArrayList<SimpleBlock> allCannonBlocks = new ArrayList<SimpleBlock>();
    private ArrayList<Vector> loadingInterface = new ArrayList<Vector>();
    private ArrayList<Vector> chestsAndSigns = new ArrayList<Vector>();
    private ArrayList<Vector> redstoneTorches = new ArrayList<Vector>();
    private ArrayList<Vector> redstoneWiresAndRepeater = new ArrayList<Vector>();
    private ArrayList<Vector> redstoneTrigger = new ArrayList<Vector>();
    private ArrayList<Vector> rightClickTrigger = new ArrayList<Vector>();
    private ArrayList<Vector> firingIndicator = new ArrayList<Vector>();
    

    /**
     * returns true if this block is part of the loading interface
     * @param loc
     * @return
     */
    public boolean isLoadingInterface(Vector loc)
    {
    	for (Vector loadingBlock : loadingInterface)
    	{
    		if (loc.equals(loadingBlock))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * returns the location off one firing Trigger
     * @param loc
     * @return
     */
    public Vector getFiringTrigger()
    {
    	//return one tigger
    	if (rightClickTrigger != null && rightClickTrigger.size() > 0)
    		return rightClickTrigger.get(0);	
    	if (redstoneTrigger != null && redstoneTrigger.size() > 0)
        		return redstoneTrigger.get(0);
        return null;	  	
    }
    
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
	public ArrayList<Vector> getRedstoneTorches()
	{
		return redstoneTorches;
	}
	public void setRedstoneTorches(ArrayList<Vector> redstoneTorches)
	{
		this.redstoneTorches = redstoneTorches;
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

	public ArrayList<Vector> getChestsAndSigns()
	{
		return chestsAndSigns;
	}

	public void setChestsAndSigns(ArrayList<Vector> chestsAndSigns)
	{
		this.chestsAndSigns = chestsAndSigns;
	}

	public ArrayList<Vector> getRedstoneWiresAndRepeater()
	{
		return redstoneWiresAndRepeater;
	}

	public void setRedstoneWiresAndRepeater(ArrayList<Vector> redstoneWiresAndRepeater)
	{
		this.redstoneWiresAndRepeater = redstoneWiresAndRepeater;
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