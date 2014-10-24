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
        command="countdown",
        staticParams={"seconds=info"},
        usage="/{plugin-command} {command} countdown [seconds]",
        description="Set or view game start countdown seconds in the currently selected arena.")

public class CountdownSubCommand extends AbstractPVCommand {

    @Localizable static final String _COUNTDOWN_INFO = "Game start countdown seconds in arena '{0}' is set to {1}.";
    @Localizable static final String _COUNTDOWN_SET = "Game start countdown seconds in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "seconds"));
        if (arena == null)
            return; // finish

        if (args.getString("seconds").equals("info")) {

            int seconds = arena.getLobbyManager().getSettings().getStartCountdownSeconds();
            tell(sender, Lang.get(_COUNTDOWN_INFO, arena.getName(), seconds));
        }
        else {

            int seconds = args.getInt("seconds");

            arena.getLobbyManager().getSettings().setStartCountdownSeconds(seconds);

            tellSuccess(sender, Lang.get(_COUNTDOWN_SET, arena.getName(), seconds));
        }
    }
}

