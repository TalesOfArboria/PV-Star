package com.jcwhatever.bukkit.pvs.commands.admin.scripts.reload;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="reload",
        command="arena",
        usage="/{plugin-command} {command} reload arena",
        description="Reload scripts in the currently selected arena.")

public class ArenaSubCommand extends AbstractPVCommand {

    @Localizable static final String _SUCCESS = "All scripts in arena '{0}' reloaded.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finished

        arena.getScriptManager().reload();

        tellSuccess(sender, Lang.get(_SUCCESS, arena.getName()));
    }
}
