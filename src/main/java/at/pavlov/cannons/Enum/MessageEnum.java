package at.pavlov.cannons.Enum;

public enum MessageEnum
{
	//Error Messages
    ErrorFiringInProgress ("Error.FiringInProgress", true),
	ErrorBarrelTooHot ("Error.BarrelTooHot", true),
    ErrorNotCleaned ("Error.NotCleaned", true),
	ErrorNoGunpowder ("Error.NoGunpowder", true),
    ErrorNoProjectile ("Error.NoProjectile", true),
    ErrorNotPushed ("Error.NotPushed", true),
	ErrorNoFlintAndSteel ("Error.NoFlintAndSteel", true),
	ErrorMaximumGunpowderLoaded ("Error.MaximumGunpowderLoaded", true),
	ErrorProjectileAlreadyLoaded ("Error.ProjectileAlreadyLoaded", true),
	ErrorCannonBuiltLimit ("Error.CannonBuiltLimit", true),
	ErrorNotTheOwner ("Error.NotTheOwner", true),
    ErrorMissingSign ("Error.MissingSign", true),
	
	//Aiming
	SettingCombinedAngle ("Aiming.SettingCombinedAngle"),
	SettingVerticalAngleUp ("Aiming.SettingVerticalAngleUp"),
	SettingVerticalAngleDown ("Aiming.SettingVerticalAngleDown"),
	SettingHorizontalAngleRight ("Aiming.SettingHorizontalAngleRight"),
	SettingHorizontalAngleLeft ("Aiming.SettingHorizontalAngleLeft"),
	AimingModeEnabled ("Aiming.EnableAimingMode"),
	AimingModeDisabled ("Aiming.DisableAimingMode"),
    AimingModeTooFarAway ("Aiming.TooFarForAimingMode"),
	
	//load
	loadProjectile ("Load.Projectile"),
	loadGunpowder ("Load.Gunpowder"),
	
	//cannon
	CannonCreated ("Cannon.Created"),
	CannonDestroyed ("Cannon.Destroyed"),
    CannonsReseted ("Cannon.Reseted"),
	CannonFire ("Cannon.Fire"),

    //projectile
    ProjectileExplosion ("Projectile.Explosion"),
    ProjectileCanceled ("Projectile.Canceled"),

    //heatManagement
    HeatManagementBurn ("HeatManagement.Burn"),
    HeatManagementCooling ("HeatManagement.Cooling"),
    HeatManagementInfo ("HeatManagement.Info"),
    HeatManagementCritical ("HeatManagement.Critical"),
    HeatManagementOverheated ("HeatManagement.Overheated"),

    //ramrod
    RamrodCleaning ("Ramrod.Cleaning"),
    RamrodCleaningDone ("Ramrod.CleaningDone"),
    RamrodPushingProjectile ("Ramrod.PushingProjectile"),
    RamrodPushingProjectileDone ("Ramrod.PushingProjectileDone"),

    //imitatedEffects
    ImitatedEffectsEnabled ("ImitatedEffects.Enabled"),
    ImitatedEffectsDisabled ("ImitatedEffects.Disabled"),
	
	//Permission
	PermissionErrorRedstone ("Permission.ErrorRedstone", true),
	PermissionErrorBuild ("Permission.ErrorBuild", true),
	PermissionErrorFire ("Permission.ErrorFire", true),
	PermissionErrorLoad ("Permission.ErrorLoad", true),
	PermissionErrorAdjust ("Permission.ErrorAdjust", true),
	PermissionErrorProjectile ("Permission.ErrorProjectile", true),
    PermissionErrorThermometer ("Permission.ErrorThermometer", true),
    PermissionErrorRamrod ("Permission.ErrorRamrod", true),
	
	//Help
	HelpText ("Help.Text"),
	HelpBuild ("Help.Build"),
    HelpFire ("Help.Fire"),
	HelpAdjust ("Help.Adjust");

	
	private final String str;
	private final boolean isError;
	
	MessageEnum(String str, boolean e)
	{
		this.str = str;
		isError = e;
	}
	MessageEnum(String str)
	{
		this.str = str;
		isError = false;
	}

	public String getString()
	{
		return str;
	}
	public boolean isError()
	{
		return isError;
	}
}
