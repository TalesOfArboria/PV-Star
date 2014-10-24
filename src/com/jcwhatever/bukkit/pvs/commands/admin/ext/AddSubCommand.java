package com.jcwhatever.bukkit.pvs.commands.admin.ext;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.extensions.ArenaExtension;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="ext",
        command="add",
        staticParams = { "extName" },
        usage="/{plugin-command} {command} add <extName>",
        description="Add an extension to the currently selected arena.")

public class AddSubCommand extends AbstractPVCommand {

    @Localizable static final String _EXT_NOT_FOUND = "An arena extension named '{0}' was not found.";
    @Localizable static final String _FAILED =  "Failed to add extension '{0}' to arena '{1}'.";
    @Localizable static final String _SUCCESS =  "Added extension '{0}' to arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args)
            throws InvalidCommandSenderException, InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        String extName = args.getName("extName");

        if (PVStarAPI.getExtensionManager().getExtensionClass(extName) == null) {
            tellError(sender, Lang.get(_EXT_NOT_FOUND, extName));
            return; // finish
        }

        ArenaExtension extension = arena.getExtensionManager().add(extName);
        if (extension == null) {
            tellError(sender, Lang.get(_FAILED, extName, arena.getName()));
            return; // finish
        }

        tellSuccess(sender, Lang.get(_SUCCESS, extension.getName(), arena.getName()));
    }
}
