package at.pavlov.cannons.config;

public enum MessageEnum
{
	//Error Messages
	ErrorBarrelTooHot ("Error.BarrelTooHot"),
	ErrorNoProjectile ("Error.NoProjectile"),
	ErrorNoGunpowder ("Error.NoGunpowder"),
	ErrorNoFlintAndSteel ("Error.NoFlintAndSteel"),
	ErrorMaximumGunpowderLoaded ("Error.MaximumGunpowderLoaded"),
	ErrorProjectileAlreadyLoaded ("Error.ProjectileAlreadyLoaded"),
	ErrorCannonBuiltLimit ("Error.CannonBuiltLimit"),
	ErrorNotTheOwner ("Error.NotTheOwner"),
    ErrorMissingSign ("Error.MissingSign"),
	
	//Aiming
	SettingCombinedAngle ("Aiming.SettingCombinedAngle"),
	SettingVerticalAngleUp ("Aiming.SettingVerticalAngleUp"),
	SettingVerticalAngleDown ("Aiming.SettingVerticalAngleDown"),
	SettingHorizontalAngleRight ("Aiming.SettingHorizontalAngleRight"),
	SettingHorizontalAngleLeft ("Aiming.SettingHorizontalAngleLeft"),
	AimingModeEnabled ("Aiming.EnableAimingMode"),
	AimingModeDisabled ("Aiming.DisableAimingMode"),
	
	//load
	loadProjectile ("Load.Projectile"),
	loadGunpowder ("Load.Gunpowder"),
	
	//cannon
	CannonCreated ("Cannon.Created"),
	CannonDestroyed ("Cannon.Destroyed"),
    CannonsReseted ("Cannon.Reseted"),
	CannonFire ("Cannon.Fire"),
	
	//Permission
	PermissionErrorRedstone ("Permission.ErrorRedstone"),
	PermissionErrorBuild ("Permission.ErrorBuild"),
	PermissionErrorFire ("Permission.ErrorFire"),
	PermissionErrorLoad ("Permission.ErrorLoad"),
	PermissionErrorAdjust ("Permission.ErrorAdjust"),
	PermissionErrorProjectile ("Permission.ErrorProjectile"),
	
	//Help
	HelpText ("Help.Text"),
	HelpBuild ("Help.Build"),
    HelpFire ("Help.Fire"),
	HelpAdjust ("Help.Adjust");

	
	private final String str;
	
	MessageEnum(String str)
	{
		this.str = str;
	}

	public String getString()
	{
		return str;
	}
}
