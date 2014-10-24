package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="typename",
        staticParams={"name=info"},
        usage="/{plugin-command} {command} typename [name]",
        description="Set or view the type display name for the selected arena.")

public class TypeNameSubCommand extends AbstractPVCommand {

    @Localizable static final String _VIEW_TYPE_NAME = "The type name of arena '{0}' is '{1}'.";
    @Localizable static final String _SET_TYPE_NAME = "Type name of arena '{0}' set to '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "min"));
        if (arena == null)
            return; // finish

        if (args.getString("name").equals("info")) {
            tell(sender, Lang.get(_VIEW_TYPE_NAME, arena.getName(), arena.getSettings().getTypeDisplayName()));
        }
        else {
            String name = args.getString("name");

            arena.getSettings().setTypeDisplayName(name);

            tellSuccess(sender, Lang.get(_SET_TYPE_NAME, arena.getName(), name));
        }
    }
}

