package com.jcwhatever.bukkit.pvs.commands.admin.scripts;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.scripts.reload.ReloadCommand;

@ICommandInfo(
        command="scripts",
        description="Manage arena scripts.")

public class ScriptsCommand extends AbstractPVCommand {

    public ScriptsCommand() {
        super();

        registerSubCommand(AddSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(ListAllSubCommand.class);
        registerSubCommand(ListSubCommand.class);
        registerSubCommand(ReloadCommand.class);
    }
}
