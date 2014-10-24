package com.jcwhatever.bukkit.pvs.commands.admin.game;

import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.pvs.api.arena.Arena;
import com.jcwhatever.bukkit.pvs.api.commands.AbstractPVCommand;
import com.jcwhatever.bukkit.pvs.api.utils.Lang;
import org.bukkit.command.CommandSender;

@ICommandInfo(
        parent="game",
        command="reservespawns",
        staticParams={"on|off|info=info"},
        usage="/{plugin-command} {command} reservespawns [on|off]",
        description="Turn spawn reservations on or off in the selected arenas game.")

public class ReserveSpawnsSubCommand extends AbstractPVCommand {

    @Localizable static final String _RESERVE_ENABLED = "Arena '{0}' game Spawn Reserving is enabled.";
    @Localizable static final String _RESERVE_DISABLED = "Arena '{0}' game Spawn Reserving is {RED}disabled.";
    @Localizable static final String _RESERVE_CHANGE_ENABLED = "Arena '{0}' game Spawn Reserving changed to enabled.";
    @Localizable static final String _RESERVE_CHANGE_DISABLED = "Arena '{0}' game Spawn Reserving changed to {RED}disabled.";

    @Override
    public void execute(CommandSender sender, CommandArguments args) throws InvalidValueException {

        Arena arena = getSelectedArena(sender, ArenaReturned.getInfoToggled(args, "on|off|info"));
        if (arena == null)
            return; // finish

        if (args.getString("on|off|info").equals("info")) {

            boolean isEnabled = arena.getGameManager().getSettings().isPlayerSpawnsReserved();

            if (isEnabled) {
                tell(sender, Lang.get(_RESERVE_ENABLED, arena.getName()));
            }
            else {
                tell(sender, Lang.get(_RESERVE_DISABLED, arena.getName()));
            }
        }
        else {

            boolean isEnabled = args.getBoolean("on|off|info");

            arena.getGameManager().getSettings().setPlayerSpawnsReserved(isEnabled);

            if (isEnabled) {
                tellSuccess(sender, Lang.get(_RESERVE_CHANGE_ENABLED, arena.getName()));
            }
            else {
                tellSuccess(sender, Lang.get(_RESERVE_CHANGE_DISABLED, arena.getName()));
            }
        }
    }
}

