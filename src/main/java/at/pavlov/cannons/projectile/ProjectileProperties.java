package at.pavlov.cannons.projectile;

public enum ProjectileProperties
{
	SUPERBREAKER("SUPERBREAKER"), 
	INCENDIARY("INCENDIARY"), 
	SHOOTER_AS_PASSENGER("SHOOTER_AS_PASSENGER"), 
	TELEPORT("TELEPORT"),
    OBSERVER("OBSERVER");

	String string;

	ProjectileProperties(String str)
	{
		this.string = str;
	}

	public String getString()
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
