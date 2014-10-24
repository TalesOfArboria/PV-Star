package com.jcwhatever.bukkit.pvs.commands.admin.game.autostart;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="autostart",
        command="minplayers",
        staticParams={"amount=info"},
        usage="/{plugin-command} {command} autostart minplayers [amount]",
        description="Set or view autostart minimum players in the currently selected arena.")

public class MinPlayersSubCommand extends AbstractPVCommand {

    @Localizable static final String _MIN_PLAYERS_INFO = "Minimum autostart players in arena '{0}' is set to {1}.";
    @Localizable static final String _MIN_PLAYERS_SET = "Minimum autostart players in arena '{0}' changed to {1}.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.NOT_RUNNNING);
        if (arena == null)
            return; // finish

        if (args.getString("amount").equals("info")) {

            int min = arena.getLobbyManager().getSettings().getMinAutoStartPlayers();
            tell(sender, Lang.get(_MIN_PLAYERS_INFO, arena.getName(), min));
        }
        else {

            int min = args.getInt("amount");

            arena.getLobbyManager().getSettings().setMinAutoStartPlayers(min);

            tellSuccess(sender, Lang.get(_MIN_PLAYERS_SET, arena.getName(), min));
        }
    }
}