package at.pavlov.Cannons.enums;

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
	
	//Aiming
	SettingCombinedAngle ("Aiming.SettingCombinedAngle"),
	SettingVerticalAngleUp ("Aiming.SettingVerticalAngleUp"),
	SettingVerticalAngleDown ("Aiming.SettingVerticalAngleDown"),
	SettingHorizontalAngleRight ("Aiming.SettingHorizontalAngleRight"),
	SettingHorizontalAngleLeft ("Aiming.SettingHorizontalAngleLeft"),
	AimingModeEnabled ("Aiming.EnableAimingMode"),
	AimingModeDisabled ("Aiming.DisableAimingMode"),
	
	//load
	loadProjectile ("load.Projectile"),
	loadGunpowder ("load.Gunpowder"),
	
	//cannon
	CannonCreated ("Cannon.Created"),
	CannonDestroyed ("Cannon.Destroyed"),
	CannonsReseted ("Cannons.Reseted"),
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
