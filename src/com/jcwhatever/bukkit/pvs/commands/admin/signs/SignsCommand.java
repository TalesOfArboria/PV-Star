package com.jcwhatever.bukkit.pvs.commands.admin.signs;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;

@ICommandInfo(
        command="signs",
        description="View sign information.")

public class SignsCommand extends AbstractPVCommand {

    public SignsCommand() {
        super();

        registerSubCommand(TypesSubCommand.class);
        registerSubCommand(UsageSubCommand.class);
    }
}
