package com.jcwhatever.bukkit.pvs.commands.admin.kits;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.commands.admin.kits.items.ItemsCommand;

@ICommandInfo(
        command="kits",
        description="Manage kits.")

public class KitsCommand extends AbstractPVCommand {

    public KitsCommand() {
        super();

        registerSubCommand(ItemsCommand.class);

        registerSubCommand(AddSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(GiveSubCommand.class);
        registerSubCommand(ListSubCommand.class);
    }
}

