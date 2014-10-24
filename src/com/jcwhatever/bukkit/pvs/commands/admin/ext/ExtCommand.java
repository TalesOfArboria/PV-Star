package com.jcwhatever.bukkit.pvs.commands.admin.ext;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="ext",
        description="Manage arena extensions.")

public class ExtCommand extends AbstractPVCommand {

    public ExtCommand() {
        super();

        registerSubCommand(AddSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(ListSubCommand.class);
        registerSubCommand(TypesSubCommand.class);
    }
}
