package com.jcwhatever.bukkit.pvs.commands.admin.arena;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="arena",
        command="minplayers",
        staticParams={"min=info"},
        usage="/{plugin-command} {command} minplayers [min]",
        description="Set or view the minimum players setting for your selected arena.")

public class MinPlayersSubCommand extends AbstractPVCommand {

    @Localizable static final String _SET_MIN = "Minimum players for arena '{0}' has been set to {1}.";
    @Localizable static final String _VIEW_MIN = "Minimum players for arena '{0}' is {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "min"));
        if (arena == null)
            return; // finish

        if (args.getString("min").equals("info")) {
            tell(sender, Lang.get(_VIEW_MIN, arena.getName(), arena.getSettings().getMinPlayers()));
        }
        else {
            int min = args.getInt("min");

            arena.getSettings().setMinPlayers(min);

            tellSuccess(sender, Lang.get(_SET_MIN, arena.getName(), min));
        }

    }
}
