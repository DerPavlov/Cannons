package at.pavlov.cannons.Enum;


public enum CommandList {
    BUILD("build", "/cannons build", "cannons.player.command", false),
    FIRE("pilot", "/cannons fire", "cannons.player.command", false),
    COMMANDS("commands", "/cannons commands", "cannons.player.command", false),
    IMITATE("imitate", "/cannons imitate", null, false),
    RENAME("rename", "/cannons rename [OLD] [NEW]", "cannons.player.rename", false),
    INFO("info", "/cannons info", "cannons.player.info", false),
    LIST("list", "/cannons list", "cannons.player.list", false),
    BUY("buy", "/cannons buy", "cannons.player.buy", false),
    DISMANTLE("dismantle", "/cannons dismantle", "cannons.player.dismantle", false),
    RESET("reset", "/cannons reset", "cannons.player.reset", false),
    CLAIM("claim", "/cannons claim", "cannons.player.claim", false),
    //admin commands
    LIST_ADMIN("list", "/cannons list [NAME]", "cannons.admin.list", true),
    CREATE("create", "/cannons create [DESIGN]", "cannons.admin.create", true),
    DISMANTLE_ADMIN("dismantle", "/cannons dismantle", "cannons.admin.dismantle", true),
    RESET_ADMIN("reset", "/cannons reset [NAME]", "cannons.admin.reset", true),
    RELOAD("reload", "/cannons reload", "cannons.admin.reload", true),
    SAVE("save", "/cannons save", "cannons.admin.save", true),
    LOAD("load", "/cannons load", "cannons.admin.load", true),
    PERMISSIONS("permissions", "/cannons permissions [NAME]", "cannons.admin.permissions", true),
    BLOCKDATA("blockdata", "/cannons blockdata", "cannons.admin.blockdata", true);

    private String command;
    private String usage;
    private String permission;
    private Boolean adminCmd;

    CommandList(String command, String usage, String permission, boolean adminCmd){
        this.command = command;
        this.usage = usage;
        this.permission = permission;
        this.adminCmd = adminCmd;
    }

    public String getCommand() {
        return command;
    }

    public String getPermission() {
        return permission;
    }

    public String getUsage() {
        return usage;
    }

    public boolean isAdminCmd(){
        return this.adminCmd;
    }
}
