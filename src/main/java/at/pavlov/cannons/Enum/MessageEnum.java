package at.pavlov.cannons.Enum;

public enum MessageEnum
{
	//Error Messages
    ErrorFiringInProgress ("Error.FiringInProgress", true),
	ErrorLoadingInProgress ("Error.LoadingInProgress", true),
	ErrorBarrelTooHot ("Error.BarrelTooHot", true),
    ErrorNotCleaned ("Error.NotCleaned", true),
	ErrorNoGunpowder ("Error.NoGunpowder", true),
    ErrorNoGunpowderNeeded ("Error.NoGunpowderNeeded", true),
    ErrorNoProjectile ("Error.NoProjectile", true),
    ErrorNoGunpowderInChest ("Error.NoGunpowderInChest", true),
    ErrorNoProjectileInChest ("Error.NoProjectileInChest", true),
    ErrorNotPushed ("Error.NotPushed", true),
	ErrorNoFlintAndSteel ("Error.NoFlintAndSteel", true),
	ErrorMaximumGunpowderLoaded ("Error.MaximumGunpowderLoaded", true),
	ErrorProjectileAlreadyLoaded ("Error.ProjectileAlreadyLoaded", true),
	ErrorCannonBuiltLimit ("Error.CannonBuiltLimit", true),
	ErrorNotTheOwner ("Error.NotTheOwner", true),
	ErrorDismantlingNotOwner ("Error.DismantlingNotOwner", true),
    ErrorMissingSign ("Error.MissingSign", true),
	ErrorNoMoney ("Error.NoMoney", true),
	ErrorNotPaid ("Error.NotPaid", true),
	ErrorAlreadyPaid ("Error.AlreadyPaid", true),
	ErrorPlayerNotFound ("Error.PlayerNotFound", true),

    //Commands
    CmdSelectCannon ("Commands.SelectCannon"),
	CmdSelectBlock ("Commands.SelectBlock"),
    CmdSelectCanceled ("Commands.SelectCanceled"),
    CmdCannonNotFound ("Commands.CannonNotFound"),
	CmdNoSentryWhitelist ("Commands.NoSentryWhitelist"),
	CmdAddedWhitelist ("Commands.AddedWhitelist"),
	CmdRemovedWhitelist ("Commands.RemovedWhitelist"),
	CmdRemovedWhitelistOwner ("Commands.RemovedWhitelistOwner"),
	CmdToggledTargetMob ("Commands.ToggledTargetMob"),
	CmdToggledTargetPlayer ("Commands.ToggledTargetPlayer"),
	CmdToggledTargetCannon ("Commands.ToggledTargetCannon"),
	CmdToggledTargetOther ("Commands.ToggledTargetOther"),
	CmdBuyCannon ("Commands.BuyCannon"),
	CmdPaidCannon ("Commands.PaidCannon"),
	CmdClaimCannonsStarted ("Commands.ClaimCannonsStarted"),
	CmdClaimCannonsFinished ("Commands.ClaimCannonsFinished"),

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
	loadGunpowderAndProjectile ("Load.GunpowderAndProjectile"),
	loadGunpowderNormalLimit ("Load.loadGunpowderNormalLimit"),
	loadOverloadedGunpowder ("Load.OverloadedGunpowder"),
	
	//cannon
	CannonCreated ("Cannon.Created"),
	CannonDismantled ("Cannon.Dismantled"),
	CannonDestroyed ("Cannon.Destroyed"),
    CannonsReseted ("Cannon.Reseted"),
	CannonFire ("Cannon.Fire"),
    CannonObserverAdded ("Cannon.ObserverAdded"),
    CannonObserverRemoved ("Cannon.ObserverRemoved"),
	CannonInfo ("Cannon.Info"),
    CannonRenameSuccess ("Cannon.RenameSuccess"),
    CannonRenameFail ("Cannon.RenameFail"),

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
	PermissionErrorDismantle ("Permission.ErrorDismantle", true),
	PermissionErrorBuild ("Permission.ErrorBuild", true),
    PermissionErrorRename ("Permission.ErrorRename", true),
	PermissionErrorFire ("Permission.ErrorFire", true),
	PermissionErrorLoad ("Permission.ErrorLoad", true),
	PermissionErrorAdjust ("Permission.ErrorAdjust", true),
    PermissionErrorAutoaim ("Permission.ErrorAutoaim", true),
    PermissionErrorObserver ("Permission.ErrorObserver", true),
	PermissionErrorProjectile ("Permission.ErrorProjectile", true),
    PermissionErrorThermometer ("Permission.ErrorThermometer", true),
    PermissionErrorRamrod ("Permission.ErrorRamrod", true),

	//Death
	DeathMessage1 ("Death.message1"),
	DeathMessage2 ("Death.message2"),
	DeathMessage3 ("Death.message3"),
	
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
        this.isError = e;
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
	public boolean isValid()
	{
		return !isError;
	}
	public boolean isError()
	{
		return isError;
	}
}
