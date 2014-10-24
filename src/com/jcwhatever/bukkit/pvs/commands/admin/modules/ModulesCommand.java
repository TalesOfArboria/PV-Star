package com.jcwhatever.bukkit.pvs.commands.admin.modules;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="modules",
        description="Manage PV-Star modules.")

public class ModulesCommand extends AbstractPVCommand {

    public ModulesCommand() {
        super();

        registerSubCommand(ListSubCommand.class);

    }
}
