package com.jcwhatever.bukkit.pvs.commands.admin.points;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.PVStarAPI;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.points.PointsHandler;
import com.jcwhatever.bukkit.pvs.api.points.PointsType;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="points",
        command="set",
        staticParams={ "typeName", "amount=info" },
        usage="/{plugin-command} {command} set <typeName> <amount>",
        description="Set points received or deducted for a points type in the selected arena.")

public class SetSubCommand extends AbstractPVCommand {

    @Localizable static final String _TYPE_NOT_FOUND = "A points type named '{0}' was not found.";
    @Localizable static final String _TYPE_NOT_ADDED = "Points type '{0}' is not added to arena '{1}'";
    @Localizable static final String _INFO = "Points type value in arena '{0}' is {1}.";
    @Localizable static final String _CHANGED = "Points type value in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "amount"));
        if (arena == null)
            return;

        String typeName = args.getName("typeName", 32);

        PointsType type = PVStarAPI.getPointsManager().getType(typeName);
        if (type == null) {
            tellError(sender, Lang.get(_TYPE_NOT_FOUND, typeName));
            return; // finished
        }

        PointsHandler handler = type.getHandler(arena);
        if (handler == null) {
            tellError(sender, Lang.get(_TYPE_NOT_ADDED, type.getName(), arena.getName()));
            return; // finished
        }

        if (args.getString("amount").equals("info")) {

            int points = handler.getPoints();

            tell(sender, Lang.get(_INFO, arena.getName(), points));
        }
        else {

            int points = args.getInt("amount");

            handler.setPoints(points);

            tellSuccess(sender, Lang.get(_CHANGED, arena.getName(), points));

        }

    }
}

