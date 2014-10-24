package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="arena",
        description="Manage arena settings.")

public class ArenaCommand extends AbstractPVCommand {

    public ArenaCommand() {
        super();

        registerSubCommand(ArenaDamageSubCommand.class);
        registerSubCommand(CreateSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(RemoveLocationSubCommand.class);
        registerSubCommand(RestoreRegionSubCommand.class);
        registerSubCommand(SaveRegionSubCommand.class);
        registerSubCommand(MaxPlayersSubCommand.class);
        registerSubCommand(MinPlayersSubCommand.class);
        registerSubCommand(MobSpawnSubCommand.class);
        registerSubCommand(OutsidersSubCommand.class);
        registerSubCommand(SelectSubCommand.class);
        registerSubCommand(SetRegionSubCommand.class);
        registerSubCommand(TypeNameSubCommand.class);
    }

}
