package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.PointsBehavior;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="pointsbehavior",
        staticParams={"static|reset|additive|info=info"},
        usage="/{plugin-command} {command} pointsbehavior [static|reset|additive]",
        description="Set or view points behavior when forwarded to the currently selected arena.")

public class PointsBehaviorSubCommand extends AbstractPVCommand {

    @Localizable static final String _POINTS_INFO = "Player points behavior in arena '{0}' is set to {1}.";
    @Localizable static final String _POINTS_SET = "Player points behavior in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "static|reset|additive|info"));
        if (arena == null)
            return; // finish

        if (args.getString("static|reset|additive|info").equals("info")) {

            PointsBehavior behavior = arena.getGameManager().getSettings().getPointsBehavior();
            tell(sender, Lang.get(_POINTS_INFO, arena.getName(), behavior));
        }
        else {

            PointsBehavior behavior = args.getEnum("static|reset|additive|info", PointsBehavior.class);

            arena.getGameManager().getSettings().setPointsBehavior(behavior);

            tellSuccess(sender, Lang.get(_POINTS_SET, arena.getName(), behavior));
        }
    }
}
