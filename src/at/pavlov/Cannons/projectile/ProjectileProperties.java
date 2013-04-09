package at.pavlov.Cannons.projectile;

public enum ProjectileProperties
{
	SUPERBREAKER ("SUPERBREAKER"),
	INCENDIARY ("INCENDIARY"),
	SHOOTER_AS_PASSENGER ("SHOOTER_AS_PASSENGER"),
	CLUSTERBOMB ("CLUSTERBOMB");
	
	String string;
	
	ProjectileProperties(String str)
	{
		this.string = str;
	}
	
	public String getString()
	{
		return string;
	}
}
