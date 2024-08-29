package at.pavlov.cannons.cannon;

import at.pavlov.cannons.container.SimpleBlock;
import org.bukkit.util.Vector;

import java.util.ArrayList;


class CannonBlocks
{
	private Vector rotationCenter;	//center off all rotation blocks
    private Vector muzzle;			//center off all muzzle blocks - spawing Vector for snowball

	private ArrayList<SimpleBlock> allCannonBlocks = new ArrayList<>();
    private ArrayList<Vector> barrelBlocks = new ArrayList<>();
    private ArrayList<SimpleBlock> chestsAndSigns = new ArrayList<>();
    private ArrayList<Vector> redstoneTorches = new ArrayList<>();
    private ArrayList<SimpleBlock> redstoneWiresAndRepeater = new ArrayList<>();
    private ArrayList<Vector> redstoneTrigger = new ArrayList<>();
    private ArrayList<Vector> rightClickTrigger = new ArrayList<>();
    private ArrayList<Vector> firingIndicator = new ArrayList<>();
    private ArrayList<Vector> destructibleBlocks = new ArrayList<>();
    

    /**
     * returns true if this block is part of the loading interface
     * @param loc
     * @return
     */
    public boolean isLoadingInterface(Vector loc) {
    	for (Vector loadingBlock : barrelBlocks) {
    		if (loc.equals(loadingBlock)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * returns the location off one firing Trigger
     * @return the firing trigger. (can be null if there is no trigger on the cannon)
     */
    public Vector getFiringTrigger() {
    	//return one tigger
    	if (rightClickTrigger!= null && !rightClickTrigger.isEmpty())
    		return rightClickTrigger.get(0);	
    	if (redstoneTrigger != null && !redstoneTrigger.isEmpty())
        	return redstoneTrigger.get(0);
        return null;
    }
    
	public Vector getRotationCenter() {
		return rotationCenter;
	}
	public void setRotationCenter(Vector rotationCenter) {
		this.rotationCenter = rotationCenter;
	}

	public Vector getMuzzle() {
		return muzzle;
	}
	public void setMuzzle(Vector muzzle) {
		this.muzzle = muzzle;
	}

	public ArrayList<SimpleBlock> getAllCannonBlocks() {
		return allCannonBlocks;
	}
	public void setAllCannonBlocks(ArrayList<SimpleBlock> allCannonBlocks) {
		this.allCannonBlocks = allCannonBlocks;
	}
	public void addAllCannonBlocks(SimpleBlock add) {
		this.allCannonBlocks.add(add);
	}

	public ArrayList<Vector> getBarrelBlocks() {
		return barrelBlocks;
	}
	public void setBarrel(ArrayList<Vector> barrelBlocks) {
		this.barrelBlocks = barrelBlocks;
	}
	public void addBarrel(Vector add) {
		this.barrelBlocks.add(add);
	}

	public ArrayList<Vector> getRedstoneTorches() {
		return redstoneTorches;
	}
	public void setRedstoneTorches(ArrayList<Vector> redstoneTorches) {
		this.redstoneTorches = redstoneTorches;
	}
	public void addRedstoneTorch(Vector add) {
		this.redstoneTorches.add(add);
	}

	public ArrayList<Vector> getRedstoneTrigger() {
		return redstoneTrigger;
	}
	public void setRedstoneTrigger(ArrayList<Vector> redstoneTrigger) {
		this.redstoneTrigger = redstoneTrigger;
	}
	public void addRedstoneTrigger(Vector add) {
		this.redstoneTrigger.add(add);
	}

	public ArrayList<Vector> getRightClickTrigger() {
		return rightClickTrigger;
	}
	public void setRightClickTrigger(ArrayList<Vector> rightClickTrigger) {
		this.rightClickTrigger = rightClickTrigger;
	}
	public void addRightClickTrigger(Vector add) {
		this.rightClickTrigger.add(add);
	}

	public ArrayList<SimpleBlock> getChestsAndSigns() {
		return chestsAndSigns;
	}
	public void setChestsAndSigns(ArrayList<SimpleBlock> chestsAndSigns) {
		this.chestsAndSigns = chestsAndSigns;
	}
	public void addChestsAndSigns(SimpleBlock add) {
		this.chestsAndSigns.add(add);
	}

	public ArrayList<SimpleBlock> getRedstoneWiresAndRepeater() {
		return redstoneWiresAndRepeater;
	}
	public void setRedstoneWiresAndRepeater(ArrayList<SimpleBlock> redstoneWiresAndRepeater) {
		this.redstoneWiresAndRepeater = redstoneWiresAndRepeater;
	}
	public void addRedstoneWiresAndRepeater(SimpleBlock add) {
		this.redstoneWiresAndRepeater.add(add);
	}

	public ArrayList<Vector> getFiringIndicator() {
		return firingIndicator;
	}
	public void setFiringIndicator(ArrayList<Vector> firingIndicator) {
		this.firingIndicator = firingIndicator;
	}
	public void addFiringIndicator(Vector add) {
		this.firingIndicator.add(add);
	}

	public ArrayList<Vector> getDestructibleBlocks() {
		return destructibleBlocks;
	}
	public void setDestructibleBlocks(ArrayList<Vector> destructibleBlocks) {
		this.destructibleBlocks = destructibleBlocks;
	}
	public void addDestructibleBlocks(Vector add) {
		this.destructibleBlocks.add(add);
	}
    
}