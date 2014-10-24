package com.jcwhatever.bukkit.pvs.commands.admin.scripts;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="scripts",
        command="del",
        staticParams={ "scriptName" },
        usage="/{plugin-command} {command} del <scriptName>",
        description="Remove the specified script from the selected arena.")

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _FAILED = "A script named '{0}' is not installed in arena '{1}'.";
    @Localizable static final String _SUCCESS = "Script named '{0}' removed from arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finished

        String scriptName = args.getString("scriptName");

        if (!arena.getScriptManager().removeScript(scriptName)) {
            tellError(sender, Lang.get(_FAILED, scriptName, arena.getName()));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, scriptName, arena.getName()));
    }
}
