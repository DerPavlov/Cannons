package at.pavlov.cannons.projectile;

public enum ProjectileProperties
{
	SUPERBREAKER("SUPERBREAKER"), 
	INCENDIARY("INCENDIARY"), 
	HUMAN_CANNONBALL("HUMAN_CANNONBALL"),
	TELEPORT("TELEPORT"),
    OBSERVER("OBSERVER");

	private final String string;

	ProjectileProperties(String str)
	{
		this.string = str;
	}

	String getString()
	{
		return string;
	}

	public static ProjectileProperties getByName(String str)
	{
		if (str != null)
		{
			for (ProjectileProperties p : ProjectileProperties.values())
			{
				if (str.equalsIgnoreCase(p.getString()))
				{
					return p;
				}
			}
		}
		return null;
	}
}
