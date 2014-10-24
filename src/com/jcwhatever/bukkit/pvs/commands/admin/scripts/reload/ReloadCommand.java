package com.jcwhatever.bukkit.pvs.commands.admin.scripts.reload;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="scripts",
        command="reload",
        usage="/{plugin-command} {command} reload",
        description="Reload scripts from the scripts folder. Arena scripts must be reloaded separately.")

public class ReloadCommand extends AbstractPVCommand {

    @Localizable static final String _SUCCESS = "Scripts reloaded.";

    public ReloadCommand() {
        super();

        registerSubCommand(ArenaSubCommand.class);
    }

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        PVStarAPI.getScriptManager().reload();

        tellSuccess(sender, Lang.get(_SUCCESS));
    }
}
