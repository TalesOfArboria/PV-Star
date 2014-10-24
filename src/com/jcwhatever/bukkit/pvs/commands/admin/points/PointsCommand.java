package com.jcwhatever.bukkit.pvs.commands.admin.points;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="points",
        description="Manage arena points.")

public class PointsCommand extends AbstractPVCommand {

    public PointsCommand() {
        super();

        registerSubCommand(AddSubCommand.class);
        registerSubCommand(CurrentSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(ListSubCommand.class);
        registerSubCommand(SetSubCommand.class);
    }
}
