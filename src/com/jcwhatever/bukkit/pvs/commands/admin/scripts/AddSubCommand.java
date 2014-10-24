package com.jcwhatever.bukkit.pvs.commands.admin.scripts;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.scripting.Script;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="scripts",
        command="add",
        staticParams={ "scriptName" },
        usage="/{plugin-command} {command} add <scriptName>",
        description="Add the specified script to the selected arena.")

public class AddSubCommand extends AbstractPVCommand {

    @Localizable static final String _SCRIPT_NOT_FOUND = "A script named '{0}' was not found.";
    @Localizable static final String _SUCCESS = "Script named '{0}' added to arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.ALWAYS);
        if (arena == null)
            return; // finished

        String scriptName = args.getString("scriptName");

        Script script = PVStarAPI.getScriptManager().getScript(scriptName);
        if (script == null) {
            tellError(sender, Lang.get(_SCRIPT_NOT_FOUND, scriptName));
            return; // finish
        }

        arena.getScriptManager().addScript(script);

        tellSuccess(sender, Lang.get(_SUCCESS, script.getName(), arena.getName()));
    }
}
