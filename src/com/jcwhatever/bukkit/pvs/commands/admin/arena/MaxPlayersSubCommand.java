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
        command="maxplayers",
        staticParams={"max=info"},
        usage="/{plugin-command} {command} maxplayers [max]",
        description="Set or view the maximum players setting for your selected arena.")

public class MaxPlayersSubCommand extends AbstractPVCommand {

    @Localizable static final String _SET_MAX = "Max players for arena '{0}' has been set to {1}.";
    @Localizable static final String _VIEW_MAX = "Max players for arena '{0}' is {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "max"));
        if (arena == null)
            return; // finish

        if (args.getString("max").equals("info")) {
            tell(sender, Lang.get(_VIEW_MAX, arena.getName(), arena.getSettings().getMinPlayers()));
        }
        else {
            int max = args.getInt("max");

            arena.getSettings().setMaxPlayers(max);

            tellSuccess(sender, Lang.get(_SET_MAX, arena.getName(), max));
        }
    }
}
