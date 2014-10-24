package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="points",
        staticParams={"amount=info"},
        usage="/{plugin-command} {command} points [amount]",
        description="Set or view player lives in the currently selected arena.")

public class PointsSubCommand extends AbstractPVCommand {

    @Localizable static final String _POINTS_INFO = "Player start points in arena '{0}' is set to {1}.";
    @Localizable static final String _POINTS_SET = "Player start points in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "amount"));
        if (arena == null)
            return; // finish

        if (args.getString("amount").equals("info")) {

            int points = arena.getGameManager().getSettings().getStartPoints();
            tell(sender, Lang.get(_POINTS_INFO, arena.getName(), points));
        }
        else {

            int points = args.getInt("amount");

            arena.getGameManager().getSettings().setStartPoints(points);

            tellSuccess(sender, Lang.get(_POINTS_SET, arena.getName(), points));
        }
    }
}

