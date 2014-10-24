package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.arena.options.LivesBehavior;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="livesbehavior",
        staticParams={"static|reset|additive|info=info"},
        usage="/{plugin-command} {command} livesbehavior [static|reset|additive]",
        description="Set or view lives behavior when forwarded to the currently selected arena.")

public class LivesBehaviorSubCommand extends AbstractPVCommand {

    @Localizable static final String _BEHAVIOR_INFO = "Player lives behavior in arena '{0}' is set to {1}.";
    @Localizable static final String _BEHAVIOR_SET = "Player lives behavior in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "static|reset|additive|info"));
        if (arena == null)
            return; // finish

        if (args.getString("static|reset|additive|info").equals("info")) {

            LivesBehavior behavior = arena.getGameManager().getSettings().getLivesBehavior();
            tell(sender, Lang.get(_BEHAVIOR_INFO, arena.getName(), behavior));
        }
        else {

            LivesBehavior behavior = args.getEnum("static|reset|additive|info", LivesBehavior.class);

            arena.getGameManager().getSettings().setLivesBehavior(behavior);

            tellSuccess(sender, Lang.get(_BEHAVIOR_SET, arena.getName(), behavior));
        }
    }
}