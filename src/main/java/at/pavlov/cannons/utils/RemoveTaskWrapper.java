package at.pavlov.cannons.utils;

import at.pavlov.cannons.Enum.BreakCause;
import at.pavlov.cannons.cannon.Cannon;


public class RemoveTaskWrapper {
	private Cannon cannon;
    private boolean breakCannon;
    private boolean canExplode;
    private BreakCause cause;
    private boolean removeEntry;
    private boolean ignoreInvalid;

	public RemoveTaskWrapper(Cannon cannon, boolean breakCannon, boolean canExplode, BreakCause cause, boolean removeEntry, boolean ignoreInvalid)
    {
        this.cannon = cannon;
        this.breakCannon = breakCannon;
        this.canExplode = canExplode;
        this.cause = cause;
        this.removeEntry = removeEntry;
        this.ignoreInvalid = ignoreInvalid;
	}

    public Cannon getCannon() {
        return cannon;
    }

    public void setCannon(Cannon cannon) {
        this.cannon = cannon;
    }

    public boolean breakCannon() {
        return breakCannon;
    }

    public void setBreakCannon(boolean breakCannon) {
        this.breakCannon = breakCannon;
    }

    public boolean canExplode() {
        return canExplode;
    }

    public void setCanExplode(boolean canExplode) {
        this.canExplode = canExplode;
    }

    public BreakCause getCause() {
        return cause;
    }

    public void setCause(BreakCause cause) {
        this.cause = cause;
    }

    public boolean removeEntry() {
        return removeEntry;
    }

    public void setRemoveEntry(boolean removeEntry) {
        this.removeEntry = removeEntry;
    }

    public boolean ignoreInvalid() {
        return ignoreInvalid;
    }

    public void setIgnoreInvalid(boolean ignoreInvalid) {
        this.ignoreInvalid = ignoreInvalid;
    }
}
