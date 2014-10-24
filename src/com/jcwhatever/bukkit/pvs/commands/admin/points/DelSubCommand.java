package com.jcwhatever.bukkit.pvs.commands.admin.points;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.points.PointsType;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="points",
        command="del",
        staticParams={ "typeName" },
        usage="/{plugin-command} {command} del <typeName>",
        description="Remove points type from the currently selected arena.")

public class DelSubCommand extends AbstractPVCommand {

    @Localizable static final String _TYPE_NOT_FOUND = "A points type named '{0}' was not found.";
    @Localizable static final String _SUCCESS = "Points type '{0}' removed from arena '{1}'.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return;

        String typeName = args.getName("typeName", 32);

        PointsType type = PVStarAPI.getPointsManager().getType(typeName);
        if (type == null) {
            tellError(sender, Lang.get(_TYPE_NOT_FOUND, typeName));
            return; // finished
        }

        type.remove(arena);

        tellSuccess(sender, Lang.get(_SUCCESS, type.getName(), arena.getName()));
    }
}
