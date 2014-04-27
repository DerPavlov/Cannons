package at.pavlov.cannons.Enum;

public enum OverloadingType
{
    DISABLED(false),
    SAFE(true),
    REAL(true);

    OverloadingType(boolean e)
    {
        enabled = e;
    }
    boolean enabled;
    public boolean isEnabled()
    {
        return enabled;
    }
    public static OverloadingType get(int b)
    {
        switch(b)
        {
            case 1: return OverloadingType.SAFE;
            case 2: return OverloadingType.REAL;
            default: return OverloadingType.DISABLED;
        }
    }
}
