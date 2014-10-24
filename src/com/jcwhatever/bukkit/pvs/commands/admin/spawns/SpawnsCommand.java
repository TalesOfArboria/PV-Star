package com.jcwhatever.bukkit.pvs.commands.admin.spawns;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="spawns",
        description="Manage arena spawn points.")

public class SpawnsCommand extends AbstractPVCommand {

    public SpawnsCommand() {
        super();

        registerSubCommand(AddSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(ListSubCommand.class);
        registerSubCommand(TypesSubCommand.class);
    }

}
