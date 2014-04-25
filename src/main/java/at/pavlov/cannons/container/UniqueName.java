package at.pavlov.cannons.container;

public class UniqueName
{
    private String cannonName;
    private String owner;

    public UniqueName(String cannonName, String owner)
    {
        this.cannonName = cannonName;
        this.owner = owner;
    }

    public String getCannonName() {
        return cannonName;
    }

    public void setCannonName(String cannonName) {
        this.cannonName = cannonName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}